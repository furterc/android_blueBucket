package com.christo.bluetoothplayground;

import android.app.Dialog;
import android.app.DialogFragment;
import android.app.TimePickerDialog;
import android.provider.CalendarContract;
import android.support.v4.app.Fragment;
import android.app.ProgressDialog;
import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.Nullable;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import java.text.DateFormat;
import java.util.Calendar;
import java.util.Locale;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Created by christo on 2017/06/27.
 */

public class HomeFragment extends Fragment {

    FragmentActivity myContext;
    View mView;
    Context mContext;

    static int mHour;
    static int mMinute;
    static final Object mWaitObj = new Object();

    TextView textViewTime;
    TextView textViewAlarm;

    private Handler mBTHandler;

    private SetPointClass kitchenSetPoints[] = new SetPointClass[5];

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

    public HomeFragment() {

    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        MainActivity.setHandler(mHandler);
        mBTHandler = MainActivity.getMainBTHandler();
        setHasOptionsMenu(true);
        super.onCreate(savedInstanceState);
    }

    @Override
    public void onAttach(Context context) {
        myContext = (FragmentActivity) context;
        super.onAttach(context);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        mView = inflater.inflate(R.layout.fragment_home, container, false);

        setHasOptionsMenu(true);
        mContext = mView.getContext();

        textViewTime = (TextView) mView.findViewById(R.id.textView_home_time);
        textViewAlarm = (TextView) mView.findViewById(R.id.textView_home_alarm);

        requestData();

        final Button buttonGetTime = (Button) mView.findViewById(R.id.button_home_setTime);
        buttonGetTime.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mBTHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        cMsg cmsg = new cMsg(cMsg.TYPE.TYPE_SET, cMsg.TAG.TAG_TIME, 0, Calendar.getInstance().get(Calendar.HOUR_OF_DAY));
                        Communication.getInstance().sendFramedData(cmsg.toBytes());

                        try {
                            Thread.sleep(150);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }

                        cmsg = new cMsg(cMsg.TYPE.TYPE_SET, cMsg.TAG.TAG_TIME, 1, Calendar.getInstance().get(Calendar.MINUTE));
                        Communication.getInstance().sendFramedData(cmsg.toBytes());

                        try {
                            Thread.sleep(150);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }

                        cmsg = new cMsg(cMsg.TYPE.TYPE_SET, cMsg.TAG.TAG_TIME, 2, Calendar.getInstance().get(Calendar.SECOND));
                        Communication.getInstance().sendFramedData(cmsg.toBytes());

                        request(cMsg.TAG.TAG_TIME, textViewTime);
                    }
                });
            }


        });

        final Button buttonSetAlarm = (Button) mView.findViewById(R.id.button_home_setAlarm);
        buttonSetAlarm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                DialogFragment newFragment = new TimePickerFragment();
                newFragment.show(myContext.getFragmentManager(), "timepicker");

                mHour = 25;
                mMinute = 61;
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        synchronized (mWaitObj) {
                            try {
                                mWaitObj.wait(30000);
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }
                        }
                        if (mHour == 25 && mMinute == 61) {
                            /* failed to set*/
                            return;
                        }
                        Log.i("setTime", "h: " + mHour + " m: " + mMinute);

                        mBTHandler.post(new Runnable() {
                            @Override
                            public void run() {
                                cMsg cmsg = new cMsg(cMsg.TYPE.TYPE_SET, cMsg.TAG.TAG_ALARM, 0, mHour);
                                Communication.getInstance().sendFramedData(cmsg.toBytes());

                                try {
                                    Thread.sleep(150);
                                } catch (InterruptedException e) {
                                    e.printStackTrace();
                                }

                                cmsg = new cMsg(cMsg.TYPE.TYPE_SET, cMsg.TAG.TAG_ALARM, 1, mMinute);
                                Communication.getInstance().sendFramedData(cmsg.toBytes());

                                try {
                                    Thread.sleep(150);
                                } catch (InterruptedException e) {
                                    e.printStackTrace();
                                }

                                request(cMsg.TAG.TAG_ALARM, textViewAlarm);
                            }
                        });
                    }
                }).start();
            }
        });


        final SeekBar seekTop = (SeekBar) mView.findViewById(R.id.seekBar_kitchenTop);
        seekTop.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                cMsg msg = new cMsg();
                msg.setType(cMsg.TYPE.TYPE_SET);
                msg.setTag(cMsg.TAG.TAG_LED_KITCHEN);
                msg.setData1((byte) i);
                msg.setData0((byte) 0);
                Communication.getInstance().sendFramedData(msg.toBytes());

            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });

        final SeekBar seekBot = (SeekBar) mView.findViewById(R.id.seekBar_kitchenBot);
        seekBot.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                cMsg msg = new cMsg();
                msg.setType(cMsg.TYPE.TYPE_SET);
                msg.setTag(cMsg.TAG.TAG_LED_KITCHEN);
                msg.setData1((byte) i);
                msg.setData0((byte) 1);
                Communication.getInstance().sendFramedData(msg.toBytes());
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });

