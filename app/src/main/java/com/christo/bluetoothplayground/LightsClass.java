package com.christo.bluetoothplayground;


class LightsClass {
    private String mName;
    private int mDuty;

    void update(int light, int val) {
        mDuty = val;
        Packet packet = new Packet();
        packet.setType(Packet.TYPE.TYPE_SET);
        packet.setData((byte) val);
        switch (light) {
            case 0:
                packet.setTag(Packet.TAG.BT_KITCH_TOP);
                break;
            case 1:
                packet.setTag(Packet.TAG.BT_KITCH_BOT);
                break;
            case 2:
                packet.setTag(Packet.TAG.BT_STUDY_TOP);
                break;
            case 3:
                packet.setTag(Packet.TAG.BT_STUDY_BOT);
                break;
            default:
                break;
        }

        Communication.getInstance().sendPacket(packet);
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
