package com.personal.neelesh.soundloud;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.net.wifi.WpsInfo;
import android.net.wifi.p2p.WifiP2pConfig;
import android.net.wifi.p2p.WifiP2pDevice;
import android.net.wifi.p2p.WifiP2pManager;
import android.net.wifi.p2p.WifiP2pManager.*;
import android.net.wifi.p2p.nsd.WifiP2pDnsSdServiceInfo;
import android.net.wifi.p2p.nsd.WifiP2pDnsSdServiceRequest;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import java.util.HashMap;
import java.util.Map;

import com.personal.neelesh.soundloud.DeviceListFragment.DeviceActionListener;

public class MainActivity extends AppCompatActivity implements DeviceActionListener, ChannelListener{

    private static final int SERVER_PORT = 3736;
    private WifiP2pManager mManager;
    private Channel mChannel;
    private ConnectionInfoListener connectionInfoListener;
    private HashMap<String, String> buddies = new HashMap<>();

    private final IntentFilter intentFilter = new IntentFilter();
    private WiFiDirectBroadcastReceiver receiver = null;

    public static final String TAG = String.valueOf(MainActivity.class);
    private String username;
    private boolean isWifiP2pEnabled = false;
    private boolean retryChannel = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        addActionsToIntentFilter();

        setupUI();

