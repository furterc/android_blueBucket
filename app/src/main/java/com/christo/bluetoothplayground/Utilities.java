package com.christo.bluetoothplayground;

public class Utilities {
    public static String byteArrayToHex(byte[] a) {
        StringBuilder sb = new StringBuilder(a.length * 2);
        for (byte b : a)
            sb.append(String.format("0x%02X, ", b));
        return sb.toString();
    }

}