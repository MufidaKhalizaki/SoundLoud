package com.personal.neelesh.soundloud;

import android.content.IntentFilter;
import android.net.wifi.p2p.WifiP2pDevice;
import android.net.wifi.p2p.WifiP2pManager;
import android.net.wifi.p2p.nsd.WifiP2pDnsSdServiceInfo;
import android.net.wifi.p2p.nsd.WifiP2pDnsSdServiceRequest;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import java.util.HashMap;
import java.util.Map;

public class MainActivity extends FragmentActivity {

    private static final int SERVER_PORT = 3736;
    private WifiP2pManager mManager;
    private WifiP2pManager.Channel mChannel;
    private HashMap<String, String> buddies = new HashMap<>();

    private final IntentFilter intentFilter = new IntentFilter();
    private WiFiDirectBroadcastReceiver receiver;

    private final String TAG = String.valueOf(MainActivity.class);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //  Indicates a change in the Wi-Fi P2P status.
        intentFilter.addAction(WifiP2pManager.WIFI_P2P_STATE_CHANGED_ACTION);

        // Indicates a change in the list of available peers.
        intentFilter.addAction(WifiP2pManager.WIFI_P2P_PEERS_CHANGED_ACTION);

        // Indicates the state of Wi-Fi P2P connectivity has changed.
        intentFilter.addAction(WifiP2pManager.WIFI_P2P_CONNECTION_CHANGED_ACTION);

        // Indicates this device's details have changed.
        intentFilter.addAction(WifiP2pManager.WIFI_P2P_THIS_DEVICE_CHANGED_ACTION);

        Button host_button = (Button) findViewById(R.id.button_host);
        Button discover_button = (Button) findViewById(R.id.button_discover);

        host_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startRegistration();
            }
        });
        discover_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                discoverService();
            }
        });

        mManager = (WifiP2pManager) getSystemService(WIFI_P2P_SERVICE);
        mChannel = mManager.initialize(this, getMainLooper(), null);
    }

    private void startRegistration() {
        Map record = new HashMap();
        record.put("listenport", String.valueOf(SERVER_PORT));
        record.put("buddyname", "John Doe" + (int) (Math.random() * 1000));
        record.put("available", "visible");


        WifiP2pDnsSdServiceInfo serviceInfo = WifiP2pDnsSdServiceInfo
                .newInstance("_test", "_presence._tcp", record);

        mManager.addLocalService(mChannel, serviceInfo, new WifiP2pManager.ActionListener() {
            @Override
            public void onSuccess() {
                Log.d(TAG, "startReg -> addLocalService -> success");
            }

            @Override
            public void onFailure(int reason) {
                Log.d(TAG, "startReg -> addLocalService -> failure");
                showWifiP2pError(reason);
            }
        });
    }

    public void showWifiP2pError(int reason) {
        if (reason == WifiP2pManager.P2P_UNSUPPORTED) {
            Utility.showToast(getApplicationContext(), "p2p unsupported");
        } else if (reason == WifiP2pManager.BUSY) {
            Utility.showToast(getApplicationContext(), "busy");
        } else if (reason == WifiP2pManager.ERROR) {
            Utility.showToast(getApplicationContext(), "error");
        }
    }

    private void discoverService(){

        WifiP2pManager.DnsSdTxtRecordListener txtListener = new WifiP2pManager.DnsSdTxtRecordListener() {
            @Override
            public void onDnsSdTxtRecordAvailable(String fullDomainName, Map<String, String> record, WifiP2pDevice srcDevice) {
                Log.d(TAG, "DnsSdTxtRecord available -" + record.toString());
                buddies.put(srcDevice.deviceAddress, record.get("buddyname"));
            }
        };

        WifiP2pManager.DnsSdServiceResponseListener servListener = new WifiP2pManager.DnsSdServiceResponseListener() {
            @Override
            public void onDnsSdServiceAvailable(String instanceName, String registrationType, WifiP2pDevice resourceType) {
                resourceType.deviceName = buddies
                        .containsKey(resourceType.deviceAddress)?
                        buddies.get(resourceType.deviceAddress):resourceType.deviceName;

                WiFiDirectServicesList fragment = (WiFiDirectServicesList) getFragmentManager().findFragmentByTag("DeviceListFragment");
                WiFiDevicesAdapter adapter;
                if (fragment != null) {
                    adapter = (WiFiDevicesAdapter) fragment.getListAdapter();
                    adapter.add(resourceType);
                    adapter.notifyDataSetChanged();
                } else {
                    Log.d(TAG, "Device List Fragment Not Found");
                }

                Log.d(TAG, "onBonjourServiceAvailable - " + instanceName);
            }
        };

        mManager.setDnsSdResponseListeners(mChannel, servListener, txtListener);

        WifiP2pDnsSdServiceRequest serviceRequest = WifiP2pDnsSdServiceRequest.newInstance();

        mManager.addServiceRequest(mChannel, serviceRequest, new WifiP2pManager.ActionListener() {
            @Override
            public void onSuccess() {
                // success
                Log.d(TAG, "addServiceRequest -> success");
            }

            @Override
            public void onFailure(int reason) {
                showWifiP2pError(reason);
            }
        });

        mManager.discoverServices(mChannel, new WifiP2pManager.ActionListener() {
            @Override
            public void onSuccess() {
                // success
                Log.d(TAG, "discoverServices -> success");
            }

            @Override
            public void onFailure(int reason) {
                showWifiP2pError(reason);
            }
        });
    }

    public void setIsWifiP2pEnabled (boolean state) {
        if (state) {
            Utility.showToast(getApplicationContext(), "p2p is enabled");
        } else {
            Utility.showToast(getApplicationContext(), "p2p is disabled");
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        receiver = new WiFiDirectBroadcastReceiver(mManager, mChannel, this);
        registerReceiver(receiver, intentFilter);
    }

    @Override
    protected void onPause() {
        super.onPause();
        unregisterReceiver(receiver);
    }
}
