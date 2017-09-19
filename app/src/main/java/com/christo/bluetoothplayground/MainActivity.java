package com.christo.bluetoothplayground;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.content.DialogInterface;
import android.content.res.Configuration;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.os.Message;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;


public class MainActivity extends AppCompatActivity {

    private DrawerLayout mDrawerLayout;
    private ListView mDrawerList;
    private ActionBarDrawerToggle mDrawerToggle;

    private CharSequence mDrawerTitle;
    private CharSequence mTitle;
    private String[] mNavigationEntries;

    public static FragmentManager mFragmentManager;

    private HandlerThread mMainBTHandlerThread = null;
    private static Handler mMainBTHandler;

    private static Handler  mHandler = new Handler(new Handler.Callback() {
        @Override
        public boolean handleMessage(Message message) {
            if (message.arg1 == Communication.HANDLER_ARG1_CONNECT) {

//                if ("connected".equals((String) message.obj)) {
////                    Toast.makeText(, "Connected.", Toast.LENGTH_SHORT).show();
//                    return false;
//                }
//
//                if ("disconnect".equals((String) message.obj)) {
////                    Toast.makeText(mContext, "Device disconnected.", Toast.LENGTH_SHORT).show();
////                    getActivity().finish();
//                    return false;
//                }
 }
            return false;
        }
    });

    public static Handler getMainBTHandler() {
        return mMainBTHandler;
    }

    public static void setHandler(Handler mHandler) {
        MainActivity.mHandler = mHandler;
    }

    public static Handler getHandler() {
        return mHandler;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

//        final BluetoothDevice mBluetoothDevice = getIntent().getExtras().getParcelable("btDevice");

        mMainBTHandlerThread = new HandlerThread("mainBTThread");
        mMainBTHandlerThread.start();
        final Looper looper = mMainBTHandlerThread.getLooper();
        mMainBTHandler = new Handler(looper);

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
    public boolean onOptionsItemSelected(MenuItem item) {
        if (mDrawerToggle.onOptionsItemSelected(item)) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onDestroy() {
        Communication.getInstance().disconnect();

        if (mMainBTHandlerThread != null) {
            Thread stopThread = mMainBTHandlerThread;
            mMainBTHandlerThread = null;
            stopThread.interrupt();
        }

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
                fragment = new HomeFragment();
                break;
            case 1:
                fragment = new SettingsFragment();
                break;
            case 2:
                fragment = new LightsFragment();
                break;
            default:
                fragment = new HomeFragment();
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
    public void onBackPressed() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(R.string.alertDialog_Title);
        builder.setMessage(R.string.alertDialog_msgMainActivity);

        builder.setPositiveButton("YES", new DialogInterface.OnClickListener()
        {
            @Override
            public void onClick(DialogInterface dialogInterface, int i)
            {
                dialogInterface.dismiss();
                MainActivity.super.onBackPressed();
            }
        });

        builder.setNegativeButton("NO", new DialogInterface.OnClickListener()
        {
            @Override
            public void onClick(DialogInterface dialogInterface, int i)
            {
                dialogInterface.dismiss();
            }
        });
        AlertDialog alert = builder.create();
        alert.show();

    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        // Pass any configuration change to the drawer toggles
        mDrawerToggle.onConfigurationChanged(newConfig);
    }
}