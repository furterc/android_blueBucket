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
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import java.util.Calendar;

import static com.christo.bluetoothplayground.Packet.TAG.BT_HOURS;


public class SettingsFragment extends Fragment {

    private Context mContext;
    private View mView;

    private TextView textViewTime;
    private TimePicker mTimePicker;

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

            Log.i("Settings", Utilities.byteArrayToHex((byte[]) message.obj));
            Packet packet = new Packet();
            packet = packet.fromBytes((byte[]) message.obj);

            if (packet != null) {
                packet.dbgPrint();
                if (packet.getType() == Packet.TYPE.TYPE_SET)
                {
                    switch (packet.getTag()) {
                        case BT_HOURS:
                            updateTime(packet.getTag(), (int) packet.getData());
                            break;
                        case BT_MINUTES:
                            updateTime(packet.getTag(), (int) packet.getData());
                            break;
                        case BT_SECONDS:
                            updateTime(packet.getTag(), (int) packet.getData());
                            break;
                        case BT_ALARM_HOUR:
                            mTimePicker.setCurrentHour((int) packet.getData());
                            break;
                        case BT_ALARM_MINUTE:
                            mTimePicker.setCurrentMinute((int) packet.getData());
                            break;
                    }
                }

            }


            return false;
        }
    });

    private void updateTime(Packet.TAG tag, int value)
    {
        CharSequence charSequence = textViewTime.getText();
        String strings[] = charSequence.toString().split(":");

        if (strings.length != 3)
            strings = new String[3];

        switch (tag)
        {
            case BT_HOURS:
                strings[0] = String.valueOf(value);
                break;
            case BT_MINUTES:
                strings[1] = String.valueOf(value);
                break;
            case BT_SECONDS:
                strings[2] = String.valueOf(value);
                break;
        }
        textViewTime.setText(strings[0] + ":" + strings[1] + ":" + strings[2]);
    }

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
        mView = inflater.inflate(R.layout.fragment_settings, container, false);

        setHasOptionsMenu(true);
        mContext = mView.getContext();
        Communication.getInstance().setCurrentHandler(mHandler, Communication.HANDLER_ARG1_SETTINGS);

        textViewTime = (TextView) mView.findViewById(R.id.textView_settings_time);

        mTimePicker = (TimePicker) mView.findViewById(R.id.timePicker_settings_alarm);
        mTimePicker.setIs24HourView(true);

        final Button buttonGetTime = (Button) mView.findViewById(R.id.button_settings_getTime);
        buttonGetTime.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Packet requestPacket = new Packet(Packet.TYPE.TYPE_GET, Packet.TAG.BT_HOURS, (byte) 0x00);
                Communication.getInstance().sendPacket(requestPacket);
                requestPacket.setTag(Packet.TAG.BT_MINUTES);
                Communication.getInstance().sendPacket(requestPacket);
                requestPacket.setTag(Packet.TAG.BT_SECONDS);
                Communication.getInstance().sendPacket(requestPacket);
            }
        });

        final Button buttonSetTime = (Button) mView.findViewById(R.id.button_settings_setTime);
        buttonSetTime.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        Packet packet = new Packet();
                        packet.setType(Packet.TYPE.TYPE_SET);

                        packet.setTag(BT_HOURS);
                        packet.setData((byte) Calendar.getInstance().get(Calendar.HOUR_OF_DAY));
                        Communication.getInstance().sendPacket(packet);

                        packet.setTag(Packet.TAG.BT_MINUTES);
                        packet.setData((byte) Calendar.getInstance().get(Calendar.MINUTE));
                        Communication.getInstance().sendPacket(packet);

                        packet.setTag(Packet.TAG.BT_SECONDS);
                        packet.setData((byte) Calendar.getInstance().get(Calendar.SECOND));
                        Communication.getInstance().sendPacket(packet);
                    }
                }).start();
            }
        });


        final Button btnGetAlarm = (Button) mView.findViewById(R.id.button_settings_getAlarm);
        btnGetAlarm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Packet requestPacket = new Packet(Packet.TYPE.TYPE_GET, Packet.TAG.BT_ALARM_HOUR, (byte) 0x00);
                Communication.getInstance().sendPacket(requestPacket);
                requestPacket.setTag(Packet.TAG.BT_ALARM_MINUTE);
                Communication.getInstance().sendPacket(requestPacket);
            }
        });

        final Button btnSetAlarm = (Button) mView.findViewById(R.id.button_settings_setAlarm);
        btnSetAlarm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Packet p = new Packet(Packet.TYPE.TYPE_SET, Packet.TAG.BT_ALARM_HOUR, Integer.valueOf(mTimePicker.getCurrentHour().toString()));
                Communication.getInstance().sendPacket(p);

                p = new Packet(Packet.TYPE.TYPE_SET, Packet.TAG.BT_ALARM_MINUTE, Integer.valueOf(mTimePicker.getCurrentMinute().toString()));
                Communication.getInstance().sendPacket(p);
            }
        });

        
        Packet requestPacket = new Packet(Packet.TYPE.TYPE_GET, Packet.TAG.BT_HOURS, (byte) 0x00);
        Communication.getInstance().sendPacket(requestPacket);
        requestPacket.setTag(Packet.TAG.BT_MINUTES);
        Communication.getInstance().sendPacket(requestPacket);
        requestPacket.setTag(Packet.TAG.BT_SECONDS);
        Communication.getInstance().sendPacket(requestPacket);
        requestPacket.setTag(Packet.TAG.BT_ALARM_HOUR);
        Communication.getInstance().sendPacket(requestPacket);
        requestPacket.setTag(Packet.TAG.BT_ALARM_MINUTE);
        Communication.getInstance().sendPacket(requestPacket);

        return mView;
    }
}
