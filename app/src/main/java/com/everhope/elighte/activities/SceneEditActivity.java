package com.everhope.elighte.activities;

import android.content.ClipData;
import android.content.ClipDescription;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Point;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Display;
import android.view.DragEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import com.everhope.elighte.R;
import com.everhope.elighte.helpers.AppUtils;
import com.everhope.elighte.models.Light;
import com.everhope.elighte.models.LightScene;
import com.everhope.elighte.models.Scene;

import java.util.ArrayList;
import java.util.List;

/**
 * 场景编辑Activity
 */
public class SceneEditActivity extends ActionBarActivity {

    private static final String TAG = "SceneEditActivity@Light";

    private int colorPickerYOffset;
    private ImageView topIV;
    private Handler topIVHandler;
    private float scale;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_scene_edit);

        //获取数据库存储场景信息
        Intent intent = getIntent();
        long sceneID = intent.getLongExtra("scene_id",-1);
        if (sceneID != -1) {
            //正常情况
            Scene scene = Scene.load(Scene.class, sceneID);
            List<LightScene> lightScenes = scene.lightScenes();
            List<Light> lights = new ArrayList<>();
            for (LightScene lightScene : lightScenes) {
                lights.add(lightScene.light);
            }
        }
        //设置顶部底色变化
        this.topIVHandler = new Handler() {
            @Override
            public void handleMessage(Message msg) {

                int colorInt = msg.what;
                topIV.setBackgroundColor(colorInt);
            }
        };

        //添加返回按钮
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeButtonEnabled(true);
    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);

        RelativeLayout rootRL = (RelativeLayout) findViewById(R.id.se_root);
        //获取屏幕高宽
        WindowManager windowManager = (WindowManager) getSystemService(Context.WINDOW_SERVICE);
        DisplayMetrics displayMetrics = new DisplayMetrics();
        Display display = windowManager.getDefaultDisplay();
        display.getMetrics(displayMetrics);

        int widthPixels = displayMetrics.widthPixels;     //高宽一致
        int rootHeight = rootRL.getHeight();

        //设置顶部图片大小
        int heightTop = rootHeight - widthPixels;

        this.topIV = new ImageView(SceneEditActivity.this);
        ImageView imageView = topIV;
        imageView.setId(AppUtils.generateViewId());
        imageView.setBackgroundColor(getResources().getColor(R.color.yellow));

        RelativeLayout.LayoutParams ivLayoutParams = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, heightTop);
        ivLayoutParams.addRule(RelativeLayout.ALIGN_PARENT_TOP);
        ivLayoutParams.addRule(RelativeLayout.ALIGN_PARENT_LEFT);
        rootRL.addView(imageView, ivLayoutParams);

        //设置colorpicker
        final RelativeLayout colorPickerRL = new RelativeLayout(SceneEditActivity.this);
        colorPickerRL.setBackgroundDrawable(getResources().getDrawable(R.drawable.colorpicker));
        RelativeLayout.LayoutParams pickerLP = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, widthPixels);
        pickerLP.addRule(RelativeLayout.BELOW, imageView.getId());
        LightDragListener lightDragListener = new LightDragListener();
        colorPickerRL.setOnDragListener(lightDragListener);
        //计算图片缩放比率
        this.scale = 539 / (float)widthPixels;        //539为实际图片像素数

        //light handle
        final ImageView lightIV = new ImageView(SceneEditActivity.this);
        lightIV.setTag("light-handle");
        lightIV.setImageResource(R.drawable.light_icon);
