package com.example.alli.anearaw.fragments;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.util.Log;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;

import java.lang.ref.WeakReference;
import java.util.Map;

import com.example.alli.anearaw.R;
import com.example.alli.anearaw.activities.MainActivity;
import com.example.alli.anearaw.enums.Action;
import com.example.alli.anearaw.services.AudioRecordManager;
import com.example.alli.anearaw.services.WearListenerService;


import static com.example.alli.anearaw.activities.MainActivity.KEY_AUDIO_DELAY;
import static com.example.alli.anearaw.activities.MainActivity.KEY_AUDIO_DURATION;
import static com.example.alli.anearaw.activities.MainActivity.KEY_AUDIO_ENABLE;
import static com.example.alli.anearaw.activities.MainActivity.KEY_BLACKOUT_TOGGLE;
import static com.example.alli.anearaw.activities.MainActivity.KEY_HR_TRIGGER;
import static com.example.alli.anearaw.activities.MainActivity.KEY_IDENTIFIER;
import static com.example.alli.anearaw.activities.MainActivity.KEY_PATTERN;
import static com.example.alli.anearaw.activities.MainActivity.KEY_SENSOR_ENABLE;
import static com.example.alli.anearaw.activities.MainActivity.KEY_PSENSOR_ENABLE;
import static com.example.alli.anearaw.activities.MainActivity.KEY_PSENSOR_DURATION;

/**
 * Created by Matt Constant on 2/23/17.
 * Modified for Google Wear by Andrew Peltier on 6/5/18.
 */

public class SettingsFragment extends PreferenceFragment implements SharedPreferences.OnSharedPreferenceChangeListener
{
    private static final String TAG = "SettingsFragment";
    private SharedPreferences mSharedPreferences;
    private MaterialDialog mBeginStreamDialog;
    private boolean mConnecting;
    private Context mContext;

    public static SettingsFragment newInstance() {
        return new SettingsFragment();
    }

