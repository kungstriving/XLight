package com.everhope.elighte.fragments;

import android.app.Fragment;
import android.app.FragmentManager;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.everhope.elighte.R;
import com.everhope.elighte.activities.LightListActivity;
import com.everhope.elighte.models.SubGroup;
import com.everhope.elighte.models.LightGroup;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * 灯主页
 */
public class LightFragment extends Fragment {

    private static final String ARG_HELLO_LIGHT = "hello_light";

    private LightGroupListAdapter lightGroupListAdapter;
    /**
     *
     * @param paramHelloLight
     * @return
     */
    public static LightFragment newInstance(String paramHelloLight) {
        LightFragment fragment = new LightFragment();
        Bundle args = new Bundle();
        args.putString(ARG_HELLO_LIGHT, paramHelloLight);
        fragment.setArguments(args);

        return fragment;
    }

    public LightFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
//        if (getArguments() != null) {
//            mHelloLight = getArguments().getString(ARG_HELLO_LIGHT);
//        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View rootView = inflater.inflate(R.layout.fragment_light, container, false);

        ListView listView = (ListView)rootView.findViewById(R.id.alllights_lv);

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                SubGroup subGroup = (SubGroup)parent.getItemAtPosition(position);
                Intent intent = new Intent(getActivity(), LightListActivity.class);
                intent.putExtra("subgroup_id", subGroup.getId());
                startActivity(intent);

                }
        });
        //get light groups
        ArrayList<SubGroup> groups = getLightGroups();
        Collections.sort(groups, new SubGroupComparator());
        lightGroupListAdapter = new LightGroupListAdapter(getActivity(), groups);
        lightGroupListAdapter.setNotifyOnChange(true);
        listView.setAdapter(lightGroupListAdapter);

        getActivity().setTitle(R.string.light_fragment_title);

        return rootView;
    }

    private ArrayList<SubGroup> getLightGroups() {
        ArrayList<SubGroup> list = new ArrayList<>();
        List<SubGroup> temp = SubGroup.getAll();
        for (SubGroup subGroup : temp) {
            list.add(subGroup);
        }
        return list;
    }

    class LightGroupListAdapter extends ArrayAdapter<SubGroup> {
        public LightGroupListAdapter(Context context, List<SubGroup> groups) {
            super(context,0,groups);
        }

        public LightGroupListAdapter(Context context, int resID, List<SubGroup> groups) {
            super(context, resID, groups);
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            if (convertView == null) {
//                convertView = new TextView(getContext());
                convertView = LayoutInflater.from(getContext()).inflate(R.layout.stationgroup_item, parent, false);
            }

            SubGroup group = getItem(position);
            List<LightGroup> lightGroups = group.lightGroups();
            String groupName = group.name + "(" + lightGroups.size() + ")";
            TextView textView = (TextView)convertView.findViewById(R.id.subgroup_name);
            textView.setText(groupName);
            return convertView;
        }
    }

    public void addNewGroup(String newGroupName) {
        SubGroup subGroup = new SubGroup();
        subGroup.name = newGroupName;
        subGroup.save();

        lightGroupListAdapter.sort(new SubGroupComparator());
        lightGroupListAdapter.add(subGroup);
        lightGroupListAdapter.notifyDataSetChanged();
    }

    class SubGroupComparator implements Comparator<SubGroup>{
        @Override
        public int compare(SubGroup lhs, SubGroup rhs) {
            return -lhs.getId().compareTo(rhs.getId());
        }
    }
}
