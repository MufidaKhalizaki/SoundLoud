package com.personal.neelesh.soundloud;

import android.content.Context;
import android.net.wifi.p2p.WifiP2pDevice;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ListAdapter;
import android.widget.TextView;

import java.util.ArrayList;

public class WiFiDevicesAdapter extends BaseAdapter implements ListAdapter {

    ArrayList<WifiP2pDevice> deviceArrayList;
    LayoutInflater myLayoutInflater = null;
    Context context;

    public WiFiDevicesAdapter (Context context, ArrayList<WifiP2pDevice> deviceArrayList) {
        myLayoutInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        this.context = context;
        this.deviceArrayList = deviceArrayList;
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

        holder.setDeviceName(deviceArrayList.get(position).deviceName);
        holder.setDeviceAddress(deviceArrayList.get(position).deviceAddress);
        return v;
    }

    public void add(WifiP2pDevice device){
        deviceArrayList.add(device);
    }

    public ArrayList<WifiP2pDevice> getDeviceArrayList () {
        return deviceArrayList;
    }

    @Override
    public void notifyDataSetChanged() {
        super.notifyDataSetChanged();
        Utility.showToast(context, "notified");
    }

    class ViewHolder {
        public TextView view_deviceName;
        public TextView view_deviceAddress;
        public ViewHolder (View v){
            view_deviceAddress = (TextView) v.findViewById(R.id.deviceAddress);
            view_deviceName = (TextView) v.findViewById(R.id.deviceName);
        }

        public void setDeviceName(String deviceName){
            view_deviceName.setText(deviceName);
        }

        public void setDeviceAddress(String deviceAddress) {
            view_deviceName.setText(deviceAddress);
        }
    }
}
