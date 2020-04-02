package com.aleksadjordjevic.teammate;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import android.Manifest;
import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothServerSocket;
import android.bluetooth.BluetoothSocket;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.UUID;

public class AddFriendsActivity extends AppCompatActivity
{

    //static final int REQUEST_ENABLE_BT = 0;
    static final int STATE_LISTENING = 1;
    static final int STATE_CONNECTING = 2;
    static final int STATE_CONNECTED = 3;
    static final int STATE_CONNECTION_FAILED = 4;
    static final int STATE_MESSAGE_RECEIVED = 5;
    static final String APP_NAME = "TEAMMATE";
    static final UUID MY_UUID_INSECURE = UUID.fromString("8ce255c0-200a-11e0-ac64-0800200c9a66");

    BluetoothAdapter mBlueAdapter;
    ArrayList<BluetoothDevice> mBTDevices = new ArrayList<>();
    DeviceListAdapter mDeviceListAdapter;
    SendReceive sendReceive;

    Button btnSendReq;
    TextView txtStatus;
    ImageButton btnFindFriends, btnBack;
    ListView listBTDevices;

    FirebaseAuth mAuth;
    FirebaseUser user;
    String userID;
    FirebaseFirestore mDatabase;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_friends);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);

        btnFindFriends = findViewById(R.id.btnFindFriendsAF);
        btnBack = findViewById(R.id.btnBackAF);
        listBTDevices = findViewById(R.id.listFriendsAF);
        btnSendReq = findViewById(R.id.btnSendReqAF);
        txtStatus = findViewById(R.id.txtStatusAF);

        mAuth = FirebaseAuth.getInstance();
        user = mAuth.getCurrentUser();
        userID = user.getUid();
        mDatabase = FirebaseFirestore.getInstance();

        mBTDevices = new ArrayList<>();
        mBlueAdapter = BluetoothAdapter.getDefaultAdapter();

        listBTDevices.setOnItemClickListener(new AdapterView.OnItemClickListener()
        {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id)
            {
                ClientClass clientClass = new ClientClass(mBTDevices.get(position));
                clientClass.start();
            }
        });

//        listen.setOnClickListener(new View.OnClickListener()
//        {
//            @Override
//            public void onClick(View v)
//            {
//                ServerClass serverClass = new ServerClass();
//                serverClass.start();
//            }
//        });

        btnSendReq.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                if(txtStatus.getText().equals("CONNECTED!"))
                    sendReceive.write(userID.getBytes());
            }
        });


        btnFindFriends.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                mBTDevices.clear();

                if(mBlueAdapter.isEnabled())
                    findDevices();
                else
                    Toast.makeText(getApplicationContext(), "Turn on bluetooth first.", Toast.LENGTH_SHORT).show();
            }
        });


        btnBack.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                Intent friendsMenuIntent = new Intent(getApplicationContext(), FriendMenuActivity.class);
                startActivity(friendsMenuIntent);
            }
        });
    }

    Handler handler = new Handler(new Handler.Callback()
    {
        @Override
        public boolean handleMessage(Message msg)
        {
            switch (msg.what)
            {
                case STATE_LISTENING:
                    txtStatus.setText("Listening");
                    break;
                case STATE_CONNECTING:
                    txtStatus.setText("Connecting");
                    break;
                case STATE_CONNECTED:
                    txtStatus.setText("CONNECTED!");
                    break;
                case STATE_CONNECTION_FAILED:
                    txtStatus.setText("Connection failed");
                    break;
               case STATE_MESSAGE_RECEIVED:
                    byte[] readBuff = (byte[])msg.obj;
                    String tempMSg = new String(readBuff,0,msg.arg1);
                    Toast.makeText(getApplicationContext(), tempMSg, Toast.LENGTH_SHORT).show();
                    break;

            }
            return true;
        }
    });



    @Override
    protected void onResume()
    {
        super.onResume();
//        setBluetooth();
    }

//    protected void setBluetooth()
//    {
//        if(mBlueAdapter == null)
//            Toast.makeText(getApplicationContext(), "Bluetooth is unavailable. Try again later.", Toast.LENGTH_SHORT).show();
//        else
//        {
//            if(mBlueAdapter.isEnabled())
//                btnBluetooth.setImageResource(R.drawable.bluetooth_on);
//            else
//                btnBluetooth.setImageResource(R.drawable.bluetooth_off);
//        }
//
//    }

//    protected void changeBluetooth()
//    {
//        if(mBlueAdapter == null)
//        {
//            if(!mBTDevices.isEmpty())
//                mBTDevices.clear();
//            Toast.makeText(getApplicationContext(), "Bluetooth is unavailable. Try again later.", Toast.LENGTH_SHORT).show();
//        }
//        else
//        {
//            if(mBlueAdapter.isEnabled())
//            {
//                mBlueAdapter.disable();
//                btnBluetooth.setImageResource(R.drawable.bluetooth_off);
//                if(!mBTDevices.isEmpty())
//                    mBTDevices.clear();
//                listBTDevices.setAdapter(null);
//                Toast.makeText(getApplicationContext(), "Bluetooth is now turned off", Toast.LENGTH_SHORT).show();
//
//            }
//            else
//            {
//                Intent turnOnBT = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
//                startActivityForResult(turnOnBT,REQUEST_ENABLE_BT);
//                if(!mBlueAdapter.isDiscovering())
//                {
//                    Intent discoverableBT = new Intent(BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE);
//                    startActivity(discoverableBT);
//                }
//
//            }
//        }
//    }

