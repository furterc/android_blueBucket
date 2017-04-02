package com.christo.bluetoothplayground;

import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.SeekBar;
import android.widget.Toast;

import java.util.Calendar;


public class SettingsFragment extends Fragment {

    private Context mContext;

    private SeekBar seekBar;

    private Handler mHandler = new Handler(new Handler.Callback() {
        @Override
        public boolean handleMessage(Message message) {
            if (message.obj == null)
                return false;

            if (message.arg1 == Communication.HANDLER_ARG1_CONNECT && "disconnect".equals((String) message.obj)) {
                Toast.makeText(mContext, "Device disconnected.", Toast.LENGTH_SHORT).show();
                getActivity().finish();
                return false;
            }

            Log.i("Settings", Utilities.byteArrayToHex((byte[])message.obj));
            Packet packet = new Packet();
            packet = packet.fromBytes((byte[])message.obj);

            if (packet!=null)
            {
                packet.dbgPrint();
                switch (packet.getType())
                {
                    case TYPE_SET:
                        Log.i("Settings", String.format("Data rec: 0x%02X", packet.getData()));
                        seekBar.setProgress(Utilities.fromByte(packet.getData()));
                        break;
                }
            }


            return false;
        }
    });

    public SettingsFragment() {
        // Required empty public constructor
    }

        @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        final View mView = inflater.inflate(R.layout.fragment_settings, container, false);

        mContext = mView.getContext();
        Communication.getInstance().setCurrentHandler(mHandler, Communication.HANDLER_ARG1_SETTINGS);

        seekBar = (SeekBar) mView.findViewById(R.id.seekBar);
        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                Packet packet = new Packet();
                packet.setType(Packet.TYPE.TYPE_SET);
                packet.setTag(Packet.TAG.BT_KITCH_TOP);
                packet.setData((byte)i);
                Communication.getInstance().sendPacket(packet);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });

        final Button buttonSetTime = (Button) mView.findViewById(R.id.button_settings_setTime);
        buttonSetTime.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view)
            {
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        Packet packet = new Packet();
                        packet.setType(Packet.TYPE.TYPE_SET);
                        packet.setTag(Packet.TAG.BT_HOURS);
                        packet.setData((byte)Calendar.getInstance().get(Calendar.HOUR_OF_DAY));
                        packet.dbgPrint();
                        Communication.getInstance().sendPacket(packet);

                        try {
                            Thread.sleep(50);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }

                        packet.setType(Packet.TYPE.TYPE_SET);
                        packet.setTag(Packet.TAG.BT_MINUTES);
                        packet.setData((byte)Calendar.getInstance().get(Calendar.MINUTE));
                        packet.dbgPrint();
                        Communication.getInstance().sendPacket(packet);

                        try {
                            Thread.sleep(50);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }

                        packet.setType(Packet.TYPE.TYPE_SET);
                        packet.setTag(Packet.TAG.BT_SECONDS);
                        packet.setData((byte)Calendar.getInstance().get(Calendar.SECOND));
                        packet.dbgPrint();
                        Communication.getInstance().sendPacket(packet);

                    }
                }).start();


            }
        });

        Packet requestPacket = new Packet(Packet.TYPE.TYPE_GET, Packet.TAG.BT_KITCH_TOP, (byte)0x00);
        Communication.getInstance().sendPacket(requestPacket);

        return mView;
    }

}
