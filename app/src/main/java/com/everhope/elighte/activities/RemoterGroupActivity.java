package com.everhope.elighte.activities;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.ResultReceiver;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.util.SparseBooleanArray;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.everhope.elighte.R;
import com.everhope.elighte.XLightApplication;
import com.everhope.elighte.comm.DataAgent;
import com.everhope.elighte.constants.Constants;
import com.everhope.elighte.helpers.MessageUtils;
import com.everhope.elighte.models.CommonMsgResponse;
import com.everhope.elighte.models.Light;
import com.everhope.elighte.models.LightRemoter;
import com.everhope.elighte.models.Remoter;

import org.apache.commons.lang.exception.ExceptionUtils;

import java.util.ArrayList;
import java.util.List;

public class RemoterGroupActivity extends ActionBarActivity {

    private static final String TAG = "RemoterGroupActivity@Light";

    private short remoterID;
    private Remoter remoter;
    private int groupNum;
    private List<Light> lights;
    private List<LightRemoter> lightRemoters;
    private ListView lightListView;
    private LightListViewAdapter lightListViewAdapter;

    private static final int CHOOSE_LIGHT_REQUEST_CODE = 10;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_remoter_group);

        //设置参数
        Intent intent = getIntent();
        remoterID = intent.getShortExtra("remoter_id",(short)0);
        remoter = Remoter.getRemoterByID(remoterID + "");
        groupNum = intent.getIntExtra("remoter_gp_num", 1);

        setTitle("遥控器-分组");

        setStationsList();
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeButtonEnabled(true);
    }

    private void setStationsList() {
        Remoter remoter = Remoter.getRemoterByID(remoterID + "");
        this.lights = remoter.groupLights(groupNum + "");
        this.lightRemoters = remoter.groupLightRemoters(groupNum + "");

        lightListView = (ListView)findViewById(R.id.remoter_gp_lights_lv);
        lightListView.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE);
        lightListViewAdapter = new LightListViewAdapter(RemoterGroupActivity.this, lightRemoters);
        lightListViewAdapter.setNotifyOnChange(true);
        lightListView.setAdapter(lightListViewAdapter);
    }

    class LightListViewAdapter extends ArrayAdapter<LightRemoter> {
        public LightListViewAdapter(Context context, List<LightRemoter> lights) {
            super(context, 0, lights);
        }

        @Override
        public View getView(final int position, View convertView, ViewGroup parent) {
            if (convertView == null) {
                convertView = LayoutInflater.from(getContext()).inflate(R.layout.choose_light_item, parent, false);
            }
//            convertView = LayoutInflater.from(ChooseLightActivity.this).inflate(R.layout.choose_light_item, parent,false);
            LightRemoter lightRemoter = getItem(position);
            TextView textView = (TextView)convertView.findViewById(R.id.choose_light_name_tv);
            textView.setText(lightRemoter.light.name);
            if (lightRemoter.light.lostConnection) {
                ImageView imageView = (ImageView)convertView.findViewById(R.id.choose_light_icon);
                imageView.setImageResource(R.drawable.offline);
            }
            CheckBox checkBox = (CheckBox)convertView.findViewById(R.id.select_light_cb);
            checkBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    lightListView.setItemChecked(position,isChecked);
                }
            });

            return convertView;
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == CHOOSE_LIGHT_REQUEST_CODE && resultCode == Constants.COMMON.RESULT_CODE_OK) {
            long []ids = data.getLongArrayExtra("lights_selected_ids");

            //拿到选中的灯ID 进行绑定操作
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

            final short[] lightIds = new short[listIDs.size()];
            final List<Light> addListLights = new ArrayList<>();
            for(int i = 0; i < listIDs.size(); i++) {
                Light addLight = Light.load(Light.class, listIDs.get(i));
                lightIds[i] = Short.parseShort(addLight.lightID);
                addListLights.add(addLight);
            }
            //将选中的灯绑定到遥控器分组
            DataAgent dataAgent = XLightApplication.getInstance().getDataAgent();
            dataAgent.bindStationToRemoter(RemoterGroupActivity.this, remoterID,(byte)groupNum,lightIds, new ResultReceiver(new Handler()) {
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
                        } catch (Exception e) {
                            Log.w(TAG, String.format("消息解析出错 [%s]", ExceptionUtils.getFullStackTrace(e)));
                            Toast.makeText(RemoterGroupActivity.this, "消息错误", Toast.LENGTH_SHORT).show();
                            return;
                        }
                        //检测操作结果
                        if (msgResponse.getReturnCode() != CommonMsgResponse.RETURN_CODE_OK) {
                            Log.w(TAG, String.format("消息返回错误-[%s]", msgResponse.getReturnCode() + ""));
                            Toast.makeText(RemoterGroupActivity.this, "出错啦", Toast.LENGTH_LONG).show();
                            return;
                        }
                        //绑定正确 添加到数据库 和当前列表中

                        List<LightRemoter> lightRemoters = new ArrayList<LightRemoter>();
                        for(Light l : addListLights) {
                            LightRemoter lightRemoter = new LightRemoter();
                            lightRemoter.light = l;
                            lightRemoter.remoter = remoter;
                            lightRemoter.groupName = groupNum + "";
                            lightRemoter.save();
                            lightRemoters.add(lightRemoter);

                            //add to list
                            lightListViewAdapter.add(lightRemoter);
                            lightListViewAdapter.notifyDataSetChanged();
                        }
                    } else {
                        Toast.makeText(RemoterGroupActivity.this, "出错啦", Toast.LENGTH_SHORT).show();
                        Log.w(TAG, "错误码 " + resultCode);
                    }
                }
            });
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_remoter_group, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        switch (id) {
            case R.id.action_add_light_to_remoter:
                Intent intent = new Intent(RemoterGroupActivity.this, ChooseLightActivity.class);
                startActivityForResult(intent, CHOOSE_LIGHT_REQUEST_CODE);
                return true;
            case R.id.action_remote_light_from_remoter:
                //删除灯
                ListView listView = (ListView)findViewById(R.id.remoter_gp_lights_lv);
                LightListViewAdapter lightAdapter = (LightListViewAdapter)listView.getAdapter();
                SparseBooleanArray checked = listView.getCheckedItemPositions();
                String[] selectedIDs = new String[checked.size()];
                List<LightRemoter> lightsRemove = new ArrayList<>();
                int arrCount = 0;
                int length = listView.getCount();
                for(int i = 0;i< length; i++) {
                    if (checked.get(i)) {
                        //该项被选中
                        LightRemoter lightRemoter = lightAdapter.getItem(i);
                        selectedIDs[arrCount] = lightRemoter.light.lightID;
                        lightsRemove.add(lightRemoter);
                        arrCount++;
                    }
                }
                unbindLights(selectedIDs, lightsRemove);
                return true;
            case android.R.id.home:
                onBackPressed();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void unbindLights(String[] remIDs, final List<LightRemoter> lightRemotersRemove) {
        short[] lightsID = new short[remIDs.length];
        for(int i = 0; i < remIDs.length; i++) {
            lightsID[i] = Short.parseShort(remIDs[i]);
        }
        DataAgent dataAgent = XLightApplication.getInstance().getDataAgent();
        dataAgent.unbindStationFromRemoter(RemoterGroupActivity.this, remoterID, (byte)groupNum, lightsID, new ResultReceiver(new Handler()) {
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
                    } catch (Exception e) {
                        Log.w(TAG, String.format("消息解析出错 [%s]", ExceptionUtils.getFullStackTrace(e)));
                        Toast.makeText(RemoterGroupActivity.this, "消息错误", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    //检测操作结果
                    if (msgResponse.getReturnCode() != CommonMsgResponse.RETURN_CODE_OK) {
                        Log.w(TAG, String.format("消息返回错误-[%s]", msgResponse.getReturnCode() + ""));
                        Toast.makeText(RemoterGroupActivity.this, "出错啦", Toast.LENGTH_LONG).show();
                        return;
                    }
                    //解绑定正确 删除数据库 和当前列表中

                    List<LightRemoter> lightRemoters = new ArrayList<LightRemoter>();
                    for(LightRemoter l : lightRemotersRemove) {

                        //remove from list
                        lightListViewAdapter.remove(l);
                        lightListViewAdapter.notifyDataSetChanged();
                        l.delete();
                    }
                } else {
                    Toast.makeText(RemoterGroupActivity.this, "出错啦", Toast.LENGTH_SHORT).show();
                    Log.w(TAG, "错误码 " + resultCode);
                }
            }
        });
    }
}
