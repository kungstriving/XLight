package com.everhope.elighte.fragments;

import android.app.Fragment;
import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.util.DisplayMetrics;
import android.view.Display;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.TextView;
import android.widget.Toast;

import com.everhope.elighte.R;
import com.everhope.elighte.activities.SceneEditActivity;
import com.everhope.elighte.models.Scene;

import java.util.List;

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
        int sceneImgID = getResources().getIdentifier(scene.imgName, "drawable", getActivity().getPackageName());
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

                layout.findViewById(R.id.scene_control_edit).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        popup.dismiss();
                        Intent intent = new Intent(getActivity(), SceneEditActivity.class);
                        intent.putExtra("scene_id", scene.getId());
                        startActivity(intent);
                    }
                });
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
     * 获取存储scene信息
     * @return
     */
    private List<Scene> getStoredSceneInfo() {
        return Scene.getAll();
    }
}
