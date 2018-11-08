package com.example.alli.anearaw.services;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.IBinder;
import android.os.PowerManager;
import android.os.Vibrator;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;

import com.example.alli.anearaw.activities.MainActivity;
import com.example.alli.anearaw.csv.GenerateDirectory;
import com.example.alli.anearaw.enums.Action;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.wearable.DataClient;
import com.google.android.gms.wearable.DataEvent;
import com.google.android.gms.wearable.DataEventBuffer;
import com.google.android.gms.wearable.DataItem;
import com.google.android.gms.wearable.DataMap;
import com.google.android.gms.wearable.DataMapItem;
import com.google.android.gms.wearable.Wearable;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

import static com.example.alli.anearaw.activities.MainActivity.KEY_HR_TRIGGER;

public class WearListenerService extends Service implements DataClient.OnDataChangedListener, GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener
{
    private static final String TAG = "WearListenerService";
    private static final String HR_HEADER = "Date, Time, Heart Rate, Accuracy";
    private static final String ACCELEROMETER_HEADER = "Date, Time, AX, AY, AZ";
    private static final String GYROSCOPE_HEADER = "Date, Time, GX, GY, GZ";
    private static final String HEART_RATE_FILENAME = "hr";
    private static final String ACCELEROMETER_FILENAME = "accel";
    private static final String GYROSCOPE_FILENAME = "gyro";
    private static final String HEART_RATE_DATA_ITEM = "/heart_rate";
    private static final String HEART_RATE_VALUE = "/heart_rate_value";
    private static final String HEART_RATE_ACCURACY = "/heart_rate_accuracy";
    private static final String DATA_ITEM = "/data_item";
    private static final String ACCELETOMETER_X_ = "accelerometer_x";
    private static final String ACCELETOMETER_Y_ = "accelerometer_y";
    private static final String ACCELETOMETER_Z_ = "accelerometer_z";
    private static final String GYROSCOPE_X_ = "gyroscope_x";
    private static final String GYROSCOPE_Y_ = "gyroscope_y";
    private static final String GYROSCOPE_Z_ = "gyroscope_z";
    public static final String INTENT_RECORD = "intent_record";
    public static final String INTENT_DURATION = "intent_duration";
    private final int NOTIFICATION_ID = 43;

    private GoogleApiClient googleApiClient;
    private CountDownTimer startTimer, stopTimer;
    private Context mContext;
    boolean recording;
    private PowerManager.WakeLock mWakeLock;

    @Override
    public void onCreate()
    {
        super.onCreate();
        mContext = this;

        //  Allows the application to wake up the phone when needed
        mWakeLock = ((PowerManager) getSystemService(POWER_SERVICE)).newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "BandWakeLock");
        mWakeLock.acquire();
        if (mWakeLock.isHeld()) {
            Log.i(TAG, "Wake Lock Acquired");
        }