        mManager = (WifiP2pManager) getSystemService(WIFI_P2P_SERVICE);
        mChannel = mManager.initialize(this, getMainLooper(), null);
    }

    private void addActionsToIntentFilter() {
        //  Indicates a change in the Wi-Fi P2P status.
        intentFilter.addAction(WifiP2pManager.WIFI_P2P_STATE_CHANGED_ACTION);

        // Indicates a change in the list of available peers.
        intentFilter.addAction(WifiP2pManager.WIFI_P2P_PEERS_CHANGED_ACTION);

        // Indicates the state of Wi-Fi P2P connectivity has changed.
        intentFilter.addAction(WifiP2pManager.WIFI_P2P_CONNECTION_CHANGED_ACTION);

        // Indicates this device's details have changed.
        intentFilter.addAction(WifiP2pManager.WIFI_P2P_THIS_DEVICE_CHANGED_ACTION);
    }

    private void startRegistration() {
        Map record = new HashMap();
        record.put("listenport", String.valueOf(SERVER_PORT));
        record.put("buddyname", username);
        record.put("available", "visible");


        WifiP2pDnsSdServiceInfo serviceInfo = WifiP2pDnsSdServiceInfo
                .newInstance("_test", "_presence._tcp", record);

        mManager.addLocalService(mChannel, serviceInfo, new ActionListener() {
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

        DnsSdTxtRecordListener txtListener = new DnsSdTxtRecordListener() {
            @Override
            public void onDnsSdTxtRecordAvailable(String fullDomainName, Map<String, String> record, WifiP2pDevice srcDevice) {
                Log.d(TAG, "DnsSdTxtRecord available -" + record.toString());
                buddies.put(srcDevice.deviceAddress, record.get("buddyname"));
            }
        };

        DnsSdServiceResponseListener servListener = new DnsSdServiceResponseListener() {
            @Override
            public void onDnsSdServiceAvailable(String instanceName, String registrationType, WifiP2pDevice resourceType) {
                resourceType.deviceName = buddies
                        .containsKey(resourceType.deviceAddress)?
                        buddies.get(resourceType.deviceAddress):resourceType.deviceName;

                DeviceListFragment fragment = (DeviceListFragment) getFragmentManager().findFragmentByTag("DeviceListFragment");
                DeviceListAdapter adapter;
                if (fragment != null) {
                    adapter = (DeviceListAdapter) fragment.getListAdapter();
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

        mManager.addServiceRequest(mChannel, serviceRequest, new ActionListener() {
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

        mManager.discoverServices(mChannel, new ActionListener() {
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

    public void setIsWifiP2pEnabled (boolean isWifiP2pEnabled) {
        this.isWifiP2pEnabled = isWifiP2pEnabled;
        if (isWifiP2pEnabled) {
            Utility.showToast(getApplicationContext(), "p2p is enabled");
        } else {
            Utility.showToast(getApplicationContext(), "p2p is disabled");
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        receiver = new WiFiDirectBroadcastReceiver(mManager, mChannel, this, connectionInfoListener);
        registerReceiver(receiver, intentFilter);
    }

    @Override
    protected void onPause() {
        super.onPause();
        unregisterReceiver(receiver);
    }

    public void openNameDialog () {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        final EditText editText = new EditText(this);
        editText.setPadding(40, 20, 40, 20);
        editText.setHint("This will be used to display to other users");
        builder.setTitle("Add Your Name")
                .setView(editText)
                .setPositiveButton("Save", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        saveUserName(String.valueOf(editText.getText()));
                    }
                });
        AlertDialog dialog = builder.create();
        dialog.show();
    }

    public void saveUserName(String username) {
        SharedPreferences preferences = getPreferences(MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putString("username",username);
        editor.commit();
        setUsername();
    }

    public void setupUI(){
        setUsername();
        addButtonOnClickListeners();
    }

    public void addButtonOnClickListeners() {
        Button host_button = (Button) findViewById(R.id.button_host);
        Button discover_button = (Button) findViewById(R.id.button_discover);
        Button add_name_button = (Button) findViewById(R.id.button_add_name);

        add_name_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openNameDialog();
            }
        });
        host_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startRegistration();
            }
        });
        discover_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
//                discoverService();
                if (!isWifiP2pEnabled) {
                    Utility.showToast(MainActivity.this, R.string.p2p_off_warning);
                }
                final DeviceListFragment fragment = (DeviceListFragment) getFragmentManager()
                        .findFragmentById(R.id.deviceListFragment);
                fragment.onInitiateDiscovery();
                mManager.discoverPeers(mChannel, new WifiP2pManager.ActionListener() {

                    @Override
                    public void onSuccess() {
                        Utility.showToast(MainActivity.this, "Discovery Initiated");
                    }

                    @Override
                    public void onFailure(int reasonCode) {
                        Utility.showToast(MainActivity.this, "Discovery Failed : " + reasonCode);
                    }
                });
            }
        });
    }

    public void setUsername() {
        username = getUsername();
        TextView textView = (TextView) findViewById(R.id.textview_hello_user);
        textView.setText("Hello " + username + " !");
    }

    public String getUsername() {
        SharedPreferences preferences = getPreferences(MODE_PRIVATE);
        username  = preferences.getString("username", "");
        if (username.equals("")) {
            username = "John Doe" + (int) (Math.random() * 1000);
        }
        return username;
    }

    public void connect (WifiP2pDevice device) {
        WifiP2pConfig config = new WifiP2pConfig();
        config.deviceAddress = device.deviceAddress;
        config.wps.setup = WpsInfo.PBC;

        mManager.connect(mChannel, config, new ActionListener() {
            @Override
            public void onSuccess() {
                Utility.showToast(getBaseContext(), "Connect successful");
                Intent intent = new Intent(getBaseContext(), DeviceDetailFragment.class);

                startActivity(intent);
            }

            @Override
            public void onFailure(int reason) {
                Utility.showToast(getBaseContext(), "Connect Failed. Retry");
            }
        });
    }

    /**
     * Remove all peers and clear all fields. This is called on
     * BroadcastReceiver receiving a state change event.
     */
    public void resetData() {
        DeviceListFragment fragmentList = (DeviceListFragment) getFragmentManager()
                .findFragmentById(R.id.deviceListFragment);
        DeviceDetailFragment fragmentDetails = (DeviceDetailFragment) getFragmentManager()
                .findFragmentById(R.id.deviceDetailFragment);
        if (fragmentList != null) {
            fragmentList.clearPeers();
        }
        if (fragmentDetails != null) {
            fragmentDetails.resetViews();
        }
    }

    @Override
    public void onChannelDisconnected() {
        // we will try once more
        if (mManager != null && !retryChannel) {
            Utility.showToast(this, "Channel lost. Trying again");
            resetData();
            retryChannel = true;
            mManager.initialize(this, getMainLooper(), this);
        } else {
            Utility.showToast(this, "Severe! Channel is probably lost premanently. Try Disable/Re-Enable P2P.");
        }
    }

    @Override
    public void showDetails(WifiP2pDevice device) {
        DeviceDetailFragment fragment = (DeviceDetailFragment) getFragmentManager()
                .findFragmentById(R.id.deviceDetailFragment);
        fragment.showDetails(device);

    }

    @Override
    public void cancelDisconnect() {

        /*
         * A cancel abort request by user. Disconnect i.e. removeGroup if
         * already connected. Else, request WifiP2pManager to abort the ongoing
         * request
         */
        if (mManager != null) {
            final DeviceListFragment fragment = (DeviceListFragment) getFragmentManager()
                    .findFragmentById(R.id.deviceListFragment);
            if (fragment.getDevice() == null
                    || fragment.getDevice().status == WifiP2pDevice.CONNECTED) {
                disconnect();
            } else if (fragment.getDevice().status == WifiP2pDevice.AVAILABLE
                    || fragment.getDevice().status == WifiP2pDevice.INVITED) {

                mManager.cancelConnect(mChannel, new ActionListener() {

                    @Override
                    public void onSuccess() {
                        Utility.showToast(MainActivity.this, "Aborting connection");
                    }

                    @Override
                    public void onFailure(int reasonCode) {
                        Utility.showToast(MainActivity.this, "Connect abort request failed. Reason Code: " + reasonCode);
                    }
                });
            }
        }

    }

    @Override
    public void connect(WifiP2pConfig config) {
        mManager.connect(mChannel, config, new ActionListener() {

            @Override
            public void onSuccess() {
                // WiFiDirectBroadcastReceiver will notify us. Ignore for now.
            }

            @Override
            public void onFailure(int reason) {
                Utility.showToast(MainActivity.this, "Connect failed. Retry.");
            }
        });
    }

    @Override
    public void disconnect() {
        final DeviceDetailFragment fragment = (DeviceDetailFragment) getFragmentManager()
                .findFragmentById(R.id.deviceDetailFragment);
        fragment.resetViews();
        mManager.removeGroup(mChannel, new ActionListener() {

            @Override
            public void onFailure(int reasonCode) {
                Log.d(TAG, "Disconnect failed. Reason :" + reasonCode);

            }

            @Override
            public void onSuccess() {
                fragment.getView().setVisibility(View.GONE);
            }

        });
    }
}
