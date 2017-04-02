package com.christo.bluetoothplayground;


class LightsClass {
    private String mName;
    private int mDuty;

    LightsClass()
    {
        mName = "";
        mDuty = 0;
    }

    LightsClass(String name)
    {
        mName = name;
        mDuty = 0;
    }

    void setName(String mName) {
        this.mName = mName;
    }

    String getName() {
        return mName;
    }

    void setDuty(int mDuty) {
        this.mDuty = mDuty;
    }

    int getDuty() {
        return mDuty;
    }
}
