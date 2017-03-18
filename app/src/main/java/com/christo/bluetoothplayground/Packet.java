package com.christo.bluetoothplayground;

import android.util.Log;

class Packet {

    private final static int CRC_8_CONSTANT = 0xE5;

    enum TYPE {
        TYPE_GET,
        TYPE_SET
    }

    enum TAG {
        BT_HOURS,
        BT_MINUTES,
        BT_SECONDS
    }

    private byte mType;
    private byte mTag;
    private byte mData;
    private byte mCrc;

    Packet(TYPE type, TAG tag, byte data)
    {
        mType = (byte)type.ordinal();
        mTag = (byte)tag.ordinal();
        mData = data;
        calcCrc();
    }

    Packet()
    {
        mType = 0;
        mTag = 0;
        mData = 0;
        mCrc = 0;
    }

    void setType(TYPE type) {
        this.mType = (byte) type.ordinal();
        calcCrc();
    }

    public TYPE getType() {
        return TYPE.values()[(int) mType];
    }

    void setTag(TAG mTag) {
        this.mTag = (byte) mTag.ordinal();
        calcCrc();
    }

    public byte getTag() {
        return mTag;
    }

    public void setData(byte mmData) {
        this.mData = mmData;
        calcCrc();
    }

    public byte getData() {
        return mData;
    }

    private void calcCrc() {
        byte[] data = new byte[3];
        data[0] = mType;
        data[1] = mTag;
        data[2] = mData;
        mCrc = (byte) crc8(data);
    }

    private int crc8(byte[] bytes) {
        int crc = 0x00;
        int idx = 0x00;
        while (idx < bytes.length) {
            crc ^= bytes[idx];
            for (int i = 0; i < 8; i++) {
                if ((crc & 0x80) == 0x80)
                    crc = (crc << 1) ^ CRC_8_CONSTANT;
                else
                    crc <<= 1;
            }
            idx++;
        }
        crc &= 0xFF;
        return crc;
    }

    byte[] toBytes()
    {
        return new byte[]{mType, mTag, mData, mCrc};
    }


    void dbgPrint() {
        Log.i("Packet", String.format("type:\t0x%02X\ntag:\t0x%02X\ndata:\t0x%02X\ncrc:\t0x%02X", mType, mTag, mData, mCrc));
    }
}
