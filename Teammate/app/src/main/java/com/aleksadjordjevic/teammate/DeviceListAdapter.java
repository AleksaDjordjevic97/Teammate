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

import java.util.ArrayList;
import java.util.Set;

public class DeviceListAdapter extends ArrayAdapter<BluetoothDevice>
{
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
        Set<BluetoothDevice> btPaired = mBlueAdapter.getBondedDevices();

        if (device != null)
        {
            TextView deviceName = convertView.findViewById(R.id.txtBTUserAF);
            TextView deviceAdress = convertView.findViewById(R.id.txtBTAddressAF);
            final TextView txtPair = convertView.findViewById(R.id.txtPairAF);
            final ImageButton btnPair = convertView.findViewById(R.id.btnPairAF);
            final ImageButton btnAddFriend = convertView.findViewById(R.id.btnAddFriendAF);

            if (deviceName != null)
                deviceName.setText(device.getName());
            if (deviceAdress != null)
                deviceAdress.setText(device.getAddress());

            if(btPaired.contains(device))
            {
                txtPair.setText("");
                btnPair.setClickable(false);
                btnPair.setVisibility(View.INVISIBLE);
            }
            btnPair.setOnClickListener(new View.OnClickListener()
            {
                @Override
                public void onClick(View v)
                {
                    if(mBlueAdapter.isEnabled())
                    {
                        mBlueAdapter.cancelDiscovery();
                        device.createBond();
                        txtPair.setText("");
                        btnPair.setClickable(false);
                        btnPair.setVisibility(View.INVISIBLE);
                    }
                }
            });

            btnAddFriend.setOnClickListener(new View.OnClickListener()
            {
                @Override
                public void onClick(View v)
                {

                }
            });

        }

        return convertView;
    }

}