//        LightHandleLongClick lightHandleLongClick = new LightHandleLongClick(lightIV);
//        lightIV.setOnLongClickListener(lightHandleLongClick);
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
                return false;
            }
        });

        RelativeLayout.LayoutParams lightLP = new RelativeLayout.LayoutParams(80, 80);
        lightLP.leftMargin = 200;
        lightLP.topMargin = 250;
        colorPickerRL.addView(lightIV, lightLP);

        rootRL.addView(colorPickerRL, pickerLP);

        //计算偏移量
        colorPickerYOffset = heightTop + getSupportActionBar().getHeight();
    }

    class LightDragListener implements View.OnDragListener {

        @Override
        public boolean onDrag(View v, DragEvent event) {

            int action = event.getAction();
            View view = (View)event.getLocalState();
            switch (action) {
                case DragEvent.ACTION_DRAG_STARTED:
                    return true;
                case DragEvent.ACTION_DRAG_ENTERED:
                    return true;
                case DragEvent.ACTION_DRAG_LOCATION:
//                    int x = (int)(event.getX() - 40);
//                    int y = (int)(event.getY() - 80);
                    int x = (int)(event.getX());
                    int y = (int)(event.getY());
                    Log.i(TAG, String.format("drag at x(%s) y(%s)", x + "", y + ""));
                    View viewParent = (View)view.getParent();
                    Bitmap bitmap = ((BitmapDrawable)viewParent.getBackground()).getBitmap();
                    int w = bitmap.getWidth();
                    int h = bitmap.getHeight();
                    if (x > w - 1) {
                        x = w - 1;
                    }
                    if (y > h - 1) {
                        y = h - 1;
                    }
//                    x = (int)(x * scale);
//                    y = (int)(y * scale);
                    int clrPix = bitmap.getPixel(x, y);
                    topIVHandler.sendEmptyMessage(clrPix);
                    return true;
                case DragEvent.ACTION_DRAG_EXITED:
                    RelativeLayout.LayoutParams lp = (RelativeLayout.LayoutParams)view.getLayoutParams();
                    int vx = (int)(event.getX() - 40);
                    int vy = (int)(event.getY() - 80);
                    if (vy > 640) {
                        vy = 640;
                    }
                    if (vy < colorPickerYOffset) {
                        vy = 0;
                    }
                    lp.leftMargin = vx;
                    lp.topMargin = vy;
                    view.setLayoutParams(lp);
                    view.setVisibility(View.VISIBLE);
                    return true;
                case DragEvent.ACTION_DROP:
                    RelativeLayout.LayoutParams vlp = (RelativeLayout.LayoutParams)view.getLayoutParams();
                    int vvx = (int)(event.getX() - 40);
                    int vvy = (int)(event.getY() - 80);
                    if (vvy > 640) {
                        vvy = 640;
                    }
                    if (vvy < 0) {
                        vvy = 0;
                    }
                    vlp.leftMargin = vvx;
                    vlp.topMargin = vvy;
                    view.setLayoutParams(vlp);
                    view.setVisibility(View.VISIBLE);
                    return true;
                case DragEvent.ACTION_DRAG_ENDED:
                    return true;
                default:
                    break;
            }
//                String t = event.getClipData().getItemAt(0).getText().toString();
//            Toast.makeText(SceneEditActivity.this, a + "000", Toast.LENGTH_LONG).show();
            return false;
        }
    }

    class LightHandleLongClick implements View.OnLongClickListener {
        private View dragView;

        public LightHandleLongClick(View view) {
            this.dragView = view;
        }

        @Override
        public boolean onLongClick(View v) {

            ClipData.Item item = new ClipData.Item((CharSequence) v.getTag());

            String[] mimeTypes = {ClipDescription.MIMETYPE_TEXT_PLAIN};
            ClipData dragData = new ClipData(v.getTag().toString(),
                    mimeTypes, item);

            // Instantiates the drag shadow builder.
            View.DragShadowBuilder myShadow = new View.DragShadowBuilder(v);

//            View.DragShadowBuilder myShadow = new MyDragShadowBuilder(dragView);
            v.startDrag(dragData, myShadow, v, 0);

            v.setVisibility(View.INVISIBLE);

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
            case R.id.home:
                finish();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }

    }
}
