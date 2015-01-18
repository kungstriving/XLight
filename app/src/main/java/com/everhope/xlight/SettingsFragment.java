package com.everhope.xlight;


import android.os.Bundle;
import android.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

/**
 * 设置
 */
public class SettingsFragment extends Fragment {
    private static final String ARG_HELLO_SETTINGS = "hello_settings";

    private String mHelloSettings;


    /**
     *
     * @param paramHelloSettings
     * @return
     */
    public static SettingsFragment newInstance(String paramHelloSettings) {
        SettingsFragment fragment = new SettingsFragment();
        Bundle args = new Bundle();
        args.putString(ARG_HELLO_SETTINGS, paramHelloSettings);
        fragment.setArguments(args);
        return fragment;
    }

    public SettingsFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mHelloSettings = getArguments().getString(ARG_HELLO_SETTINGS);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_settings, container, false);
    }


}
