package com.everhope.elighte.fragments;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.everhope.elighte.R;
import com.everhope.elighte.activities.RemoteControlActivity;
import com.everhope.elighte.models.LightGroup;
import com.everhope.elighte.models.Remoter;
import com.everhope.elighte.models.SubGroup;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;


public class RemoterFragment extends Fragment {
    private static final String TAG = "RemoterFragment@Light";

    private ListView listView;
    private RemoterListAdapter remoterListAdapter;

    // TODO: Rename and change types and number of parameters
    public static RemoterFragment newInstance() {
        RemoterFragment fragment = new RemoterFragment();
        return fragment;
    }

    public RemoterFragment() {
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View rootView = inflater.inflate(R.layout.fragment_remoter, container, false);

        listView = (ListView)rootView.findViewById(R.id.allremoters_lv);

        List<Remoter> remoters = Remoter.getAll();
        remoterListAdapter = new RemoterListAdapter(getActivity(), remoters);
        remoterListAdapter.setNotifyOnChange(true);
        listView.setAdapter(remoterListAdapter);

        getActivity().setTitle("遥控器");

        return rootView;
    }

    class RemoterListAdapter extends ArrayAdapter<Remoter> {
        public RemoterListAdapter(Context context, List<Remoter> remoters) {
            super(context, 0, remoters);
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            if (convertView == null) {
                convertView = LayoutInflater.from(getContext()).inflate(R.layout.remoter_item, parent, false);
            }

            final Remoter remoter = getItem(position);
            String remoterName = remoter.name;
            TextView textView = (TextView)convertView.findViewById(R.id.remoter_name);
            textView.setText(remoterName);
            textView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(getActivity(), RemoteControlActivity.class);
                    intent.putExtra("remoter_id",remoter.remoterID);
                    startActivity(intent);

                }
            });
            return convertView;
        }
    }
}
