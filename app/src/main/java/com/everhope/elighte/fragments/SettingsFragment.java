package com.everhope.elighte.fragments;


import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.ResultReceiver;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.everhope.elighte.R;
import com.everhope.elighte.XLightApplication;
import com.everhope.elighte.activities.ChooseLightActivity;
import com.everhope.elighte.activities.DeleteDevicesActivity;
import com.everhope.elighte.comm.DataAgent;
import com.everhope.elighte.constants.Constants;
import com.everhope.elighte.helpers.MessageUtils;
import com.everhope.elighte.models.ClientLoginMsgResponse;
import com.everhope.elighte.models.CommonMsgResponse;
import com.everhope.elighte.models.GetAllStationsMsgResponse;
import com.everhope.elighte.models.Light;
import com.everhope.elighte.models.LightGroup;
import com.everhope.elighte.models.LightScene;
import com.everhope.elighte.models.StationObject;
import com.everhope.elighte.models.SubGroup;

import org.apache.commons.lang.exception.ExceptionUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * 设置
 */
public class SettingsFragment extends PreferenceFragment implements SharedPreferences.OnSharedPreferenceChangeListener {

    private static final String TAG = "SettingsFragment@Light";

    private static final int REQ_CODE_CHOOSE_DEV_DELETE = 10;

    private Handler handler;
    private static final int HANDLER_NO_NEWLIGHTS = 1;
    private static final int HANDLER_GOT_NEWLIGHTS = 2;
    private static final int HANDLER_ERROR = -1;

    private ProgressDialog progressDialog;
    private String mHelloSettings;

    /**
     *
     * @param paramHelloSettings
     * @return
     */
    public static SettingsFragment newInstance(String paramHelloSettings) {
        SettingsFragment fragment = new SettingsFragment();
        return fragment;
    }

