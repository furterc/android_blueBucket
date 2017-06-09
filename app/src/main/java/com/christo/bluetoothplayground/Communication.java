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
    private Packet mPacket;
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
            }


//            Log.i(TAG, "packet received.");
//            Packet packet = new Packet();
//            packet = packet.fromBytes((byte[]) msg.obj);
//
//            if (packet == null) {
//                Log.e(TAG, "packet invalid CRC.");
//                return false;
//            }

//            packet.dbgPrint();
//            mPacket = packet;
            synchronized (mWaitObject) {
                mWaitObject.notify();
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

    void framerTry()
    {
//        Packet requestPacket = new Packet(Packet.TYPE.TYPE_GET, Packet.TAG.BT_HOURS, (byte) 0x00);
//        Communication.getInstance().sendPacket(requestPacket);
        Framer framer = new Framer();
        byte[] bytes = {0x07,0x07,0x07,0x07};

        mBluetoothThread.sendPacket(framer.frameCreate(bytes));
    }


    int requestPacket(final Packet.TAG tag) {
        //blocking call

        //try 10 times
        for (int i = 0; i < 10; i++) {
            Packet requestPacket = new Packet(Packet.TYPE.TYPE_GET, tag, (byte) 0x00);
            Communication.getInstance().sendPacket(requestPacket);

            synchronized (mWaitObject) {
                try {
                    mWaitObject.wait(1000);
                } catch (InterruptedException e) {
                    Log.e(TAG, "request packet: " + e);
                }
            }

            if (mPacket != null && mPacket.getType() == Packet.TYPE.TYPE_SET && mPacket.getTag() == tag)
                return Utilities.fromByte(mPacket.getData());

            Log.w(TAG, "request failed, trying again..");
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