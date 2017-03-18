package com.christo.bluetoothplayground;

import android.bluetooth.BluetoothDevice;
import android.os.Handler;
import android.os.Message;


class Communication {

    private static Communication instance = new Communication();

    static Communication getInstance() {
        return instance;
    }

    private static final String TAG = Communication.class.getSimpleName();
    private Handler mMainHandler;

    private BluetoothThread mBluetoothThread;


    static final int HANDLER_ARG1_UI = 1;
    static final int HANDLER_ARG1_TERM = 2;

    private int mCurrArg1 = 0;
    private Handler mCurrHandler;

    private Communication() {
        mBluetoothThread = new BluetoothThread(mHandler);
    }

    /* Handler for the Bluetooth thread */
    private Handler mHandler = new Handler(new Handler.Callback() {
        @Override
        public boolean handleMessage(Message msg) {
            /* This checks that the device connected successfully */
            if (msg.arg1 == HANDLER_ARG1_UI) {
                Message mainMessage = new Message();
                mainMessage.arg1 = HANDLER_ARG1_UI;
                mainMessage.obj = msg.obj;
                mMainHandler.sendMessage(mainMessage);
                return false;
            }

            byte[] data = (byte[]) msg.obj;

            if (data.length > 0) {

                if (mCurrArg1 == 0)
                    return false;

                switch (mCurrArg1) {
                    case HANDLER_ARG1_TERM:
                        Message termMessage = new Message();
                        termMessage.obj = new String(data);
                        termMessage.arg1 = HANDLER_ARG1_TERM;
                        mCurrHandler.sendMessage(termMessage);
                        break;

                }
            }
                return false;
        }
    });


//                Packet packet = blue.receive(data);
//                if (packet != null) {
//                    switch (packet.getType()) {
//                        case Packet.TYPE_GET:
//                            Log.i(TAG, String.format("PACKET GET\ntag: 0x%02X\ndata: 0x%02X", packet.getTag(), packet.getData()));
//                            break;
//
//                            case Packet.TAG_SET:
//                                Log.i(TAG, String.format("PACKET S\ntag: 0x%02X\ndata: 0x%02X", packet.getTag(), packet.getData()));
//                                break;


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

    void write(final byte[] bytes) {
        mBluetoothThread.sendPacket(bytes);
    }
}