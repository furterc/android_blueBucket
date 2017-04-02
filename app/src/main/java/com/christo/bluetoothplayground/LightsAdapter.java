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

    @Override
    public View getView(final int i, View view, ViewGroup viewGroup) {

        if (view == null)
        {
            LayoutInflater layoutInflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            view = layoutInflater.inflate(R.layout.custom_row_lights, viewGroup, false);
        }

        final TextView textView = (TextView) view.findViewById(R.id.textView_crLightsHeading);
        final TextView textViewVal = (TextView) view.findViewById(R.id.textView_crLightsDuty);
        final SeekBar seekBar = (SeekBar) view.findViewById(R.id.seekBar_crLights);

        textView.setText(mLights.get(i).getName());
        textViewVal.setText(String.valueOf(mLights.get(i).getDuty()));
        seekBar.setProgress(mLights.get(i).getDuty());

        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            int val;
            @Override
            public void onProgressChanged(SeekBar seekBar, int v, boolean b) {
                val = v;
                textViewVal.setText(String.valueOf(v));
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                mLights.get(i).update(i,val);
            }
        });

        return view;
    }
}
