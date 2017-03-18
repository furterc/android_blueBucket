package com.christo.bluetoothplayground;

import android.util.Log;

class BlueCommands {

    private final static int CRC_8_CONSTANT = 0xE5;

    BlueCommands() {
        byte[] mBytes = {0x01, 0x22, 0x01};
        Log.i("Yay", String.format("crc: 0x%02X", crc8(mBytes)));
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

    Packet receive(byte[] bytes) {
        byte[] data = new byte[3];

        for (int i = 0; i < 3; i++) {
            data[i] = bytes[i];
            Log.i("DATA", String.format("0x%02X", data[i]));
        }

        byte crc = (byte) crc8(data);
        Log.i("CRC", String.format("0x%02X", crc));
        Log.i("byte3", String.format("0x%02X", bytes[3]));
        if (crc == bytes[3]) {
            Log.i("LOG", "valid frame!");
            Packet mPacket = new Packet();
//            mPacket.setType(bytes[0]);
//            mPacket.setTag(bytes[1]);
            mPacket.setData(bytes[2]);
            mPacket.dbgPrint();
            return mPacket;
        } else
            Log.e("LOG", "invalid frame!");

        return null;
    }


}
