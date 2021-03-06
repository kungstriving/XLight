package com.everhope.elighte.activities;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.ResultReceiver;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.util.SparseBooleanArray;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.PopupWindow;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.everhope.elighte.R;
import com.everhope.elighte.XLightApplication;
import com.everhope.elighte.comm.DataAgent;
import com.everhope.elighte.constants.Constants;
import com.everhope.elighte.helpers.AppUtils;
import com.everhope.elighte.helpers.MessageUtils;
import com.everhope.elighte.models.CommonMsgResponse;
import com.everhope.elighte.models.Light;
import com.everhope.elighte.models.LightGroup;
import com.everhope.elighte.models.SubGroup;

import org.apache.commons.lang.exception.ExceptionUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * 显示一个分组的所有灯
 */
public class LightListActivity extends ActionBarActivity {

    private static final String TAG = "LightListActivity@Light";
    /**
     * 选择灯的标志 用来打开子活动
     */
    private static final int REQUEST_CODE_CHOOSE_LIGHTS = 1;

    private static final int ADD_LIGHT_TO_LIST = 1;

    private List<Light> lights;
    private SubGroup subGroup;
    private Handler handler;
    private ProgressDialog progressDialog;
    private ListView listView;
    private long subGroupID = 1;
    private SubGroupLightsListViewAdapter subGroupLightsListViewAdapter;
    private Handler checkHandler;
    private final int DELAY = 5000;

    private Runnable checkRunnable = new Runnable() {
        @Override
        public void run() {
            List<LightGroup> lightGroupList = subGroup.lightGroups();

//            listView = (ListView)findViewById(R.id.subgroup_lights_lv);

//            subGroupLightsListViewAdapter =
//                    new SubGroupLightsListViewAdapter(LightListActivity.this, lightGroupList);
//            subGroupLightsListViewAdapter.setNotifyOnChange(true);
//            listView.setAdapter(subGroupLightsListViewAdapter);
//            listView.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE);

            subGroupLightsListViewAdapter.clear();
//            subGroupLightsListViewAdapter.getView()
//            int count = subGroupLightsListViewAdapter.getCount();
//            for(int i = 0; i < count; i++) {
//                LightGroup tempGroup = subGroupLightsListViewAdapter.getItem(i);
//                for(LightGroup nowLight : lightGroupList) {
//                    if (tempGroup.light.lightMac.equals(nowLight.light.lightMac)) {
//                        tempGroup.light.lostConnection = nowLight.light.lostConnection;
//                    }
//                }
//            }
            for(LightGroup groupLight : lightGroupList) {
                subGroupLightsListViewAdapter.insert(groupLight,subGroupLightsListViewAdapter.getCount());
            }
//            subGroupLightsListViewAdapter.addAll(lightGroupList);
//            subGroupLightsListViewAdapter.notifyDataSetInvalidated();
            subGroupLightsListViewAdapter.notifyDataSetChanged();


            checkHandler.postDelayed(checkRunnable, DELAY);
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_light_list);
        Intent intent = getIntent();

        //1 默认是所有灯分组
        subGroupID = intent.getLongExtra("subgroup_id", 1);
        subGroup = SubGroup.load(SubGroup.class, subGroupID);
        setTitle(subGroup.name);
        lights = new ArrayList<>();
        List<LightGroup> lightGroupList = subGroup.lightGroups();
        for(LightGroup lightGroup : lightGroupList) {
            lights.add(lightGroup.light);
        }
        listView = (ListView)findViewById(R.id.subgroup_lights_lv);
        subGroupLightsListViewAdapter =
                new SubGroupLightsListViewAdapter(LightListActivity.this, lightGroupList);
        subGroupLightsListViewAdapter.setNotifyOnChange(true);
        listView.setAdapter(subGroupLightsListViewAdapter);
        listView.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE);

        //添加返回按钮
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeButtonEnabled(true);

