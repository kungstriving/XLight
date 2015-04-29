package com.everhope.elighte.activities;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Handler;
import android.os.ResultReceiver;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.text.InputFilter;
import android.util.Log;
import android.util.SparseBooleanArray;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
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
import com.everhope.elighte.models.LightGroup;
import com.everhope.elighte.models.LightRemoter;
import com.everhope.elighte.models.LightScene;
import com.everhope.elighte.models.Remoter;
import com.everhope.elighte.models.SubGroup;

import org.apache.commons.lang.exception.ExceptionUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * 删除设备
 */
public class DeleteDevicesActivity extends ActionBarActivity {

    private static final String TAG = "DeleteDevicesActivity@Light";

    private ProgressDialog progressDialog;
    private ListView lightListView;
    private ChooseLightsListViewAdapter chooseLightsListViewAdapter;
    private int lastCheckedPos = -1;
    private CheckBox lastCheckBox = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_delete_devices);

        //获取所传入的分组
        Intent intent = getIntent();
        // 1是默认是所有灯分组
        int groupID = intent.getIntExtra("subgroup_id", 1);
        SubGroup subGroup = SubGroup.load(SubGroup.class, groupID);
        List<Light> lights = new ArrayList<>();
        List<LightGroup> lightGroupList = subGroup.lightGroups();
        for(LightGroup lightGroup : lightGroupList) {
            lights.add(lightGroup.light);
        }
        //添加遥控器 模拟灯
        List<Remoter> remoters = Remoter.getAll();
        for(Remoter remoter : remoters) {
            Light temp = new Light();
            temp.lightID = remoter.remoterID;
            temp.name = "[遥控]" + remoter.name;
            lights.add(temp);
        }

        lightListView = (ListView)findViewById(R.id.delete_lights_lv);
//        lightListView.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE);
        lightListView.setChoiceMode(ListView.CHOICE_MODE_SINGLE);
        chooseLightsListViewAdapter = new ChooseLightsListViewAdapter(DeleteDevicesActivity.this, lights);
        chooseLightsListViewAdapter.setNotifyOnChange(true);
        lightListView.setAdapter(chooseLightsListViewAdapter);

