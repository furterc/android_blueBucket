package com.christo.bluetoothplayground;

public class Utilities {
    public static String byteArrayToHex(byte[] a) {
        StringBuilder sb = new StringBuilder(a.length * 2);
        for (byte b : a)
            sb.append(String.format("0x%02X, ", b));
        return sb.toString();
    }

    static int fromByte(byte bytes) {
        return bytes & 0xFF;
    }

    static int fromByteArray(byte[] bytes) {

//        int len = bytes.length;
        if (bytes.length == 1) return bytes[0] & 0xFF;

        if (bytes.length == 4)
            return bytes[0] << 24 | (bytes[1] & 0xFF) << 16 | (bytes[2] & 0xFF) << 8 | (bytes[3] & 0xFF);

        return 0;
    }

}