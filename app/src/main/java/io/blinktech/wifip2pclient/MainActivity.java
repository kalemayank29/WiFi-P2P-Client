package io.blinktech.wifip2pclient;

import android.app.Activity;
import android.app.Application;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.NetworkInfo;
import android.net.wifi.WifiManager;
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

class MyAppApplication extends Application {

    public static int count = 0;

}

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

        WifiManager manager = (WifiManager) getSystemService(Context.WIFI_SERVICE);
        manager.setWifiEnabled(false);
        Log.e("Resetting Wifi","now");
        manager.setWifiEnabled(true);

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




    public static class FileServerAsyncTask extends AsyncTask {

        Context context;
        String host;
        int port = 8888;
        int len;
        Socket socket = new Socket();
        byte buf[] = new byte[1024];
        //Activity main;

        public FileServerAsyncTask(Context context, String host) {
          //  this.main = main;
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
                    //String data ="This is the data";

                    HashMap<String,String> map = new HashMap<String, String>();
                    map.put("Mayank","Kale");
                    map.put("Kyle","CS GOD");

                    ByteArrayOutputStream bos = new ByteArrayOutputStream();
                    ObjectOutput out = null;
                    out = new ObjectOutputStream(bos);
                    out.writeObject(map);
                    byte[] array = bos.toByteArray();
                   // buf = data.getBytes();
                    OutputStream outputStream = socket.getOutputStream();
                    int len = array.length;
                    outputStream.write(array, 0 , len);

                    outputStream.close();
                    bos.close();
                    //main.function_count++;
                    MyAppApplication.count++;

                    socket.close();
                    
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            return null;
        }
    }
}


