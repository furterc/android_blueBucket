package com.christo.bluetoothplayground;

import android.app.ProgressDialog;
import android.bluetooth.BluetoothDevice;
import android.content.res.Configuration;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;


public class MainActivity extends AppCompatActivity {

    static final String TAG = MainActivity.class.getSimpleName();

    private DrawerLayout mDrawerLayout;
    private ListView mDrawerList;
    private ActionBarDrawerToggle mDrawerToggle;

    private CharSequence mDrawerTitle;
    private CharSequence mTitle;
    private String[] mNavigationEntries;

    public static FragmentManager mFragmentManager;
    private ProgressDialog loadingDialog = null;



//    BlueCommands blue = new BlueCommands();

    /* Handler for the Bluetooth thread */
    private final Handler mBluetoothHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            /* This checks that the device connected successfully */
            if (msg.arg1 == Communication.HANDLER_ARG1_CONNECT) {
//                final String msgObj = new String((byte[])msg.obj);
                final String msgObj = (String) msg.obj;
                if (msg.obj == null)
                    return;

                switch (msg.arg1) {
                    case Communication.HANDLER_ARG1_CONNECT:
                        final String msgString = (String) msg.obj;
                        if ("connected".equals(msgString)) {
                            if (loadingDialog != null && loadingDialog.isShowing())
                                loadingDialog.dismiss();
                        } else if ("disconnect".equals(msgString)) {
                            if (loadingDialog != null && loadingDialog.isShowing())
                                loadingDialog.dismiss();
                            Log.e(TAG, "disconnected!");
                            Toast.makeText(getApplicationContext(), "Unable to connect to device", Toast.LENGTH_SHORT).show();
                            finish();
                        } else if ("failed".equals(msgString)) {
                            if (loadingDialog != null && loadingDialog.isShowing())
                                loadingDialog.dismiss();
                            Log.e(TAG, "failed!");
                            Toast.makeText(getApplicationContext(), "Device disconnected", Toast.LENGTH_SHORT).show();
                            finish();
                        }

                        break;

                }


            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        loadingDialog = ProgressDialog.show(this, "Connected", "Please wait...", true, false);

        Communication.getInstance().setMainHandler(mBluetoothHandler);

        final BluetoothDevice mBluetoothDevice = getIntent().getExtras().getParcelable("btDevice");
        Communication.getInstance().connect(mBluetoothDevice);

        mTitle = mDrawerTitle = getTitle();
        mNavigationEntries = getResources().getStringArray(R.array.navigation_drawer_array);
        mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        mDrawerList = (ListView) findViewById(R.id.left_drawer);

        /* Set custom shadow to that overlays the main content when the drawer opens */
        mDrawerLayout.setDrawerShadow(R.drawable.drawer_shadow, GravityCompat.START);

        /* Set p the drawer's list view with items and on click Listener */
        mDrawerList.setAdapter(new ArrayAdapter<>(this, R.layout.drawer_list_item, mNavigationEntries));
        mDrawerList.setOnItemClickListener(new DrawerItemClickListener());

        /* Enable ActionBar application icon to behave as action to toggle nav drawer */
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setHomeButtonEnabled(true);
        }

        mDrawerToggle = new ActionBarDrawerToggle(
                this,                               /* host activity */
                mDrawerLayout,                      /* DrawerLayout object */
                null,                               /* nav drawer image to replace the 'Up' caret */
                R.string.navigation_drawer_open,    /* "open drawer" description for accessibility */
                R.string.navigation_drawer_close    /* "close drawer" description for accessibility */
        ) {
            @Override
            public void onDrawerClosed(View drawerView) {
                getSupportActionBar().setTitle(mTitle);
                supportInvalidateOptionsMenu();
                invalidateOptionsMenu();
            }

            @Override
            public void onDrawerOpened(View drawerView) {
                getSupportActionBar().setTitle(mDrawerTitle);
                supportInvalidateOptionsMenu();
            }
        };
        mDrawerLayout.addDrawerListener(mDrawerToggle);

        if (savedInstanceState == null)
            selectItem(0);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    private class DrawerItemClickListener implements AdapterView.OnItemClickListener {
        @Override
        public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
            selectItem(i);
        }

    }

    private void selectItem(int position) {
        /* Update the main content by replacing fragments */
        Fragment fragment;
        switch (position) {
            case 0:
                fragment = new TerminalFragment();
                break;
            case 1:
                fragment = new SettingsFragment();
                break;
            case 2:
                fragment = new LightsFragment();
                break;
            default:
                fragment = new TerminalFragment();
                break;
        }

        mFragmentManager = getSupportFragmentManager();
        mFragmentManager.beginTransaction().replace(R.id.content_frame, fragment).commit();

        /* Update the selected item and title, then close the drawer */
        mDrawerList.setItemChecked(position, true);
        setTitle(mNavigationEntries[position]);
        mDrawerLayout.closeDrawer(mDrawerList);
    }

    @Override
    public void setTitle(CharSequence title) {
        mTitle = title;

        if (getSupportActionBar() != null)
            getSupportActionBar().setTitle(mTitle);

        super.setTitle(title);
    }

    /**
     * When using the ActionBarToggle, you must call it during
     * onPostCreate() and onConfigurationChanged()
     */

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        // Sync the toggle state after onRestoreInstanceState has occurred.
        mDrawerToggle.syncState();
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        // Pass any configuration change to the drawer toggles
        mDrawerToggle.onConfigurationChanged(newConfig);
    }

}
