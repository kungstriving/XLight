package com.everhope.elighte.fragments;

import android.app.Fragment;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.ResultReceiver;
import android.support.annotation.Nullable;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Display;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.ScrollView;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.everhope.elighte.R;
import com.everhope.elighte.XLightApplication;
import com.everhope.elighte.activities.SceneEditActivity;
import com.everhope.elighte.comm.DataAgent;
import com.everhope.elighte.constants.Constants;
import com.everhope.elighte.helpers.MessageUtils;
import com.everhope.elighte.models.CommonMsgResponse;
import com.everhope.elighte.models.LightScene;
import com.everhope.elighte.models.Scene;

import org.apache.commons.lang.exception.ExceptionUtils;

import java.util.Arrays;
import java.util.List;

/**
 * 主页fragment
 * Created by kongxiaoyang on 2015/1/13.
 */
public class HomeFragment extends Fragment{

    private static final String TAG = "HomeFragment@Light";

    private ScrollView homeContentSV;

    public HomeFragment() {}

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_home, container, false);
        this.homeContentSV = (ScrollView)rootView;
        //获取屏幕宽度
        WindowManager windowManager = (WindowManager)getActivity().getSystemService(Context.WINDOW_SERVICE);
        DisplayMetrics displayMetrics = new DisplayMetrics();
        Display display = windowManager.getDefaultDisplay();
        display.getMetrics(displayMetrics);
        //px = dp * (dpi / 160)
        //计算每个操作块的大小
        int dpi = displayMetrics.densityDpi;
        int widthInPx = displayMetrics.widthPixels;

        int temp = 20 * (dpi/160);          //20dp margin=4*5dp
        int cellWidth = (widthInPx - temp)/3;

        //获取最外围container，纵向布局
        LinearLayout linearLayout = (LinearLayout)rootView.findViewById(R.id.home_frag_outerll);

        List<Scene> allScenes = getStoredSceneInfo();
        int positionInCurrentLine = 0;
        LinearLayout.LayoutParams currentLP =
                new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        LinearLayout currentLL = new LinearLayout(getActivity());
        currentLL.setOrientation(LinearLayout.HORIZONTAL);
        currentLP.setMargins(10,10,10,10);
        linearLayout.addView(currentLL, currentLP);

        for(int i = 0; i < allScenes.size(); i++) {
            Scene scene = allScenes.get(i);
            //判断当前行是不是已经有三个控制块
            if (positionInCurrentLine == 3) {
                currentLL = new LinearLayout(getActivity());
                currentLL.setOrientation(LinearLayout.HORIZONTAL);
                linearLayout.addView(currentLL, currentLP);
                positionInCurrentLine = 0;
            }

            //建立场景控制块布局和事件响应
            LinearLayout sceneLL = buildScenePane(scene, cellWidth);

            //添加到当前行
            LinearLayout.LayoutParams sceneLayoutParams = new LinearLayout.LayoutParams(cellWidth, cellWidth + 50);
            //每行共有20间隔
            sceneLayoutParams.setMargins(5,5,5,5);
            currentLL.addView(sceneLL, sceneLayoutParams);
            positionInCurrentLine++;
        }

        getActivity().setTitle(R.string.home_fragment_title);
        return rootView;
    }

    private LinearLayout buildScenePane(final Scene scene, int cellWidth) {
        //外围布局
        LinearLayout scenePane = new LinearLayout(getActivity());
        scenePane.setOrientation(LinearLayout.VERTICAL);

        //场景图片
        ImageView sceneImg = new ImageView(getActivity());
        final int sceneImgID = getResources().getIdentifier(scene.imgName, "drawable", getActivity().getPackageName());
        sceneImg.setImageResource(sceneImgID);
        //设置边框
        sceneImg.setBackgroundDrawable(getResources().getDrawable(R.drawable.scene_border));

        //设置图片点击事件
        sceneImg.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
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
                popup.showAsDropDown(v);

                //设置背景模糊
                int bgImgID = getResources().getIdentifier(scene.imgName + "_blur", "drawable", getActivity().getPackageName());
                homeContentSV.setBackgroundDrawable(getResources().getDrawable(bgImgID));
                layout.findViewById(R.id.scene_control_edit).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        popup.dismiss();
                        Intent intent = new Intent(getActivity(), SceneEditActivity.class);
                        intent.putExtra("scene_id", scene.getId());
                        startActivity(intent);
                    }
                });

                //添加场景亮度调节
                final SeekBar seekBar = (SeekBar)layout.findViewById(R.id.scene_bright_sb);
                seekBar.setProgress(scene.brightness);

                BrightChangeListener brightChangeListener = new BrightChangeListener(scene);
                seekBar.setOnSeekBarChangeListener(brightChangeListener);

                //添加设置场景开关事件
                ImageView imageView = (ImageView)layout.findViewById(R.id.scene_power_switch);
                imageView.setImageResource(scene.status == 1 ? R.drawable.light_off : R.drawable.light_on);
                imageView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        ImageView imageView = (ImageView)v;
                        if (scene.status == 1) {
                            //关闭 off
                            imageView.setImageResource(R.drawable.light_on);
                            scene.status = 0;
                            //发送off命令
                            sendSceneOnOffControl(scene, false);
//                            seekBar.setProgress(0);
                        } else {
                            //打开 on
                            imageView.setImageResource(R.drawable.light_off);
                            scene.status = 1;
                            seekBar.setProgress(scene.brightness);
                            //发送on命令
                            sendSceneOnOffControl(scene, true);
//                            sendSceneOnControl(scene);
                        }

                    }
                });

                //发送整个场景的设置命令
                sendScenePackControl(scene);

                //设置scene状态为开启
