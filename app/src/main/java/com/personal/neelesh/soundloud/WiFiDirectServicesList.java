package com.personal.neelesh.soundloud;

import android.app.Fragment;
import android.app.ListFragment;
import android.content.Context;
import android.net.wifi.p2p.WifiP2pDevice;
import android.os.Bundle;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListAdapter;
import android.widget.ListView;

import java.util.ArrayList;

public class WiFiDirectServicesList extends Fragment {

    private ListAdapter adapter;
    private ListView listView;


    public WiFiDirectServicesList() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (getArguments() != null) {

        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_wifidirectserviceslist_list, container, false);

        listView = (ListView) view.findViewById(R.id.deviceslistView);
        ArrayList<WifiP2pDevice> deviceArrayList = new ArrayList<WifiP2pDevice>();
        adapter = new WiFiDevicesAdapter(getActivity().getBaseContext(), deviceArrayList);
        listView.setAdapter(adapter);
        return view;
    }

    public ListAdapter getListAdapter() {
        return adapter;
    }
    public ListView getListView() {
        return listView;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
    }

    @Override
    public void onDetach() {
        super.onDetach();
    }
}
