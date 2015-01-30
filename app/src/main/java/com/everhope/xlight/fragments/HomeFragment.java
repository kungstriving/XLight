package com.everhope.xlight.fragments;

import android.app.Fragment;
import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.view.GravityCompat;
import android.util.DisplayMetrics;
import android.view.Display;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.TextView;
import android.widget.Toast;

import com.everhope.xlight.R;
import com.everhope.xlight.activities.SceneEditActivity;

/**
 * 主页fragment
 * Created by kongxiaoyang on 2015/1/13.
 */
public class HomeFragment extends Fragment{
    public HomeFragment() {}

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_home, container, false);
        //获取屏幕宽度
        WindowManager windowManager = (WindowManager)getActivity().getSystemService(Context.WINDOW_SERVICE);
        DisplayMetrics displayMetrics = new DisplayMetrics();
        Display display = windowManager.getDefaultDisplay();
        display.getMetrics(displayMetrics);
        //px = dp * (dpi / 160)
        //计算每个操作块的大小
        int dpi = displayMetrics.densityDpi;
        int widthInPx = displayMetrics.widthPixels;
//        int heightInPx = displayMetrics.heightPixels;
//        int widthInDP = (widthInPx * 160)/dpi;
//        int heightInDP = (heightInPx * 160)/dpi;

        int temp = 20 * (dpi/160);
        int cellWidth = (widthInPx - temp)/3;
//        int cellHeight = cellWidth;

        //获取最外围container，纵向布局
        LinearLayout linearLayout = (LinearLayout)rootView.findViewById(R.id.home_frag_outerll);

        //TODO 读取持久存储 解析当前共有多少场景操作块
        int sceneCount = getStoredSceneInfo();
        int currentLineCount = 0;
        LinearLayout currentLineLayout = new LinearLayout(getActivity());
        currentLineLayout.setOrientation(LinearLayout.HORIZONTAL);
        LinearLayout.LayoutParams currentLayoutparams =
                new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        currentLayoutparams.setMargins(10,10,10,10);
        linearLayout.addView(currentLineLayout, currentLayoutparams);

        //逐个加入scene控制块
        for (int i = 0; i < sceneCount; i++) {
            if (currentLineCount == 3) {
                currentLineLayout = new LinearLayout(getActivity());
                currentLineLayout.setOrientation(LinearLayout.HORIZONTAL);
                linearLayout.addView(currentLineLayout, currentLayoutparams);
                currentLineCount = 0;
            }

            //add scene to currentlinearlayout
//            View scenePane = inflater.inflate(R.layout.view_scene_pane, container);
            LinearLayout scenePane = new LinearLayout(getActivity());
            scenePane.setOrientation(LinearLayout.VERTICAL);

            ImageView sceneImg = new ImageView(getActivity());
            if (i == 0) {
                sceneImg.setImageResource(R.drawable.sunset);
            } else {
                sceneImg.setImageResource(R.drawable.ic_launcher);
            }
            sceneImg.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    final PopupWindow popup = new PopupWindow(getActivity());
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

                    layout.findViewById(R.id.scene_control_edit).setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            popup.dismiss();
                            Toast.makeText(getActivity(), "edit", Toast.LENGTH_SHORT).show();
                            Intent intent = new Intent(getActivity(), SceneEditActivity.class);
                            intent.putExtra("scene_id", "0");
                            startActivity(intent);
                        }
                    });

//                    popup.showAsDropDown(v,0, -300);
                }
            });
            LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(cellWidth-10, cellWidth-10);   //same width height
            scenePane.addView(sceneImg, layoutParams);

            TextView sceneText = new TextView(getActivity());
            LinearLayout.LayoutParams textLayoutParams = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 50);
            sceneText.setText("scene");
            sceneText.setGravity(Gravity.CENTER_HORIZONTAL);

            scenePane.addView(sceneText, textLayoutParams);

            LinearLayout.LayoutParams sceneLayoutParams = new LinearLayout.LayoutParams(cellWidth, cellWidth + 50);
            sceneLayoutParams.setMargins(5,5,5,5);
            currentLineLayout.addView(scenePane, sceneLayoutParams);
            currentLineCount++;
        }

        getActivity().setTitle(R.string.home_fragment_title);
        return rootView;
    }

    /**
     * 获取存储scene信息
     * @return
     */
    private int getStoredSceneInfo() {
        return 10;
    }
}
