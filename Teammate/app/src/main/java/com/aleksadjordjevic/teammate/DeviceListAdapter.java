package com.aleksadjordjevic.teammate;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Set;
import java.util.UUID;

public class DeviceListAdapter extends ArrayAdapter<BluetoothDevice>
{

    TextView deviceName, deviceAdress;
    private LayoutInflater mLayoutInflater;
    private ArrayList<BluetoothDevice> mDevices;
    private int mViewResourceId;

    BluetoothAdapter mBlueAdapter;

    public DeviceListAdapter(Context context, int tvResourceId, ArrayList<BluetoothDevice> devices)
    {
        super(context, tvResourceId,devices);
        this.mDevices = devices;
        mLayoutInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        mViewResourceId = tvResourceId;
    }

    public View getView(int position, View convertView, ViewGroup parent)
    {
        convertView = mLayoutInflater.inflate(mViewResourceId, null);

        final BluetoothDevice device = mDevices.get(position);
        mBlueAdapter = BluetoothAdapter.getDefaultAdapter();

        if (device != null)
        {
            deviceName = convertView.findViewById(R.id.txtBTUserAF);
            deviceAdress = convertView.findViewById(R.id.txtBTAddressAF);


            if (deviceName != null)
                deviceName.setText(device.getName());
            if (deviceAdress != null)
                deviceAdress.setText(device.getAddress());

        }

        return convertView;
    }

}
