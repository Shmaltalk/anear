package wbl.egr.uri.sensorcollector.services;

import android.Manifest;
import android.app.AlarmManager;
import android.app.IntentService;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.util.Log;
import wbl.egr.uri.sensorcollector.activities.MainActivity;
import wbl.egr.uri.sensorcollector.activities.SettingsActivity;
import wbl.egr.uri.sensorcollector.receivers.AlarmReceiver;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

/**
 * Created by mconstant on 2/23/17.
 *
 * AudioRecordManager
 *
 * This class manages the recording of audio from the MicrosoftBand.
 *
 * The class will have to check to see if the audio recording is enabled in
 * the settings, as well as the audio length and delay. After which, it will need
 * to use this data to set the alarm in which the audio should be triggered to start
 * recording.
 *
 * The manager will also start and stop the audio whenever it is necessary.
 */

public class AudioRecordManager extends IntentService {
    public static final String INTENT_ACTION = "intent_action";
    public static final int ACTION_AUDIO_START = 0;
    public static final int ACTION_AUDIO_TRIGGER = 1;
    public static final int ACTION_AUDIO_CREATE = 2;
    public static final int ACTION_AUDIO_CANCEL = 3;
    public static final int ACTION_AUDIO_SAMPLE = 4;

    private AlarmManager alarmManager;

    public static void start(Context context, int action) {
        Intent intent = new Intent(context, AudioRecordManager.class);
        intent.putExtra(INTENT_ACTION, action);
        context.startService(intent);
    }

    public AudioRecordManager() {
        super("AudioRecordManagerThread");
    }

    /** onHandleIntent
     *
     * The handle intent method will receive actions from a number of other methods,
     * including the heart rate listener and the settings activity.
     */
    @Override
    public void onHandleIntent(Intent intent) {
        if (intent == null || !intent.hasExtra(INTENT_ACTION)) {
            log("Service not Started Properly");
            return;
        }

        switch (intent.getIntExtra(INTENT_ACTION, -1)) {
            case ACTION_AUDIO_START:
                log("ACTION_AUDIO_START");
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    if (checkSelfPermission(Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED || checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                        log("anEAR must have Audio Record permission to record audio");
                        return;
                    }
                }
                //Start Recording
                startAudio(false, false);
                setAudioAlarm();
                break;

            //Activates with heart rate trigger
            case ACTION_AUDIO_TRIGGER:
                log("ACTION_AUDIO_TRIGGER");
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    if (checkSelfPermission(Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED || checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                        log("anEAR must have Audio Record permission to record audio");
                        return;
                    }
                }
                //Start Recording if Audio Recordings enabled
                if (SettingsActivity.getBoolean(this, SettingsActivity.KEY_AUDIO_ENABLE, false)) {
                    startAudio(true, false);
                }
                break;
            case ACTION_AUDIO_CREATE:
                log("ACTION_AUDIO_CREATE");
                setAudioAlarm();
                break;
            case ACTION_AUDIO_CANCEL:
                log("ACTION_AUDIO_CANCEL");
                cancelAlarm();
                break;
            case ACTION_AUDIO_SAMPLE:
                log("ACTION_AUDIO_SAMPLE");
                startAudio(false,true);
                break;
        }
    }

    /** setAudioAlarm
     *
     * Creates the time interval in which the device should wait before recording audio and creating
     * a WAV file.
     *
     * This gathers information from the settings and creates an alarm to wake up the phone and start recording
     * audio based on that information.
     */
    private void setAudioAlarm() {
        int audioDuration = Integer.parseInt(SettingsActivity.getString(this, SettingsActivity.KEY_AUDIO_DURATION, "30"));
        int audioDelay = Integer.parseInt(SettingsActivity.getString(this, SettingsActivity.KEY_AUDIO_DELAY, "12")) * 60;
        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.SECOND, audioDelay + audioDuration);

