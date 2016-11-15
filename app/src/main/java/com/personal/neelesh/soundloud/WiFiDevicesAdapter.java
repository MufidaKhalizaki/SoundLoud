package com.personal.neelesh.soundloud;

import android.content.Context;
import android.net.wifi.p2p.WifiP2pDevice;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ListAdapter;
import android.widget.TextView;

import java.util.ArrayList;

public class WiFiDevicesAdapter extends BaseAdapter {

    private ArrayList<WifiP2pDevice> deviceArrayList;
    private LayoutInflater myLayoutInflater = null;
    private Context context;

    public WiFiDevicesAdapter (Context context, ArrayList<WifiP2pDevice> deviceArrayList) {
        this.context = context;
        this.myLayoutInflater = LayoutInflater.from(this.context);
        this.deviceArrayList = deviceArrayList;
        Log.d("device list size", String.valueOf(this.deviceArrayList.size()));
    }

    @Override
    public int getCount() {
        return deviceArrayList.size();
    }

    @Override
    public Object getItem(int position) {
        return deviceArrayList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View v = convertView;

        ViewHolder holder;
        if (convertView == null) {
            v  = myLayoutInflater.inflate(R.layout.fragment_wifidirectserviceslist,parent, false);
            holder = new ViewHolder(v);
            v.setTag(holder);
        } else {
            holder = (ViewHolder) v.getTag();
        }

        holder.setDeviceName(position);
        holder.setDeviceAddress(position);
        return v;
    }

    public void add(WifiP2pDevice device){
        deviceArrayList.add(device);
    }

    public WifiP2pDevice getDeviceAtPosition(int position) {
        return deviceArrayList.get(position);
    }

    class ViewHolder {
        public TextView view_deviceName;
        public TextView view_deviceAddress;
        public ViewHolder (View v){
            view_deviceAddress = (TextView) v.findViewById(R.id.deviceAddress);
            view_deviceName = (TextView) v.findViewById(R.id.deviceName);
        }

        public void setDeviceName(int position){
            view_deviceName.setText(deviceArrayList.get(position).deviceName);
        }

        public void setDeviceAddress(int position) {
            view_deviceAddress.setText(deviceArrayList.get(position).deviceAddress);
        }
    }
}
