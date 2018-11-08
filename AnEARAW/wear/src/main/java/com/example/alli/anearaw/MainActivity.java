package com.example.alli.anearaw;

import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.wearable.activity.WearableActivity;
import android.util.Log;
import android.widget.TextView;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.wearable.DataApi;
import com.google.android.gms.wearable.DataClient;
import com.google.android.gms.wearable.DataItem;
import com.google.android.gms.wearable.MessageApi;
import com.google.android.gms.wearable.Node;
import com.google.android.gms.wearable.NodeApi;
import com.google.android.gms.wearable.PutDataMapRequest;
import com.google.android.gms.wearable.PutDataRequest;
import com.google.android.gms.wearable.Wearable;

public class MainActivity extends WearableActivity implements SensorEventListener, GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener
{
    private static final String TAG = "MainActivity(Wear)";
    private static final String HEART_RATE_DATA_ITEM = "/heart_rate";
    private static final String DATA_ITEM = "/data_item";
    private static final String HEART_RATE_VALUE = "/heart_rate_value";
    private static final String ACCELETOMETER_X_ = "accelerometer_x";
    private static final String ACCELETOMETER_Y_ = "accelerometer_y";
    private static final String ACCELETOMETER_Z_ = "accelerometer_z";
    private static final String HEART_RATE_ACCURACY = "/heart_rate_accuracy";
    private static final String GYROSCOPE_X_ = "gyroscope_x";
    private static final String GYROSCOPE_Y_ = "gyroscope_y";
    private static final String GYROSCOPE_Z_ = "gyroscope_z";

    private TextView        mTextViewHeart;
    private SensorManager   mSensorManager;
    private Sensor          mHeartRateSensor;
    private DataClient      mDataClient;
    private Sensor          mAccelerometer;
    private Sensor          mGyroscope;

    private GoogleApiClient googleApiClient;
    private int             mAccuracy = -1;
    private int             mHeartRate;
    private float           mX;
    private float           mY;
    private float           mZ;
    private float           gX;
    private float           gY;
    private float           gZ;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mTextViewHeart = findViewById(R.id.text);

        mSensorManager = (SensorManager)getSystemService(SENSOR_SERVICE);
        mHeartRateSensor = mSensorManager.getDefaultSensor(Sensor.TYPE_HEART_RATE);
        mSensorManager.registerListener(this, mHeartRateSensor, SensorManager.SENSOR_DELAY_FASTEST);

        mSensorManager = (SensorManager)getSystemService(SENSOR_SERVICE);
        mAccelerometer = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        mSensorManager.registerListener(this,mAccelerometer,SensorManager.SENSOR_DELAY_NORMAL);

        mSensorManager = (SensorManager)getSystemService(SENSOR_SERVICE);
        mGyroscope = mSensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE);
        mSensorManager.registerListener(this,mGyroscope,SensorManager.SENSOR_DELAY_NORMAL);

        googleApiClient = new GoogleApiClient.Builder(this)
                .addApiIfAvailable(Wearable.API)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .build();
        googleApiClient.connect();

        mDataClient = Wearable.getDataClient(this);

        // Enables Always-on
        setAmbientEnabled();
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        if (event.sensor.getType() == Sensor.TYPE_HEART_RATE)
        {
            String msg = "" + (int)event.values[0];
            mHeartRate = (int) event.values[0];
            mTextViewHeart.setText(msg);
            sendData(DATA_ITEM);
        }
        else if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER){
            mX = event.values[0];
            mY = event.values[1];
            mZ = event.values[2];
            sendData(DATA_ITEM);
        }
        else if (event.sensor.getType() == Sensor.TYPE_GYROSCOPE){
            gX = event.values[0];
            gY = event.values[1];
            gZ = event.values[2];
            sendData(DATA_ITEM);
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
        if(sensor.getType() == Sensor.TYPE_HEART_RATE)
        {
            Log.d(TAG, "onAccuracyChanged - accuracy: " + accuracy);
            mAccuracy = accuracy;
        }
        else if(sensor.getType() == Sensor.TYPE_ACCELEROMETER){
            Log.d(TAG, "onAccuracyChanged - accuracy ACC: " + accuracy);
            //mAccuracy = accuracy;
        }
        else if (sensor.getType() == Sensor.TYPE_GYROSCOPE){
            Log.d(TAG, "onAccuracyChanged - accuracy GRYO: " + accuracy);
        }
    }
    @Override
    protected void onStart() {
        super.onStart();
    }

    @Override
    protected void onDestroy()
    {
        googleApiClient.disconnect();
        super.onDestroy();
    }

    private void sendData(String item)
    {
        PutDataMapRequest putDataMapRequest = null;

//        if(item.equals(HEART_RATE_DATA_ITEM))
//        {
//            putDataMapRequest = PutDataMapRequest.create(HEART_RATE_DATA_ITEM);
//            putDataMapRequest.getDataMap().putInt(HEART_RATE_VALUE, mHeartRate);
//            putDataMapRequest.getDataMap().putInt(HEART_RATE_ACCURACY, mAccuracy);
//        }
        if(item.equals(DATA_ITEM))
        {
            putDataMapRequest = PutDataMapRequest.create(DATA_ITEM);
            putDataMapRequest.getDataMap().putInt(HEART_RATE_VALUE, mHeartRate);
            putDataMapRequest.getDataMap().putInt(HEART_RATE_ACCURACY, mAccuracy);
            putDataMapRequest.getDataMap().putFloat(ACCELETOMETER_X_,mX);
            putDataMapRequest.getDataMap().putFloat(ACCELETOMETER_Y_,mY);
            putDataMapRequest.getDataMap().putFloat(ACCELETOMETER_Z_,mZ);
            putDataMapRequest.getDataMap().putFloat(GYROSCOPE_X_,gX);
            putDataMapRequest.getDataMap().putFloat(GYROSCOPE_Y_,gY);
            putDataMapRequest.getDataMap().putFloat(GYROSCOPE_Z_,gZ);
        }

        if(putDataMapRequest != null)
        {
            PutDataRequest request = putDataMapRequest.asPutDataRequest();
            request.setUrgent();
            Task<DataItem> putDataTask = mDataClient.putDataItem(request);
        }
        else
            Log.e(TAG, "Data item error: Data item is null");
    }

    /**
     * Resolve the node = the connected device to send the message to
     */

    @Override
    public void onConnected(@Nullable Bundle bundle) {Log.w(TAG, "Connected!");}
    @Override
    public void onConnectionSuspended(int i) {}
    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {}
}
