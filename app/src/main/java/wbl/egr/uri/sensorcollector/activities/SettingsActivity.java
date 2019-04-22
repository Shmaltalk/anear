package wbl.egr.uri.sensorcollector.activities;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import wbl.egr.uri.sensorcollector.R;
import wbl.egr.uri.sensorcollector.fragments.SettingsFragment;
import wbl.egr.uri.sensorcollector.tasks.RequestHeartRateTask;

import java.lang.ref.WeakReference;

/**
 * Created by mconstant on 2/23/17.
 *
 * SettingsActivity
 *
 * Creates the foundation for the settings page.
 */

public class SettingsActivity extends AppCompatActivity {
    public static final String KEY_SENSOR_ENABLE = "pref_enable_sensors";
    public static final String KEY_AUDIO_ENABLE = "pref_enable_audio";
    public static final String KEY_HR_CONSENT = "pref_hr_consent";
    public static final String KEY_AUDIO_DURATION = "pref_audio_duration";
    public static final String KEY_AUDIO_DELAY = "pref_audio_delay";
    public static final String KEY_IDENTIFIER = "pref_id";
    public static final String KEY_PATTERN = "pref_pattern";
    public static final String KEY_BLACKOUT_TOGGLE = "pref_blackout_toggle";
    public static final String KEY_HR_TRIGGER = "pref_hr_trigger";

    /** "Getters and Setters"
     *
     * The following methods either retrieve or log information to or from the
     * settings preferences (i.e the SharedPreferences object).
     *
     * @param context
     *The object that manages the application in regards to the phone's system. This takes care
     * of dealing with the phone's database, preferences, and processes. Basically, it recieves information
     * from the phone that the application can use to run more effectively.
     * @param key
     * The specific application settings preference that is being dealt with.
     * @param value
     * The value of the preference that is either being retrieved or changed.
     *
     * Ex.
     * In regards to how long you want the audio to record for, you would prefer to the
     * audio record duration. This would be the **key** preference that you referring to. The
     * default **value** dictates that the audio should be recorded for 30 seconds. That value needs
     * to be received in order for it to be displayed on the UI and to be used to record audio. It also
     * needs to be changed if the user wants to record audio for a longer / shorter duration.
     */
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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_single_fragment);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            requestPermissions(new String[] {Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.RECORD_AUDIO}, 034);
        }
        if (!SettingsActivity.getBoolean(this, SettingsActivity.KEY_HR_CONSENT, false)) {
            new RequestHeartRateTask().execute(new WeakReference<Activity>(this));
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

    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    @Override
    public void onBackPressed() {
        finish();
    }
}
