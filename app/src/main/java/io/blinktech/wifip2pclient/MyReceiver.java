package io.blinktech.wifip2pclient;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.NetworkInfo;
import android.net.wifi.WpsInfo;
import android.net.wifi.p2p.WifiP2pConfig;
import android.net.wifi.p2p.WifiP2pDevice;
import android.net.wifi.p2p.WifiP2pDeviceList;
import android.net.wifi.p2p.WifiP2pInfo;
import android.net.wifi.p2p.WifiP2pManager;
import android.util.Log;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by mayank on 7/10/15.
 */
public class MyReceiver extends BroadcastReceiver {

    private WifiP2pManager mManager;
    private WifiP2pManager.Channel mChannel;
    private MainActivity mActivity;
    public Context context;
    private List peers = new ArrayList();
    public Intent intent;
    int i=0;

    public MyReceiver(WifiP2pManager manager, WifiP2pManager.Channel channel,
                      MainActivity activity, Context context) {
        super();
        this.mManager = manager;
        this.mChannel = channel;
        this.mActivity = activity;
        this.context = context;
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();
        this.intent = intent;
        if (WifiP2pManager.WIFI_P2P_STATE_CHANGED_ACTION.equals(action)) {
            // Determine if Wifi P2P mode is enabled or not, alert
            // the Activity.
            int state = intent.getIntExtra(WifiP2pManager.EXTRA_WIFI_STATE, -1);

            if (state == WifiP2pManager.WIFI_P2P_STATE_ENABLED) {
                Toast.makeText(context, "WiFiP2P enabled", Toast.LENGTH_LONG).show();
            } else {

                Toast.makeText(context, "WiFiP2P not enabled", Toast.LENGTH_LONG).show();
            }

        } else if (WifiP2pManager.WIFI_P2P_PEERS_CHANGED_ACTION.equals(action)) {

            // The peer list has changed!  We should probably do something about
            // that.
            Toast.makeText(context, "Changed Peer list", Toast.LENGTH_LONG).show();

            if (mManager != null) {
                mManager.requestPeers(mChannel, peerListListener);
            }

            // Log.e("The list of", "P2P peers has changed");

        } else if (WifiP2pManager.WIFI_P2P_CONNECTION_CHANGED_ACTION.equals(action)) {

            // Connection state changed!  We should probably do something about
            // that.
            Toast.makeText(context, "Connection Changed", Toast.LENGTH_LONG).show();


        } else if (WifiP2pManager.WIFI_P2P_THIS_DEVICE_CHANGED_ACTION.equals(action)) {
            /*    DeviceListFragment fragment = (DeviceListFragment) activity.getFragmentManager()
                    .findFragmentById(R.id.frag_list);
            fragment.updateThisDevice((WifiP2pDevice) intent.getParcelableExtra(
                    WifiP2pManager.EXTRA_WIFI_P2P_DEVICE));*/
            Toast.makeText(context, "Changed Device Config", Toast.LENGTH_LONG).show();

        }
    }

    public void connect() {
        WifiP2pConfig config = new WifiP2pConfig();
        WifiP2pDevice device = (WifiP2pDevice) peers.get(0);
        config.deviceAddress = device.deviceAddress;
        config.wps.setup = WpsInfo.PBC;
        config.groupOwnerIntent = 0;

        mManager.connect(mChannel, config, new WifiP2pManager.ActionListener() {

            @Override
            public void onSuccess() {
                Log.e("Connecting to Kyles Tab", "YES");

            }

            @Override
            public void onFailure(int i) {
                Log.e("Not connected to Kyle", "Damn");
            }

        });

        mManager.requestConnectionInfo(mChannel, new WifiP2pManager.ConnectionInfoListener() {
            @Override

            public void onConnectionInfoAvailable(final WifiP2pInfo info) {
                Log.e("In =","connection info");
                NetworkInfo networkInfo = mActivity.getIntent().getParcelableExtra(WifiP2pManager.EXTRA_NETWORK_INFO);

                if(info.groupFormed | (networkInfo!=null && networkInfo.isConnected())){
                        Log.e("GROUP FORMER",info.groupOwnerAddress.getHostAddress());
                        if(MyAppApplication.count == 0){
                            Log.e("In the","If statement");
                            new MainActivity.FileServerAsyncTask(context,info.groupOwnerAddress.getHostAddress()).execute();
                        }


                }

            }
        });
    }

    public WifiP2pManager.PeerListListener peerListListener = new WifiP2pManager.PeerListListener() {
        @Override
        public void onPeersAvailable(WifiP2pDeviceList peerList) {

            // Out with the old, in with the new.
            peers.clear();
            peers.addAll(peerList.getDeviceList());

            //Log.e("YES", "Function called");

            if (peers.size() == 0) {
                Log.e("Error: ", "No devices found");
                return;
            } else {
                Log.e("YES ", peers.size() + " Devices Found");
                connect();
            }
        }
    };
}
