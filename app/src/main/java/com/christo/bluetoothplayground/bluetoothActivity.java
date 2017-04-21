package com.christo.bluetoothplayground;

import android.app.ListActivity;
import android.app.ProgressDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Looper;
import android.provider.Settings;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.concurrent.ThreadLocalRandom;

public class bluetoothActivity extends ListActivity {
    BluetoothAdapter mBluetoothAdapter;
    ArrayAdapter<String> btListAdapter;
    ArrayList<BluetoothDevice> btListDevices;
    IntentFilter intentFilter;
    BroadcastReceiver broadcastReceiver;

    private Context mContext;
    Boolean mAutoConnect = false;
    String mBTAddress = null;

    public static final String PREFS_NAME = "MyPrefsFile";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mContext = this;
        SharedPreferences settings = getSharedPreferences(PREFS_NAME, 0);
        mAutoConnect = settings.getBoolean("autoConnect", false);
        mBTAddress = settings.getString("btAddress", "0");

        initBT();

        if (mBluetoothAdapter == null) {
            /* Device does not support Bluetooth */
            Toast.makeText(getApplicationContext(), "Bluetooth not supported", Toast.LENGTH_LONG).show();
            finish();
        } else {
            if (!mBluetoothAdapter.isEnabled()) {
                turnOnBT();
            }
            startDiscovery();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        invalidateOptionsMenu();
        Log.i("BTDebug", "onResume() called");
        startDiscovery();
    }

    @Override
    protected void onPause() {
        super.onPause();

        if (mBluetoothAdapter.isDiscovering())
            mBluetoothAdapter.cancelDiscovery();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.i("BTDebug", "onDestroy() called");

        /* If there is a BluetoothAdapter, cancelDiscovery and unregister the receiver */
        if (mBluetoothAdapter != null) {
            if (mBluetoothAdapter.isDiscovering())
                mBluetoothAdapter.cancelDiscovery();
            unregisterReceiver(broadcastReceiver);
        }
    }

