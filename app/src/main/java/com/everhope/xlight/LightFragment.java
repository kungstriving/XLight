package com.everhope.xlight;


import android.app.Activity;
import android.app.Fragment;
import android.os.Bundle;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.BaseExpandableListAdapter;
import android.widget.ExpandableListView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;


/**
 * 灯主页
 */
public class LightFragment extends Fragment {

    private static final String ARG_HELLO_LIGHT = "hello_light";
    private  List<String> groupArray;
    private  List<List<String>> childArray;

    private String mHelloLight;

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
        // Required empty public constructor
        groupArray = new ArrayList<>();
        childArray = new ArrayList<>();

        groupArray.add("卧室");
        groupArray.add("客厅");

        List<String> tempArray = new ArrayList<>();
        tempArray.add("00001");
        tempArray.add("00002");
        tempArray.add("00003");
        tempArray.add("00004");
        tempArray.add("00005");
        tempArray.add("00006");

        //add 卧室
        childArray.add(tempArray);

        tempArray = new ArrayList<>();
        tempArray.add("10001");
        tempArray.add("10002");
        tempArray.add("10003");
        tempArray.add("10004");
        tempArray.add("10005");
        tempArray.add("10006");

        childArray.add(tempArray);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mHelloLight = getArguments().getString(ARG_HELLO_LIGHT);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View rootView = inflater.inflate(R.layout.fragment_light, container, false);

        ExpandableListView expandableListView = (ExpandableListView)rootView.findViewById(R.id.expandableListView);
        expandableListView.setAdapter(new LightExpandableListAdapter(getActivity()));

        getActivity().setTitle(R.string.light_fragment_title);
        return rootView;
    }

    class LightExpandableListAdapter extends BaseExpandableListAdapter {

        Activity activity;
        public LightExpandableListAdapter(Activity activity) {
            this.activity = activity;
        }

        @Override
        public Object getChild(int groupPosition, int childPosition) {
            return childArray.get(groupPosition).get(childPosition);
        }

        @Override
        public long getChildId(int groupPosition, int childPosition) {
            return childPosition;
        }

        @Override
        public int getChildrenCount(int groupPosition) {
            return childArray.get(groupPosition).size();
        }

        @Override
        public View getChildView(int groupPosition, int childPosition, boolean isLastChild, View convertView, ViewGroup parent) {
            String str = childArray.get(groupPosition).get(childPosition);
            return generateView(str);
        }

        @Override
        public Object getGroup(int groupPosition) {
            return groupArray.get(groupPosition);
        }

        @Override
        public int getGroupCount() {
            return groupArray.size();
        }

        @Override
        public long getGroupId(int groupPosition) {
            return groupPosition;
        }

        @Override
        public View getGroupView(int groupPosition, boolean isExpanded, View convertView, ViewGroup parent) {
            String string = groupArray.get(groupPosition);
            return  generateView(string);
        }

        @Override
        public boolean hasStableIds() {
            return false;
        }

        @Override
        public boolean isChildSelectable(int groupPosition, int childPosition) {
            return true;
        }

        public TextView generateView(String string)
        {
            // Layout parameters for the ExpandableListView
            AbsListView.LayoutParams layoutParams = new  AbsListView.LayoutParams(
                    ViewGroup.LayoutParams.FILL_PARENT, 64 );
            TextView text = new  TextView(activity);
            text.setLayoutParams(layoutParams);
            // Center the text vertically
            text.setGravity(Gravity.CENTER_VERTICAL | Gravity.LEFT);
            // Set the text starting position
            text.setPadding(36 , 0 , 0 , 0 );
            text.setText(string);
            return  text;
        }
    }

}