        alarmManager = (AlarmManager) getBaseContext().getSystemService(ALARM_SERVICE);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT)
        {
            alarmManager.setExact(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), getPendingIntent(AlarmReceiver.AUDIO_ID, ACTION_AUDIO_START));

        } else {
            alarmManager.set(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), getPendingIntent(AlarmReceiver.AUDIO_ID, ACTION_AUDIO_START));
        }
    }

    //Stops the alarm
    private void cancelAlarm() {
        PendingIntent pendingIntent;
        pendingIntent = getPendingIntent(AlarmReceiver.AUDIO_ID, ACTION_AUDIO_START);
        stopService(new Intent(this, AudioRecorderService.class));
        ((AlarmManager) getSystemService(ALARM_SERVICE)).cancel(pendingIntent);
    }

    //Receives an action that the manager will then handle
    private PendingIntent getPendingIntent(int id, int action) {
        Intent intent = new Intent(this, AlarmReceiver.class);
        intent.putExtra(AlarmReceiver.KEY_ALARM_ID, id);
        intent.putExtra(AlarmReceiver.KEY_ACTION, action);

        return PendingIntent.getBroadcast(this, id, intent, PendingIntent.FLAG_UPDATE_CURRENT);
    }

    /** startAudio()
     *
     * Starts the audio recording.
     *
     * While the code for receiving audio data lies in the AudioRecordService, this method starts that process,
     * as well as create the file name and path for the .wav file.
     *
     * Also, this checks to see whether or not the audio is being recorded during a blackout time if the blackout
     * setting has been enabled in the settings. If so, this will not be executed during school hours.
     *
     * @param trigger: Whether or not the audio has been started by the patient's heart rate.
     */
    private void startAudio(boolean trigger, boolean sample) {
        //Checks to see if the time is during the blackout period
        //Get time
        Calendar currentTime = Calendar.getInstance();
        //Check if between 1am and 5am
        Calendar calendar1am = Calendar.getInstance();
        calendar1am.set(Calendar.HOUR_OF_DAY, 1);
        Calendar calendar5am = Calendar.getInstance();
        calendar5am.set(Calendar.HOUR_OF_DAY, 5);
        if (currentTime.compareTo(calendar1am) > 0) {
            //After 1am
            if (currentTime.compareTo(calendar5am) < 0) {
                //Before 5am
                log("In Blackout Time");
                return;
            }
        }

        if (SettingsActivity.getBoolean(this, SettingsActivity.KEY_BLACKOUT_TOGGLE, false)) {
            //Checks if time is during school hours
            Calendar calendar730am = Calendar.getInstance();
            calendar730am.set(Calendar.HOUR_OF_DAY, 7);
            calendar730am.set(Calendar.MINUTE, 30);
            Calendar calendar3pm = Calendar.getInstance();
            calendar3pm.set(Calendar.HOUR_OF_DAY, 15);

            String weekDay;
            SimpleDateFormat dayFormat = new SimpleDateFormat("EEEE", Locale.US);

            Calendar calendar = Calendar.getInstance();
            weekDay = dayFormat.format(calendar.getTime());

            //  Checks for the weekend
            if(!weekDay.equals("Saturday") && !weekDay.equals("Sunday"))
            {
                //  Checks the time
                if (currentTime.compareTo(calendar730am) > 0)
                {
                    //After 7:30am
                    if (currentTime.compareTo(calendar3pm) < 0)
                    {
                        //Before 3pm
                        log("In Blackout Time");
                        return;
                    }
                }
            }
        }

        // Creates the name of the WAV file
        File directory;
        if(sample)
        {
            directory = new File(MainActivity.getRootFile(this), "/Sample_Audio");
            if (!directory.exists()) {
                if (directory.mkdirs()) {
                    log("Made parent directories");
                }
            }
        }
        else
            directory = MainActivity.getRootFile(this);
        String wavFileName = "";
        String p_id = SettingsActivity.getString(this, SettingsActivity.KEY_IDENTIFIER, null);
        if (p_id != null && p_id != "") {
            wavFileName += p_id + "_";
        }
        wavFileName += (new SimpleDateFormat("MM_dd_yyyy", Locale.US).format(new Date()) + "_");
        wavFileName += (new SimpleDateFormat("HH.mm.ss", Locale.US).format(new Date()) + "_");
        int count = 0;
        if (directory != null) {
            for (File file : directory.listFiles()) {
                if (file.getPath().endsWith(".wav"))
                {
                    count++;
                }
            }
        }

        // Adds a heart rate extension to the file name if it was triggered
        // by the heart rate listener
        if(trigger)
            wavFileName += count + ".HR.wav";
        else if(sample)
            wavFileName += count + ".SAMP.wav";
        else
            wavFileName += count + ".wav";


        // Adds the raw audio file to the directory
        File file = new File(directory, wavFileName);
        File temp = new File(directory, "raw_audio.tmp");
        File audioRecordLog = new File(directory, "AudioRecordLog.csv");

        // Adds the WAV file to the directory
        Intent intent = new Intent(this, AudioRecorderService.class);
        intent.putExtra(AudioRecorderService.INTENT_WAV_FILE, file.getAbsolutePath());
        intent.putExtra(AudioRecorderService.INTENT_TEMP_FILE, temp.getAbsolutePath());
        intent.putExtra(AudioRecorderService.INTENT_LOG_FILE, audioRecordLog.getAbsolutePath());
        intent.putExtra(AudioRecorderService.INTENT_AUDIO_TRIGGER, trigger);
        intent.putExtra(AudioRecorderService.INTENT_AUDIO_SAMPLE, sample);
        startService(intent);
    }

    private void log(String message) {
        Log.d(this.getClass().getSimpleName(), message);
    }
}
