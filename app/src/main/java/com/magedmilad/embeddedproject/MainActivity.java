package com.magedmilad.embeddedproject;

import android.app.ProgressDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.Set;

public class MainActivity extends AppCompatActivity {

    ListView pairedListView;
    private BluetoothAdapter mBtAdapter;
    private ArrayAdapter<String> mPairedDevicesArrayAdapter;
    private ProgressDialog mProgressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        mProgressDialog = new ProgressDialog(this);
        mProgressDialog.setTitle("Loading");
        mProgressDialog.setMessage("Searching for Devices");


        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mProgressDialog.setButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        unregisterReceiver(_foundReceiver);
                        unregisterReceiver(_discoveryReceiver);
                        mBtAdapter.cancelDiscovery();
                        dialog.dismiss();
                    }
                });
                IntentFilter discoveryFilter = new IntentFilter(BluetoothAdapter.ACTION_DISCOVERY_FINISHED);
                registerReceiver(_discoveryReceiver, discoveryFilter);
                IntentFilter foundFilter = new IntentFilter(BluetoothDevice.ACTION_FOUND);
                registerReceiver(_foundReceiver, foundFilter);
                mBtAdapter.startDiscovery();
                mProgressDialog.show();
            }
            private BroadcastReceiver _foundReceiver = new BroadcastReceiver() {
                public void onReceive(Context context, Intent intent) {
                    BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                    mPairedDevicesArrayAdapter.add(device.getName() + "\n" + device.getAddress());
                    mPairedDevicesArrayAdapter.notifyDataSetChanged();
                    Toast.makeText(getBaseContext(), "Found: "+device.getName() + "\n" + device.getAddress(), Toast.LENGTH_SHORT).show();
                }
            };
            private BroadcastReceiver _discoveryReceiver = new BroadcastReceiver() {

                @Override
                public void onReceive(Context context, Intent intent)
                {
                    unregisterReceiver(_foundReceiver);
                    unregisterReceiver(this);
                    mBtAdapter.cancelDiscovery();
                    mProgressDialog.dismiss();
                }
            };

        });

        mPairedDevicesArrayAdapter = new ArrayAdapter<String>(this, R.layout.device_name);
        pairedListView = (ListView) findViewById(R.id.paired_devices);
        pairedListView.setAdapter(mPairedDevicesArrayAdapter);

        pairedListView.setOnItemClickListener(mDeviceClickListener);


    }

    @Override
    public void onResume() {
        super.onResume();
        checkBTState();
        mPairedDevicesArrayAdapter.clear();
        mBtAdapter = BluetoothAdapter.getDefaultAdapter();
        Set<BluetoothDevice> pairedDevices = mBtAdapter.getBondedDevices();
        if (pairedDevices.size() > 0) {
            for (BluetoothDevice device : pairedDevices) {
                mPairedDevicesArrayAdapter.add(device.getName() + "\n" + device.getAddress());
            }
        }
    }


    private void checkBTState()
    {

        mBtAdapter=BluetoothAdapter.getDefaultAdapter();
        if(mBtAdapter==null) {
            Toast.makeText(getBaseContext(), "Device does not support Bluetooth", Toast.LENGTH_SHORT).show();
            finish();
        } else {
            if (!mBtAdapter.isEnabled()) {
                Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                startActivityForResult(enableBtIntent, 1);
            }
        }
    }

    private AdapterView.OnItemClickListener mDeviceClickListener = new AdapterView.OnItemClickListener()
    {
        public void onItemClick(AdapterView<?> av, View v, int arg2, long arg3)
        {
            String info = ((TextView) v).getText().toString();
            String address = info.substring(info.length() - 17);
            Intent i = new Intent(MainActivity.this, PlayActivity.class);
            i.putExtra(Intent.EXTRA_UID, address);
            startActivity(i);
        }
    };

}
