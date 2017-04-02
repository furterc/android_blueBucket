package com.christo.bluetoothplayground;

import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;

import java.util.ArrayList;

public class LightsFragment extends Fragment {

    static final String TAG = LightsFragment.class.getSimpleName();

    private Context mContext;

    private ArrayList<LightsClass> mLightsArrayList = new ArrayList<>();
    private LightsAdapter mListAdapter;

    private final Handler mHandler = new Handler(new Handler.Callback() {
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
                switch (packet.getType()) {
                    case TYPE_SET:
                        int i = Utilities.fromByte(packet.getTag()) - 3;
                        mLightsArrayList.get(i).setDuty(Utilities.fromByte(packet.getData()));
                        mListAdapter.notifyDataSetChanged();
                        break;
                }
            }

            return false;
        }
    });

    public LightsFragment() {
    }


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_lights, container, false);

        String[] lights = getResources().getStringArray(R.array.ligts_array);
        for (String light : lights) {
            LightsClass lightsClass = new LightsClass(light);
            mLightsArrayList.add(lightsClass);
        }

        setHasOptionsMenu(true);
        mContext = view.getContext();
        Communication.getInstance().setCurrentHandler(mHandler, Communication.HANDLER_ARG1_SETTINGS);

        ListView listView = (ListView) view.findViewById(R.id.listView_LightFragment);
        mListAdapter = new LightsAdapter(getActivity().getBaseContext(), mLightsArrayList);
        listView.setAdapter(mListAdapter);

        requestData();

        Button button = (Button) view.findViewById(R.id.button_LightFragment);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.i(TAG, "refresh button clicked.");
                requestData();
            }
        });
        return view;
    }

    void requestData() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                Packet requestPacket = new Packet(Packet.TYPE.TYPE_GET, Packet.TAG.BT_KITCH_TOP, (byte) 0x00);
                Communication.getInstance().sendPacket(requestPacket);

                try {
                    Thread.sleep(50);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                requestPacket.setTag(Packet.TAG.BT_KITCH_BOT);
                Communication.getInstance().sendPacket(requestPacket);

                try {
                    Thread.sleep(50);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }

                requestPacket.setTag(Packet.TAG.BT_STUDY_TOP);
                Communication.getInstance().sendPacket(requestPacket);

                try {
                    Thread.sleep(50);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                requestPacket.setTag(Packet.TAG.BT_STUDY_BOT);
                Communication.getInstance().sendPacket(requestPacket);
            }
        }).start();
    }

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

}
