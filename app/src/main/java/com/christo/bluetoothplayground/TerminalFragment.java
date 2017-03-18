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
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;

import java.util.ArrayList;


public class TerminalFragment extends Fragment {

    private static final String TAG = TerminalFragment.class.getSimpleName();
    ArrayList<String> mList =  new ArrayList<>();
    ArrayAdapter<String> mArrayAdapter;


    private Handler mHandler = new Handler(new Handler.Callback() {
        @Override
        public boolean handleMessage(Message message) {
            final String string = (String) message.obj;
            if (string == null)
                return false;

            mList.add("bt: " + string);
            mArrayAdapter.notifyDataSetChanged();
            return false;
        }
    });

    public TerminalFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public void onDestroy() {
        Communication.getInstance().setCurrentHandler(null, Communication.HANDLER_ARG1_UI);
        super.onDestroy();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        final View mView = inflater.inflate(R.layout.fragment_terminal, container, false);
        final Context mContext = mView.getContext();

        Communication.getInstance().setCurrentHandler(mHandler, Communication.HANDLER_ARG1_TERM);

        mArrayAdapter = new ArrayAdapter<>(mContext, android.R.layout.simple_list_item_1, mList);
        final ListView listView = (ListView) mView.findViewById(R.id.listView);
        listView.setAdapter(mArrayAdapter);

        final EditText editText = (EditText) mView.findViewById(R.id.editText);
        final Button button = (Button) mView.findViewById(R.id.button);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String text = editText.getText().toString() + "\n\r";
                mList.add("droid: " + text);
                mArrayAdapter.notifyDataSetChanged();
                Log.i(TAG, "sending: " + text);
                Communication.getInstance().write(text.getBytes());

                editText.setText("");
            }
        });

        return mView;
    }


}