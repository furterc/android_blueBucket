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
import java.util.LinkedList;
import java.util.List;
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
        msg.arg1 = Communication.HANDLER_ARG1_CONNECT;
        mHandler.sendMessage(msg);
    }

    void disconnect() {
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
        msg.arg1 = Communication.HANDLER_ARG1_CONNECT;
        mHandler.sendMessage(msg);

        Log.i(TAG, "socket disconnected");
    }

    void sendPacket(final byte[] bytes) {
        if (btConnected.get()) {
            new Thread(new Runnable() {
                public void run() {
                    try {
                        mConnectedThread.flush();
                        mConnectedThread.write(bytes);
                        mConnectedThread.flush();

                        Log.v(TAG, "bt_send size: " + bytes.length + " data: " + Utilities.byteArrayToHex(bytes));
                    } catch (Exception e) {
                        Log.e(TAG, "bt_send_error: ", e);
                    }
                }
            }).start();
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
                msg.arg1 = Communication.HANDLER_ARG1_CONNECT;
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

        private final Object mmSendObj = new Object();
        final List<byte[]> mmSendList = new LinkedList<>();

        ConnectedThread(BluetoothSocket socket) {
            Log.d(TAG, "create ConnectedThread");
            mmSocket = socket;
            InputStream tmpIn = null;
            OutputStream tmpOut = null;


            Thread sendShit = new Thread(new Runnable() {
                @Override
                public void run() {
                    while(true)
                    {
                        synchronized (mmSendObj) {
                            try {
                                mmSendObj.wait();
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }

                            while (!mmSendList.isEmpty())
                            {
                                byte buffer[] = mmSendList.remove(0);
                                try {
                                    mmOutStream.write(buffer, 0, buffer.length);
                                } catch (IOException e) {
                                    e.printStackTrace();
                                }
                                try {
                                    Thread.sleep(120);
                                } catch (InterruptedException e) {
                                    e.printStackTrace();
                                }
                            }
                        }
                    }

                }
            });

            sendShit.start();

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
            Log.i(TAG, "mConnectedThread run()");

            ArrayList<Byte> byteList = new ArrayList<>();

            byte[] buffer = new byte[1024];

            while (btConnected.get()) {
                try {
                    /* Read the InputStream into buffer */
                    final int read = mmInStream.read(buffer, 0, 1);
//                    Log.i(TAG, "bt_data_in: " + String.format("0x%02X", buffer[0]));
                    if (buffer[0] != 0x0D) {
                        byteList.add(buffer[0]);
                    } else {
                        byte[] outBytes = new byte[byteList.size()];

                        for (int tel = 0; tel < byteList.size(); tel++)
                            outBytes[tel] = byteList.get(tel);

                        byteList.clear();
                        Message msg = new Message();
                        msg.obj = outBytes;
                        msg.arg1 = Communication.HANDLER_ARG1_TERM;
                        mHandler.sendMessage(msg);

                        Log.i(TAG, "bt_valid_data: " + new String(outBytes) + " hex: " + Utilities.byteArrayToHex(outBytes));
                    }
                } catch (IOException e) {
                    Log.e(TAG, "disconnected", e);
                    disconnect();
                    break;
                }
            }
        }

        void write(byte[] buffer) {
                mmSendList.add(buffer);
            synchronized (mmSendObj)
            {
                mmSendObj.notify();
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

