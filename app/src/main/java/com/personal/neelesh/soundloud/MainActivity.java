package com.personal.neelesh.soundloud;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.net.wifi.WpsInfo;
import android.net.wifi.p2p.WifiP2pConfig;
import android.net.wifi.p2p.WifiP2pDevice;
import android.net.wifi.p2p.WifiP2pInfo;
import android.net.wifi.p2p.WifiP2pManager;
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

import java.net.InetAddress;
import java.util.HashMap;
import java.util.Map;

public class MainActivity extends AppCompatActivity{

    private static final int SERVER_PORT = 3736;
    private WifiP2pManager mManager;
    private WifiP2pManager.Channel mChannel;
    private WifiP2pManager.ConnectionInfoListener connectionInfoListener;
    private HashMap<String, String> buddies = new HashMap<>();

    private final IntentFilter intentFilter = new IntentFilter();
    private WiFiDirectBroadcastReceiver receiver;

    public static final String TAG = String.valueOf(MainActivity.class);
    private String username;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        addActionsToIntentFilter();

        setupUI();

        connectionInfoListener =  new WifiP2pManager.ConnectionInfoListener() {
            @Override
            public void onConnectionInfoAvailable(WifiP2pInfo info) {
                // InetAddress from WifiP2pInfo struct.
//        InetAddress groupOwnerAddress = info.groupOwnerAddress.getHostAddress();

                // After the group negotiation, we can determine the group owner.
                if (info.groupFormed && info.isGroupOwner) {
                    // Do whatever tasks are specific to the group owner.
                    // One common case is creating a server thread and accepting
                    // incoming connections.
                } else if (info.groupFormed) {
                    // The other device acts as the client. In this case,
                    // you'll want to create a client thread that connects to the group
                    // owner.
                }

            }
        };

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
                discoverService();
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

        mManager.connect(mChannel, config, new WifiP2pManager.ActionListener() {
            @Override
            public void onSuccess() {
                Utility.showToast(getBaseContext(), "Connect successful");
                Intent intent = new Intent(getBaseContext(), StreamActivity.class);

                startActivity(intent);
            }

            @Override
            public void onFailure(int reason) {
                Utility.showToast(getBaseContext(), "Connect Failed. Retry");
            }
        });
    }
}
