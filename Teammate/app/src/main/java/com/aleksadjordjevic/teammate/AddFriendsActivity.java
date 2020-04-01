package com.aleksadjordjevic.teammate;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.Manifest;
import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.UUID;

public class AddFriendsActivity extends AppCompatActivity
{

    private static final int REQUEST_ENABLE_BT = 0;
    //private static final int REQUEST_DISCOVER_BT = 1;
    BluetoothAdapter mBlueAdapter;
    BluetoothDevice mBTDevice;
    ArrayList<BluetoothDevice> mBTDevices = new ArrayList<>();
    DeviceListAdapter mDeviceListAdapter;
    BluetoothConnectionService mBluetoothConnection;
    private static final UUID MY_UUID_INSECURE = UUID.fromString("8ce255c0-200a-11e0-ac64-0800200c9a66");



    ImageButton btnBluetooth, btnFindFriends, btnBack;
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

        btnBluetooth = findViewById(R.id.bluetoothIcon);
        btnFindFriends = findViewById(R.id.btnFindFriendsAF);
        btnBack = findViewById(R.id.btnBackAF);
        listBTDevices = findViewById(R.id.listFriendsAF);
        mBTDevices = new ArrayList<>();

        mAuth = FirebaseAuth.getInstance();
        user = mAuth.getCurrentUser();
        userID = user.getUid();
        mDatabase = FirebaseFirestore.getInstance();

        mBlueAdapter = BluetoothAdapter.getDefaultAdapter();

//        listBTDevices.setOnItemClickListener(new AdapterView.OnItemClickListener()
//        {
//            @Override
//            public void onItemClick(AdapterView<?> parent, View view, int position, long id)
//            {
//                mBlueAdapter.cancelDiscovery();
//                //String deviceName = mBTDevices.get(position).getName();
//               // String deviceAddress = mBTDevices.get(position).getAddress();
//
//                mBTDevices.get(position).createBond();
//                mBTDevice = mBTDevices.get(position);
//                mBluetoothConnection = new BluetoothConnectionService(AddFriendsActivity.this);
//                mBluetoothConnection.startClient(mBTDevice,MY_UUID_INSECURE);
//                byte[] bytes = userID.getBytes(Charset.defaultCharset());
//                mBluetoothConnection.write(bytes);
//            }
//        });


        btnBluetooth.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                changeBluetooth();
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
                Intent indexIntent = new Intent(getApplicationContext(), IndexActivity.class);
                startActivity(indexIntent);
            }
        });
    }



    @Override
    protected void onResume()
    {
        super.onResume();
        setBluetooth();
    }

    protected void setBluetooth()
    {
        if(mBlueAdapter == null)
        {
            Toast.makeText(getApplicationContext(), "Bluetooth is unavailable. Try again later.", Toast.LENGTH_SHORT).show();
        }
        else
        {
            if(mBlueAdapter.isEnabled())
            {
                btnBluetooth.setImageResource(R.drawable.bluetooth_on);
            }
            else
            {
                btnBluetooth.setImageResource(R.drawable.bluetooth_off);
            }
        }

    }

    protected void changeBluetooth()
    {
        if(mBlueAdapter == null)
        {
            if(!mBTDevices.isEmpty())
                mBTDevices.clear();
            Toast.makeText(getApplicationContext(), "Bluetooth is unavailable. Try again later.", Toast.LENGTH_SHORT).show();
        }
        else
        {
            if(mBlueAdapter.isEnabled())
            {
                mBlueAdapter.disable();
                btnBluetooth.setImageResource(R.drawable.bluetooth_off);
                if(!mBTDevices.isEmpty())
                    mBTDevices.clear();
                listBTDevices.setAdapter(null);
                Toast.makeText(getApplicationContext(), "Bluetooth is now turned off", Toast.LENGTH_SHORT).show();

            }
            else
            {
                Intent turnOnBT = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                startActivityForResult(turnOnBT,REQUEST_ENABLE_BT);
                if(!mBlueAdapter.isDiscovering())
                {
                    Intent discoverableBT = new Intent(BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE);
                    startActivity(discoverableBT);
                }
            }
        }
    }

    protected void findDevices()
    {
        if(mBlueAdapter.isDiscovering())
            mBlueAdapter.cancelDiscovery();

        checkBTPermissions();
        mBlueAdapter.startDiscovery();
        IntentFilter discoverDevicesIntent = new IntentFilter(BluetoothDevice.ACTION_FOUND);
        registerReceiver(mBroadcastReceiver, discoverDevicesIntent);
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data)
    {
        switch (requestCode)
        {
            case REQUEST_ENABLE_BT:
                if(resultCode == RESULT_OK)
                {
                    btnBluetooth.setImageResource(R.drawable.bluetooth_on);
                    Toast.makeText(getApplicationContext(), "Bluetooth is now turned on", Toast.LENGTH_SHORT).show();
                }
                else
                    Toast.makeText(getApplicationContext(), "Error turning Bluetooth on. Try again later.", Toast.LENGTH_SHORT).show();
                break;

        }
        super.onActivityResult(requestCode, resultCode, data);
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
}
