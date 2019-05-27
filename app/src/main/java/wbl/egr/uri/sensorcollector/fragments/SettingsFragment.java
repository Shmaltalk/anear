package wbl.egr.uri.sensorcollector.fragments;

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
import wbl.egr.uri.sensorcollector.R;
import wbl.egr.uri.sensorcollector.activities.SettingsActivity;
import wbl.egr.uri.sensorcollector.receivers.BandUpdateReceiver;
import wbl.egr.uri.sensorcollector.services.AudioRecordManager;
import wbl.egr.uri.sensorcollector.services.BandCollectionService;
import wbl.egr.uri.sensorcollector.tasks.RequestHeartRateTask;

import java.lang.ref.WeakReference;
import java.util.Map;

import static wbl.egr.uri.sensorcollector.activities.SettingsActivity.*;

/**
 * Created by Matt Constant on 2/23/17.
 */

public class SettingsFragment extends PreferenceFragment implements SharedPreferences.OnSharedPreferenceChangeListener {
    private SharedPreferences mSharedPreferences;

    public static SettingsFragment newInstance() {
        return new SettingsFragment();
    }

    //Creates the contents of the settings page / activity
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.preferences);
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
        //getActivity().unregisterReceiver(mBandUpdateReceiver);
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
                    BandCollectionService.connect(getActivity());
                } else {
                    BandCollectionService.disconnect(getActivity());
                }
                break;
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
                    if (SettingsActivity.getBoolean(getActivity(), SettingsActivity.KEY_SENSOR_ENABLE, false)) {
                        summary = "Sensor Recordings are Enabled";
                    } else {
                        summary = "Sensor Recordings are not Enabled";
                    }
                    break;
                case KEY_AUDIO_ENABLE:
                    if (SettingsActivity.getBoolean(getActivity(), SettingsActivity.KEY_AUDIO_ENABLE, false)) {
                        summary = "Periodic Audio Recordings are Enabled";
                    } else {
                        summary = "Periodic Audio Recordings are not Enabled";
                    }
                    break;
                case KEY_AUDIO_DURATION:
                    int dur = Integer.parseInt(SettingsActivity.getString(getActivity(), KEY_AUDIO_DURATION, "30"));
                    summary = "Audio Recordings will last for " + dur + " seconds";
                    break;
                case KEY_AUDIO_DELAY:
                    int delay = Integer.parseInt(SettingsActivity.getString(getActivity(), KEY_AUDIO_DELAY, "12"));
                    summary = "There will be a " + delay + " minute delay between Audio Recordings";
                    break;
                case KEY_IDENTIFIER:
                    String p_id = SettingsActivity.getString(getActivity(), KEY_IDENTIFIER, null);
                    if (p_id == null) {
                        summary = "No Patient Identifier Set";
                    } else {
                        summary = "Patient Identifier set to: " + p_id;
                    }
                    break;
                case KEY_BLACKOUT_TOGGLE:
                    summary = "School Blackout period is between 7:30am and 3:00pm";
                    break;
                case KEY_HR_TRIGGER:
                    int trigger = Integer.parseInt(SettingsActivity.getString(getActivity(), KEY_HR_TRIGGER, "100"));
                    summary = "Audio recording will trigger at " + trigger + "bpm";
                    break;
                case KEY_PATTERN:
                    break;
                default:
                    return;
            }
            //TODO: Added Key Pattern to end summary bug
            if(!key.equals(KEY_PATTERN))
                findPreference(key).setSummary(summary);
        }
    }
}
