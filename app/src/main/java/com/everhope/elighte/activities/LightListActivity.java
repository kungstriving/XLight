package com.everhope.elighte.activities;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Handler;
import android.os.ResultReceiver;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.everhope.elighte.R;
import com.everhope.elighte.XLightApplication;
import com.everhope.elighte.comm.DataAgent;
import com.everhope.elighte.constants.Constants;
import com.everhope.elighte.helpers.MessageUtils;
import com.everhope.elighte.models.CommonMsgResponse;
import com.everhope.elighte.models.GetAllStationsMsgResponse;
import com.everhope.elighte.models.Light;
import com.everhope.elighte.models.LightGroup;
import com.everhope.elighte.models.SubGroup;

import java.util.ArrayList;
import java.util.List;

/**
 * 显示一个分组的所有灯
 */
public class LightListActivity extends ActionBarActivity {

    private static final String TAG = "LightListActivity@Light";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_light_list);
        Intent intent = getIntent();

        //1 默认是所有灯分组
        long subGroupID = intent.getLongExtra("subgroup_id", 1);
        SubGroup subGroup = SubGroup.load(SubGroup.class, subGroupID);
        setTitle(subGroup.name);
        List<Light> lights = new ArrayList<>();
        List<LightGroup> lightGroupList = subGroup.lightGroups();
        for(LightGroup lightGroup : lightGroupList) {
            lights.add(lightGroup.light);
        }
        ListView listView = (ListView)findViewById(R.id.subgroup_lights_lv);
        final SubGroupLightsListViewAdapter subGroupLightsListViewAdapter =
                new SubGroupLightsListViewAdapter(LightListActivity.this, lights);
        listView.setAdapter(subGroupLightsListViewAdapter);

        //设置列表点击事件
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, final View view, int position, long id) {
                //进入站点识别状态
                final Light light = subGroupLightsListViewAdapter.getItem(position);
                String lightID = light.lightID;

                DataAgent dataAgent = XLightApplication.getInstance().getDataAgent();
                dataAgent.enterStationIdentify(LightListActivity.this, new ResultReceiver(new Handler()) {
                    @Override
                    protected void onReceiveResult(int resultCode, Bundle resultData) {
                        if (resultCode == Constants.COMMON.RESULT_CODE_OK) {
                            //读到了回应消息
                            byte[] msgBytes = resultData.getByteArray(Constants.KEYS_PARAMS.NETWORK_READED_BYTES_CONTENT);
                            //解析回应消息
                            CommonMsgResponse enterStationIdReturnMsg = MessageUtils.decomposeEnterStationIdReturnMsg(msgBytes, msgBytes.length);
                            //检测消息ID
                            short msgRandID = resultData.getShort(Constants.KEYS_PARAMS.MESSAGE_RANDOM_ID);
                            if (enterStationIdReturnMsg.getMessageID() != msgRandID) {
                                Log.w(TAG, "消息ID不匹配");
                                return;
                            }
                            if (enterStationIdReturnMsg.getReturnCode() != CommonMsgResponse.RETURN_CODE_OK) {
                                Log.w(TAG, String.format("消息返回错误[%s]", enterStationIdReturnMsg.getReturnCode() + ""));
                                return;
                            }

                            Log.i(TAG, "进入站点识别--回应消息 " + enterStationIdReturnMsg.toString());

                            //弹出对话框进行输入
                            EditText editText = new EditText(LightListActivity.this);

                            RenameLightListener renameLightListener = new RenameLightListener(light,view,editText);
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

//                Toast.makeText(LightListActivity.this, "light id" + light.lightID, Toast.LENGTH_LONG).show();
            }
        });
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
                        //解析回应消息
                        CommonMsgResponse exitStationIdReturnMsg = MessageUtils.decomposeExitStationIdReturnMsg(msgBytes, msgBytes.length);
                        //检测消息ID
                        short msgRandID = resultData.getShort(Constants.KEYS_PARAMS.MESSAGE_RANDOM_ID);
                        if (exitStationIdReturnMsg.getMessageID() != msgRandID) {
                            Log.w(TAG, "消息ID不匹配");
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
                        //解析回应消息
                        CommonMsgResponse exitStationIdReturnMsg = MessageUtils.decomposeExitStationIdReturnMsg(msgBytes, msgBytes.length);
                        //检测消息ID
                        short msgRandID = resultData.getShort(Constants.KEYS_PARAMS.MESSAGE_RANDOM_ID);
                        if (exitStationIdReturnMsg.getMessageID() != msgRandID) {
                            Log.w(TAG, "消息ID不匹配");
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

    class SubGroupLightsListViewAdapter extends ArrayAdapter<Light> {
        public SubGroupLightsListViewAdapter(Context context, List<Light> lights) {
            super(context, 0, lights);
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            if (convertView == null) {
                convertView = LayoutInflater.from(getContext()).inflate(R.layout.light_item, parent, false);
            }

            Light light = getItem(position);
            TextView textView = (TextView)convertView.findViewById(R.id.light_name_tv);
            textView.setText(light.name);
            return convertView;
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_light_list, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