//        View v = mView.findViewById(R.id.include_kitchenTop1);
        kitchenSetPoints[0] = new SetPointClass();
        kitchenSetPoints[0].init(mView.findViewById(R.id.include_kitchenTop1), seekTop, seekBot);

        kitchenSetPoints[1] = new SetPointClass();
        kitchenSetPoints[1].init(mView.findViewById(R.id.include_kitchenTop2), seekTop, seekBot);

        kitchenSetPoints[2] = new SetPointClass();
        kitchenSetPoints[2].init(mView.findViewById(R.id.include_kitchenTop3), seekTop, seekBot);

        kitchenSetPoints[3] = new SetPointClass();
        kitchenSetPoints[3].init(mView.findViewById(R.id.include_kitchenTop4), seekTop, seekBot);

        kitchenSetPoints[4] = new SetPointClass();
        kitchenSetPoints[4].init(mView.findViewById(R.id.include_kitchenTop5), seekTop, seekBot);


        return mView;
    }

    void requestData() {
        request(cMsg.TAG.TAG_TIME, textViewTime);
        request(cMsg.TAG.TAG_ALARM, textViewAlarm);
    }

    private void request(final cMsg.TAG tag, final TextView textView) {
        final ProgressDialog progressDialog = ProgressDialog.show(mContext, "Updating", "Please wait...", true, false);

        mBTHandler.post(new Runnable() {
            @Override
            public void run() {
                updateTextView(0, Communication.getInstance().requestPacket(tag, 0), textView);
                updateTextView(1, Communication.getInstance().requestPacket(tag, 1), textView);

                progressDialog.dismiss();
            }
        });
    }

    private void updateTextView(int data0, int value, final TextView textView) {
        CharSequence charSequence = textView.getText();

        final String strings[] = charSequence.toString().split(":");

        if (strings.length != 2)
            return;

        if (value < 10)
            strings[data0] = String.format(Locale.US, "%02d", value);
        else
            strings[data0] = String.valueOf(value);

        mView.post(new Runnable() {
            @Override
            public void run() {
                textView.setText(String.format("%2s:%2s", strings[0], strings[1]));
            }
        });
    }

    /*--- Options Menu ---------------------------------------------------------------------------*/
    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.ligths, menu);
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_lightRefresh:
                requestData();
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    /*--- Set Point Class ------------------------------------------------------------------------*/
    private class SetPointClass {
        private View mView;

        private TextView textViewTop;
        private TextView textViewBot;

        private SeekBar seekTop;
        private SeekBar seekBot;

        private int mTopValue;
        private int mBotValue;

        private AtomicBoolean mLongPressedBool = new AtomicBoolean(false);

        SetPointClass() {

        }

        void updateValues() {
            mTopValue = seekTop.getProgress();
            mBotValue = seekBot.getProgress();
        }

        void setValues(int top, int bot) {
            seekTop.setProgress(top);
            seekBot.setProgress(bot);
        }

        Runnable mLongPressed = new Runnable() {
            public void run() {
                Log.e("TRY", "Longpress");
                mLongPressedBool.set(true);
                updateValues();
                setText(mTopValue, mBotValue);
            }
        };

        void init(View view, SeekBar sTop, SeekBar sBot) {
            if (view == null || sTop == null || sBot == null)
                return;

            seekTop = sTop;
            seekBot = sBot;

            textViewTop = (TextView) view.findViewById(R.id.textView_include_top);
            textViewBot = (TextView) view.findViewById(R.id.textView_include_bot);
            mView = view;
            mView.setOnTouchListener(new View.OnTouchListener() {
                @Override
                public boolean onTouch(View view, final MotionEvent motionEvent) {
                    if (motionEvent.getAction() == MotionEvent.ACTION_DOWN) {
                        mHandler.postDelayed(mLongPressed, android.view.ViewConfiguration.getLongPressTimeout());
                        return true;
                    }

                    if (motionEvent.getAction() == MotionEvent.ACTION_UP) {
                        mHandler.removeCallbacks(mLongPressed);

                        if (!mLongPressedBool.get()) {
                            setValues(mTopValue, mBotValue);
                            return true;
                        }

                        mLongPressedBool.set(false);
                    }

                    return false;
                }
            });

        }

        void setText(final int top, final int bot) {
            if (textViewTop == null || textViewBot == null)
                return;

            mView.post(new Runnable() {
                @Override
                public void run() {
                    textViewTop.setText(String.format(Locale.US, "%d", top));
                    textViewBot.setText(String.format(Locale.US, "%d", bot));
                }
            });
        }

    }


    /*--- Time Picker ----------------------------------------------------------------------------*/
    public static class TimePickerFragment extends DialogFragment implements TimePickerDialog.OnTimeSetListener {
        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {

            //set current time as default
            final Calendar c = Calendar.getInstance();
            int hour = c.get(Calendar.HOUR_OF_DAY);
            int minute = c.get(Calendar.MINUTE);

            //Create a new instance of TimePickerDialog and return it.
            return new TimePickerDialog(getActivity(), this, hour, minute, true);
        }

        @Override
        public void onTimeSet(TimePicker timePicker, int i, int i1) {
            mHour = i;
            mMinute = i1;

            synchronized (mWaitObj) {
                mWaitObj.notify();
            }
        }
    }

}