        handler = new Handler() {
            @Override
            public void handleMessage(Message msg) {
                switch (msg.what) {
                    case ADD_LIGHT_TO_LIST:
                        LightGroup tmpLight = (LightGroup)msg.obj;
                        subGroupLightsListViewAdapter.add(tmpLight);

                        subGroupLightsListViewAdapter.notifyDataSetChanged();
                        break;
                    default:
                        break;
                }
            }
        };

        checkHandler = new Handler();

        checkRunnable.run();
    }



    class RenameLightCancelListener implements DialogInterface.OnClickListener {

        private Light light;
        public RenameLightCancelListener(Light light) {
            this.light = light;
        }
        @Override
        public void onClick(DialogInterface dialog, int which) {
            exitStationId();
        }

        private void exitStationId() {
            //退出站点识别
            DataAgent dataAgent = XLightApplication.getInstance().getDataAgent();
            dataAgent.exitStationIdentify(LightListActivity.this, this.light.lightID, new ResultReceiver(new Handler()) {
                @Override
                protected void onReceiveResult(int resultCode, Bundle resultData) {
                    if (resultCode == Constants.COMMON.RESULT_CODE_OK) {
                        //读到了回应消息
                        byte[] msgBytes = resultData.getByteArray(Constants.KEYS_PARAMS.NETWORK_READED_BYTES_CONTENT);
                        short idShould = resultData.getShort(Constants.KEYS_PARAMS.MESSAGE_RANDOM_ID);
                        //解析回应消息
                        CommonMsgResponse exitStationIdReturnMsg = null;
                        try {
                            exitStationIdReturnMsg = MessageUtils.decomposeExitStationIdReturnMsg(msgBytes, msgBytes.length, idShould);
                        } catch (Exception e) {
                            Log.w(TAG, String.format("消息解析出错 [%s]", ExceptionUtils.getFullStackTrace(e)));
                            Toast.makeText(LightListActivity.this, "消息错误",Toast.LENGTH_LONG).show();
                            return;
                        }
                        if (exitStationIdReturnMsg.getReturnCode() != CommonMsgResponse.RETURN_CODE_OK) {
                            Log.w(TAG, String.format("消息返回错误-[%s]", exitStationIdReturnMsg.getReturnCode() + ""));
                            return;
                        }

                        Log.i(TAG, String.format("退出站点识别-回应消息[%s]", exitStationIdReturnMsg.toString()));
                    } else {
                        Toast.makeText(LightListActivity.this, "出错啦", Toast.LENGTH_SHORT).show();
                        Log.w(TAG, "错误码 " + resultCode);
                    }
                }
            });
        }
    }

    class RenameLightListener implements DialogInterface.OnClickListener {

        private Light light;
        private View view;
        private EditText newNameET;
        public RenameLightListener(Light light, View lightView, EditText editText) {
            this.light = light;
            this.view = lightView;
            this.newNameET = editText;
        }
        @Override
        public void onClick(DialogInterface dialog, int which) {
            //修改view的名称
            String newName = this.newNameET.getText().toString();

            TextView textView = (TextView)view.findViewById(R.id.light_name_tv);
            textView.setText(newName);
            //存储Light
            this.light.name = newName;
            this.light.save();
            exitStationId();
        }

        private void exitStationId() {
            //退出站点识别
            DataAgent dataAgent = XLightApplication.getInstance().getDataAgent();
            dataAgent.exitStationIdentify(LightListActivity.this, this.light.lightID, new ResultReceiver(new Handler()) {
                @Override
                protected void onReceiveResult(int resultCode, Bundle resultData) {
                    if (resultCode == Constants.COMMON.RESULT_CODE_OK) {
                        //读到了回应消息
                        byte[] msgBytes = resultData.getByteArray(Constants.KEYS_PARAMS.NETWORK_READED_BYTES_CONTENT);
                        short isShould = resultData.getShort(Constants.KEYS_PARAMS.MESSAGE_RANDOM_ID);
                        //解析回应消息
                        CommonMsgResponse exitStationIdReturnMsg = null;
                        try {
                            exitStationIdReturnMsg = MessageUtils.decomposeExitStationIdReturnMsg(msgBytes, msgBytes.length, isShould);
                        } catch (Exception e) {
                            Log.w(TAG, String.format("消息解析出错 [%s]", ExceptionUtils.getFullStackTrace(e)));
                            Toast.makeText(LightListActivity.this, "消息错误",Toast.LENGTH_LONG).show();
                            return;
                        }
                        if (exitStationIdReturnMsg.getReturnCode() != CommonMsgResponse.RETURN_CODE_OK) {
                            Log.w(TAG, String.format("消息返回错误-[%s]", exitStationIdReturnMsg.getReturnCode() + ""));
                            return;
                        }

                        Log.i(TAG, String.format("退出站点识别-回应消息[%s]", exitStationIdReturnMsg.toString()));
                    } else {
                        Toast.makeText(LightListActivity.this, "出错啦", Toast.LENGTH_SHORT).show();
                        Log.w(TAG, "错误码 " + resultCode);
                    }
                }
            });
        }
    }

    class SubGroupLightsListViewAdapter extends ArrayAdapter<LightGroup> {
        public SubGroupLightsListViewAdapter(Context context, List<LightGroup> lights) {
            super(context, 0, lights);
        }

        @Override
        public View getView(final int position, View convertView, ViewGroup parent) {
            if (convertView == null) {
                convertView = LayoutInflater.from(getContext()).inflate(R.layout.light_item, parent, false);

                final View lightView = convertView;
                final LightGroup lightGroup = getItem(position);
                TextView textView = (TextView)convertView.findViewById(R.id.light_name_tv);
                textView.setText(lightGroup.light.name);
                if (lightGroup.light.lostConnection) {
                    ImageView imageView = (ImageView)convertView.findViewById(R.id.light_status_iv);
                    imageView.setImageResource(R.drawable.offline);
                }

                ImageView editIV = (ImageView)convertView.findViewById(R.id.light_rename);
                editIV.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        progressDialog = ProgressDialog.show(LightListActivity.this, Constants.SYSTEM_SETTINGS.ELIGHTE,"",true);
                        progressDialog.setCancelable(true);
                        //进入站点识别状态
//                        final LightGroup lightGroup = subGroupLightsListViewAdapter.getItem(position);
                        if (lightGroup.light.lostConnection) {
                            Toast.makeText(LightListActivity.this, "该灯当前不可用",Toast.LENGTH_LONG).show();
                            progressDialog.dismiss();
                            return;
                        }
                        String lightID = lightGroup.light.lightID;
                        final Light light = lightGroup.light;
                        DataAgent dataAgent = XLightApplication.getInstance().getDataAgent();
                        dataAgent.enterStationIdentify(LightListActivity.this, new ResultReceiver(new Handler()) {
                            @Override
                            protected void onReceiveResult(int resultCode, Bundle resultData) {
                                progressDialog.dismiss();
                                if (resultCode == Constants.COMMON.RESULT_CODE_OK) {
                                    //读到了回应消息
                                    byte[] msgBytes = resultData.getByteArray(Constants.KEYS_PARAMS.NETWORK_READED_BYTES_CONTENT);
                                    short isShould = resultData.getShort(Constants.KEYS_PARAMS.MESSAGE_RANDOM_ID);
                                    //解析回应消息
                                    CommonMsgResponse enterStationIdReturnMsg = null;
                                    try {
                                        enterStationIdReturnMsg = MessageUtils.decomposeEnterStationIdReturnMsg(msgBytes, msgBytes.length, isShould);
                                    } catch (Exception e) {
                                        Log.w(TAG, String.format("消息解析出错 [%s]", ExceptionUtils.getFullStackTrace(e)));
                                        Toast.makeText(LightListActivity.this, "消息错误",Toast.LENGTH_LONG).show();
                                        return;
                                    }

                                    if (enterStationIdReturnMsg.getReturnCode() != CommonMsgResponse.RETURN_CODE_OK) {
                                        Log.w(TAG, String.format("消息返回错误[%s]", enterStationIdReturnMsg.getReturnCode() + ""));
                                        Toast.makeText(LightListActivity.this, AppUtils.getErrorInfo(enterStationIdReturnMsg.getReturnCode() + ""), Toast.LENGTH_LONG).show();
                                        return;
                                    }

                                    Log.i(TAG, "进入站点识别--回应消息 " + enterStationIdReturnMsg.toString());

                                    //弹出对话框进行输入
                                    EditText editText = new EditText(LightListActivity.this);

                                    RenameLightListener renameLightListener = new RenameLightListener(light,lightView,editText);
                                    RenameLightCancelListener renameLightCancelListener = new RenameLightCancelListener(light);
                                    AlertDialog.Builder builder = new AlertDialog.Builder(LightListActivity.this).setTitle("重命名").setIcon(android.R.drawable.ic_menu_edit)
                                            .setView(editText)
                                            .setPositiveButton("确定", renameLightListener)
                                            .setNegativeButton("取消", renameLightCancelListener);
                                    AlertDialog alertDialog = builder.create();
                                    alertDialog.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE);
                                    alertDialog.show();

                                    editText.setFocusable(true);
                                    editText.requestFocus();
                                } else {
                                    Toast.makeText(LightListActivity.this, "出错啦", Toast.LENGTH_SHORT).show();
                                    Log.w(TAG, "错误码 " + resultCode);
                                }
                            }
                        }, lightID);
                    }
                });
                textView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        final PopupWindow popup = new PopupWindow(LightListActivity.this);
                        //设置弹出窗口的样式
                        View layout = LightListActivity.this.getLayoutInflater().inflate(R.layout.popup_scene_control, null);

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
                                Intent intent = new Intent(LightListActivity.this, GroupControlActivity.class);
                                intent.putExtra("subgroup_id", -1);
                                intent.putExtra("single_light_id", lightGroup.light.lightID);
                                startActivity(intent);
                            }
                        });

                        //添加灯亮度调节
                        final SeekBar seekBar = (SeekBar)layout.findViewById(R.id.scene_bright_sb);
                        seekBar.setProgress(lightGroup.light.brightness);

                        BrightChangeListener brightChangeListener = new BrightChangeListener(lightGroup.light);
                        seekBar.setOnSeekBarChangeListener(brightChangeListener);

                        //添加设置单灯开关事件
                        ImageView imageView = (ImageView)layout.findViewById(R.id.scene_power_switch);
                        imageView.setImageResource(lightGroup.light.switchOn ? R.drawable.light_on : R.drawable.light_off);
                        imageView.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                ImageView imageView = (ImageView)v;
                                if (lightGroup.light.switchOn) {
                                    //关闭 off
                                    imageView.setImageResource(R.drawable.light_off);
                                    lightGroup.light.switchOn = false;
                                    //发送off命令
                                    sendSceneOnOffControl(lightGroup.light, false);
//                            seekBar.setProgress(0);
                                } else {
                                    //打开 on
                                    imageView.setImageResource(R.drawable.light_on);
                                    lightGroup.light.switchOn = true;
                                    seekBar.setProgress(lightGroup.light.brightness);
                                    //发送on命令
                                    sendSceneOnOffControl(lightGroup.light, true);
//                            sendSceneOnControl(scene);
                                }

                            }
                        });

                        //添加设置场景亮度为0的事件
