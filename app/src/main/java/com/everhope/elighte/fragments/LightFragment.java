package com.everhope.elighte.fragments;

import android.app.Fragment;
import android.app.FragmentManager;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.ResultReceiver;
import android.util.Log;
import android.util.SparseBooleanArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.PopupWindow;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.everhope.elighte.R;
import com.everhope.elighte.XLightApplication;
import com.everhope.elighte.activities.GroupControlActivity;
import com.everhope.elighte.activities.LightListActivity;
import com.everhope.elighte.activities.SceneEditActivity;
import com.everhope.elighte.comm.DataAgent;
import com.everhope.elighte.constants.Constants;
import com.everhope.elighte.helpers.MessageUtils;
import com.everhope.elighte.models.CommonMsgResponse;
import com.everhope.elighte.models.Light;
import com.everhope.elighte.models.LightScene;
import com.everhope.elighte.models.Scene;
import com.everhope.elighte.models.SubGroup;
import com.everhope.elighte.models.LightGroup;

import org.apache.commons.lang.exception.ExceptionUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * 灯主页
 */
public class LightFragment extends Fragment {

    private static final String TAG = "LightFragment@Light";

    private ListView listView;
    private LightGroupListAdapter lightGroupListAdapter;
    /**
     *
     * @param paramHelloLight
     * @return
     */
    public static LightFragment newInstance(String paramHelloLight) {
        LightFragment fragment = new LightFragment();

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

        listView = (ListView)rootView.findViewById(R.id.alllights_lv);
        listView.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE);
        //添加分组操作事件
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                //弹出窗口进行亮度选择
                final PopupWindow popup = new PopupWindow(getActivity());
                //设置弹出窗口的样式
                View layout = getActivity().getLayoutInflater().inflate(R.layout.popup_scene_control, null);

                popup.setContentView(layout);
                // Set content width and height
                popup.setHeight(WindowManager.LayoutParams.WRAP_CONTENT);
                popup.setWidth(WindowManager.LayoutParams.MATCH_PARENT);
                // Closes the popup window when touch outside of it - when looses focus
                popup.setOutsideTouchable(true);
                popup.setFocusable(true);

                // Show anchored to button
                popup.setBackgroundDrawable(new BitmapDrawable());
                popup.showAsDropDown(view);

                layout.findViewById(R.id.scene_control_edit).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        popup.dismiss();
                        Intent intent = new Intent(getActivity(), SceneEditActivity.class);
//                        intent.putExtra("scene_id", scene.getId());
                        startActivity(intent);
                    }
                });

                //添加场景亮度调节
                final SeekBar seekBar = (SeekBar)layout.findViewById(R.id.scene_bright_sb);

                //添加设置场景亮度为0的事件
                layout.findViewById(R.id.scene_power_switch).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        seekBar.setProgress(0);
                    }
                });

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
        public View getView(final int position, View convertView, ViewGroup parent) {
            if (convertView == null) {
//                convertView = new TextView(getContext());
                convertView = LayoutInflater.from(getContext()).inflate(R.layout.stationgroup_item, parent, false);
            }

            final SubGroup group = getItem(position);
            List<LightGroup> lightGroups = group.lightGroups();
            String groupName = group.name + "(" + lightGroups.size() + ")";
            TextView textView = (TextView)convertView.findViewById(R.id.subgroup_name);
            textView.setText(groupName);
            textView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    final PopupWindow popup = new PopupWindow(getActivity());
                    //设置弹出窗口的样式
                    View layout = getActivity().getLayoutInflater().inflate(R.layout.popup_scene_control, null);

                    popup.setContentView(layout);
                    // Set content width and height
                    popup.setHeight(WindowManager.LayoutParams.WRAP_CONTENT);
                    popup.setWidth(WindowManager.LayoutParams.MATCH_PARENT);
                    // Closes the popup window when touch outside of it - when looses focus
                    popup.setOutsideTouchable(true);
                    popup.setFocusable(true);

                    // Show anchored to button
                    popup.setBackgroundDrawable(new BitmapDrawable());
                    popup.showAsDropDown(v);
                    layout.findViewById(R.id.scene_control_edit).setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            popup.dismiss();
                            Intent intent = new Intent(getActivity(), GroupControlActivity.class);
                            intent.putExtra("subgroup_id", group.getId());
                            startActivity(intent);
                        }
                    });

                    //添加场景亮度调节
                    final SeekBar seekBar = (SeekBar)layout.findViewById(R.id.scene_bright_sb);
                    seekBar.setProgress(group.brightness);

                    BrightChangeListener brightChangeListener = new BrightChangeListener(group);
                    seekBar.setOnSeekBarChangeListener(brightChangeListener);

                    //添加设置场景亮度为0的事件
                    layout.findViewById(R.id.scene_power_switch).setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            seekBar.setProgress(0);
                        }
                    });

                    //发送整个分组的设置命令
