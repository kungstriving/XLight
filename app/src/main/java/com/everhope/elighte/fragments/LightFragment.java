package com.everhope.elighte.fragments;

import android.app.Fragment;
import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.everhope.elighte.R;
import com.everhope.elighte.models.SubGroup;
import com.everhope.elighte.models.LightGroup;

import java.util.List;

/**
 * 灯主页
 */
public class LightFragment extends Fragment {

    private static final String ARG_HELLO_LIGHT = "hello_light";

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
//        groupArray = new ArrayList<>();
//        childArray = new ArrayList<>();
//
//        groupArray.add("卧室");
//        groupArray.add("客厅");
//        groupArray.add("未分组");
//
//        List<String> tempArray = new ArrayList<>();
//        tempArray.add("00001");
//        tempArray.add("00002");
//        tempArray.add("00003");
//        tempArray.add("00004");
//        tempArray.add("00005");
//        tempArray.add("00006");
//
//        //add 卧室
//        childArray.add(tempArray);
//
//        tempArray = new ArrayList<>();
//        tempArray.add("10001");
//        tempArray.add("10002");
//        tempArray.add("10003");
//        tempArray.add("10004");
//        tempArray.add("10005");
//        tempArray.add("10006");
//
//        //add 客厅
//        childArray.add(tempArray);
//        //add 未分组
//        tempArray = new ArrayList<>();
//        tempArray.add("20001");
//        tempArray.add("20002");
//        tempArray.add("20003");
//        tempArray.add("20004");
//        tempArray.add("20005");
//        tempArray.add("20006");
//
//        childArray.add(tempArray);
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

        //get light groups
        List<SubGroup> groups = getLightGroups();
        LightGroupListAdapter lightGroupListAdapter = new LightGroupListAdapter(getActivity(), groups);

        listView.setAdapter(lightGroupListAdapter);

        getActivity().setTitle(R.string.light_fragment_title);
        return rootView;
    }

    private List<SubGroup> getLightGroups() {
        return SubGroup.getAll();
    }

    class LightGroupListAdapter extends ArrayAdapter<SubGroup> {
        public LightGroupListAdapter(Context context, List<SubGroup> groups) {
            super(context,0,groups);
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            if (convertView == null) {
                convertView = new TextView(getContext());
            }

            SubGroup group = getItem(position);
            List<LightGroup> lightGroups = group.lightGroups();
            String groupName = group.name + "(" + lightGroups.size() + ")";
            TextView textView = (TextView)convertView;
            textView.setText(groupName);
            return convertView;
        }
    }
//    class LightExpandableListAdapter extends BaseExpandableListAdapter {
//
//        Activity activity;
//        public LightExpandableListAdapter(Activity activity) {
//            this.activity = activity;
//        }
//
//        @Override
//        public Object getChild(int groupPosition, int childPosition) {
//            return childArray.get(groupPosition).get(childPosition);
//        }
//
//        @Override
//        public long getChildId(int groupPosition, int childPosition) {
//            return childPosition;
//        }
//
//        @Override
//        public int getChildrenCount(int groupPosition) {
//            return childArray.get(groupPosition).size();
//        }
//
//        @Override
//        public View getChildView(int groupPosition, int childPosition, boolean isLastChild, View convertView, ViewGroup parent) {
//            String childName = childArray.get(groupPosition).get(childPosition);
//
//            final LinearLayout ll = new LinearLayout(getActivity());
//            ll.setOrientation(LinearLayout.HORIZONTAL);
//            ll.setPadding(96, 10, 0, 0);
//            ll.setMinimumHeight(100);
//            LinearLayout.LayoutParams textViewLayoutParams =
//                    new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
//            TextView textView = new TextView(getActivity());
//            textView.setText(childName);
//            textView.setTextSize(24);
////            textView.setHeight(64);
//            /*
//            此处textview 会遮住linearlayout 的OnChildClick事件 所以需要单独设置一个按钮，当用户长按该按钮时候进行拖放
//             */
//            ll.addView(textView, textViewLayoutParams);
//            textView.setTag("drag");
//            textView.setOnLongClickListener(new View.OnLongClickListener() {
//                @Override
//                public boolean onLongClick(View v) {
//                    ClipData.Item item = new ClipData.Item((CharSequence) v.getTag());
//
//                    String[] mimeTypes = {ClipDescription.MIMETYPE_TEXT_PLAIN};
//                    ClipData dragData = new ClipData(v.getTag().toString(),
//                            mimeTypes, item);
//
//                    // Instantiates the drag shadow builder.
//                    View.DragShadowBuilder myShadow = new View.DragShadowBuilder(ll);
//
//                    // Starts the drag
//                    v.startDrag(dragData,  // the data to be dragged
//                            myShadow,  // the drag shadow builder
//                            null,      // no need to use local data
//                            0          // flags (not currently used, set to 0)
//                    );
//                    return false;
//                }
//            });
//
//
//            return ll;
//        }
//
//        @Override
//        public Object getGroup(int groupPosition) {
//            return groupArray.get(groupPosition);
//        }
//
//        @Override
//        public int getGroupCount() {
//            return groupArray.size();
//        }
//
//        @Override
//        public long getGroupId(int groupPosition) {
//            return groupPosition;
//        }
//
//        @Override
//        public View getGroupView(int groupPosition, boolean isExpanded, View convertView, ViewGroup parent) {
//            String groupName = groupArray.get(groupPosition);
//
//            LinearLayout ll = new LinearLayout(getActivity());
//            ll.setOrientation(LinearLayout.HORIZONTAL);
//            ll.setPadding(64, 0, 0, 0);
//            ll.setBackgroundColor(Color.parseColor("gray"));
//            TextView textView = new TextView(getActivity());
//            textView.setText(groupName);
//            textView.setTextSize(18);
//
//            ll.addView(textView);
//
//            return  ll;
//        }
//
//        @Override
//        public boolean hasStableIds() {
//            return false;
//        }
//
//        @Override
//        public boolean isChildSelectable(int groupPosition, int childPosition) {
//            return true;
//        }
//
//        public TextView generateView(String string)
//        {
//            // Layout parameters for the ExpandableListView
//            AbsListView.LayoutParams layoutParams = new  AbsListView.LayoutParams(
//                    ViewGroup.LayoutParams.FILL_PARENT, 64 );
//            TextView text = new  TextView(activity);
//            text.setLayoutParams(layoutParams);
//            // Center the text vertically
//            text.setGravity(Gravity.CENTER_VERTICAL | Gravity.LEFT);
//            // Set the text starting position
//            text.setPadding(36 , 0 , 0 , 0 );
//            text.setText(string);
//            return  text;
//        }
//    }

}
