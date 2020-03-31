package com.aleksadjordjevic.teammate;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageButton;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

public class AddFriendsActivity extends AppCompatActivity
{

    private static final int REQUEST_ENABLE_BT = 0;
    //private static final int REQUEST_DISCOVER_BT = 1;
    BluetoothAdapter mBlueAdapter;

    ImageButton btnBluetooth, btnBack;
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
        btnBack = findViewById(R.id.btnBackAF);

        mAuth = FirebaseAuth.getInstance();
        user = mAuth.getCurrentUser();
        userID = user.getUid();
        mDatabase = FirebaseFirestore.getInstance();

        mBlueAdapter = BluetoothAdapter.getDefaultAdapter();


        btnBluetooth.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                changeBluetooth();
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
            Toast.makeText(getApplicationContext(), "Bluetooth is unavailable. Try again later.", Toast.LENGTH_SHORT).show();
        }
        else
        {
            if(mBlueAdapter.isEnabled())
            {
                mBlueAdapter.disable();
                btnBluetooth.setImageResource(R.drawable.bluetooth_off);
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
                {
                    Toast.makeText(getApplicationContext(), "Error turning Bluetooth on. Try again later.", Toast.LENGTH_SHORT).show();
                }
                break;

        }
        super.onActivityResult(requestCode, resultCode, data);
    }
}