//        lightListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
//            @Override
//            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
//                boolean selected = lightListView.isItemChecked(position);
//                lightListView.setItemChecked(position, selected);
//                if (selected) {
//                    view.setBackgroundColor(getResources().getColor(R.color.goldenrod));
//                } else {
//                    view.setBackgroundColor(getResources().getColor(R.color.whitesmoke));
//                }
//            }
//        });

        setTitle("请选择设备");

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeButtonEnabled(true);
    }

    class ChooseLightsListViewAdapter extends ArrayAdapter<Light> {
        public ChooseLightsListViewAdapter(Context context, List<Light> lights) {
            super(context, 0, lights);
        }

        @Override
        public View getView(final int position, View convertView, ViewGroup parent) {
            if (convertView == null) {
                convertView = LayoutInflater.from(getContext()).inflate(R.layout.choose_light_item, parent, false);
            }

            Light light = getItem(position);
            TextView textView = (TextView)convertView.findViewById(R.id.choose_light_name_tv);
            textView.setFilters(new InputFilter[] { new InputFilter.LengthFilter(25) });
            textView.setText(light.name);

            if (light.lostConnection) {
                ImageView imageView = (ImageView)convertView.findViewById(R.id.choose_light_icon);
                imageView.setImageResource(R.drawable.offline);
            }
            final CheckBox checkBox = (CheckBox)convertView.findViewById(R.id.select_light_cb);
            checkBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    lightListView.setItemChecked(position,isChecked);
                    if (isChecked) {
                        if (lastCheckedPos != -1) {
                            lastCheckBox.setChecked(false);
                        }
                        lastCheckedPos = position;
                        lastCheckBox = checkBox;
                    }
                }
            });
            return convertView;
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_delete_devices, menu);
        return true;
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        switch (id) {
            case R.id.action_delete_devs:
                //删除设备

                //获取当前选中的灯ID
                ListView listView = (ListView)findViewById(R.id.delete_lights_lv);
                ChooseLightsListViewAdapter lightAdapter = (ChooseLightsListViewAdapter)listView.getAdapter();
//                final long[] selectedIDs = new long[1];
                final Light[] selectedLights = new Light[1];
                int pos = listView.getCheckedItemPosition();
                final Light light = lightAdapter.getItem(pos);
//                selectedIDs[0] = light.getId();
                selectedLights[0] = light;

//
//                SparseBooleanArray checked = listView.getCheckedItemPositions();
//
//                int arrCount = 0;
//                int length = listView.getCount();
//                for(int i = 0;i< length; i++) {
//                    if (checked.get(i)) {
//                        //该项被选中
//                        Light light = lightAdapter.getItem(i);
//                        selectedIDs[arrCount] = light.getId();
//                        selectedLights[arrCount] = light;
//                        arrCount++;
//                    }
//                }
                if (selectedLights.length != 0) {
                    if (light.lostConnection || light.name.startsWith("[遥控]")) {
                        AlertDialog.Builder builder = new AlertDialog.Builder(DeleteDevicesActivity.this)
                                .setTitle("确认")
                                .setMessage("当前设备处于失联状态，是否确定强制删除？（强制删除可能造成设备不可用，请谨慎使用）")
                                .setNegativeButton("取消",null)
                                .setPositiveButton("确定", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        progressDialog = ProgressDialog.show(DeleteDevicesActivity.this, "E-Lighte", "删除中...", true);
                                        progressDialog.setCancelable(true);
                                        deleteStationForce(selectedLights[0]);
                                    }
                                });
                        builder.create().show();
                    } else {
                        AlertDialog.Builder builder = new AlertDialog.Builder(DeleteDevicesActivity.this)
                                .setTitle("确认")
                                .setMessage("是否确定删除该设备？")
                                .setNegativeButton("取消",null)
                                .setPositiveButton("确定", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        progressDialog = ProgressDialog.show(DeleteDevicesActivity.this, "E-Lighte", "删除中...", true);
                                        progressDialog.setCancelable(true);

                                        deleteStation(selectedLights[0]);
                                    }
                                });
                        builder.create().show();

                    }
                } else {
                    Toast.makeText(DeleteDevicesActivity.this, "请选择要删除的设备",Toast.LENGTH_LONG).show();
                }

                return true;
            case android.R.id.home:
                onBackPressed();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void deleteStationForce(final Light deleteLight) {
        short stationID = Short.parseShort(deleteLight.lightID);
        //目前只支持删除一个站点
        DataAgent dataAgent = XLightApplication.getInstance().getDataAgent();
        dataAgent.deleteStationForce(DeleteDevicesActivity.this, stationID, new ResultReceiver(new Handler()) {
            @Override
            protected void onReceiveResult(int resultCode, Bundle resultData) {
                //接收到回应消息
                if (resultCode == Constants.COMMON.RESULT_CODE_OK) {
                    //解析消息
                    int bytesCount = resultData.getInt(Constants.KEYS_PARAMS.NETWORK_READED_BYTES_COUNT);
                    byte[] bytesData = resultData.getByteArray(Constants.KEYS_PARAMS.NETWORK_READED_BYTES_CONTENT);
                    short idShould = resultData.getShort(Constants.KEYS_PARAMS.MESSAGE_RANDOM_ID);

                    CommonMsgResponse deleteStationForceResponse = null;
                    try {
                        deleteStationForceResponse = MessageUtils.decomposeCommonMsgResponse(bytesData, bytesCount, idShould);
                    } catch (Exception e) {
                        progressDialog.dismiss();
                        Log.w(TAG, String.format("消息解析出错 [%s]", ExceptionUtils.getFullStackTrace(e)));
                        return;
                    }

                    Log.i(TAG, String.format("强制删除站点回应消息 [%s]", deleteStationForceResponse.toString()));

                    if (deleteStationForceResponse.getReturnCode() == CommonMsgResponse.RETURN_CODE_OK) {
                        if (deleteLight.name.startsWith("[遥控]")) {
                            //当前要删除的是遥控器
                            Remoter remoter = Remoter.getRemoterByID(deleteLight.lightID);
                            unbindRemoter(remoter, deleteLight);
//                            Toast.makeText(DeleteDevicesActivity.this, "删除遥控器", Toast.LENGTH_LONG).show();
                        } else {
                            //删除与该灯关联的场景和分组
                            List<LightScene> lightScenes = deleteLight.lightScenes();
                            for(LightScene lightScene : lightScenes) {
                                lightScene.delete();
                            }
                            List<LightGroup> lightGroups = deleteLight.lightGroups();
                            for(LightGroup lightGroup : lightGroups) {
                                lightGroup.delete();
                            }
                            deleteLight.delete();
                            chooseLightsListViewAdapter.remove(deleteLight);
                            chooseLightsListViewAdapter.notifyDataSetChanged();

                            progressDialog.dismiss();
                        }

                    } else {
                        progressDialog.dismiss();
                        Log.w(TAG, String.format("消息返回错误 [%s]", deleteStationForceResponse.getReturnCode() + ""));
                    }

                } else {
                    progressDialog.dismiss();
                    Toast.makeText(DeleteDevicesActivity.this, "网络错误", Toast.LENGTH_SHORT).show();
                }
            }
        });

    }

    private void unbindRemoter(final Remoter remoter, final Light deleteLight) {
        final List<LightRemoter> lightRemoters = remoter.groupLightRemoters("1");
        short[] lightsID = new short[lightRemoters.size()];
        String[] remIDs = new String[lightsID.length];
        short remoterID = Short.parseShort(remoter.remoterID);
        int groupNum = 1;
        for(int i = 0; i < remIDs.length; i++) {
            lightsID[i] = Short.parseShort(lightRemoters.get(i).light.lightID);
        }
        DataAgent dataAgent = XLightApplication.getInstance().getDataAgent();
        dataAgent.unbindStationFromRemoter(DeleteDevicesActivity.this, remoterID, (byte) groupNum, lightsID, new ResultReceiver(new Handler()) {
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
                        Toast.makeText(DeleteDevicesActivity.this, "消息错误", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    //检测操作结果
                    if (msgResponse.getReturnCode() != CommonMsgResponse.RETURN_CODE_OK) {
                        Log.w(TAG, String.format("消息返回错误-[%s]", msgResponse.getReturnCode() + ""));
                        Toast.makeText(DeleteDevicesActivity.this, "出错啦", Toast.LENGTH_LONG).show();
                        return;
                    }
                    //解绑定正确 删除数据库 和当前列表中

                    for (LightRemoter l : lightRemoters) {

                        //remove from list
                        l.delete();
                    }
                    remoter.delete();

                    chooseLightsListViewAdapter.remove(deleteLight);
                    chooseLightsListViewAdapter.notifyDataSetChanged();
                } else {
                    Toast.makeText(DeleteDevicesActivity.this, "出错啦", Toast.LENGTH_SHORT).show();
                    Log.w(TAG, "错误码 " + resultCode);
                }
            }
        });
    }

    private void deleteStation(final Light deleteLight) {
        short stationID = Short.parseShort(deleteLight.lightID);
        //目前只支持删除一个站点
        DataAgent dataAgent = XLightApplication.getInstance().getDataAgent();
        dataAgent.deleteStation(DeleteDevicesActivity.this, stationID, new ResultReceiver(new Handler()) {
            @Override
            protected void onReceiveResult(int resultCode, Bundle resultData) {
                //接收到回应消息
                if (resultCode == Constants.COMMON.RESULT_CODE_OK) {
                    //解析消息
                    int bytesCount = resultData.getInt(Constants.KEYS_PARAMS.NETWORK_READED_BYTES_COUNT);
                    byte[] bytesData = resultData.getByteArray(Constants.KEYS_PARAMS.NETWORK_READED_BYTES_CONTENT);
                    short idShould = resultData.getShort(Constants.KEYS_PARAMS.MESSAGE_RANDOM_ID);

                    CommonMsgResponse deleteStationResponse = null;
                    try {
                        deleteStationResponse = MessageUtils.decomposeSearchStationsResponse(bytesData, bytesCount, idShould);
                    } catch (Exception e) {
                        progressDialog.dismiss();
                        Log.w(TAG, String.format("消息解析出错 [%s]", ExceptionUtils.getFullStackTrace(e)));
                        return;
                    }

                    Log.i(TAG, String.format("删除站点回应消息 [%s]", deleteStationResponse.toString()));

                    if (deleteStationResponse.getReturnCode() == CommonMsgResponse.RETURN_CODE_OK) {
                        if (deleteLight.name.startsWith("[遥控]")) {
                            //当前要删除的是遥控器
                            Remoter remoter = Remoter.getRemoterByID(deleteLight.lightID);
                            unbindRemoter(remoter, deleteLight);
//                            Toast.makeText(DeleteDevicesActivity.this, "删除遥控器", Toast.LENGTH_LONG).show();
                        } else {
                            //删除与该灯关联的场景和分组
                            List<LightScene> lightScenes = deleteLight.lightScenes();
                            for(LightScene lightScene : lightScenes) {
                                lightScene.delete();
                            }
                            List<LightGroup> lightGroups = deleteLight.lightGroups();
                            for(LightGroup lightGroup : lightGroups) {
                                lightGroup.delete();
                            }
                            deleteLight.delete();

                            chooseLightsListViewAdapter.remove(deleteLight);
                            chooseLightsListViewAdapter.notifyDataSetChanged();
                        }

                        progressDialog.dismiss();
                    } else {
                        progressDialog.dismiss();
                        Log.w(TAG, String.format("消息返回错误 [%s]", deleteStationResponse.getReturnCode() + ""));
                    }

                } else {
                    progressDialog.dismiss();
                    Toast.makeText(DeleteDevicesActivity.this, "网络错误", Toast.LENGTH_SHORT).show();
                }
            }
        });

    }
}