//                    sendScenePackControl(group);
                }
            });
            ImageView detailIV = (ImageView)convertView.findViewById(R.id.subgroup_details);
            detailIV.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(getActivity(), LightListActivity.class);
                    intent.putExtra("subgroup_id", group.getId());
                    startActivity(intent);
                }
            });

            CheckBox checkBox = (CheckBox)convertView.findViewById(R.id.select_group_cb);
            checkBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    listView.setItemChecked(position,isChecked);
                }
            });
            if (group.getId() == 1) {
                //所有分组
                checkBox.setEnabled(false);
            }
            return convertView;
        }
    }

    public void deleteGroup() {
        ListView lv = (ListView)getActivity().findViewById(R.id.alllights_lv);
        LightGroupListAdapter groupListAdapter = (LightGroupListAdapter)lv.getAdapter();
        int count = lv.getCheckedItemCount();
        if (count != 0) {
            SparseBooleanArray checkedItemPositions = lv.getCheckedItemPositions();
            int length = lv.getCount();
            for(int i = 0;i< length; i++) {
                if (checkedItemPositions.get(i)) {
                    //该项被选中
                    SubGroup subGroup = groupListAdapter.getItem(i);
                    List<LightGroup> list = subGroup.lightGroups();
                    for(LightGroup temp : list) {
                        temp.delete();
                    }
                    groupListAdapter.remove(subGroup);
                    groupListAdapter.notifyDataSetChanged();
                    subGroup.delete();
                }
            }
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

    class BrightChangeListener implements SeekBar.OnSeekBarChangeListener {
        private boolean sendBright = true;
        private DataAgent dataAgent = XLightApplication.getInstance().getDataAgent();

        private Handler sendBrightHandler = new Handler();
        private Runnable sendBrightRunnable = new Runnable() {
            @Override
            public void run() {
                sendBright = true;
            }
        };
        private SubGroup psubGroup;
        private short[] stationIDs;
        private int stationCount;
        public BrightChangeListener(SubGroup subGroup) {
            this.psubGroup = subGroup;
            List<LightGroup> lightGroups = psubGroup.lightGroups();
            stationCount = lightGroups.size();
            stationIDs = new short[lightGroups.size()];
            for (int i = 0; i < stationIDs.length; i++) {
                stationIDs[i] = Short.parseShort(lightGroups.get(i).light.lightID);
            }
        }

        @Override
        public void onProgressChanged(SeekBar seekBar, final int progress, boolean fromUser) {
            if (sendBright) {
                Log.d(TAG, "亮度调节度 " + progress);
                sendBright = false;
                float temp = progress/100f;
                int brightValue = (int)Math.floor(temp*254f + 0.5);
                byte[] brights = new byte[stationCount];
                Arrays.fill(brights, (byte) brightValue);

                dataAgent.setMultiStationBrightness(getActivity(),stationIDs, brights, new ResultReceiver(new Handler()) {
                    @Override
                    protected void onReceiveResult(int resultCode, Bundle resultData) {
                        if (resultCode == Constants.COMMON.RESULT_CODE_OK) {
                            //读到了回应消息
                            byte[] msgBytes = resultData.getByteArray(Constants.KEYS_PARAMS.NETWORK_READED_BYTES_CONTENT);
                            short idShould = resultData.getShort(Constants.KEYS_PARAMS.MESSAGE_RANDOM_ID);
                            //解析回应消息
                            CommonMsgResponse msgResponse = null;
                            try {
                                msgResponse = MessageUtils.decomposeMultiStationBrightControlResponse(msgBytes, msgBytes.length, idShould);
                            } catch (Exception e) {
                                Log.w(TAG, String.format("消息解析出错 [%s]", ExceptionUtils.getFullStackTrace(e)));
                                Toast.makeText(getActivity(), "消息错误",Toast.LENGTH_LONG).show();
                                return;
                            }
                            //检测操作结果
                            if (msgResponse.getReturnCode() != CommonMsgResponse.RETURN_CODE_OK) {
                                Log.w(TAG, String.format("消息返回错误-[%s]", msgResponse.getReturnCode() + ""));
                                Toast.makeText(getActivity(), "出错啦", Toast.LENGTH_LONG).show();
                                return;
                            }
                            //设置正确
                            //调整数据库中该场景的亮度值
                            psubGroup.brightness = progress;
                            psubGroup.save();
                        } else {
                            Toast.makeText(getActivity(), "出错啦", Toast.LENGTH_SHORT).show();
                            Log.w(TAG, "错误码 " + resultCode);
                        }
                    }
                });
                //500毫秒之后再接收消息
                sendBrightHandler.postDelayed(sendBrightRunnable, 500);
            }

        }

        @Override
        public void onStartTrackingTouch(SeekBar seekBar) {

        }

        @Override
        public void onStopTrackingTouch(SeekBar seekBar) {

        }
    }

    class SubGroupComparator implements Comparator<SubGroup>{
        @Override
        public int compare(SubGroup lhs, SubGroup rhs) {
            return -lhs.getId().compareTo(rhs.getId());
        }
    }
}
