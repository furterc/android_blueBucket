package com.christo.bluetoothplayground;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicBoolean;

class BluetoothThread {
    private static final String TAG = "BluetoothThread";

    private Handler mHandler;      // Handler for messages in the main thread

    private ConnectThread mConnectThread;
    private ConnectedThread mConnectedThread;

    private BluetoothAdapter mBluetoothAdapter;

    private AtomicBoolean btConnected = new AtomicBoolean(false);
    private int stepSize = 100;

    // Constructor - make a new datagram mBluetoothSocket
    BluetoothThread(Handler handler) {
//        mContext = currentContext;
        mHandler = handler;
        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        Log.i(TAG, "BT + 1");
    }

    void connect(BluetoothDevice mBTDevice) {
        /* Cancel any thread that is currently attempting to make a connection */
        if (mConnectThread != null) {
            mConnectThread.cancel();
            mConnectThread = null;
        }

        /* Cancel any thread that is currently running a connection */
        if (mConnectedThread != null) {
            mConnectedThread.cancel();
            mConnectedThread = null;
        }

        /* Start the thread to connect with the given device */
        mConnectThread = new ConnectThread(mBTDevice);
        mConnectThread.start();
    }

    private void connected(BluetoothSocket socket) {
        Log.d(TAG, "connected");

        /* Cancel any thread currently running a connection */
        if (mConnectedThread != null) {
            mConnectedThread.cancel();
            mConnectedThread = null;
        }

        /* Start the thread to manage the connection and perform transmissions */
        mConnectedThread = new ConnectedThread(socket);
        mConnectedThread.start();
        btConnected.set(true);

        Message msg = new Message();
        msg.obj = "connected";
        msg.arg1 = Communication.HANDLER_ARG1_UI;
        mHandler.sendMessage(msg);
    }

    private void disconnect() {
        btConnected.set(false);

         /* Cancel any thread that is currently attempting to make a connection */
        if (mConnectThread != null) {
            mConnectThread.cancel();
            mConnectThread = null;
        }

        /* Cancel any thread that is currently running a connection */
        if (mConnectedThread != null) {
            mConnectedThread.cancel();
            mConnectedThread = null;
        }
        Message msg = new Message();
        msg.obj = "disconnect";
        msg.arg1 = Communication.HANDLER_ARG1_UI;
        mHandler.sendMessage(msg);

        Log.i(TAG, "socket disconnected");
    }

    public void sendPacket(final byte[] bytes) {
        Log.i(TAG, "SENDing Shit");

        if (btConnected.get()) {
            Thread thread = new Thread(new Runnable() {
                public void run() {
                    try {



                        for (int i = 0; i < (bytes.length / stepSize) + 1; i++) {
                            if ((i + 1) * stepSize < bytes.length) {
                                mConnectedThread.write(bytes, i * stepSize, stepSize);
                                mConnectedThread.flush();
                                try {
                                    Log.i(TAG, "send i = " + i);
                                    Thread.currentThread();
                                    Thread.sleep(100);
                                } catch (InterruptedException e) {
                                    Log.e(TAG, "Interrupted Exception", e);
//                                    e.printStackTrace();
                                }
                            } else {
                                Log.i(TAG, i * stepSize + " : " + (bytes.length - (i) * stepSize));
                                mConnectedThread.write(bytes, i * stepSize, bytes.length - (i) * stepSize);
                                mConnectedThread.flush();
                            }
                        }
                        mConnectedThread.flush();
                        String bla = "";

                        for (byte sbyte : bytes) {
                            bla = bla + " " + String.format("%02X", sbyte);
                        }
                        Log.i("BBluetooth Send", "BT size:" + bytes.length + " + " + bla);
                        Log.i("MSG_ERR", "BT size:" + bytes.length + " + ");
                    } catch (Exception e) {
                        Log.i("Error SEND", e.getMessage());
                    }

                }
            });
            thread.setPriority(Thread.MAX_PRIORITY);
            thread.start();
        }
    }


    //////////////////////////////////////////////////////////////////////////////////////////////////////
    private class ConnectThread extends Thread {
        private final BluetoothSocket mmSocket;