//                scene.status = 1;
            }
        });

        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(cellWidth-10, cellWidth-10);   //same width height
        scenePane.addView(sceneImg, layoutParams);

        //添加下方文字
        TextView sceneText = new TextView(getActivity());
        LinearLayout.LayoutParams textLayoutParams = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 50);
        sceneText.setText(scene.name);
        sceneText.setGravity(Gravity.CENTER_HORIZONTAL);

        scenePane.addView(sceneText, textLayoutParams);

        return scenePane;
    }

    /**
     * 发送场景off命令
     * @param scene
     */
    private void sendSceneOnOffControl(final Scene scene, boolean onoff) {
        //获取当前场景所有灯
        List<LightScene> lightScenes = scene.lightScenes();
        if (lightScenes.size() != 0) {
            int length = lightScenes.size();
            short[] ids = new short[length];
            for(int i = 0; i < length; i++) {
                LightScene lightScene = lightScenes.get(i);
                ids[i] = Short.parseShort(lightScene.light.lightID);
            }

            DataAgent dataAgent = XLightApplication.getInstance().getDataAgent();

            dataAgent.setLightsOnOff(getActivity(), ids, onoff, new ResultReceiver(new Handler()) {
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
                            Toast.makeText(getActivity(), "消息错误", Toast.LENGTH_LONG).show();
                            return;
                        }
                        //检测操作结果
                        if (msgResponse.getReturnCode() != CommonMsgResponse.RETURN_CODE_OK) {
                            Log.w(TAG, String.format("消息返回错误-[%s]", msgResponse.getReturnCode() + ""));
                            Toast.makeText(getActivity(), "出错啦", Toast.LENGTH_LONG).show();
                            return;
                        }

                        //操作成功 保存
                        scene.save();
                    } else {
                        Toast.makeText(getActivity(), "出错啦", Toast.LENGTH_SHORT).show();
                        Log.w(TAG, "错误码 " + resultCode);
                    }
                }
            });
        }
    }

    private void sendScenePackControl(Scene scene) {
        //获取当前场景的所有站点的id和颜色
        List<LightScene> lightScenes = scene.lightScenes();
        if (lightScenes.size() != 0) {
            int length = lightScenes.size();
            short[] ids = new short[length];
            int[] colors = new int[length];
            for(int i = 0; i < length; i++) {
                LightScene lightScene = lightScenes.get(i);
                ids[i] = Short.parseShort(lightScene.light.lightID);
                int r = lightScene.rColor;
                int g = lightScene.gColor;
                int b = lightScene.bColor;
                colors[i] = Color.rgb(r,g,b);
            }

            DataAgent dataAgent = XLightApplication.getInstance().getDataAgent();
            dataAgent.sendSceneControlCmd(getActivity(), ids, colors, new ResultReceiver(new Handler()) {
                @Override
                protected void onReceiveResult(int resultCode, Bundle resultData) {
                    if (resultCode == Constants.COMMON.RESULT_CODE_OK) {
                        //读到了回应消息
                        byte[] msgBytes = resultData.getByteArray(Constants.KEYS_PARAMS.NETWORK_READED_BYTES_CONTENT);
                        short idShould = resultData.getShort(Constants.KEYS_PARAMS.MESSAGE_RANDOM_ID);
                        //解析回应消息
                        CommonMsgResponse msgResponse = null;
                        try {
                            msgResponse = MessageUtils.decomposeCommonMsgResponse(msgBytes,msgBytes.length,idShould);
                            Log.i(TAG, String.format("场景控制命令返回-[%s]", msgResponse.toString()));
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
                    } else {
                        Toast.makeText(getActivity(), "出错啦", Toast.LENGTH_SHORT).show();
                        Log.w(TAG, "错误码 " + resultCode);
                    }
                }
            });
        }

    }

    /**
     * 获取存储scene信息
     * @return
     */
    private List<Scene> getStoredSceneInfo() {
        return Scene.getAll();
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
        private Scene pScene;
        private short[] stationIDs;
        private int stationCount;
        public BrightChangeListener(Scene scene) {
            this.pScene = scene;
            List<LightScene> lightScenes = scene.lightScenes();
            stationCount = lightScenes.size();
            stationIDs = new short[lightScenes.size()];
            for (int i = 0; i < stationIDs.length; i++) {
                stationIDs[i] = Short.parseShort(lightScenes.get(i).light.lightID);
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
                Arrays.fill(brights, (byte)brightValue);

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
                            pScene.brightness = progress;
                            pScene.save();
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
}
