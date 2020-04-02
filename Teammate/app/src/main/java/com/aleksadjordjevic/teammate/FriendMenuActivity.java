package com.aleksadjordjevic.teammate;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.bluetooth.BluetoothAdapter;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.Toast;

public class FriendMenuActivity extends AppCompatActivity
{

    static final int REQUEST_ENABLE_BT = 0;
    BluetoothAdapter mBlueAdapter;

    Button btnSendReq, btnViewReq;
    ImageButton btnBack, btnBluetooth;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_friend_menu);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);

        btnBack = findViewById(R.id.btnBackFM);
        btnSendReq = findViewById(R.id.btnSendReqFM);
        btnViewReq = findViewById(R.id.btnViewReqFM);
        btnBluetooth = findViewById(R.id.bluetoothIconFM);

        mBlueAdapter = BluetoothAdapter.getDefaultAdapter();

        btnBack.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                Intent indexIntent = new Intent(getApplicationContext(),IndexActivity.class);
                startActivity(indexIntent);
            }
        });

        btnBluetooth.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                changeBluetooth();
            }
        });

        btnSendReq.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                if(mBlueAdapter.isEnabled())
                {
                    Intent sendReqIntent = new Intent(getApplicationContext(), AddFriendsActivity.class);
                    startActivity(sendReqIntent);
                }
                else
                    Toast.makeText(getApplicationContext(), "Turn on bluetooth first.", Toast.LENGTH_SHORT).show();


            }
        });

        btnViewReq.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                if(mBlueAdapter.isEnabled())
                {
                    Intent viewReqIntent = new Intent(getApplicationContext(), ViewRequestsActivity.class);
                    startActivity(viewReqIntent);
                }
                else
                    Toast.makeText(getApplicationContext(), "Turn on bluetooth first.", Toast.LENGTH_SHORT).show();
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
            Toast.makeText(getApplicationContext(), "Bluetooth is unavailable. Try again later.", Toast.LENGTH_SHORT).show();
        else
        {
            if(mBlueAdapter.isEnabled())
                btnBluetooth.setImageResource(R.drawable.bluetooth_on);
            else
                btnBluetooth.setImageResource(R.drawable.bluetooth_off);
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
                    Toast.makeText(getApplicationContext(), "Error turning Bluetooth on. Try again later.", Toast.LENGTH_SHORT).show();
                break;

        }
        super.onActivityResult(requestCode, resultCode, data);
    }
}