    //Creates the contents of the settings page / activity
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.preferences);
        mConnecting = false;
        mContext = MainActivity.getContext();

        final WeakReference<Activity> activityWeakReference = new WeakReference<Activity>(getActivity());
        mBeginStreamDialog = new MaterialDialog.Builder(getActivity())
                .title("Connecting...")
                .content("Would you like to begin collecting data?")
                .positiveText("Yes")
                .negativeText("No")
                .onPositive(new MaterialDialog.SingleButtonCallback() {
                    @Override
                    public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                        if (activityWeakReference != null && activityWeakReference.get() != null) {
//                            Intent intent = new Intent(mContext, WearListenerService.class);
//                            intent.putExtra(WearListenerService.INTENT_RECORD, true);
//                            mContext.startService(intent);
                        }
                        mConnecting = false;
                        mBeginStreamDialog.dismiss();
                    }
                })
                .onNegative(new MaterialDialog.SingleButtonCallback() {
                    @Override
                    public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                        if (activityWeakReference != null && activityWeakReference.get() != null) {
//                            Intent intent = new Intent(mContext, WearListenerService.class);
//                            intent.putExtra(WearListenerService.INTENT_RECORD, false);
//                            mContext.startService(intent);
                        }
                        mConnecting = false;
                        mBeginStreamDialog.dismiss();
                    }
                })
                .build();
    }

    @Override
    public void onResume() {
        super.onResume();
        mSharedPreferences = PreferenceManager.getDefaultSharedPreferences(getActivity());
        mSharedPreferences.registerOnSharedPreferenceChangeListener(this);
        updateSummaries();
    }

    @Override
    public void onPause() {
        super.onPause();
        mSharedPreferences.unregisterOnSharedPreferenceChangeListener(this);
    }

    @Override
    public void onDestroy() {
//        getActivity().unregisterReceiver(mBandUpdateReceiver);
        super.onDestroy();
    }

    /** onSharredPreferenceChanged
     *
     * Method that acts upon toggling the enabling of a settings preference.
     *
     * @param sharedPreferences
     * Shared Preferences are the components of the settings activity, including
     * the toggled enabling of the sensor and audio settings, the patient ID, and anything else that
     * can be accessed within the settings. The Shared Preferences
     * stores this information so that other methods act upon the content of a preference
     * @param key
     * The key is the specific preference that is being targeted, which, in this case, is
     * either the sensor enable preference or the audio enable preference.
     */
    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        switch (key) {
            case KEY_SENSOR_ENABLE:
                if (sharedPreferences.getBoolean(key, false)) {
                    Log.e(TAG, "Starting record intent...");
                    mConnecting = true;
                    Intent intent = new Intent(mContext, WearListenerService.class);
                    intent.setAction(Action.enabled.toString());
                    mContext.startService(intent);
                } else {
                    Log.e(TAG, "Stopping record intent...");
                    Intent intent = new Intent(mContext, WearListenerService.class);
                    intent.setAction(Action.disabled.toString());
                    mContext.startService(intent);
                }
                break;
            case KEY_PSENSOR_ENABLE:
                if(sharedPreferences.getBoolean(key,false))
                {
                    Log.e(TAG, "Starting periodic record intent...");
                    Intent intent = new Intent(mContext, WearListenerService.class);
                    intent.setAction(Action.p_enabled.toString());
                    mContext.startService(intent);
                }
                else
                {
                    Log.e(TAG, "Stopping periodic record intent...");
                    Intent intent = new Intent(mContext, WearListenerService.class);
                    intent.setAction(Action.p_disabled.toString());
                    mContext.startService(intent);
                }
            case KEY_AUDIO_ENABLE:
                if (sharedPreferences.getBoolean(key, false)) {
                    AudioRecordManager.start(getActivity(), AudioRecordManager.ACTION_AUDIO_START);
                } else {
                    AudioRecordManager.start(getActivity(), AudioRecordManager.ACTION_AUDIO_CANCEL);
                }
                break;
        }
        updateSummaries();
    }

    //Updates the settings according to whatever preference is changed
    private void updateSummaries() {
        Map<String, ?> preferences = mSharedPreferences.getAll();
        for (String key : preferences.keySet()) {
            String summary = "";
            switch (key) {
                case KEY_SENSOR_ENABLE:
                    if (MainActivity.getBoolean(getActivity(), MainActivity.KEY_SENSOR_ENABLE, false)) {
                        summary = "Sensor Recordings are Enabled";
                    } else {
                        summary = "Sensor Recordings are not Enabled";
                    }
                    break;
                case KEY_PSENSOR_ENABLE:
                    if (MainActivity.getBoolean(getActivity(), MainActivity.KEY_PSENSOR_ENABLE, false)) {
                        summary = "Periodic Sensor Recordings are Enabled";
                    } else {
                        summary = "Periodic Sensor Recordings are not Enabled";
                    }
                    break;
                case KEY_PSENSOR_DURATION:
                    int dur = Integer.parseInt(MainActivity.getString(getActivity(), KEY_PSENSOR_DURATION, "3"));
                    summary = "Sensor Recordings will turn on/off for " + dur + " minutes at a time";
                    break;
                case KEY_AUDIO_ENABLE:
                    if (MainActivity.getBoolean(getActivity(), MainActivity.KEY_AUDIO_ENABLE, false)) {
                        summary = "Periodic Audio Recordings are Enabled";
                    } else {
                        summary = "Periodic Audio Recordings are not Enabled";
                    }
                    break;
                case KEY_AUDIO_DURATION:
                    int sensorDur = Integer.parseInt(MainActivity.getString(getActivity(), KEY_AUDIO_DURATION, "30"));
                    summary = "Audio Recordings will last for " + sensorDur + " seconds";
                    break;
                case KEY_AUDIO_DELAY:
                    int delay = Integer.parseInt(MainActivity.getString(getActivity(), KEY_AUDIO_DELAY, "12"));
                    summary = "There will be a " + delay + " minute delay between Audio Recordings";
                    break;
                case KEY_HR_TRIGGER:
                    int trigger = Integer.parseInt(MainActivity.getString(getActivity(), KEY_HR_TRIGGER, "100"));
                    summary = "Audio recording will trigger at " + trigger + "bpm";
                    break;
                case KEY_IDENTIFIER:
                    String p_id = MainActivity.getString(getActivity(), KEY_IDENTIFIER, null);
                    if (p_id == null) {
                        summary = "No Patient Identifier Set";
                    } else {
                        summary = "Patient Identifier set to: " + p_id;
                    }
                    break;
                case KEY_BLACKOUT_TOGGLE:
                    summary = "School Blackout period is between 7:30am and 3:00pm";
                    break;
                case KEY_PATTERN:
                    break;
                default:
                    return;
            }
            if(!key.equals(KEY_PATTERN))
                findPreference(key).setSummary(summary);
        }
    }
}
