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
    static final int HANDLER_BT_MSG = 0;
    static final int HANDLER_ARG1_CONNECT = 1;

    private boolean mConnected = false;
    private BluetoothThread mBluetoothThread;
    private final Object mWaitObject = new Object();
    private Handler mMainHandler;
    private Framer framer = new Framer();
    private byte[] mData;

    private Communication() {
        mBluetoothThread = new BluetoothThread(mHandler);
    }

    /* Handler for the Bluetooth thread */
    private Handler mHandler = new Handler(new Handler.Callback() {
        @Override
        public boolean handleMessage(Message msg) {

            /* This checks that the device connected successfully */
            if (msg.arg1 == HANDLER_ARG1_CONNECT) {
                if ("connected".equals(msg.obj))
                {
                    mConnected = true;

                }

                Message mainMessage = new Message();
                mainMessage.arg1 = HANDLER_ARG1_CONNECT;
                mainMessage.obj = msg.obj;
                mMainHandler.sendMessage(mainMessage);
                return false;
            }

            byte data = (byte) msg.obj;
//            Log.i(TAG, String.format("bt_byte: 0x%02X", data));

            if (framer.rxData(data)) {
                //data ready
                mData = framer.getFrame();

                Log.i(TAG, "HDLC data ready - hex: " + Utilities.byteArrayToHex(mData));
                Log.i(TAG, "HDLC data len: " + mData.length);

                synchronized (mWaitObject) {
                    mWaitObject.notify();
                }
            }
            return false;
        }
    });

    void connect(BluetoothDevice bluetoothDevice, Object waitObject) {
        mMainHandler = MainActivity.getHandler();
        mBluetoothThread.connect(bluetoothDevice, waitObject);

    }

    void setConnected(boolean mConnected) {
        this.mConnected = mConnected;
    }

    boolean isConnected() {
        return mConnected;
    }

    void disconnect() {
        mBluetoothThread.disconnect();
        mConnected = false;
    }

    void sendFramedData(byte[] data)
    {
        Framer framer = new Framer();
        mBluetoothThread.sendPacket(framer.frameCreate(data));
    }

    int requestPacket(final TAG tag, final int data0) {
        //blocking call

        //try 10 times
        for (int i = 0; i < 10; i++) {
            cMsg requestMessage = new cMsg(TYPE.TYPE_GET, tag, (byte)data0, (byte)0);
//            Packet requestPacket = new Packet(Packet.TYPE.TYPE_GET, tag, (byte) 0x00);
            Communication.getInstance().sendFramedData(requestMessage.toBytes());

            synchronized (mWaitObject) {
                try {
                    mWaitObject.wait(1000);
                } catch (InterruptedException e) {
                    Log.e(TAG, "request packet: " + e);
                }
            }

            if (mData == null)
                continue;

            cMsg cmsgOut = new cMsg(mData);
            return Utilities.fromByte(cmsgOut.getData1());
        }
        //failed 10 times
        Log.w(TAG, "request failed 10 times...");
        return -1;
    }

    void sendPacket(Packet packet) {
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