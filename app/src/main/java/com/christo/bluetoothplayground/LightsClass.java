package com.christo.bluetoothplayground;


class LightsClass {
    private String mName;
    private int mDuty;

    void update(int light, int val) {
        mDuty = val;
        cMsg msg = new cMsg();
        msg.setType(TYPE.TYPE_SET);
        msg.setData1((byte) val);
        switch (light) {
            case 0:
                msg.setTag(TAG.TAG_LED_KITCHEN);
                msg.setData0((byte)0);
                break;
            case 1:
                msg.setTag(TAG.TAG_LED_KITCHEN);
                msg.setData0((byte)1);
                break;
            case 2:
                msg.setTag(TAG.TAG_LED_STUDY);
                msg.setData0((byte)0);
                break;
            case 3:
                msg.setTag(TAG.TAG_LED_STUDY);
                msg.setData0((byte)1);
                break;
            case 4:
                msg.setTag(TAG.TAG_LED_BED);
                msg.setData0((byte)0);
                break;
            case 5:
                msg.setTag(TAG.TAG_LED_BED);
                msg.setData0((byte)1);
                break;
            case 6:
                msg.setTag(TAG.TAG_LED_BED);
                msg.setData0((byte)2);
                break;
            case 7:
                msg.setTag(TAG.TAG_LED_BED);
                msg.setData0((byte)3);
                break;
            default:
                break;
        }

        Communication.getInstance().sendFramedData(msg.toBytes());
    }

    LightsClass(String name) {
        mName = name;
        mDuty = 0;
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