        ConnectThread(BluetoothDevice device) {
            BluetoothSocket tmp = null;

            try {
                tmp = device.createRfcommSocketToServiceRecord(UUID.fromString("00001101-0000-1000-8000-00805f9b34fb"));
            } catch (IOException e) {
                Log.e(TAG, "Socket create() failed", e);
            }
            mmSocket = tmp;
        }

        public void run() {
            Log.i(TAG, "BEGIN mConnectThread");
            setName("ConnectThread");

            /* Always cancel discovery, it will slow down a connection */
            mBluetoothAdapter.cancelDiscovery();

            /* Make a connection to the BluetoothSocket */
            try {
                //This is a blocking call and will only return on a successful connection or an exception
                mmSocket.connect();
            } catch (IOException e) {
                Log.e(TAG, "disconnect", e);
                try {
                    mmSocket.close();
                } catch (IOException e2) {
                    Log.e(TAG, "unable to close() socket during connection failure", e2);
                }
                Message msg = new Message();
                msg.obj = "failed";
                msg.arg1 = Communication.HANDLER_ARG1_UI;
                mHandler.sendMessage(msg);
                return;
            }
            connected(mmSocket);
        }

        void cancel() {
            try {
                mmSocket.close();
            } catch (IOException e) {
                Log.e(TAG, "close of connect(), socket failed", e);
            }
        }
    }

    ///////////////////////////////////////////////////////////////////////////////////////////////////////
    private class ConnectedThread extends Thread {
        private final BluetoothSocket mmSocket;
        private final InputStream mmInStream;
        private final OutputStream mmOutStream;

        ConnectedThread(BluetoothSocket socket) {
            Log.d(TAG, "create ConnectedThread");
            mmSocket = socket;
            InputStream tmpIn = null;
            OutputStream tmpOut = null;

            /* Get the BluetoothSocket input and output streams */
            try {
                tmpIn = socket.getInputStream();
                tmpOut = socket.getOutputStream();
                Log.i(TAG, "streams were created");
            } catch (IOException e) {
                Log.e(TAG, "temp sockets not created", e);
            }

            mmInStream = tmpIn;
            mmOutStream = tmpOut;
        }

        public void run() {
            Log.i(TAG, "BEGIN mConnectedThread");

            ArrayList<Byte> byteList = new ArrayList<>();

            byte[] buffer = new byte[1024];

            while (btConnected.get()) {
                try {
                    /* Read the InputStream into buffer */

                    final int read = mmInStream.read(buffer, 0, 1);


                    Log.i("HI", "data: " + String.format("0x%02X", buffer[0]));

                    if (buffer[0] != 0x0D) {
                        byteList.add(buffer[0]);
                    } else {
                        byte[] outBytes = new byte[byteList.size()];
                        for (int tel = 0; tel < byteList.size(); tel++)
                            outBytes[tel] = byteList.get(tel);

                        byteList.clear();
                        Message msg = new Message();
                        msg.obj = outBytes;
                        msg.arg1 = 0;
                        mHandler.sendMessage(msg);

                        Log.i("BBluetooth Receive", "BT + " + new String(outBytes));
                    }
                } catch (IOException e) {
                    Log.e(TAG, "disconnected", e);
                    disconnect();
                    break;
                }
            }
        }

        /**
         * write to the connected OutStream
         */
//        public void write(byte[] buffer)
//        {
//            try
//            {
//                mmOutStream.write(buffer);
//            } catch (IOException e)
//            {
//                Log.e(TAG, "Exception during write", e);
//            }
//        }
        void write(byte[] buffer, int off, int len) {
            try {
                mmOutStream.write(buffer, off, len);
            } catch (IOException e) {
                Log.e(TAG, "Exception during write", e);
            }
        }
        void flush() {
            try {
                mmOutStream.flush();
            } catch (IOException e) {
                Log.e(TAG, "Exception during flush", e);
            }
        }

        private void cancel() {
            try {
                mmSocket.close();
            } catch (IOException e) {
                Log.e(TAG, "close() of connection failed", e);
            }
        }
    }


}

