package com.everhope.elighte.activities;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Handler;
import android.os.Message;
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
import com.everhope.elighte.helpers.AppUtils;
import com.everhope.elighte.helpers.MessageUtils;
import com.everhope.elighte.models.CommonMsgResponse;
import com.everhope.elighte.models.GetAllStationsMsgResponse;
import com.everhope.elighte.models.Light;
import com.everhope.elighte.models.LightGroup;
import com.everhope.elighte.models.LightScene;
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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_light_list);
        Intent intent = getIntent();

        //1 默认是所有灯分组
        long subGroupID = intent.getLongExtra("subgroup_id", 1);
        subGroup = SubGroup.load(SubGroup.class, subGroupID);
        setTitle(subGroup.name);
        lights = new ArrayList<>();
        List<LightGroup> lightGroupList = subGroup.lightGroups();
        for(LightGroup lightGroup : lightGroupList) {
            lights.add(lightGroup.light);
        }
        ListView listView = (ListView)findViewById(R.id.subgroup_lights_lv);
        final SubGroupLightsListViewAdapter subGroupLightsListViewAdapter =
                new SubGroupLightsListViewAdapter(LightListActivity.this, lights);
        subGroupLightsListViewAdapter.setNotifyOnChange(true);
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

        //添加返回按钮
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeButtonEnabled(true);

        handler = new Handler() {
            @Override
            public void handleMessage(Message msg) {
                switch (msg.what) {
                    case ADD_LIGHT_TO_LIST:
                        Light tmpLight = (Light)msg.obj;
                        subGroupLightsListViewAdapter.add(tmpLight);
                        subGroupLightsListViewAdapter.notifyDataSetChanged();
                        break;
                    default:
                        break;
                }
            }
        };

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
                message.obj = newLight;
                handler.sendMessage(message);
            }
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
        int id = item.getItemId();

        switch (id) {
            case R.id.action_lightlist_add:
                Intent intent = new Intent(LightListActivity.this, ChooseLightActivity.class);
                //默认从所有灯分组选取
                intent.putExtra("subgroup_id",1);
                startActivityForResult(intent, REQUEST_CODE_CHOOSE_LIGHTS);
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
