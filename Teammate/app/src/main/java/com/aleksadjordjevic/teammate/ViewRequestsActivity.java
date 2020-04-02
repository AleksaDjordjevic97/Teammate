package com.aleksadjordjevic.teammate;


import androidx.appcompat.app.AppCompatActivity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothServerSocket;
import android.bluetooth.BluetoothSocket;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.SetOptions;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.Array;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class ViewRequestsActivity extends AppCompatActivity
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
    SendReceive sendReceive;

    ImageButton btnListening, btnAccept, btnDecline, btnBack;
    TextView txtStatus, txtUser;

    FirebaseAuth mAuth;
    FirebaseUser user;
    String userID;
    FirebaseFirestore mDatabase;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_requests);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);

        btnListening = findViewById(R.id.btnListeningVR);
        btnAccept = findViewById(R.id.btnAcceptVR);
        btnDecline = findViewById(R.id.btnDeclineVR);
        btnBack = findViewById(R.id.btnBackVR);
        txtStatus = findViewById(R.id.txtStatusVR);
        txtUser = findViewById(R.id.txtUserVR);

        txtUser.setVisibility(View.INVISIBLE);
        btnAccept.setVisibility(View.INVISIBLE);
        btnDecline.setVisibility(View.INVISIBLE);

        mAuth = FirebaseAuth.getInstance();
        user = mAuth.getCurrentUser();
        userID = user.getUid();
        mDatabase = FirebaseFirestore.getInstance();

        mBlueAdapter = BluetoothAdapter.getDefaultAdapter();


        btnListening.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                //turnOnBluetooth();
                ServerClass serverClass = new ServerClass();
                serverClass.start();
                btnListening.setImageResource(R.drawable.listening_on);
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

        btnAccept.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                final String otherID = txtUser.getText().toString();
                Map<String,Object> userMap = new HashMap<>();
                userMap.put("friends", Arrays.asList(otherID));
                final Map<String,Object> otherMap = new HashMap<>();
                userMap.put("friends", Arrays.asList(userID));

//                mDatabase.collection("users").document(userID).update(userMap).addOnSuccessListener(new OnSuccessListener<Void>()
//                {
//                    @Override
//                    public void onSuccess(Void aVoid)
//                    {
//                        Toast.makeText(getApplicationContext(), "You are now friends!", Toast.LENGTH_SHORT).show();
//
//                    }
//                });

//                mDatabase.collection("users").document(otherID).update(otherMap).addOnSuccessListener(new OnSuccessListener<Void>()
//                {
//                    @Override
//                    public void onSuccess(Void aVoid)
//                    {
//                        Toast.makeText(getApplicationContext(), "You are now friends!", Toast.LENGTH_SHORT).show();
//                    }
//                });
            }
        });


    }

//    protected void turnOnBluetooth()
//    {
//        if(mBlueAdapter == null)
//        {
//            Toast.makeText(getApplicationContext(), "Bluetooth is unavailable. Try again later.", Toast.LENGTH_SHORT).show();
//        }
//        else
//        {
//            if(!mBlueAdapter.isEnabled())
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
//
//    @Override
//    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data)
//    {
//        switch (requestCode)
//        {
//            case REQUEST_ENABLE_BT:
//                if(resultCode == RESULT_OK)
//                {
//                    Toast.makeText(getApplicationContext(), "Bluetooth is now turned on", Toast.LENGTH_SHORT).show();
//                }
//                else
//                    Toast.makeText(getApplicationContext(), "Error turning Bluetooth on. Try again later.", Toast.LENGTH_SHORT).show();
//                break;
//
//        }
//        super.onActivityResult(requestCode, resultCode, data);
//    }

    Handler handler = new Handler(new Handler.Callback()
    {
        @Override
        public boolean handleMessage(Message msg)
        {
            switch (msg.what)
            {
                case STATE_LISTENING:
                    txtStatus.setText("Listening");
                    txtStatus.setTextColor(Color.WHITE);
                    break;
                case STATE_CONNECTING:
                    txtStatus.setText("Connecting");
                    txtStatus.setTextColor(Color.WHITE);
                    break;
                case STATE_CONNECTED:
                    txtStatus.setText("Connected Successfully!");
                    txtStatus.setTextColor(Color.GREEN);
                    break;
                case STATE_CONNECTION_FAILED:
                    txtStatus.setText("Connection failed");
                    txtStatus.setTextColor(Color.RED);
                    break;
                case STATE_MESSAGE_RECEIVED:
                    byte[] readBuff = (byte[])msg.obj;
                    String tempMSg = new String(readBuff,0,msg.arg1);
                    txtUser.setVisibility(View.VISIBLE);
                    btnAccept.setVisibility(View.VISIBLE);
                    btnDecline.setVisibility(View.VISIBLE);
                   txtUser.setText(tempMSg);
                    break;

            }
            return true;
        }
    });

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
                    message.what = STATE_LISTENING;
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