    public SettingsFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.preference);

        this.handler = new Handler() {
            @Override
            public void handleMessage(Message msg) {
                int what = msg.what;
                switch (what) {
                    case HANDLER_GOT_NEWLIGHTS:
                        //获取到了新灯
                        progressDialog.dismiss();
                        int count = msg.arg1;
                        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity())
                                .setTitle("E-Lighte")
                                .setMessage(String.format("搜索到[%s]个新站点，请到[灯]页面中查看", count + ""))
                                .setPositiveButton("确定", null);
                        builder.create().show();
                        break;
                    case HANDLER_NO_NEWLIGHTS:
                        progressDialog.dismiss();
                        AlertDialog.Builder builderNo = new AlertDialog.Builder(getActivity())
                                .setTitle("E-Lighte")
                                .setMessage(String.format("未搜索到新站点"))
                                .setPositiveButton("确定", null);
                        builderNo.create().show();
                        break;
                    case HANDLER_ERROR:
                        progressDialog.dismiss();
                        break;
                    default:
                        progressDialog.dismiss();
                        break;
                }
            }
        };
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        PreferenceManager preferenceManager = getPreferenceManager();
        Preference preference;

        //添加事件响应
        preference = preferenceManager.findPreference("search_new_lights");
        preference.setOnPreferenceClickListener(new SearchNewLightsListener());

        preference = preferenceManager.findPreference("delete_devices");
        preference.setOnPreferenceClickListener(new DeleteDevicesListener());
        return super.onCreateView(inflater, container, savedInstanceState);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQ_CODE_CHOOSE_DEV_DELETE) {
            Toast.makeText(getActivity(), "ok", Toast.LENGTH_LONG).show();
        }
    }

    class DeleteDevicesListener implements Preference.OnPreferenceClickListener {
        @Override
        public boolean onPreferenceClick(Preference preference) {
            Intent intent = new Intent(getActivity(), DeleteDevicesActivity.class);
            //从所有分组中选择
            intent.putExtra("subgroup_id",1);
//            startActivityForResult(intent, REQ_CODE_CHOOSE_DEV_DELETE);
            startActivity(intent);
            return false;
        }
    }

    class SearchNewLightsListener implements Preference.OnPreferenceClickListener {
        @Override
        public boolean onPreferenceClick(Preference preference) {
            progressDialog = ProgressDialog.show(getActivity(), "E-Lighte","搜索一分钟，请耐心等待...",true);
            progressDialog.setCancelable(true);

            DataAgent dataAgent = XLightApplication.getInstance().getDataAgent();
            dataAgent.searchNewStations(getActivity(), Constants.SYSTEM_SETTINGS.SEARCH_STATIONS_LAST_SECONDS, new ResultReceiver(new Handler()) {
                @Override
                protected void onReceiveResult(int resultCode, Bundle resultData) {
                    //接收到回应消息
                    if (resultCode == Constants.COMMON.RESULT_CODE_OK) {
                        //解析消息
                        int bytesCount = resultData.getInt(Constants.KEYS_PARAMS.NETWORK_READED_BYTES_COUNT);
                        byte[] bytesData = resultData.getByteArray(Constants.KEYS_PARAMS.NETWORK_READED_BYTES_CONTENT);
                        short idShould = resultData.getShort(Constants.KEYS_PARAMS.MESSAGE_RANDOM_ID);

                        CommonMsgResponse searchStationsResponse = null;
                        try {
                            searchStationsResponse = MessageUtils.decomposeSearchStationsResponse(bytesData, bytesCount, idShould);
                        } catch (Exception e) {
                            progressDialog.dismiss();
                            Log.w(TAG, String.format("消息解析出错 [%s]", ExceptionUtils.getFullStackTrace(e)));
                            return;
                        }

                        Log.i(TAG, String.format("搜索站点回应消息 [%s]", searchStationsResponse.toString()));

                        if (searchStationsResponse.getReturnCode() == CommonMsgResponse.RETURN_CODE_OK) {
                            //获取所有站点与原站点进行比对 提示用户是否搜索到新站点
                            getAllLightsAndShow();
                        } else {
                            progressDialog.dismiss();
                            Log.w(TAG, String.format("消息返回错误 [%s]", searchStationsResponse.getReturnCode() + ""));
                        }

                    } else {
                        progressDialog.dismiss();
                        Toast.makeText(getActivity(), "网络错误", Toast.LENGTH_SHORT).show();
                    }
                }
            });

            return true;
        }
    }

    private void getAllLightsAndShow() {

        DataAgent dataAgent = XLightApplication.getInstance().getDataAgent();
        dataAgent.getAllLights(getActivity(), new ResultReceiver(new Handler()) {
            @Override
            protected void onReceiveResult(int resultCode, Bundle resultData) {
                if (resultCode == Constants.COMMON.RESULT_CODE_OK) {
                    //读到了回应消息
                    byte[] msgBytes = resultData.getByteArray(Constants.KEYS_PARAMS.NETWORK_READED_BYTES_CONTENT);
                    short idShould = resultData.getShort(Constants.KEYS_PARAMS.MESSAGE_RANDOM_ID);
                    //解析回应消息
                    GetAllStationsMsgResponse getAllStationsMsgResponse = null;
                    try {
                        getAllStationsMsgResponse = MessageUtils.decomposeGetAllStationsMsgResponse(msgBytes, msgBytes.length, idShould);
                    } catch (Exception e) {
                        progressDialog.dismiss();
                        Log.w(TAG, String.format("消息解析出错 [%s]", ExceptionUtils.getFullStackTrace(e)));
                        Toast.makeText(getActivity(), "消息错误",Toast.LENGTH_LONG).show();
                        return;
                    }
                    Log.i(TAG, "获取所有站点列表回应消息 " + getAllStationsMsgResponse.toString());
                    //取出当前数据库表中的所有站点进行比对
                    //新增 或 删除
                    List<Light> myLights = Light.getAll();
                    StationObject[] gateAllLights = getAllStationsMsgResponse.getStationObjects();
                    int length = gateAllLights.length;
                    List<Light> newLights = new ArrayList<Light>();

                    //判断新增
                    if (myLights.size() == 0) {
                        for (StationObject stationObject : gateAllLights) {
                            Light newLight = new Light();
                            newLight.lightID = stationObject.getId() + "";
                            newLight.name = "[" + stationObject.getMac() + "]";
                            newLight.lightMac = stationObject.getMac();
                            //新增的灯加入到未分组 组中
                            SubGroup ungroup = SubGroup.load(SubGroup.class, 1);
                            LightGroup lightGroup = new LightGroup();
                            lightGroup.light = newLight;
                            lightGroup.subgroup = ungroup;
                            newLight.save();
                            lightGroup.save();

                            newLights.add(newLight);
                        }
                    } else {
                        for (StationObject stationObject : gateAllLights) {
                            boolean add = true;

                            for (int j = 0; j < myLights.size(); j++) {

                                Light light = myLights.get(j);
                                if (light.lightMac.equals(stationObject.getMac())) {
                                    //已存在 不用新增
                                    add = false;
                                    light.lightID = stationObject.getId() + "";
                                    light.save();
                                    break;
                                }
                            }

                            if (add) {
                                //新增
                                Light newLight = new Light();
                                newLight.lightID = stationObject.getId() + "";
                                newLight.name = "[" + stationObject.getMac() + "]";
                                newLight.lightMac = stationObject.getMac();
                                //新增的灯加入到未分组 组中
                                SubGroup ungroup = SubGroup.load(SubGroup.class, 1);
                                LightGroup lightGroup = new LightGroup();
                                lightGroup.light = newLight;
                                lightGroup.subgroup = ungroup;
                                newLight.save();
                                lightGroup.save();

                                newLights.add(newLight);
                            }
                        }
                    }

                    //判断是否有新增灯
                    if (newLights.size() != 0) {
                        //有新增灯
                        Message message = new Message();
                        message.what = HANDLER_GOT_NEWLIGHTS;
                        message.arg1 = newLights.size();
                        handler.sendMessage(message);
                    } else {
                        //无新增灯
                        Message message = new Message();
                        message.what = HANDLER_NO_NEWLIGHTS;
                        handler.sendMessage(message);
                    }
                } else {
                    //出错
                    Log.e(TAG, String.format("获取所有站点列表出错 Code=[%s]", resultCode + ""));
                    handler.sendEmptyMessage(HANDLER_ERROR);
                }
            }
        });
    }

    @Override
    public void onResume() {
        super.onResume();
        getPreferenceScreen().getSharedPreferences().registerOnSharedPreferenceChangeListener(this);
    }

    @Override
    public void onPause() {
        super.onPause();
        getPreferenceScreen().getSharedPreferences().unregisterOnSharedPreferenceChangeListener(this);
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {

    }
}
