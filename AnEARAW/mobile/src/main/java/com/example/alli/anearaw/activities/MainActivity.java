package com.example.alli.anearaw.activities;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.TextView;
import com.example.alli.anearaw.R;
import com.example.alli.anearaw.fragments.SettingsFragment;

public class MainActivity extends AppCompatActivity
{
    public static final String KEY_SENSOR_ENABLE = "pref_enable_sensors";
    public static final String KEY_PSENSOR_ENABLE = "pref_enable_psensors";
    public static final String KEY_PSENSOR_DURATION = "pref_sensors_duration";
    public static final String KEY_AUDIO_ENABLE = "pref_enable_audio";
    public static final String KEY_HR_CONSENT = "pref_hr_consent";
    public static final String KEY_AUDIO_DURATION = "pref_audio_duration";
    public static final String KEY_AUDIO_DELAY = "pref_audio_delay";
    public static final String KEY_IDENTIFIER = "pref_id";
    public static final String KEY_PATTERN = "pref_pattern";
    public static final String KEY_BLACKOUT_TOGGLE = "pref_blackout_toggle";
    public static final String KEY_HR_TRIGGER = "pref_hr_trigger";

    private final static String[] PERMISSIONS = {
            Manifest.permission.WRITE_EXTERNAL_STORAGE,     // Required for Local I/O Operations
            Manifest.permission.WAKE_LOCK,                  // Required for MQTT Paho Library
            Manifest.permission.VIBRATE,
            Manifest.permission.RECEIVE_BOOT_COMPLETED,
            Manifest.permission.RECORD_AUDIO,
            Manifest.permission.BLUETOOTH
    };
    private static final int PERMISSION_CODE = 111;
    private static Context mContext;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_single_fragment);
        mContext = this;
    }

    public static void putString(Context context, String key, String value) {
        SharedPreferences.Editor editor = PreferenceManager.getDefaultSharedPreferences(context).edit();
        editor.putString(key, value);
        editor.apply();
    }

    public static String getString(Context context, String key, String defaultValue) {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
        return sharedPreferences.getString(key, defaultValue);
    }

    public static void putBoolean(Context context, String key, boolean value) {
        SharedPreferences.Editor editor = PreferenceManager.getDefaultSharedPreferences(context).edit();
        editor.putBoolean(key, value);
        editor.commit();
    }

    public static boolean getBoolean(Context context, String key, boolean defaultValue) {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
        return sharedPreferences.getBoolean(key, defaultValue);
    }

    public static Context getContext()
    {
        return mContext;
    }

    @Override
    protected void onStart()
    {
        super.onStart();
        // Check Permissions at Runtime (Android M+), and Request if Necessary
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            // The Results from this Request are handled in a Callback Below.
            requestPermissions(PERMISSIONS, PERMISSION_CODE);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        getFragmentManager().beginTransaction()
                .replace(R.id.fragment_container, SettingsFragment.newInstance(), "settings_fragment")
                .commit();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu)
    {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.main_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item)
    {
        Intent intent;
        switch (item.getItemId())
        {
            case R.id.menu_audio_sample:
                intent = new Intent(this, AudioSampleActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(intent);
                finish();
                return true;
            case R.id.menu_settings:
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

}