//                        layout.findViewById(R.id.scene_power_switch).setOnClickListener(new View.OnClickListener() {
//                            @Override
//                            public void onClick(View v) {
//                                seekBar.setProgress(0);
//                            }
//                        });

                        //发送整个分组的设置命令
//                    sendScenePackControl(group);
                    }
                });

                CheckBox checkBox = (CheckBox)convertView.findViewById(R.id.select_light_item_cb);
                checkBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                    @Override
                    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                        listView.setItemChecked(position,isChecked);
                    }
                });
            }
            return convertView;
        }
    }

    /**
     * 发送场景off命令
     * @param light
     */
    private void sendSceneOnOffControl(final Light light, boolean onoff) {
        //获取当前场景所有灯
        if (light != null) {
            short[] ids = new short[1];
            ids[0] = Short.parseShort(light.lightID);

            DataAgent dataAgent = XLightApplication.getInstance().getDataAgent();

            dataAgent.setLightsOnOff(LightListActivity.this, ids, onoff, new ResultReceiver(new Handler()) {
                @Override
                protected void onReceiveResult(int resultCode, Bundle resultData) {
                    if (resultCode == Constants.COMMON.RESULT_CODE_OK) {
                        //读到了回应消息
                        byte[] msgBytes = resultData.getByteArray(Constants.KEYS_PARAMS.NETWORK_READED_BYTES_CONTENT);
                        short idShould = resultData.getShort(Constants.KEYS_PARAMS.MESSAGE_RANDOM_ID);
                        //解析回应消息
                        CommonMsgResponse msgResponse = null;
                        try {
                            msgResponse = MessageUtils.decomposeCommonMsgResponse(msgBytes, msgBytes.length, idShould);
                            Log.i(TAG, String.format("场景开关命令返回-[%s]", msgResponse.toString()));
                        } catch (Exception e) {
                            Log.w(TAG, String.format("消息解析出错 [%s]", ExceptionUtils.getFullStackTrace(e)));
                            Toast.makeText(LightListActivity.this, "消息错误", Toast.LENGTH_LONG).show();
                            return;
                        }
                        //检测操作结果
                        if (msgResponse.getReturnCode() != CommonMsgResponse.RETURN_CODE_OK) {
                            Log.w(TAG, String.format("消息返回错误-[%s]", msgResponse.getReturnCode() + ""));
                            Toast.makeText(LightListActivity.this, "出错啦", Toast.LENGTH_LONG).show();
                            return;
                        }

                        //操作成功 保存
                        light.save();
                    } else {
                        Toast.makeText(LightListActivity.this, "出错啦", Toast.LENGTH_SHORT).show();
                        Log.w(TAG, "错误码 " + resultCode);
                    }
                }
            });
        }
    }

    class BrightChangeListener implements SeekBar.OnSeekBarChangeListener {
        private boolean sendBright = true;
        private DataAgent dataAgent = XLightApplication.getInstance().getDataAgent();
        private Light brightLight;
        private Handler sendBrightHandler = new Handler();
        private Runnable sendBrightRunnable = new Runnable() {
            @Override
            public void run() {
                sendBright = true;
            }
        };

        private short[] stationIDs;
        private int stationCount;
        private int MIN_PROGRESS = 2;

        public BrightChangeListener(Light light) {
            stationCount = 1;
            stationIDs = new short[1];
            stationIDs[0] = Short.parseShort(light.lightID);
            this.brightLight = light;
        }

        @Override
        public void onProgressChanged(SeekBar seekBar, final int progress, boolean fromUser) {
            if (sendBright) {
                Log.d(TAG, "亮度调节度 " + progress);
                sendBright = false;
                sendBrightCmd(progress);
                //500毫秒之后再接收消息
                sendBrightHandler.postDelayed(sendBrightRunnable, 500);
            }
        }

        @Override
        public void onStartTrackingTouch(SeekBar seekBar) {

        }

        @Override
        public void onStopTrackingTouch(SeekBar seekBar) {
            sendBrightCmd(seekBar.getProgress());
        }

        private void sendBrightCmd( int progress) {
            if (progress < MIN_PROGRESS) {
                progress = MIN_PROGRESS;
            }
            final int realProgress = progress;
            float temp = progress/100f;
            int brightValue = (int)Math.floor(temp*254f + 0.5);
            byte[] brights = new byte[stationCount];
            Arrays.fill(brights, (byte) brightValue);

            dataAgent.setMultiStationBrightness(LightListActivity.this,stationIDs, brights, new ResultReceiver(new Handler()) {
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
                            Toast.makeText(LightListActivity.this, "消息错误",Toast.LENGTH_LONG).show();
                            return;
                        }
                        //检测操作结果
                        if (msgResponse.getReturnCode() != CommonMsgResponse.RETURN_CODE_OK) {
                            Log.w(TAG, String.format("消息返回错误-[%s]", msgResponse.getReturnCode() + ""));
                            Toast.makeText(LightListActivity.this, "出错啦", Toast.LENGTH_LONG).show();
                            return;
                        }
                        //设置正确
                        //调整数据库中该场景的亮度值
                        brightLight.brightness = realProgress;
                        brightLight.save();
                    } else {
                        Toast.makeText(LightListActivity.this, "出错啦", Toast.LENGTH_SHORT).show();
                        Log.w(TAG, "错误码 " + resultCode);
                    }
                }
            });
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_CODE_CHOOSE_LIGHTS && resultCode == Constants.COMMON.RESULT_CODE_OK) {
            long []ids = data.getLongArrayExtra("lights_selected_ids");

            List<Long> listIDs = new ArrayList<>();
            for(int i = 0; i < ids.length; i++) {
                listIDs.add(ids[i]);
            }

            for(Light light : lights) {
                Long id = light.getId();
                if (listIDs.contains(id)) {
                    listIDs.remove(id);
                }
            }

            //将选中的灯加入到该分组中
            for(Long newID : listIDs) {
                Light newLight = Light.load(Light.class, newID);
                LightGroup newLightGroup = new LightGroup();
                newLightGroup.light = newLight;
                newLightGroup.subgroup = this.subGroup;
                newLightGroup.save();
                //
                Message message = new Message();
                message.what = ADD_LIGHT_TO_LIST;
                message.obj = newLightGroup;
                handler.sendMessage(message);
                lights.add(newLight);
            }
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        checkHandler.removeCallbacks(checkRunnable);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_light_list, menu);
        if (subGroupID == 1) {
            menu.findItem(R.id.action_lightlist_add).setVisible(false);
            menu.findItem(R.id.action_remove_light_from_group).setVisible(false);
        }
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        switch (id) {
            case R.id.action_lightlist_add:
                Intent intent = new Intent(LightListActivity.this, ChooseLightActivity.class);
                //默认从所有灯分组选取
                intent.putExtra("subgroup_id",1);
                long[] rmIDs = new long[lights.size()];
                for (int i = 0; i < rmIDs.length; i++) {
                    rmIDs[i] = Long.parseLong(lights.get(i).lightID);
                }
                intent.putExtra("filter_rm_ids", rmIDs);
                startActivityForResult(intent, REQUEST_CODE_CHOOSE_LIGHTS);
                return true;
            case R.id.action_remove_light_from_group:
                //删除灯
                ListView listView = (ListView)findViewById(R.id.subgroup_lights_lv);
                SubGroupLightsListViewAdapter lightAdapter = (SubGroupLightsListViewAdapter)listView.getAdapter();
                SparseBooleanArray checked = listView.getCheckedItemPositions();
//                String[] selectedIDs = new String[checked.size()];
//                List<LightGroup> lightsRemove = new ArrayList<>();
//                int arrCount = 0;
                int length = listView.getCount();
                for(int i = 0;i< length; i++) {
                    if (checked.get(i)) {
                        //该项被选中
                        LightGroup lightGroup = lightAdapter.getItem(i);
                        lightAdapter.remove(lightGroup);
                        lightAdapter.notifyDataSetChanged();

                        lightGroup.delete();
//                        selectedIDs[arrCount] = lightGroup.light.lightID;
//                        lightsRemove.add(lightGroup);
//                        arrCount++;
                    }
                }
                return true;
            case android.R.id.home:
                //按压actionbar中的回退按钮
                onBackPressed();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

}
