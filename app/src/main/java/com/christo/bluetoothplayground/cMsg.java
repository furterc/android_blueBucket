package com.christo.bluetoothplayground;

import android.util.Log;

/**
 * Created by christo on 2017/06/09.
 */

public class cMsg {
    static enum TYPE {
        TYPE_GET,
        TYPE_SET
    }

    static enum TAG {
        TAG_TIME,
        TAG_ALARM,
        TAG_LED_KITCHEN,
        TAG_LED_STUDY
    }

    private byte mType;
    private byte mTag;
    private byte mData0;
    private byte mData1;

    cMsg(byte[] data)
    {
        mType = data[0];
        mTag = data[1];
        mData0 = data[2];
        mData1 = data[3];
    }

    cMsg(cMsg.TYPE type, cMsg.TAG tag, byte data0, byte data1)
    {
        mType = (byte)type.ordinal();
        mTag = (byte)tag.ordinal();
        mData0 = data0;
        mData1 = data1;
    }

    cMsg(cMsg.TYPE type, cMsg.TAG tag, int data0, int data1)
    {
        mType = (byte)type.ordinal();
        mTag = (byte)tag.ordinal();
        mData0 = (byte)data0;
        mData1 = (byte)data1;
    }

    cMsg()
    {
        mType = 0;
        mTag = 0;
        mData0 = 0;
        mData1 = 0;
    }

    void setType(cMsg.TYPE type) {
        this.mType = (byte) type.ordinal();
    }

    public cMsg.TYPE getType() {
        return cMsg.TYPE.values()[(int) mType];
    }

    void setTag(cMsg.TAG mTag) {
        this.mTag = (byte) mTag.ordinal();
    }

    public byte getTagNum() {
        return mTag;
    }

    public cMsg.TAG getTag()
    {
        return cMsg.TAG.values()[(int) mTag];
    }

    public void setData(byte data0, byte data1) {
        this.mData0 = data0;
        this.mData1 = data1;
    }

    public void setData0(byte data0) {
        this.mData0 = data0;
    }

    public void setData1(byte data1) {
        this.mData1 = data1;
    }

    public byte getData0() {
        return mData0;
    }

    public byte getData1() {
        return mData1;
    }

    byte[] toBytes()
    {
        return new byte[]{mType, mTag, mData0, mData1};
    }

    cMsg fromBytes(byte[] bytes)
    {
        if (bytes.length != 4)
            return null;

        cMsg cMsg = new cMsg(bytes);
        cMsg.dbgPrint();
        return cMsg;
    }


    void dbgPrint() {
        Log.i("cMsg", String.format("type:\t0x%02X\ntag:\t0x%02X\ndata0:\t0x%02X\ndata1:\t0x%02X", mType, mTag, mData0, mData1));
    }
}
