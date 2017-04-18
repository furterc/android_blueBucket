package com.christo.bluetoothplayground;

import android.app.ProgressDialog;
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

//    static final String TAG = LightsFragment.class.getSimpleName();

    private Context mContext;
    private View mView;
    private Handler mBTHandler;

    private ArrayList<LightsClass> mLightsArrayList = new ArrayList<>();
    private LightsAdapter mListAdapter;

    private final Handler mHandler = new Handler(new Handler.Callback() {
        @Override
        public boolean handleMessage(Message message) {
            if (message.obj == null)
                return false;

            if (message.arg1 == Communication.HANDLER_ARG1_CONNECT && "disconnect".equals(message.obj)) {
                Toast.makeText(mContext, "Device disconnected.", Toast.LENGTH_SHORT).show();
                getActivity().finish();
                return false;
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
        mView = inflater.inflate(R.layout.fragment_lights, container, false);

        String[] lights = getResources().getStringArray(R.array.ligts_array);
        for (String light : lights) {
            LightsClass lightsClass = new LightsClass(light);
            mLightsArrayList.add(lightsClass);
        }

        setHasOptionsMenu(true);
        mContext = mView.getContext();
        MainActivity.setHandler(mHandler);
        mBTHandler = MainActivity.getMainBTHandler();

        requestData();

        ListView listView = (ListView) mView.findViewById(R.id.listView_LightFragment);
        mListAdapter = new LightsAdapter(getActivity().getBaseContext(), mLightsArrayList);
        listView.setAdapter(mListAdapter);

        Button button = (Button) mView.findViewById(R.id.button_LightFragment);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                requestData();
            }
        });

        return mView;
    }

    void requestData() {
        final ProgressDialog progressDialog = ProgressDialog.show(mContext, "Updating Data", "Please wait...", true, false);

        mBTHandler.post(new Runnable() {
            @Override
            public void run() {
                Packet.TAG tag = Packet.TAG.BT_KITCH_TOP;
                updateLights(tag, Communication.getInstance().requestPacket(tag));

                tag = Packet.TAG.BT_KITCH_BOT;
                updateLights(tag, Communication.getInstance().requestPacket(tag));

                tag = Packet.TAG.BT_STUDY_BOT;
                updateLights(tag, Communication.getInstance().requestPacket(tag));

                tag = Packet.TAG.BT_STUDY_TOP;
                updateLights(tag, Communication.getInstance().requestPacket(tag));

                progressDialog.dismiss();
            }
        });
    }

    public void updateLights(final Packet.TAG tag, final int duty)
    {
        mView.post(new Runnable() {
            @Override
            public void run() {
                int i = tag.ordinal() - 3;
                mLightsArrayList.get(i).setDuty(duty);
                mListAdapter.notifyDataSetChanged();
            }
        });
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
