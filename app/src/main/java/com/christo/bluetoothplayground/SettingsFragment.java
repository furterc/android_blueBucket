package com.christo.bluetoothplayground;

import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;

import java.sql.Time;
import java.util.Calendar;


public class SettingsFragment extends Fragment {

    private Context mContext;

    private Handler mHandler = new Handler(new Handler.Callback() {
        @Override
        public boolean handleMessage(Message message) {
            if (message.obj == null)
                return false;

            final String msgObj = (String) message.obj;
            if (message.arg1 == Communication.HANDLER_ARG1_CONNECT && "disconnect".equals(msgObj)) {
                Toast.makeText(mContext, "Device disconnected.", Toast.LENGTH_SHORT).show();
                getActivity().finish();
                return false;
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
                            Thread.sleep(200);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }

                        packet.setType(Packet.TYPE.TYPE_SET);
                        packet.setTag(Packet.TAG.BT_MINUTES);
                        packet.setData((byte)Calendar.getInstance().get(Calendar.MINUTE));
                        packet.dbgPrint();
                        Communication.getInstance().sendPacket(packet);

                        try {
                            Thread.sleep(200);
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

        return mView;
    }

}
