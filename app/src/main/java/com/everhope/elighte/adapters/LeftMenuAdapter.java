package com.everhope.elighte.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.everhope.elighte.R;

import java.util.ArrayList;

/**
 * 左侧抽屉菜单项定义
 * Created by kongxiaoyang on 2015/2/13.
 */
public class LeftMenuAdapter extends ArrayAdapter<String> {

    public LeftMenuAdapter(Context context, ArrayList<String> items) {
        super(context, 0, items);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        String itemStr = getItem(position);
        // Check if an existing view is being reused, otherwise inflate the view
        if (convertView == null) {
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.leftmenu_item, parent, false);
        }

        ImageView imageView = (ImageView)convertView.findViewById(R.id.item_iv);
        TextView textView = (TextView)convertView.findViewById(R.id.item_tv);
        textView.setText(itemStr);
        switch (itemStr) {
            case "我的家":
                imageView.setImageResource(R.drawable.leftmenu_home);
                break;
            case "灯":
                imageView.setImageResource(R.drawable.leftmenu_light);
                break;
            case "开关":
                imageView.setImageResource(R.drawable.leftmenu_switch);
                break;
            case "遥控":
                imageView.setImageResource(R.drawable.leftmenu_remote);
                break;
            case "设置":
                imageView.setImageResource(R.drawable.leftmenu_settings);
                break;
            default:
                imageView.setImageResource(R.drawable.leftmenu_settings);
                break;
        }
        return convertView;
    }
}
