package com.personal.neelesh.soundloud;

import android.content.Context;
import android.net.wifi.p2p.WifiP2pDevice;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import java.util.ArrayList;

public class DeviceListAdapter extends BaseAdapter {

    private ArrayList<WifiP2pDevice> deviceArrayList;
    private LayoutInflater myLayoutInflater = null;
    private Context context;

    public DeviceListAdapter(Context context, ArrayList<WifiP2pDevice> deviceArrayList) {
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
            v  = myLayoutInflater.inflate(R.layout.device_list_row,parent, false);
            holder = new ViewHolder(v);
            v.setTag(holder);
        } else {
            holder = (ViewHolder) v.getTag();
        }

        holder.setDeviceName(position);
        holder.setDeviceStatus(position);
        return v;
    }

    public void add(WifiP2pDevice device){
        deviceArrayList.add(device);
    }

    private String getDeviceStatus(int position) {
        int deviceStatus = getDevice(position).status;
        Log.d("Device List Adapter", "Peer status :" + deviceStatus);
        switch (deviceStatus) {
            case WifiP2pDevice.AVAILABLE:
                return "Available";
            case WifiP2pDevice.INVITED:
                return "Invited";
            case WifiP2pDevice.CONNECTED:
                return "Connected";
            case WifiP2pDevice.FAILED:
                return "Failed";
            case WifiP2pDevice.UNAVAILABLE:
                return "Unavailable";
            default:
                return "Unknown";
        }
    }

    private WifiP2pDevice getDevice(int position) {
        return deviceArrayList.get(position);
    }

    public WifiP2pDevice getDeviceAtPosition(int position) {
        return deviceArrayList.get(position);
    }

    class ViewHolder {
        public TextView view_deviceName;
        public TextView view_deviceStatus;
        public ViewHolder (View v){
            view_deviceStatus = (TextView) v.findViewById(R.id.deviceStatus);
            view_deviceName = (TextView) v.findViewById(R.id.deviceName);
        }

        public void setDeviceName(int position){
            view_deviceName.setText(getDevice(position).deviceName);
        }

        public void setDeviceStatus(int position) {
            view_deviceStatus.setText(getDeviceStatus(position));
        }
    }
}
