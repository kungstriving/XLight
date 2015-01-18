package com.everhope.xlight;

import android.app.Fragment;
import android.content.Context;
import android.graphics.Point;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.util.DisplayMetrics;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.GridLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.Toast;

import java.util.Locale;

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
        int heightInPx = displayMetrics.heightPixels;
//        int widthInDP = (widthInPx * 160)/dpi;
//        int heightInDP = (heightInPx * 160)/dpi;

        int temp = 20 * (dpi/160);
        int cellWidth = (widthInPx - temp)/3;
        int cellHeight = cellWidth;

        LinearLayout linearLayout = (LinearLayout)rootView.findViewById(R.id.home_frag_ll);

        Button button = new Button(getActivity());
        button.setText("plus");
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                PopupWindow popup = new PopupWindow(getActivity());
                View layout = getActivity().getLayoutInflater().inflate(R.layout.popup_scene_control, null);
                popup.setContentView(layout);
                // Set content width and height
                popup.setHeight(WindowManager.LayoutParams.WRAP_CONTENT);
                popup.setWidth(WindowManager.LayoutParams.WRAP_CONTENT);
                // Closes the popup window when touch outside of it - when looses focus
                popup.setOutsideTouchable(true);
                popup.setFocusable(true);
                // Show anchored to button
                popup.setBackgroundDrawable(new BitmapDrawable());
                popup.showAsDropDown(v);
            }
        });
        button.setBackgroundColor(getResources().getColor(R.color.red));
        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(cellWidth, cellHeight);
        linearLayout.addView(button, layoutParams);

        button = new Button(getActivity());
        button.setText("plus");
        button.setBackgroundColor(getResources().getColor(R.color.yellow));
        linearLayout.addView(button, layoutParams);

        button = new Button(getActivity());
        button.setText("plus");
        button.setBackgroundColor(getResources().getColor(R.color.gray));
        linearLayout.addView(button, layoutParams);

        getActivity().setTitle(R.string.home_fragment_title);
        return rootView;
    }
}
