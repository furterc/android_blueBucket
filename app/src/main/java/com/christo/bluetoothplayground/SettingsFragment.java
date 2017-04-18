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

    private Handler mBTHandler;

    private Handler mHandler = new Handler(new Handler.Callback() {
        @Override
        public boolean handleMessage(Message message) {
            if (message.obj == null)
                return false;

            if (message.arg1 == Communication.HANDLER_ARG1_CONNECT) {
                if (MainActivity.loadingDialog.isShowing())
                    MainActivity.loadingDialog.dismiss();

                if ("connected".equals((String) message.obj)) {
                    Toast.makeText(mContext, "Connected.", Toast.LENGTH_SHORT).show();
                    return false;
                }

                if ("disconnect".equals((String) message.obj)) {
                    Toast.makeText(mContext, "Device disconnected.", Toast.LENGTH_SHORT).show();
//                    getActivity().finish();
                    return false;
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
        MainActivity.setHandler(mHandler);
        mBTHandler = MainActivity.getMainBTHandler();
        super.onCreate(savedInstanceState);
    }

    @Override
    public void onDestroy() {

        super.onDestroy();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        mView = inflater.inflate(R.layout.fragment_settings, container, false);

        setHasOptionsMenu(true);
        mContext = mView.getContext();

        textViewTime = (TextView) mView.findViewById(R.id.textView_settings_time);

        mTimePicker = (TimePicker) mView.findViewById(R.id.timePicker_settings_alarm);
        mTimePicker.setIs24HourView(true);

        final Button buttonGetTime = (Button) mView.findViewById(R.id.button_settings_getTime);
        buttonGetTime.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
        requestTime();
            }
        });

        final Button buttonSetTime = (Button) mView.findViewById(R.id.button_settings_setTime);
        buttonSetTime.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mBTHandler.post(new Runnable() {
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
                });
            }
        });


        final Button btnGetAlarm = (Button) mView.findViewById(R.id.button_settings_getAlarm);
        btnGetAlarm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
               requestAlarm();
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

        new Thread(new Runnable() {
            @Override
            public void run() {
                while (!Communication.getInstance().isConnected())
                {
                    Log.i("SettingsFragment", "waiting for connect");
                    try {
                        Thread.sleep(100);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
                requestTime();
                requestAlarm();
            }
        }).start();


        return mView;
    }

    private void requestTime() {
//        final ProgressDialog progressDialog = ProgressDialog.show(mContext, "Updating Time", "Please wait...", true, false);

        mBTHandler.post(new Runnable() {
            @Override
            public void run() {
                Packet.TAG tag = Packet.TAG.BT_HOURS;
                updateTime(tag, Communication.getInstance().requestPacket(tag));

                tag = Packet.TAG.BT_MINUTES;
                updateTime(tag, Communication.getInstance().requestPacket(tag));

                tag = Packet.TAG.BT_SECONDS;
                updateTime(tag, Communication.getInstance().requestPacket(tag));

//                progressDialog.dismiss();
            }
        });
    }

    private void updateTime(Packet.TAG tag, int value) {
        CharSequence charSequence = textViewTime.getText();

        final String strings[] = charSequence.toString().split(":");

        if (strings.length != 3)
            return;

        switch (tag) {
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
        mView.post(new Runnable() {
            @Override
            public void run() {
                textViewTime.setText(strings[0] + ":" + strings[1] + ":" + strings[2]);
            }
        });
    }

    private void requestAlarm()
    {
//        final ProgressDialog progressDialog = ProgressDialog.show(mContext, "Updating Alarm", "Please wait...", true, false);

        mBTHandler.post(new Runnable() {
            @Override
            public void run() {

                Packet.TAG tag = Packet.TAG.BT_ALARM_HOUR;
                final int hour = Communication.getInstance().requestPacket(tag);

                tag = Packet.TAG.BT_ALARM_MINUTE;
                final int minute = Communication.getInstance().requestPacket(tag);


                mView.post(new Runnable() {
                    @Override
                    public void run() {
                        mTimePicker.setCurrentHour(hour);
                        mTimePicker.setCurrentMinute(minute);
                    }
                });

//                progressDialog.dismiss();
            }
        });
    }
}
