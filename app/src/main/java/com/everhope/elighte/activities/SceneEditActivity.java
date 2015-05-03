package com.everhope.elighte.activities;

import android.app.AlertDialog;
import android.content.ClipData;
import android.content.ClipDescription;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Point;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.ResultReceiver;
import android.support.v7.app.ActionBarActivity;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Display;
import android.view.DragEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.PopupWindow;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.everhope.elighte.R;
import com.everhope.elighte.XLightApplication;
import com.everhope.elighte.comm.DataAgent;
import com.everhope.elighte.constants.Constants;
import com.everhope.elighte.helpers.AppUtils;
import com.everhope.elighte.helpers.MessageUtils;
import com.everhope.elighte.models.CommonMsgResponse;
import com.everhope.elighte.models.Light;
import com.everhope.elighte.models.LightScene;
import com.everhope.elighte.models.Scene;
import com.everhope.elighte.models.SubGroup;

import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang.exception.ExceptionUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * 场景编辑Activity
 * 可以增加场景中的灯，可以对场景中的灯调节颜色等
 */
public class SceneEditActivity extends ActionBarActivity {

    private static final String TAG = "SceneEditActivity@Light";

    /**
     * 选择灯的标志 用来打开子活动
     */
    private final int REQUEST_CODE_CHOOSE_LIGHTS = 1;
    //选色板竖直向的偏移量
    private int colorPickerYOffset;
    //顶部logo图片
    private ImageView topIV;
    //顶部图片设置handler
    private Handler topIVHandler;
    //选色板布局器
//    private RelativeLayout colorPickerRL;
    private FrameLayout colorPickerFL;
    //选色板图片
    private Bitmap colorPickerBitmap;
    //选色板图片宽度
    private int colorPickerWidth;
    //选色板图片高度
    private int colorPickerHeight;
    //缩放比率
    private float scale;

