package com.christo.bluetoothplayground;

import android.app.ListActivity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

import java.util.ArrayList;

public class bluetoothActivity extends ListActivity
{
    BluetoothAdapter mBluetoothAdapter;
    ArrayAdapter<String> btListAdapter;
    ArrayList<BluetoothDevice> btListDevices;
    IntentFilter intentFilter;
    BroadcastReceiver broadcastReceiver;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);

        initBT();

        if (mBluetoothAdapter == null)
        {
            /* Device does not support Bluetooth */
            Toast.makeText(getApplicationContext(), "Bluetooth not supported", Toast.LENGTH_LONG).show();
            finish();
        } else
        {
            if (!mBluetoothAdapter.isEnabled())
            {
                turnOnBT();
            }
            startDiscovery();
        }
    }

    @Override
    protected void onResume()
    {
        super.onResume();
        invalidateOptionsMenu();
        Log.i("BTDebug", "onResume() called");
        startDiscovery();
    }

    @Override
    protected void onPause()
    {
        super.onPause();

        if (mBluetoothAdapter.isDiscovering())
            mBluetoothAdapter.cancelDiscovery();
    }

    @Override
    protected void onDestroy()
    {
        super.onDestroy();
        Log.i("BTDebug", "onDestroy() called");

        /* If there is a BluetoothAdapter, cancelDiscovery and unregister the receiver */
        if (mBluetoothAdapter != null)
        {
            if (mBluetoothAdapter.isDiscovering())
                mBluetoothAdapter.cancelDiscovery();
            unregisterReceiver(broadcastReceiver);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu)
    {
        getMenuInflater().inflate(R.menu.bluetooth, menu);
        if (!mBluetoothAdapter.isDiscovering())
        {
            menu.findItem(R.id.menu_scan).setVisible(true);
            menu.findItem(R.id.menu_stop).setVisible(false);
            menu.findItem(R.id.menu_refresh).setActionView(null);
        } else
        {
            menu.findItem(R.id.menu_scan).setVisible(false);
            menu.findItem(R.id.menu_stop).setVisible(true);
            menu.findItem(R.id.menu_refresh).setActionView(R.layout.actionbar_indeterminate_progress);
        }
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item)
    {
        switch (item.getItemId())
        {
            case R.id.menu_scan:
                startDiscovery();
                break;
            case R.id.menu_stop:
                if (mBluetoothAdapter.isDiscovering())
                    mBluetoothAdapter.cancelDiscovery();
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    private void initBT()
    {
        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        btListAdapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, 0);
        btListDevices = new ArrayList<>();
        setListAdapter(btListAdapter);
        broadcastReceiver =  new BroadcastReceiver()
        {
            @Override
            public void onReceive(Context context, Intent intent)
            {
                String action = intent.getAction();
                /* When discovery finds a device */
                if (BluetoothDevice.ACTION_FOUND.equals(action))
                {
                    /* Get the BluetoothDevice object from the device */
                    BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);

                    /* Create an array of the bluetooth devices */
                    btListDevices.add(device);

                    /* Add the device to the List */
                    if (device.getBondState() == BluetoothDevice.BOND_BONDED)
                        btListAdapter.add(device.getName() + " (Paired)\n" + device.getAddress());
                    else
                        btListAdapter.add(device.getName() + "\n" + device.getAddress());
                } else if (BluetoothAdapter.ACTION_DISCOVERY_STARTED.equals(action)) {
                    invalidateOptionsMenu();
                } else if (BluetoothAdapter.ACTION_DISCOVERY_FINISHED.equals(action)) {
                    invalidateOptionsMenu();
                } else if (BluetoothAdapter.ACTION_STATE_CHANGED.equals(action))
                {
                    /* State of bluetooth changed */
                    if (mBluetoothAdapter.getState() == BluetoothAdapter.STATE_OFF)
                    {
                        /* Request to enable Bluetooth if switched off during operation */
                        turnOnBT();
                    }
                }
            }
        };
        registerReceivers();
    }

    private void registerReceivers()
    {
        intentFilter = new IntentFilter(BluetoothDevice.ACTION_FOUND);
        registerReceiver(broadcastReceiver, intentFilter);
        intentFilter = new IntentFilter(BluetoothAdapter.ACTION_DISCOVERY_STARTED);
        registerReceiver(broadcastReceiver, intentFilter);
        intentFilter = new IntentFilter(BluetoothAdapter.ACTION_DISCOVERY_FINISHED);
        registerReceiver(broadcastReceiver, intentFilter);
        intentFilter = new IntentFilter(BluetoothAdapter.ACTION_STATE_CHANGED);
        registerReceiver(broadcastReceiver, intentFilter);
    }

    private void turnOnBT()
    {
        /* Request to turn on bluetooth */
        Intent enableBTIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
        startActivityForResult(enableBTIntent, 1);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data)
    {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == RESULT_OK)
        {
            /* User pressed OK*/
            Toast.makeText(getApplicationContext(), "Bluetooth enabled", Toast.LENGTH_SHORT).show();
        }
        if (resultCode == RESULT_CANCELED)
        {
            /* User declined the request */
            Toast.makeText(getApplicationContext(), "Bluetooth required to continue", Toast.LENGTH_SHORT).show();
            this.finish();
        }
    }

    private void startDiscovery()
    {
        /* Clear the lists */
        btListAdapter.clear();
        btListDevices.clear();

        /* If discovering cancel discovery and start again */
        if (mBluetoothAdapter.isDiscovering())
            mBluetoothAdapter.cancelDiscovery();
        mBluetoothAdapter.startDiscovery();
    }

    @Override
    protected void onListItemClick(ListView l, View v, int position, long id) {
        super.onListItemClick(l, v, position, id);
            /* If device is discovering, cancel discovery */
        if (mBluetoothAdapter.isDiscovering())
            mBluetoothAdapter.cancelDiscovery();

        BluetoothDevice selectedBTDevice = btListDevices.get(position);
        Log.i("BTDebug", "Device clicked: " + selectedBTDevice.getName() + "\t addr: " + selectedBTDevice.getAddress());

        if (selectedBTDevice.getBondState() == BluetoothDevice.BOND_BONDED)
        {
            Log.i("BTDebug", "Device is paired!");
            Intent keyIntent = new Intent(getApplicationContext(), MainActivity.class);
            keyIntent.putExtra("btDevice", selectedBTDevice);
            Log.i("BTDebug", "Starting new Activity..");
            startActivity(keyIntent);
        } else
        {
            Intent enableBTIntent = new Intent(Settings.ACTION_BLUETOOTH_SETTINGS);
            startActivityForResult(enableBTIntent, -1);
            Toast.makeText(getApplicationContext(), "Please pair: " + selectedBTDevice.getName(), Toast.LENGTH_LONG).show();
        }
    }
}
