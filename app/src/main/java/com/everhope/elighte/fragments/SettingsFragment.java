package com.everhope.elighte.fragments;


import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.everhope.elighte.R;
import com.everhope.elighte.constants.Constants;

/**
 * 设置
 */
public class SettingsFragment extends PreferenceFragment implements SharedPreferences.OnSharedPreferenceChangeListener {
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
        addPreferencesFromResource(R.xml.preference);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        PreferenceManager preferenceManager = getPreferenceManager();
        Preference preference;

        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getActivity());

        preference = preferenceManager.findPreference(Constants.SYSTEM_SETTINGS.GATE_STA_IP);
        String gateStaIP = sharedPreferences.getString(Constants.SYSTEM_SETTINGS.GATE_STA_IP, "--");
        preference.setSummary(gateStaIP);

        preference = preferenceManager.findPreference(Constants.SYSTEM_SETTINGS.GATE_MAC);
        String gateMac = sharedPreferences.getString(Constants.SYSTEM_SETTINGS.GATE_MAC, "--");
        preference.setSummary(gateMac);

        preference = preferenceManager.findPreference(Constants.SYSTEM_SETTINGS.GATE_VER);
        String gateVer = sharedPreferences.getString(Constants.SYSTEM_SETTINGS.GATE_VER, "--");
        preference.setSummary(gateVer);

        preference = preferenceManager.findPreference(Constants.SYSTEM_SETTINGS.GATE_DESC);
        String gateDesc = sharedPreferences.getString(Constants.SYSTEM_SETTINGS.GATE_DESC, "--");
        preference.setSummary(gateDesc);

        return super.onCreateView(inflater, container, savedInstanceState);
    }

    @Override
    public void onResume() {
        super.onResume();
        getPreferenceScreen().getSharedPreferences().registerOnSharedPreferenceChangeListener(this);
    }

    @Override
    public void onPause() {
        super.onPause();
        getPreferenceScreen().getSharedPreferences().unregisterOnSharedPreferenceChangeListener(this);
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {

    }
}