//    @Override
//    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data)
//    {
//        switch (requestCode)
//        {
//            case REQUEST_ENABLE_BT:
//                if(resultCode == RESULT_OK)
//                {
//                    btnBluetooth.setImageResource(R.drawable.bluetooth_on);
//                    Toast.makeText(getApplicationContext(), "Bluetooth is now turned on", Toast.LENGTH_SHORT).show();
//                }
//                else
//                    Toast.makeText(getApplicationContext(), "Error turning Bluetooth on. Try again later.", Toast.LENGTH_SHORT).show();
//                break;
//
//        }
//        super.onActivityResult(requestCode, resultCode, data);
//    }

    protected void findDevices()
    {
        if(mBlueAdapter.isDiscovering())
            mBlueAdapter.cancelDiscovery();

        checkBTPermissions();
        mBlueAdapter.startDiscovery();
        IntentFilter discoverDevicesIntent = new IntentFilter(BluetoothDevice.ACTION_FOUND);
        registerReceiver(mBroadcastReceiver, discoverDevicesIntent);
    }


    private BroadcastReceiver mBroadcastReceiver = new BroadcastReceiver()
    {
        @Override
        public void onReceive(Context context, Intent intent)
        {
            final String action = intent.getAction();

            if (action.equals(BluetoothDevice.ACTION_FOUND))
            {
                BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                if(!mBTDevices.contains(device))
                    mBTDevices.add(device);
                mDeviceListAdapter = new DeviceListAdapter(context, R.layout.add_friends_view, mBTDevices);
                listBTDevices.setAdapter(mDeviceListAdapter);
            }
        }
    };


    protected void checkBTPermissions()
    {
        if(Build.VERSION.SDK_INT > Build.VERSION_CODES.LOLLIPOP)
        {
            int permissionCheck = this.checkSelfPermission("Manifest.permission.ACCESS_FINE_LOCATION");
            permissionCheck += this.checkSelfPermission("Manifest.permission.ACCESS_COARSE_LOCATION");
            if (permissionCheck != 0)
                this.requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION}, 1001);
        }
    }


    private class ServerClass extends Thread
    {
        private BluetoothServerSocket serverSocket;

        public ServerClass()
        {
            try
            {
                serverSocket = mBlueAdapter.listenUsingRfcommWithServiceRecord(APP_NAME,MY_UUID_INSECURE);
            } catch (IOException e)
            {
                e.printStackTrace();
            }
        }

        public void run()
        {
            BluetoothSocket socket = null;

            while(socket == null)
            {

                try
                {
                    Message message = Message.obtain();
                    message.what = STATE_CONNECTING;
                    handler.sendMessage(message);
                    socket = serverSocket.accept();
                } catch (IOException e)
                {
                    Message message = Message.obtain();
                    message.what = STATE_CONNECTION_FAILED;
                    handler.sendMessage(message);
                    e.printStackTrace();
                }

                if(socket != null)
                {
                    Message message = Message.obtain();
                    message.what = STATE_CONNECTED;
                    handler.sendMessage(message);

                    sendReceive = new SendReceive(socket);
                    sendReceive.start();
                    break;
                }
            }
        }
    }

    private class ClientClass extends Thread
    {
        private BluetoothDevice device;
        private BluetoothSocket socket;

        public ClientClass(BluetoothDevice device1)
        {
            device = device1;

            try
            {
                socket = device.createRfcommSocketToServiceRecord(MY_UUID_INSECURE);
            } catch (IOException e)
            {
                e.printStackTrace();
            }
        }

        public void run()
        {
            try
            {
                socket.connect();
                Message message = Message.obtain();
                message.what = STATE_CONNECTED;
                handler.sendMessage(message);

                sendReceive = new SendReceive(socket);
                sendReceive.start();
            } catch (IOException e)
            {
                e.printStackTrace();
                Message message = Message.obtain();
                message.what = STATE_CONNECTION_FAILED;
                handler.sendMessage(message);
            }
        }
    }

    private class SendReceive extends Thread
    {
        private final BluetoothSocket bluetoothSocket;
        private final InputStream inputStream;
        private final OutputStream outputStream;

        public SendReceive(BluetoothSocket socket)
        {
            bluetoothSocket = socket;
            InputStream tempIn = null;
            OutputStream tempOut = null;

            try
            {
                tempIn = bluetoothSocket.getInputStream();
                tempOut = bluetoothSocket.getOutputStream();
            } catch (IOException e)
            {
                e.printStackTrace();
            }

            inputStream = tempIn;
            outputStream = tempOut;

        }

        public void run()
        {
            byte[] buffer = new byte[1024];
            int bytes;

            while (true)
            {
                try
                {
                    bytes =  inputStream.read(buffer);
                    handler.obtainMessage(STATE_MESSAGE_RECEIVED,bytes,-1,buffer).sendToTarget();
                } catch (IOException e)
                {
                    e.printStackTrace();
                }

            }
        }

        public void write(byte[] bytes)
        {
            try
            {
                outputStream.write(bytes);
            } catch (IOException e)
            {
                e.printStackTrace();
            }
        }

    }

}
