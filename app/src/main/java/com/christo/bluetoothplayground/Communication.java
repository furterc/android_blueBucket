package com.christo.bluetoothplayground;

import android.bluetooth.BluetoothDevice;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

import java.io.ByteArrayOutputStream;
import java.io.IOException;


class Communication {

    private static Communication instance = new Communication();

    static Communication getInstance() {
        return instance;
    }

    private static final String TAG = Communication.class.getSimpleName();
    private Handler mMainHandler;

    private BluetoothThread mBluetoothThread;


    static final int HANDLER_ARG1_CONNECT = 1;
    static final int HANDLER_ARG1_TERM = 2;
    static final int HANDLER_ARG1_SETTINGS = 3;

    private int mCurrArg1 = 0;
    private Handler mCurrHandler;

    private Communication() {
        mBluetoothThread = new BluetoothThread(mHandler);
    }

    /* Handler for the Bluetooth thread */
    private Handler mHandler = new Handler(new Handler.Callback() {
        @Override
        public boolean handleMessage(Message msg) {

            Log.i(TAG, "mCurrArg1: " + mCurrArg1);


            /* This checks that the device connected successfully */
            if (msg.arg1 == HANDLER_ARG1_CONNECT) {
                Message mainMessage = new Message();
                mainMessage.arg1 = HANDLER_ARG1_CONNECT;
                mainMessage.obj = msg.obj;
                mMainHandler.sendMessage(mainMessage);
                return false;
            }

            byte[] data = (byte[]) msg.obj;

            if (data.length > 0) {
                switch (mCurrArg1) {
                    case HANDLER_ARG1_TERM:
                        Message termMessage = new Message();
                        termMessage.obj = new String(data);
                        termMessage.arg1 = HANDLER_ARG1_TERM;
                        mCurrHandler.sendMessage(termMessage);
                        break;

                    case HANDLER_ARG1_SETTINGS:
                        Message settingMessage = new Message();
                        settingMessage.obj = data;
                        settingMessage.arg1 = HANDLER_ARG1_SETTINGS;
                        mCurrHandler.sendMessage(settingMessage);
                }
            }
                return false;
        }
    });

    void setMainHandler(Handler mainHandler) {
        this.mMainHandler = mainHandler;
    }

    void setCurrentHandler(Handler currentHandler, int currentArg1) {
        this.mCurrHandler = currentHandler;
        this.mCurrArg1 = currentArg1;
    }

    void connect(BluetoothDevice bluetoothDevice) {
        mBluetoothThread.connect(bluetoothDevice);
    }

    void disconnect()
    {
    mBluetoothThread = null;
    }

    void write(final byte[] bytes) {
        mBluetoothThread.sendPacket(bytes);
    }

    void sendPacket(Packet packet)
    {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        try {
            outputStream.write(packet.toBytes());
            outputStream.write("\n".getBytes());
        } catch (IOException e) {
            e.printStackTrace();
        }
        mBluetoothThread.sendPacket(outputStream.toByteArray());
    }
}