    private Drawable mAutoIcon;

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.bluetooth, menu);

        mAutoIcon = menu.findItem(R.id.menu_autoConnect).getIcon();

        if (mAutoConnect) {
            if ("0".equals(mBTAddress))
                mAutoIcon.setColorFilter(Color.CYAN, PorterDuff.Mode.SRC_ATOP);
            else
                mAutoIcon.setColorFilter(Color.GREEN, PorterDuff.Mode.SRC_ATOP);
        } else
            mAutoIcon.setColorFilter(Color.WHITE, PorterDuff.Mode.SRC_ATOP);

        if (!mBluetoothAdapter.isDiscovering()) {
            menu.findItem(R.id.menu_scan).setVisible(true);
            menu.findItem(R.id.menu_stop).setVisible(false);
            menu.findItem(R.id.menu_refresh).setActionView(null);
        } else {
            menu.findItem(R.id.menu_scan).setVisible(false);
            menu.findItem(R.id.menu_stop).setVisible(true);
            menu.findItem(R.id.menu_refresh).setActionView(R.layout.actionbar_indeterminate_progress);
        }
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_scan:
                startDiscovery();
                break;
            case R.id.menu_stop:
                if (mBluetoothAdapter.isDiscovering())
                    mBluetoothAdapter.cancelDiscovery();
                break;
            case R.id.menu_autoConnect:
                mAutoConnect = !mAutoConnect;

                SharedPreferences settings = getSharedPreferences(PREFS_NAME, 0);
                SharedPreferences.Editor editor = settings.edit();
                editor.putBoolean("autoConnect", mAutoConnect);
                editor.apply();

                if (mAutoConnect) {
                    if ("0".equals(mBTAddress))
                        mAutoIcon.setColorFilter(Color.CYAN, PorterDuff.Mode.SRC_ATOP);
                    else
                        mAutoIcon.setColorFilter(Color.GREEN, PorterDuff.Mode.SRC_ATOP);

                    Toast.makeText(getApplicationContext(), "AutoConnect selected for next device.", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(getApplicationContext(), "AutoConnect off.", Toast.LENGTH_SHORT).show();
                    mAutoIcon.setColorFilter(Color.WHITE, PorterDuff.Mode.SRC_ATOP);
                    mBTAddress = "0";
                    editor.putString("btAddress", mBTAddress);
                    editor.apply();
                }


                break;
        }
        return super.onOptionsItemSelected(item);
    }

    private void initBT() {
        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        btListAdapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, 0);
        btListDevices = new ArrayList<>();
        setListAdapter(btListAdapter);
        broadcastReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                String action = intent.getAction();
                /* When discovery finds a device */
                if (BluetoothDevice.ACTION_FOUND.equals(action)) {
                    /* Get the BluetoothDevice object from the device */
                    BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);

                    /* Create an array of the bluetooth devices */
                    btListDevices.add(device);

                    /* Add the device to the List */
                    if (device.getBondState() == BluetoothDevice.BOND_BONDED) {
                        btListAdapter.add(device.getName() + " (Paired)\n" + device.getAddress());

                        if (mAutoConnect && device.getAddress().equals(mBTAddress))
                            connect(device);
                    } else
                        btListAdapter.add(device.getName() + "\n" + device.getAddress());
                } else if (BluetoothAdapter.ACTION_DISCOVERY_STARTED.equals(action)) {
                    invalidateOptionsMenu();
                } else if (BluetoothAdapter.ACTION_DISCOVERY_FINISHED.equals(action)) {
                    invalidateOptionsMenu();
                } else if (BluetoothAdapter.ACTION_STATE_CHANGED.equals(action)) {
                    /* State of bluetooth changed */
                    if (mBluetoothAdapter.getState() == BluetoothAdapter.STATE_OFF) {
                        /* Request to enable Bluetooth if switched off during operation */
                        turnOnBT();
                    }
                }
            }
        };
        registerReceivers();
    }

    private void registerReceivers() {
        intentFilter = new IntentFilter(BluetoothDevice.ACTION_FOUND);
        registerReceiver(broadcastReceiver, intentFilter);
        intentFilter = new IntentFilter(BluetoothAdapter.ACTION_DISCOVERY_STARTED);
        registerReceiver(broadcastReceiver, intentFilter);
        intentFilter = new IntentFilter(BluetoothAdapter.ACTION_DISCOVERY_FINISHED);
        registerReceiver(broadcastReceiver, intentFilter);
        intentFilter = new IntentFilter(BluetoothAdapter.ACTION_STATE_CHANGED);
        registerReceiver(broadcastReceiver, intentFilter);
    }

    private void turnOnBT() {
        /* Request to turn on bluetooth */
        Intent enableBTIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
        startActivityForResult(enableBTIntent, 1);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == RESULT_OK) {
            /* User pressed OK*/
            Toast.makeText(getApplicationContext(), "Bluetooth enabled", Toast.LENGTH_SHORT).show();
        }
        if (resultCode == RESULT_CANCELED) {
            /* User declined the request */
            Toast.makeText(getApplicationContext(), "Bluetooth required to continue", Toast.LENGTH_SHORT).show();
            this.finish();
        }
    }

    private void startDiscovery() {
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

        if (selectedBTDevice.getBondState() == BluetoothDevice.BOND_BONDED) {
            mBTAddress = selectedBTDevice.getAddress();
            SharedPreferences settings = getSharedPreferences(PREFS_NAME, 0);
            SharedPreferences.Editor editor = settings.edit();
            editor.putString("btAddress", mBTAddress);
            editor.apply();

            connect(selectedBTDevice);
        } else {
            Intent enableBTIntent = new Intent(Settings.ACTION_BLUETOOTH_SETTINGS);
            startActivityForResult(enableBTIntent, -1);
            Toast.makeText(getApplicationContext(), "Please pair: " + selectedBTDevice.getName(), Toast.LENGTH_LONG).show();
        }
    }

    private final Object mConnectObject = new Object();

    private void connect(final BluetoothDevice device) {
        final ProgressDialog progressDialog = ProgressDialog.show(mContext, "Connecting.", "Please wait...", true, false);

        Log.i("BTDebug", "Try connect");
        Communication.getInstance().connect(device, mConnectObject);


        new Thread(new Runnable() {
            @Override
            public void run() {
                synchronized (mConnectObject) {
                    try {
                        mConnectObject.wait(15000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }

                    if (!Communication.getInstance().isConnected()) {
                        Log.i("BTDebug", "Niege konnekteer");
//                        Toast.makeText(getApplicationContext(), "Unable to connect", Toast.LENGTH_SHORT).show();
                        progressDialog.dismiss();
                    } else {
                        final Intent keyIntent = new Intent(getApplicationContext(), MainActivity.class);
                        keyIntent.putExtra("btDevice", device);
                        Log.i("BTDebug", "Starting new Activity..");
                        progressDialog.dismiss();
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                startActivity(keyIntent);
                            }
                        });
                    }
                }
            }
        }).start();
    }
}