    //当前场景-灯关联对象列表
    private List<LightScene> lightScenes;
    //当前场景
    private Scene scene;
    //是否已经加载过界面的标志
    private boolean loaded = false;
    //是否向网关发送颜色消息
    private boolean sendColor = true;
    //场景名称
    private String title = "";
    //当前操作的灯
    private LightScene currentLightScene;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_scene_edit);

        //获取数据库存储场景信息
        Intent intent = getIntent();
        long sceneID = intent.getLongExtra("scene_id",-1);
        if (sceneID != -1) {
            //正常情况
            this.scene = Scene.load(Scene.class, sceneID);
            this.lightScenes = scene.lightScenes();
            setTitle(scene.name);
            this.title = scene.name;
        } else {
            Toast.makeText(SceneEditActivity.this, "出错啦", Toast.LENGTH_SHORT).show();
        }
        final Handler sendColorHandler = new Handler();
        final Runnable timerRunnable = new Runnable() {
            @Override
            public void run() {
                sendColor = true;
//                sendColorHandler.postDelayed(this, 400);
            }
        };
        //设置顶部底色变化
        this.topIVHandler = new Handler() {
            @Override
            public void handleMessage(Message msg) {

                //颜色值
                final int colorInt = msg.what;
                short lightID = (short)msg.arg1;
                final byte[] hsb = AppUtils.rgbColorValueToHSB(colorInt);
                DataAgent dataAgent = XLightApplication.getInstance().getDataAgent();

                //发送消息
                if (sendColor) {
                    //发送颜色
                    sendColor = false;
                    dataAgent.setStationColor(SceneEditActivity.this, lightID, hsb, new ResultReceiver(new Handler()) {
                        @Override
                        protected void onReceiveResult(int resultCode, Bundle resultData) {
                            if (resultCode == Constants.COMMON.RESULT_CODE_OK) {
                                //读到了回应消息
                                byte[] msgBytes = resultData.getByteArray(Constants.KEYS_PARAMS.NETWORK_READED_BYTES_CONTENT);
                                short idShould = resultData.getShort(Constants.KEYS_PARAMS.MESSAGE_RANDOM_ID);
                                //解析回应消息
                                CommonMsgResponse msgResponse = null;
                                try {
                                    msgResponse = MessageUtils.decomposeStationColorControlMsg(msgBytes, msgBytes.length, idShould);
                                } catch (Exception e) {
                                    Log.w(TAG, String.format("消息解析出错 [%s]", ExceptionUtils.getFullStackTrace(e)));
                                    Toast.makeText(SceneEditActivity.this, "消息错误",Toast.LENGTH_LONG).show();
                                    return;
                                }
                                if (msgResponse.getReturnCode() != CommonMsgResponse.RETURN_CODE_OK) {
                                    Log.w(TAG, String.format("消息返回错误-[%s]", AppUtils.getErrorInfo(msgResponse.getReturnCode() + "")));
                                    return;
                                }
                                Log.i(TAG, "设置颜色 " + Arrays.toString(hsb));
                                //设置颜色正常
                                topIV.setBackgroundColor(colorInt);
                            } else {
                                Toast.makeText(SceneEditActivity.this, "出错啦", Toast.LENGTH_SHORT).show();
                                Log.w(TAG, "错误码 " + resultCode);
                            }
                        }
                    });
                    //颜色发送成功后，开始计时
                    sendColorHandler.postDelayed(timerRunnable, 400);
                }
            }
        };

        //添加返回按钮
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeButtonEnabled(true);
    }

    @Override
    protected void onPause() {
        super.onPause();

        //持久化到数据库相关数据
        for(LightScene lightScene : lightScenes) {
            lightScene.save();
        }
    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);

        if (!hasFocus || loaded) {
            //失去焦点的时候或者已经加载过的 则直接返回
            return;
        }

        RelativeLayout rootRL = (RelativeLayout) findViewById(R.id.se_root);
        //获取屏幕高宽
        WindowManager windowManager = (WindowManager) getSystemService(Context.WINDOW_SERVICE);
        DisplayMetrics displayMetrics = new DisplayMetrics();
        Display display = windowManager.getDefaultDisplay();
        display.getMetrics(displayMetrics);

        int widthPixels = displayMetrics.widthPixels;     //高宽一致
        int rootHeight = rootRL.getHeight();

        //设置顶部
        int heightTop = rootHeight - widthPixels;

        this.topIV = new ImageView(SceneEditActivity.this);
        ImageView imageView = topIV;
        imageView.setId(AppUtils.generateViewId());
        imageView.setBackgroundColor(getResources().getColor(R.color.orange));
        imageView.setImageResource(R.drawable.scene_edit_top_bg);
        imageView.setScaleType(ImageView.ScaleType.FIT_XY);

        RelativeLayout.LayoutParams ivLayoutParams = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, heightTop);
        ivLayoutParams.addRule(RelativeLayout.ALIGN_PARENT_TOP);
        ivLayoutParams.addRule(RelativeLayout.ALIGN_PARENT_LEFT);
        rootRL.addView(imageView, ivLayoutParams);

        //设置colorpicker
        this.colorPickerFL = new FrameLayout(SceneEditActivity.this);
        int bgImgID = getResources().getIdentifier(scene.imgName, "drawable", getPackageName());
        colorPickerFL.setBackgroundDrawable(getResources().getDrawable(bgImgID));
        RelativeLayout.LayoutParams pickerLP = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, widthPixels);
        pickerLP.addRule(RelativeLayout.BELOW, imageView.getId());
        LightDragListener lightDragListener = new LightDragListener();
        colorPickerFL.setOnDragListener(lightDragListener);
        //计算图片缩放比率
        this.scale = 539 / (float)widthPixels;        //539为实际图片像素数

        //light handle 根据数据库lights数量
        for(int i = 0; i<lightScenes.size(); i++) {
            LightScene lightScene = lightScenes.get(i);
            final Light light = lightScene.light;
            final ImageView lightIV = new ImageView(SceneEditActivity.this);
            lightIV.setTag(i+"");
            lightIV.setImageResource(R.drawable.light_icon);
            if (light.lostConnection) {
//                lightIV.setBackgroundColor(getResources().getColor(R.color.lightgray));
                lightIV.setBackgroundDrawable(getResources().getDrawable(R.drawable.light_offline_border));
            } else {
                lightIV.setOnTouchListener(new View.OnTouchListener() {

                    @Override
                    public boolean onTouch(View v, MotionEvent event) {

                        ClipData.Item item = new ClipData.Item((CharSequence) v.getTag());

                        String[] mimeTypes = {ClipDescription.MIMETYPE_TEXT_PLAIN};
                        ClipData dragData = new ClipData(v.getTag().toString(),
                                mimeTypes, item);

                        // 实例化拖动阴影
                        View.DragShadowBuilder lightShadow = new MyDragShadowBuilder(lightIV);

                        v.startDrag(dragData, lightShadow, v, 0);
                        v.setVisibility(View.INVISIBLE);
                        return true;
                    }
                });
            }

            FrameLayout.LayoutParams lightLP = new FrameLayout.LayoutParams(80, 80);
            lightLP.leftMargin = lightScene.x;
            lightLP.topMargin = lightScene.y;

            colorPickerFL.addView(lightIV, lightLP);
        }

        rootRL.addView(colorPickerFL, pickerLP);

        //计算偏移量
        this.colorPickerYOffset = heightTop + getSupportActionBar().getHeight();
        //计算colorpicker高宽
        this.colorPickerBitmap = ((BitmapDrawable)this.colorPickerFL.getBackground()).getBitmap();
        this.colorPickerWidth = colorPickerBitmap.getWidth();
        this.colorPickerHeight = colorPickerBitmap.getHeight();

        //设置加载标志
        loaded = true;
    }

    class LightDragListener implements View.OnDragListener {

        @Override
        public boolean onDrag(View v, DragEvent event) {

            int action = event.getAction();

            View view = (View)event.getLocalState();        //light-icon view
            int lightIndex = Integer.parseInt(view.getTag().toString());
            LightScene lightScene = lightScenes.get(lightIndex);
            currentLightScene = lightScene;
            final String lightID = lightScene.light.lightID;
            String lightName = lightScene.light.name;
            switch (action) {
                case DragEvent.ACTION_DRAG_STARTED:

                    setTitle(title + " - " + lightName);
                    return true;
                case DragEvent.ACTION_DRAG_ENTERED:
                    return true;
                case DragEvent.ACTION_DRAG_LOCATION:
                    int x = (int)(event.getX());
                    int y = (int)(event.getY());
                    //bitmap.getPixel() 只能取高宽范围各减少一个像素的范围
                    if (x > colorPickerWidth - 1) {
                        x = colorPickerWidth - 1;
                    }
                    if (y > colorPickerHeight - 1) {
                        y = colorPickerHeight - 1;
                    }
                    int clrPix = colorPickerBitmap.getPixel(x, y);
                    //发送变色信号
//                    topIVHandler.sendEmptyMessage(clrPix);
                    Message colorMsg = new Message();
                    colorMsg.what = clrPix;
                    colorMsg.arg1 = Integer.parseInt(lightID);
                    topIVHandler.sendMessage(colorMsg);

                    return true;
                case DragEvent.ACTION_DRAG_EXITED:
                    FrameLayout.LayoutParams lp = (FrameLayout.LayoutParams)view.getLayoutParams();
                    //事件触发坐标在shadowbuilder中发生了偏移
                    int ex = (int)(event.getX() - 40);
                    int ey = (int)(event.getY() - 80);
                    int tempWidth = colorPickerWidth - 80;
                    if (ey > tempWidth) {
                        ey = tempWidth;
                    }
                    if (ey < colorPickerYOffset) {
                        ey = 0;
                    }
                    lp.leftMargin = ex;
                    lp.topMargin = ey;
                    view.setLayoutParams(lp);
                    view.setVisibility(View.VISIBLE);
                    return true;
                case DragEvent.ACTION_DROP:
                    FrameLayout.LayoutParams vlp = (FrameLayout.LayoutParams)view.getLayoutParams();
                    int dx = (int)(event.getX() - 40);
                    int dy = (int)(event.getY() - 80);
                    int dropWidth = colorPickerWidth - 80;
                    if (dy > dropWidth) {
                        dy = dropWidth;
                    }
                    if (dy < 0) {
                        dy = 0;
                    }
                    vlp.leftMargin = dx;
                    vlp.topMargin = dy;
                    view.setLayoutParams(vlp);
                    view.setVisibility(View.VISIBLE);
                    //记录位置
                    lightScene.x = dx;
                    lightScene.y = dy;
                    //记录颜色值
                    x = (int)(event.getX());
                    y = (int)(event.getY());
                    //bitmap.getPixel() 只能取高宽范围各减少一个像素的范围
                    if (x > colorPickerWidth - 1) {
                        x = colorPickerWidth - 1;
                    }
                    if (y > colorPickerHeight - 1) {
                        y = colorPickerHeight - 1;
                    }
                    final int dropClrPix = colorPickerBitmap.getPixel(x, y);
                    lightScene.rColor = Color.red(dropClrPix);
                    lightScene.gColor = Color.green(dropClrPix);
                    lightScene.bColor = Color.blue(dropClrPix);

                    //拖动结束前再进行一次发送 确保最终看到的结果一致
                    new Handler().postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            Message lastColorMsg = new Message();
                            lastColorMsg.what = dropClrPix;
                            lastColorMsg.arg1 = Integer.parseInt(lightID);
                            topIVHandler.sendMessage(lastColorMsg);
                        }
                    }, 500);

                    return true;
                case DragEvent.ACTION_DRAG_ENDED:
                    return true;
                default:
                    break;
            }
            return false;
        }
    }

    private static class MyDragShadowBuilder extends View.DragShadowBuilder {

        // The drag shadow image, defined as a drawable thing
        private static Drawable shadow;

        // Defines the constructor for myDragShadowBuilder
        public MyDragShadowBuilder(View v) {

            // Stores the View parameter passed to myDragShadowBuilder.
            super(v);

            Bitmap light = BitmapFactory.decodeResource(v.getResources(), R.drawable.light_icon);
            shadow = new BitmapDrawable(v.getResources(), light);
        }

        // Defines a callback that sends the drag shadow dimensions and touch point back to the
        // system.
        @Override
        public void onProvideShadowMetrics(Point size, Point touch) {

            // Defines local variables
            int width, height;

            // Sets the width of the shadow to half the width of the original View
            width = getView().getWidth() * 2;

            // Sets the height of the shadow to half the height of the original View
            height = getView().getHeight() * 2;

            // The drag shadow is a ColorDrawable. This sets its dimensions to be the same as the
            // Canvas that the system will provide. As a result, the drag shadow will fill the
            // Canvas.
            shadow.setBounds(0, 0, width, height);

            // Sets the size parameter's width and height values. These get back to the system
            // through the size parameter.
            size.set(width, height);

            // Sets the touch point's position to be in the middle of the drag shadow
            touch.set(width / 2, height);
//            touch.set(0,0);
        }

        // Defines a callback that draws the drag shadow in a Canvas that the system constructs
        // from the dimensions passed in onProvideShadowMetrics().
        @Override
        public void onDrawShadow(Canvas canvas) {
            // Draws the ColorDrawable in the Canvas passed in from the system.
            shadow.draw(canvas);
        }

    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_CODE_CHOOSE_LIGHTS && resultCode == Constants.COMMON.RESULT_CODE_OK) {
            long []ids = data.getLongArrayExtra("lights_selected_ids");

            List<Long> listIDs = new ArrayList<>();
            for(int i = 0; i < ids.length; i++) {
                listIDs.add(ids[i]);
            }

            for(LightScene lightScene : lightScenes) {
                Long id = lightScene.light.getId();
                if (listIDs.contains(id)) {
                    listIDs.remove(id);
                }
            }

            //将选中的灯加入到该场景中
            int count = 1;
            for(Long newID : listIDs) {
                Light newLight = Light.load(Light.class, newID);
                LightScene newLightScene = new LightScene();
                newLightScene.light = newLight;
                newLightScene.x = 5*count;
                newLightScene.y = 5*count;
                newLightScene.scene = this.scene;
                newLightScene.save();
                count++;
            }

            this.lightScenes = this.scene.lightScenes();
            loaded = false;
            this.onWindowFocusChanged(true);
//            Toast.makeText(SceneEditActivity.this, Arrays.toString(ids), Toast.LENGTH_LONG).show();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_scene_edit, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        switch (id) {
            case R.id.action_sceneedit_add:

                //获取所有分组
                List<SubGroup> subGroups = SubGroup.getAll();
                String[] groupNames = new String[subGroups.size()];
                final long[] groupIDs = new long[subGroups.size()];
                for(int i = 0; i < subGroups.size(); i++) {
                    groupNames[i] = subGroups.get(i).name;
                    groupIDs[i] = subGroups.get(i).getId();
                }
                new AlertDialog.Builder(this).setTitle("灯分组").setIcon(
                        android.R.drawable.ic_dialog_info).setSingleChoiceItems(
                            groupNames, -1,
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                long id = groupIDs[which];
                                Intent intent = new Intent(SceneEditActivity.this, ChooseLightActivity.class);
                                //默认从所有灯分组中选取灯
                                intent.putExtra("subgroup_id",id);
                                startActivityForResult(intent, REQUEST_CODE_CHOOSE_LIGHTS);
                                dialog.dismiss();
                            }
                        }).show();


                return true;
            case R.id.action_sceneedit_delete:
                //从场景中删除灯
                if (currentLightScene != null) {
                    currentLightScene.delete();
                    this.lightScenes = this.scene.lightScenes();
                    loaded = false;
                    this.onWindowFocusChanged(true);
                    setTitle(scene.name);
                } else {
                    Toast.makeText(SceneEditActivity.this, "请选择要删除的灯", Toast.LENGTH_LONG).show();
                }
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }

    }
}
