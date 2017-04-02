package com.christo.bluetoothplayground;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.EditText;
import android.widget.SeekBar;
import android.widget.TextView;

import java.util.ArrayList;


class LightsAdapter extends BaseAdapter
{
    private static ArrayList<LightsClass> mLights;
    private Context mContext;
    private LayoutInflater mLayoutInflater;

    private TextView textView;
    private EditText editText;
    private SeekBar seekBar;

    LightsAdapter(Context context, ArrayList<LightsClass> lights)
    {
        mContext = context;
        mLights = lights;
        mLayoutInflater = LayoutInflater.from(mContext);
    }

    @Override
    public int getCount() {
        return mLights.size();
    }

    @Override
    public Object getItem(int i) {
        return mLights.get(i);
    }

    @Override
    public long getItemId(int i) {
        return i;
    }

    void updateValues(int i)
    {
        textView.setText(mLights.get(i).getName());
        editText.setText(String.valueOf(i));
        seekBar.setProgress(mLights.get(i).getDuty());
    }

    @Override
    public View getView(int i, View view, ViewGroup viewGroup) {

        if (view == null)
        {
            LayoutInflater layoutInflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            view = layoutInflater.inflate(R.layout.custom_row_lights, viewGroup, false);
        }

        textView = (TextView) view.findViewById(R.id.textView_crLights);
        editText = (EditText) view.findViewById(R.id.editText_crDuty);
        seekBar = (SeekBar) view.findViewById(R.id.seekBar_crLights);


        updateValues(i);

        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            int val;
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                val = i;
                editText.setText(String.valueOf(i));
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

        return view;
    }
}
