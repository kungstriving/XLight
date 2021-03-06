package com.everhope.elighte.activities;

import android.content.ClipData;
import android.content.ClipDescription;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Point;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Handler;
import android.os.Message;
import android.os.ResultReceiver;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
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
import com.everhope.elighte.models.LightGroup;
import com.everhope.elighte.models.LightScene;
import com.everhope.elighte.models.Scene;
import com.everhope.elighte.models.SubGroup;

import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang.exception.ExceptionUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class GroupControlActivity extends ActionBarActivity {

    private static final String TAG = "GroupControlActivity@Light";

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

    //当前分组
    private SubGroup subGroup;
    private List<LightGroup> lightGroups;
    private short[] ids;
    //是否已经加载过界面的标志
    private boolean loaded = false;
    //是否向网关发送颜色消息
    private boolean sendColor = true;
    //分组名称
    private String title = "";

    private long groupID = 0;

    private Light light;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_group_control);

        //获取数据库存储分组信息
        Intent intent = getIntent();
        //默认获取所有分组
        groupID = intent.getLongExtra("subgroup_id",-1);
        if (groupID != -1) {
            //正常情况
            this.subGroup = SubGroup.load(SubGroup.class, groupID);
            setTitle(this.subGroup.name);
            this.lightGroups = this.subGroup.lightGroups();
            this.title = this.subGroup.name;
            this.ids = new short[this.lightGroups.size()];
            for(int i = 0; i < this.ids.length; i++) {
                this.ids[i] = Short.parseShort(this.lightGroups.get(i).light.lightID);
            }
            setTitle(this.subGroup.name);
        } else {
            //groupID = -1 为单灯操作情况
            this.ids = new short[1];
            short lightID = Short.parseShort(intent.getStringExtra("single_light_id"));
            this.light = Light.getByLightID(lightID + "");

            this.ids[0] = lightID;
            this.subGroup = new SubGroup();
            this.lightGroups = new ArrayList<>();
            this.lightGroups.add(new LightGroup());
            setTitle("灯操作");
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
                int[] colors = new int[lightGroups.size()];
                Arrays.fill(colors, colorInt);

                DataAgent dataAgent = XLightApplication.getInstance().getDataAgent();

                //发送消息
                if (sendColor == true) {
                    //发送颜色
                    sendColor = false;
                    dataAgent.sendSceneControlCmd(GroupControlActivity.this, ids, colors, new ResultReceiver(new Handler()) {
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
                                    Toast.makeText(GroupControlActivity.this, "消息错误",Toast.LENGTH_LONG).show();
                                    return;
                                }
                                if (msgResponse.getReturnCode() != CommonMsgResponse.RETURN_CODE_OK) {
                                    Log.w(TAG, String.format("消息返回错误-[%s]", AppUtils.getErrorInfo(msgResponse.getReturnCode() + "")));
                                    return;
                                }
                                //设置颜色正常
                                topIV.setBackgroundColor(colorInt);
                            } else {
                                Toast.makeText(GroupControlActivity.this, "出错啦", Toast.LENGTH_SHORT).show();
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

        if (groupID != -1) {
            //持久化到数据库相关数据
            this.subGroup.save();
        } else {
            this.light.save();
        }

    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);

        if (!hasFocus || loaded) {
            //失去焦点的时候或者已经加载过的 则直接返回
            return;
        }

        RelativeLayout rootRL = (RelativeLayout) findViewById(R.id.gc_root);
        //获取屏幕高宽
        WindowManager windowManager = (WindowManager) getSystemService(Context.WINDOW_SERVICE);
        DisplayMetrics displayMetrics = new DisplayMetrics();
        Display display = windowManager.getDefaultDisplay();
        display.getMetrics(displayMetrics);

        int widthPixels = displayMetrics.widthPixels;     //高宽一致
        int rootHeight = rootRL.getHeight();

        //设置顶部
        int heightTop = rootHeight - widthPixels;

        this.topIV = new ImageView(GroupControlActivity.this);
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
        this.colorPickerFL = new FrameLayout(GroupControlActivity.this);
        int bgImgID = getResources().getIdentifier("rainbow", "drawable", getPackageName());
        colorPickerFL.setBackgroundDrawable(getResources().getDrawable(bgImgID));
        RelativeLayout.LayoutParams pickerLP = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, widthPixels);
        pickerLP.addRule(RelativeLayout.BELOW, imageView.getId());
        LightDragListener lightDragListener = new LightDragListener();
        colorPickerFL.setOnDragListener(lightDragListener);
        //计算图片缩放比率
        this.scale = 539 / (float)widthPixels;        //539为实际图片像素数

        final ImageView lightIV = new ImageView(GroupControlActivity.this);
        lightIV.setTag("0");
        lightIV.setImageResource(R.drawable.light_group_icon);

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

        FrameLayout.LayoutParams lightLP = new FrameLayout.LayoutParams(80, 80);
        if (this.groupID != -1) {
            lightLP.leftMargin = subGroup.x;
            lightLP.topMargin = subGroup.y;
        } else {
            lightLP.leftMargin = this.light.x;
            lightLP.topMargin = this.light.y;
        }


        colorPickerFL.addView(lightIV, lightLP);

        //light handle 根据数据库lights数量
//        for(int i = 0; i<lightScenes.size(); i++) {
//            LightScene lightScene = lightScenes.get(i);
//            final Light light = lightScene.light;
//            final ImageView lightIV = new ImageView(GroupControlActivity.this);
//            lightIV.setTag(i+"");
//            lightIV.setImageResource(R.drawable.light_icon);
//            if (light.lostConnection) {
////                lightIV.setBackgroundColor(getResources().getColor(R.color.lightgray));
//                lightIV.setBackgroundDrawable(getResources().getDrawable(R.drawable.light_offline_border));
//            } else {
//                lightIV.setOnTouchListener(new View.OnTouchListener() {
//
//                    @Override
//                    public boolean onTouch(View v, MotionEvent event) {
//
//                        ClipData.Item item = new ClipData.Item((CharSequence) v.getTag());
//
//                        String[] mimeTypes = {ClipDescription.MIMETYPE_TEXT_PLAIN};
//                        ClipData dragData = new ClipData(v.getTag().toString(),
//                                mimeTypes, item);
//
//                        // 实例化拖动阴影
//                        View.DragShadowBuilder lightShadow = new MyDragShadowBuilder(lightIV);
//
//                        v.startDrag(dragData, lightShadow, v, 0);
//                        v.setVisibility(View.INVISIBLE);
//                        return true;
//                    }
//                });
//            }
//
//            FrameLayout.LayoutParams lightLP = new FrameLayout.LayoutParams(80, 80);
//            lightLP.leftMargin = lightScene.x;
//            lightLP.topMargin = lightScene.y;
//
//            colorPickerFL.addView(lightIV, lightLP);
//        }

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
            switch (action) {
                case DragEvent.ACTION_DRAG_STARTED:

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
                    if (groupID != -1) {
                        subGroup.x = dx;
                        subGroup.y = dy;
                    } else {
                        light.x = dx;
                        light.y = dy;
                    }

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
                    subGroup.rColor = Color.red(dropClrPix);
                    subGroup.gColor = Color.green(dropClrPix);
                    subGroup.bColor = Color.blue(dropClrPix);

                    //拖动结束前再进行一次发送 确保最终看到的结果一致
                    new Handler().postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            Message lastColorMsg = new Message();
                            lastColorMsg.what = dropClrPix;
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
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_group_control, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        switch (id) {
            case android.R.id.home:
                onBackPressed();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }

    }
}
