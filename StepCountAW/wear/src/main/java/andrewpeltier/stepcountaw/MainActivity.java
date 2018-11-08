package andrewpeltier.stepcountaw;

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
import com.google.android.gms.wearable.DataApi;
import com.google.android.gms.wearable.PutDataMapRequest;
import com.google.android.gms.wearable.PutDataRequest;
import com.google.android.gms.wearable.Wearable;

public class MainActivity extends WearableActivity implements SensorEventListener, GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener{
    private static final String TAG = "MainActivity(Wear)";
    private static final String STEP_COUNTER_DATA_ITEM = "/step_counter";
    private static final String STEP_COUNTER_VALUE = "/step_counter_value";
    private static final String STEP_COUNTER_ACCURACY = "/step_counter_accuracy";

    private SensorManager mSensorManager;
    private Sensor mStepCounterSensor;
    private GoogleApiClient googleApiClient;
    private TextView mTextView;

    private int mSteps;
    private int mAccuracy = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mTextView = (TextView) findViewById(R.id.text);

        mSensorManager = (SensorManager)getSystemService(SENSOR_SERVICE);
        //TODO: Maybe change to the counter instead
        mStepCounterSensor = mSensorManager.getDefaultSensor(Sensor.TYPE_STEP_COUNTER);
        mSensorManager.registerListener(this, mStepCounterSensor, SensorManager.SENSOR_DELAY_FASTEST);

        googleApiClient = new GoogleApiClient.Builder(this)
                .addApiIfAvailable(Wearable.API)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .build();
        googleApiClient.connect();

        // Enables Always-on
        setAmbientEnabled();
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        if (event.sensor.getType() == Sensor.TYPE_STEP_COUNTER)
        {
            String msg = "" + (int)event.values[0];
            mSteps = (int) event.values[0];
            mTextView.setText(msg);
            Log.d(TAG, msg);
            sendStepCounterData();
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
        if(sensor.getType() == Sensor.TYPE_STEP_COUNTER)
        {
            Log.d(TAG, "onAccuracyChanged - accuracy: " + accuracy);
            mAccuracy = accuracy;
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

    private void sendStepCounterData()
    {
        PutDataMapRequest putDataMapRequest = PutDataMapRequest.create(STEP_COUNTER_DATA_ITEM);

        putDataMapRequest.getDataMap().putInt(STEP_COUNTER_VALUE, mSteps);
        putDataMapRequest.getDataMap().putInt(STEP_COUNTER_ACCURACY, mAccuracy);

        PutDataRequest request = putDataMapRequest.asPutDataRequest();
        request.setUrgent();
        Wearable.DataApi.putDataItem(googleApiClient, request)
                .setResultCallback(new ResultCallback<DataApi.DataItemResult>() {
                    @Override
                    public void onResult(DataApi.DataItemResult dataItemResult)
                    {

                    }
                });
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
