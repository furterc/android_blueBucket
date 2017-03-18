package com.christo.bluetoothplayground;

import android.util.Log;

class Packet {
    final static byte TYPE_SET = 0;
    final static byte TYPE_GET = 1;

    final static byte BT_HOURS = 0;
    final static byte BT_MINUTES = 1;
    final static byte BT_SECONDS = 2;

    private byte mType;
    private byte mTag;
    private byte mData;

    void setType(byte mmType) {
        this.mType = mmType;
    }

    public byte getType() {
        return mType;
    }

    void setTag(byte mTag) {
        this.mTag = mTag;
    }

    public byte getTag() {
        return mTag;
    }

    public void setData(byte mmData) {
        this.mData = mmData;
    }

    public byte getData() {
        return mData;
    }

    void dbgPrint()
    {
        Log.i("Packet", String.format("type:\t0x%02X\ntag:\t0x%02X\ndata:\t0x%02X\n", mType, mTag, mData));
    }
}
