package com.example.alli.anearaw.receivers;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.example.alli.anearaw.services.AudioRecordManager;


/**
 * Created by mconstant on 2/23/17.
 */

//AlarmReceiver receives any actions that trigger an audio recording
public class AlarmReceiver extends BroadcastReceiver {
    public static final String KEY_ACTION = "alarm_key_action";
    public static final String KEY_ALARM_ID = "key_alarm_id";
    public static final int AUDIO_ID = 430;

    @Override
    public void onReceive(Context context, Intent intent) {
        AudioRecordManager.start(context, intent.getIntExtra(KEY_ACTION, -1));
    }
}
