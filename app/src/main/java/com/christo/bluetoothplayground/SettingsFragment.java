package com.christo.bluetoothplayground;

import android.app.NotificationManager;
import android.app.ProgressDialog;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.support.v4.app.Fragment;
import android.support.v4.app.NotificationCompat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import java.util.Calendar;

import static android.content.Context.NOTIFICATION_SERVICE;
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
                        cMsg cmsg = new cMsg(TYPE.TYPE_SET, TAG.TAG_TIME, 0, Calendar.getInstance().get(Calendar.HOUR_OF_DAY));
                        Communication.getInstance().sendFramedData(cmsg.toBytes());

                        try {
                            Thread.sleep(150);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }

                        cmsg = new cMsg(TYPE.TYPE_SET, TAG.TAG_TIME, 1, Calendar.getInstance().get(Calendar.MINUTE));
                        Communication.getInstance().sendFramedData(cmsg.toBytes());

                        try {
                            Thread.sleep(150);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }

                        cmsg = new cMsg(TYPE.TYPE_SET, TAG.TAG_TIME, 2, Calendar.getInstance().get(Calendar.SECOND));
                        Communication.getInstance().sendFramedData(cmsg.toBytes());
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
                cMsg cmsg = new cMsg(TYPE.TYPE_SET, TAG.TAG_ALARM, 0, Integer.valueOf(mTimePicker.getCurrentHour().toString()));
                Communication.getInstance().sendFramedData(cmsg.toBytes());

                try {
                    Thread.sleep(150);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }

                cmsg = new cMsg(TYPE.TYPE_SET, TAG.TAG_ALARM, 1, Integer.valueOf(mTimePicker.getCurrentMinute().toString()));
                Communication.getInstance().sendFramedData(cmsg.toBytes());

            }
        });

        final Button btnTryA = (Button) mView.findViewById(R.id.btnTry);
        btnTryA.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                Bitmap icon = BitmapFactory.decodeResource(mContext.getResources(),
                        R.drawable.kfc_bucket);
                NotificationCompat.Builder mBuilder =
                        new NotificationCompat.Builder(mContext)
                                .setSmallIcon(R.drawable.kfc_bucket)
                                .setLargeIcon(icon)
                                .setContentTitle("My notification")
                                .setContentText("Hello World!");

// Sets an ID for the notification
                int mNotificationId = 001;
// Gets an instance of the NotificationManager service
                NotificationManager mNotifyMgr =
                        (NotificationManager) mContext.getSystemService(NOTIFICATION_SERVICE);
// Builds the notification and issues it.
                mNotifyMgr.notify(mNotificationId, mBuilder.build());

            }
        });


        requestTime();
        requestAlarm();
        return mView;
    }

    private void requestTime() {
        final ProgressDialog progressDialog = ProgressDialog.show(mContext, "Updating Time", "Please wait...", true, false);

        mBTHandler.post(new Runnable() {
            @Override
            public void run() {
                TAG tag = TAG.TAG_TIME;
                updateTime(0, Communication.getInstance().requestPacket(tag, 0));
                updateTime(1, Communication.getInstance().requestPacket(tag, 1));
                updateTime(2, Communication.getInstance().requestPacket(tag, 2));

                progressDialog.dismiss();
            }
        });
    }

    private void updateTime(int data0, int value) {
        CharSequence charSequence = textViewTime.getText();

        final String strings[] = charSequence.toString().split(":");

        if (strings.length != 3)
            return;

        strings[data0] = String.valueOf(value);

        mView.post(new Runnable() {
            @Override
            public void run() {
                textViewTime.setText(String.format("%2s:%2s:%2s",strings[0],strings[1],strings[2]));
            }
        });
    }

    private void    requestAlarm()
    {
        final ProgressDialog progressDialog = ProgressDialog.show(mContext, "Updating Alarm", "Please wait...", true, false);

        mBTHandler.post(new Runnable() {
            @Override
            public void run() {
                TAG tag = TAG.TAG_ALARM;
                final int hour = Communication.getInstance().requestPacket(tag, 0);
                final int minute = Communication.getInstance().requestPacket(tag, 1);


                mView.post(new Runnable() {
                    @Override
                    public void run() {
                        mTimePicker.setCurrentHour(hour);
                        mTimePicker.setCurrentMinute(minute);
                    }
                });

                progressDialog.dismiss();
            }
        });
    }
}
