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
import android.widget.Toast;

import org.apache.commons.io.IOUtils;

import java.io.IOException;
import java.io.InputStream;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private final IntentFilter intentFilter = new IntentFilter();
    WifiP2pManager mManager;
    WifiP2pManager.Channel thisChannel;
    MyReceiver receiver;


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


        mManager = (WifiP2pManager) getSystemService(Context.WIFI_P2P_SERVICE);
        thisChannel = mManager.initialize(this, getMainLooper(), null);


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

                Log.e("The list of", "P2P peers has changed");

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
                        Log.e("GROUP","FORMED");
                        new FileServerAsyncTask(getApplicationContext(), info.groupOwnerAddress.toString()).execute();
                       /* ServerSocket serverSocket = null;
                        try {
                            serverSocket = new ServerSocket(8888);
                            Socket client = serverSocket.accept();
                        }
                        catch(IOException e){
                            e.printStackTrace();
                        }*/

                    }   /*
                            ServerSocket serverSocket = null;
                            try {
                                serverSocket = new ServerSocket(8888);
                                Socket client = serverSocket.accept();


                                InputStream inputstream = client.getInputStream();

                                byte[] buffer = IOUtils.toByteArray(inputstream);
                                String data = new String(buffer, "UTF-8");
                                Log.e("DATA:", data);
                                Toast.makeText(context, "Data Transfer successful", Toast.LENGTH_LONG).show();
                                //copyFile(inputstream, new FileOutputStream(f));
                                serverSocket.close();

                            } catch (IOException e) {
                                e.printStackTrace();
                            }*/


                    // NetworkInfo networkInfo = mManager.EXTRA_NETWORK_INFO;
                    // NetworkInfo networkInfo = intent.getParcelableExtra(mManager.EXTRA_NETWORK_INFO);
                    // new FileServerAsyncTask(getApplicationContext());

                    // InetAddre   ss from WifiP2pInfo struct.
                    // if (info != null) {
                    //   String groupOwnerAddress = info.groupOwnerAddress.getHostAddress();
                    //    Log.e("GROUP OWNER ADD", groupOwnerAddress);
                    //Log.e("Kyle Tablet", device.deviceAddress);
                    //new FileServerAsyncTask(getApplicationContext());
                    // }

                    //  try {
                    //    Thread.sleep(50000);                 //1000 milliseconds is one second.
                    //} catch (InterruptedException ex) {
                    //   Thread.currentThread().interrupt();
                    //}
                    //                 After the group negotiation, we can determine the group owner.


                }
            });

/*


            });*/




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
                    socket.connect((new InetSocketAddress(host,port)), 500);

                    
                } catch (IOException e) {
                    e.printStackTrace();
                }


            }

/*
            ServerSocket serverSocket = null;
            try {
                serverSocket = new ServerSocket(8888);
                Socket client = serverSocket.accept();
                InputStream inputstream = client.getInputStream();

                byte[] buffer = IOUtils.toByteArray(inputstream);
                String data = new String(buffer, "UTF-8");
                Log.e("DATA:", data);
                //Toast.makeText(context, "Data Transfer successful", Toast.LENGTH_LONG).show();
                serverSocket.close();

            }
            catch(IOException e){
                e.printStackTrace();
            }*/

            return null;
        }

       /* @Override
        protected String doInBackground(Void... params) {

            /**
             * Create a server socket and wait for client connections. This
             * call blocks until a connection is accepted from a client
             */

        // String string = "This stream is up and running...@Mayank Kale";
        //byte[] b = string.getBytes();
/*
            Log.e("Inside", "AsyncTask");
            Log.e("Inside", "AsyncTask");

            try {
                Thread.sleep(50000);                 //1000 milliseconds is one second.
            } catch (InterruptedException ex) {
                Thread.currentThread().interrupt();
            }*/

              /*  ServerSocket serverSocket = new ServerSocket(8888);
                Socket client = serverSocket.accept();


                InputStream inputstream = client.getInputStream();

                byte[] buffer = IOUtils.toByteArray(inputstream);
                String data = new String(buffer, "UTF-8");
                Log.e("DATA:", data);
                Toast.makeText(context, "Data Transfer successful", Toast.LENGTH_LONG).show();
                //copyFile(inputstream, new FileOutputStream(f));
                serverSocket.close();
*/
        //    return null;
        //  }


        /**
         * Start activity that can handle the JPEG image
         */

    }
}

