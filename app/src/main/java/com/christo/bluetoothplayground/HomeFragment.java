package com.christo.bluetoothplayground;

import android.support.v4.app.Fragment;
import android.app.ProgressDialog;
import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import java.util.Calendar;

/**
 * Created by christo on 2017/06/27.
 */

public class HomeFragment extends Fragment
{
    View mView;
    Context mContext;

    TextView textViewTime;

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

    public HomeFragment()
    {

    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        MainActivity.setHandler(mHandler);
        mBTHandler = MainActivity.getMainBTHandler();
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        mView = inflater.inflate(R.layout.fragment_home, container, false);

        setHasOptionsMenu(true);
        mContext = mView.getContext();

        textViewTime = (TextView) mView.findViewById(R.id.textView_home_time);
        final Button buttonGetTime = (Button) mView.findViewById(R.id.button_home_getTime);
        buttonGetTime.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                requestTime();
            }
        });

        final Button buttonSetTime = (Button) mView.findViewById(R.id.button_home_setTime);
        buttonSetTime.setOnClickListener(new View.OnClickListener() {
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
                    }
                });
            }
        });

        return mView;
    }

    private void requestTime() {
        final ProgressDialog progressDialog = ProgressDialog.show(mContext, "Updating Time", "Please wait...", true, false);

        mBTHandler.post(new Runnable() {
            @Override
            public void run() {
                cMsg.TAG tag = cMsg.TAG.TAG_TIME;
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
}
