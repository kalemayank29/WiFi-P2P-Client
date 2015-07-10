package io.blinktech.wifip2pclient;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.NetworkInfo;
import android.net.wifi.WpsInfo;
import android.net.wifi.p2p.WifiP2pConfig;
import android.net.wifi.p2p.WifiP2pDevice;
import android.net.wifi.p2p.WifiP2pDeviceList;
import android.net.wifi.p2p.WifiP2pInfo;
import android.net.wifi.p2p.WifiP2pManager;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import org.apache.commons.io.IOUtils;
import org.apache.http.NameValuePair;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectOutput;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private final IntentFilter intentFilter = new IntentFilter();
    WifiP2pManager mManager;
    WifiP2pManager.Channel thisChannel;
    MyReceiver receiver;
    Button button;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        button = (Button) findViewById(R.id.button);


        //  Indicates a change in the Wi-Fi P2P status.
        intentFilter.addAction(WifiP2pManager.WIFI_P2P_STATE_CHANGED_ACTION);


        // Indicates a change in the list of available peers.
        intentFilter.addAction(WifiP2pManager.WIFI_P2P_PEERS_CHANGED_ACTION);


        // Indicates the state of Wi-Fi P2P connectivity has changed.
        intentFilter.addAction(WifiP2pManager.WIFI_P2P_CONNECTION_CHANGED_ACTION);


        // Indicates this device's details have changed.
        intentFilter.addAction(WifiP2pManager.WIFI_P2P_THIS_DEVICE_CHANGED_ACTION);


        mManager = (WifiP2pManager) getSystemService(Context.WIFI_P2P_SERVICE);
        thisChannel = mManager.initialize(this, getMainLooper(), null);


        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                mManager.discoverPeers(thisChannel, new WifiP2pManager.ActionListener() {
                    @Override
                    public void onSuccess() {
                        Log.e("Peer Discovery", "Successful");
                        //Toast.makeText(getApplicationContext(), "Peer connection Successful", Toast.LENGTH_LONG).show();
                    }

                    @Override
                    public void onFailure(int reasonCode) {
                        // Code for when the discovery initiation fails goes here.
                        // Alert the user that something went wrong
                        Toast.makeText(getApplicationContext(), "Peer discovery Unsuccessful", Toast.LENGTH_LONG).show();
                    }
                });

            }
        });


        //Log.e("Back","To main activity");
    }

    /**
     * register the BroadcastReceiver with the intent values to be matched
     */
    @Override
    public void onResume() {
        super.onResume();
        receiver = new MyReceiver(mManager, thisChannel, this, getApplicationContext());
        registerReceiver(receiver, intentFilter);
        // Log.e("Back", "To resume function");
    }

    @Override
    public void onPause() {
        super.onPause();
        unregisterReceiver(receiver);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private class MyReceiver extends BroadcastReceiver  {

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
                    if(info.groupFormed){
                        Log.e("GROUP FORMER",info.groupOwnerAddress.getHostAddress());

                        new FileServerAsyncTask(getApplicationContext(),info.groupOwnerAddress.getHostAddress()).execute();

                    //i++;
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
                    Log.e("Error:", "No devices found");
                    return;
                } else {
                    Log.e("YES", peers.size() + "Devices Found");
                    connect();
                }
            }
        };
    }

    public static class FileServerAsyncTask extends AsyncTask {

        Context context;
        String host;
        int port = 8888;
        int len;
        Socket socket = new Socket();
        byte buf[] = new byte[1024];

        public FileServerAsyncTask(Context context, String host) {
            this.context = context;
            this.host = host;

        }

        @Override
        protected Object doInBackground(Object[] objects) {
            Log.e("Its","here");

            if(host!= null) {

                Log.e("HOST:",host);
                try {
                    socket.bind(null);
                    socket.connect((new InetSocketAddress(host,port)), 8000);
                    String data ="This is the data";

                    HashMap<String,String> map = new HashMap<String, String>();
                    map.put("Mayank","Kale");
                    map.put("Kyle","CS GOD");

                    ByteArrayOutputStream bos = new ByteArrayOutputStream();
                    ObjectOutput out = null;
                    out = new ObjectOutputStream(bos);
                    out.writeObject(map);
                    buf = bos.toByteArray();
                   // buf = data.getBytes();
                    OutputStream outputStream = socket.getOutputStream();
                    int len = buf.length;
                    outputStream.write(buf, 0 , len);

                    outputStream.close();
                    
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            return null;
        }
    }
}


