package com.everhope.elighte.fragments;


import android.content.SharedPreferences;
import android.os.Bundle;
import android.app.Fragment;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.everhope.elighte.R;
import com.everhope.elighte.constants.Constants;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link GateInfoPrefsFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class GateInfoPrefsFragment extends PreferenceFragment {
    public static GateInfoPrefsFragment newInstance() {
        GateInfoPrefsFragment fragment = new GateInfoPrefsFragment();
        return fragment;
    }

    public GateInfoPrefsFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.pref_gate_info);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
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

        return super.onCreateView(inflater, container, savedInstanceState);
    }


}
