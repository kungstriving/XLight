package com.everhope.xlight.fragments;

import android.os.Bundle;
import android.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.everhope.xlight.R;

/**
 * 场景
 */
public class SceneFragment extends Fragment {
    private static final String ARG_HELLO_SCENE = "hello_info";

    private String mHelloInfo;

    /**
     * 获取实例
     * @param paramHelloInfo
     * @return
     */
    public static SceneFragment newInstance(String paramHelloInfo) {
        SceneFragment fragment = new SceneFragment();
        Bundle args = new Bundle();
        args.putString(ARG_HELLO_SCENE, paramHelloInfo);
        fragment.setArguments(args);
        return fragment;
    }

    public SceneFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mHelloInfo = getArguments().getString(ARG_HELLO_SCENE);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_scene, container, false);
    }

}