        //Declare as Foreground Service
        Notification notification = new Notification.Builder(this)
                .setContentTitle("ED EAR Active")
                .setSmallIcon(android.R.drawable.presence_invisible)
                .setContentText("EAR is Starting")
                .build();
        startForeground(NOTIFICATION_ID, notification);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId)
    {
        if(intent == null || intent.getAction() == null)
        {
            Log.w(TAG, "Invalid Action Packet Received");
            return super.onStartCommand(intent, flags, startId);
        }

        Action action = Action.getAction(intent.getAction());
        if(action == null) {
            Log.w(TAG, "Invalid Action Packet Received");
            return super.onStartCommand(intent, flags, startId);
        }

        switch (action)
        {
            case enabled:
                startRecording();
                Vibrator vibrator = (Vibrator) getSystemService(VIBRATOR_SERVICE);
                vibrator.vibrate(500);
                break;
            case disabled:
                stopRecording();
                updateNotification("DISCONNECTED", android.R.drawable.presence_offline);
                break;
            case p_enabled:
                if(recording)
                    startRecording();
                break;
            case p_disabled:
                if(recording)
                    startTimer.cancel();
                else
                    startRecording();
                break;
        }

        return super.onStartCommand(intent, flags, startId);
    }

    private void start()
    {
        Log.i(TAG, "Started recording.");
        googleApiClient = new GoogleApiClient.Builder(this)
                .addApiIfAvailable(Wearable.API)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .build();
        googleApiClient.connect();
        recording = true;
    }

    private void stop() {
        Log.i(TAG, "Stopped recording.");
        Wearable.getDataClient(this).removeListener(this);
        if (googleApiClient != null)
            googleApiClient.disconnect();
        recording = false;
    }

    private void startRecording()
    {
        start();
        updateNotification("STREAMING", android.R.drawable.presence_online);
        if(MainActivity.getBoolean(mContext, MainActivity.KEY_PSENSOR_ENABLE, false))
        {
            int dur = Integer.parseInt(MainActivity.getString(mContext, MainActivity.KEY_PSENSOR_DURATION, "3")) * 60000;
            startTimer = new CountDownTimer(dur,dur)
            {
                @Override
                public void onTick(long millisUntilFinished) {}

                @Override
                public void onFinish()
                {
                    stopRecording();
                    updateNotification("POWER-SAVING", android.R.drawable.presence_away);
                }
            };
            startTimer.start();
            Log.i(TAG, "Timer started.");
        }
    }
    private void stopRecording()
    {
        stop();
        if(MainActivity.getBoolean(mContext, MainActivity.KEY_PSENSOR_ENABLE, false))
        {
            int dur = Integer.parseInt(MainActivity.getString(mContext, MainActivity.KEY_PSENSOR_DURATION, "3")) * 60000;
            stopTimer = new CountDownTimer(dur, dur) {
                @Override
                public void onTick(long millisUntilFinished) { }

                @Override
                public void onFinish()
                {
                    startRecording();
                }
            };
            stopTimer.start();
            Log.i(TAG, "Timer stopped.");
        }
    }

    @Override
    public void onDataChanged(DataEventBuffer dataEventBuffer)
    {
        for (DataEvent event : dataEventBuffer) {
            if (event.getType() == DataEvent.TYPE_CHANGED) {
                DataItem item = event.getDataItem();
                DataMap dataMap = null;
                Date date = Calendar.getInstance().getTime();
                String dateString = new SimpleDateFormat("MM/dd/yyyy", Locale.US).format(date);
                String timeString = new SimpleDateFormat("kk:mm:ss:SSS", Locale.US).format(date);
                if (item.getUri().getPath().equals(DATA_ITEM)) {
                    dataMap = DataMapItem.fromDataItem(item).getDataMap();
                    int hrVal = dataMap.getInt(HEART_RATE_VALUE);
                    int hrAcc = dataMap.getInt(HEART_RATE_ACCURACY);
                    Log.d(TAG, "Heart Rate: " + hrVal + '\t' + "Accuracy: " + hrAcc);

                    float acc_x_ = dataMap.getFloat(ACCELETOMETER_X_);
                    float acc_y_ = dataMap.getFloat(ACCELETOMETER_Y_);
                    float acc_z_ = dataMap.getFloat(ACCELETOMETER_Z_);
                    Log.d(TAG, "Accelerometer: " + acc_x_ + '\t' + acc_y_ + '\t' + acc_z_);

                    float gyro_x_ = dataMap.getFloat(GYROSCOPE_X_);
                    float gyro_y_ = dataMap.getFloat(GYROSCOPE_Y_);
                    float gyro_z_ = dataMap.getFloat(GYROSCOPE_Z_);
                    Log.d(TAG, "Gyroscope: " + gyro_x_ + '\t' + gyro_y_ + '\t' + gyro_z_);

                    if(hrVal >= Integer.parseInt(MainActivity.getString(mContext, KEY_HR_TRIGGER, "100")))
                    {
                        Log.d(TAG, "Triggered HR: " + hrVal);
                        AudioRecordManager.start(mContext, AudioRecordManager.ACTION_AUDIO_TRIGGER);
                    }

                    String data = dateString + "," + timeString + "," + hrVal + "," + hrAcc;
                    DataLogService.log(mContext, new File(GenerateDirectory.getRootFile(mContext), HEART_RATE_FILENAME), data, HR_HEADER);

                    data = dateString + "," + timeString + ","
                            + acc_x_ + "," + acc_y_ + "," + acc_z_;
                    DataLogService.log(mContext, new File(GenerateDirectory.getRootFile(mContext), ACCELEROMETER_FILENAME), data, ACCELEROMETER_HEADER);

                    data = dateString + "," + timeString + ","
                            + gyro_x_ + "," + gyro_y_ + "," + gyro_z_;
                    DataLogService.log(mContext, new File(GenerateDirectory.getRootFile(mContext), GYROSCOPE_FILENAME), data, GYROSCOPE_HEADER);
                }
//                else if(item.getUri().getPath().equals(HEART_RATE_DATA_ITEM))
//                {
//                    dataMap = DataMapItem.fromDataItem(item).getDataMap();
//                    int hrVal = dataMap.getInt(HEART_RATE_VALUE);
//                    int hrAcc = dataMap.getInt(HEART_RATE_ACCURACY);
//                    Log.d(TAG, "Heart Rate: " + hrVal + '\t' + "Accuracy: " + hrAcc);
//
//                    if(hrVal >= Integer.parseInt(MainActivity.getString(mContext, KEY_HR_TRIGGER, "100")))
//                    {
//                        Log.d(TAG, "Triggered HR: " + hrVal);
//                        AudioRecordManager.start(mContext, AudioRecordManager.ACTION_AUDIO_TRIGGER);
//                    }
//
//                    String data = dateString + "," + timeString + "," + hrVal + "," + hrAcc;
//                    DataLogService.log(mContext, new File(GenerateDirectory.getRootFile(mContext), HEART_RATE_FILENAME), data, HR_HEADER);
//                }
                else
                    Log.e(TAG, "Data Item error");
            }
        }
    }

    //  Updates the streaming status icon on the top of the phone
    private void updateNotification(String status, int icon) {
        NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        Notification.Builder notificationBuilder = new Notification.Builder(this)
                .setContentTitle("ED EAR Band")
                .setContentText("Band Status: " + status)
                .setSmallIcon(icon);
        notificationManager.notify(NOTIFICATION_ID, notificationBuilder.build());
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {
        Log.e(TAG, "Connected to GoogleAPIClient!");
        Wearable.getDataClient(this).addListener(this);
    }

    @Override
    public void onConnectionSuspended(int i) {
        Log.d(TAG, "Google Api Client connection suspended");
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        Log.d(TAG, "Google Api Client connection failed");
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}
