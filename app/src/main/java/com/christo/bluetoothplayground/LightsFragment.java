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
import android.widget.EditText;
import android.widget.ListView;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;

public class LightsFragment extends Fragment {

    static final String TAG = LightsFragment.class.getSimpleName();

    private Context mContext;

    EditText editText0;
    EditText editText1;

    SeekBar seekBar0;
    SeekBar seekBar1;
//    private ArrayList<LightsClass> mLightsArrayList = new ArrayList<>();
//    private LightsAdapter mListAdapter;

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
                        Log.i("Settings", String.format("Data rec: 0x%02X", packet.getData()));
//                        seekBar.setProgress(Utilities.fromByte(packet.getData()));
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

//        String[] lights = getResources().getStringArray(R.array.ligts_array);
//        for (String light : lights)
//        {
//            LightsClass lightsClass = new LightsClass(light);
//            mLightsArrayList.add(lightsClass);
//        }

        mContext = view.getContext();
        Communication.getInstance().setCurrentHandler(mHandler, Communication.HANDLER_ARG1_SETTINGS);

        TextView textView0 = (TextView) view.findViewById(R.id.textView_lights0);
        textView0.setText("Kitchen Top");

        TextView textView1 = (TextView) view.findViewById(R.id.textView_lights1);
        textView1.setText("Kitchen Bot");

        editText0 = (EditText) view.findViewById(R.id.editText_lights0);
        editText1 = (EditText) view.findViewById(R.id.editText_lights1);

        seekBar0 = (SeekBar) view.findViewById(R.id.seekBar_lights0);
        seekBar1 = (SeekBar) view.findViewById(R.id.seekBar_lights1);

        seekBar0.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            private int val;
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                val = i;
                editText0.setText(String.valueOf(i));
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                Packet packet = new Packet();
                packet.setType(Packet.TYPE.TYPE_SET);
                packet.setTag(Packet.TAG.BT_KITCH_TOP);
                packet.setData((byte)val);
                Communication.getInstance().sendPacket(packet);
            }
        });

        seekBar1.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            private int val;
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                val = i;
                editText1.setText(String.valueOf(i));
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                Packet packet = new Packet();
                packet.setType(Packet.TYPE.TYPE_SET);
                packet.setTag(Packet.TAG.BT_KITCH_BOT);
                packet.setData((byte)val);
                Communication.getInstance().sendPacket(packet);
            }
        });

//        ListView listView = (ListView) view.findViewById(R.id.listView_LightFragment);
//        mListAdapter = new LightsAdapter(getActivity().getBaseContext(), mLightsArrayList);
//        listView.setAdapter(mListAdapter);

//        Button button = (Button) view.findViewById(R.id.button_LightFragment);
//        button.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                Log.i(TAG, "refresh button clicked.");
//            }
//        });
        return view;
    }
}
