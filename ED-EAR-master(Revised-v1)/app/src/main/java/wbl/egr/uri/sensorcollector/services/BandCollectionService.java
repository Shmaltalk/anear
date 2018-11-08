package wbl.egr.uri.sensorcollector.services;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.CountDownTimer;
import android.os.IBinder;
import android.os.PowerManager;
import android.os.Vibrator;
import android.util.Log;
import android.widget.Toast;

import com.microsoft.band.BandClient;
import com.microsoft.band.BandClientManager;
import com.microsoft.band.BandConnectionCallback;
import com.microsoft.band.BandException;
import com.microsoft.band.BandIOException;
import com.microsoft.band.BandInfo;
import com.microsoft.band.BandResultCallback;
import com.microsoft.band.ConnectionState;
import com.microsoft.band.InvalidBandVersionException;
import com.microsoft.band.notifications.VibrationType;
import com.microsoft.band.sensors.BandSensorManager;
import com.microsoft.band.sensors.GsrSampleRate;
import com.microsoft.band.sensors.SampleRate;

import java.util.concurrent.TimeUnit;

import wbl.egr.uri.sensorcollector.activities.SettingsActivity;
import wbl.egr.uri.sensorcollector.band_listeners.BandAccelerometerListener;
import wbl.egr.uri.sensorcollector.band_listeners.BandAmbientLightListener;
import wbl.egr.uri.sensorcollector.band_listeners.BandContactListener;
import wbl.egr.uri.sensorcollector.band_listeners.BandGsrListener;
import wbl.egr.uri.sensorcollector.band_listeners.BandHeartRateListener;
import wbl.egr.uri.sensorcollector.band_listeners.BandRRIntervalListener;
import wbl.egr.uri.sensorcollector.band_listeners.BandSkinTemperatureListener;
import wbl.egr.uri.sensorcollector.receivers.BandContactStateReceiver;
import wbl.egr.uri.sensorcollector.receivers.BandUpdateReceiver;
import wbl.egr.uri.sensorcollector.receivers.TestBandReceiver;

/**
 * Created by Matt Constant on 2/22/17.
 *
 * The BandCollectionService is the Service that is responsible for connecting to the Microsoft
 * Band, retrieving data about the Band, collecting Sensor data from the Band, and disconnecting
 * from the Band. This is the only component in this Application that has access to the Band.
 * The reason for this is to make interacting with the Band simpler, as well as insuring that
 * the Band is always connected to and disconnected from safely.
 *
 * Other components can receive information about the Band (such as when it is connected, the
 * connected Band's name, the connected Band's address, ect.) through this Service. For example, to
 * get the name of the connected Band, another component can call this Service's
 * requestBandInfo(Context) static method and register a BandUpdateReceiver. Once this Service
 * obtains the information, it will broadcast the information to all registered BandUpdateReceivers.
 *
 * This Service is meant to collect data from the Band indefinitely, therefore it is declared as
 * a foreground service when it is created to prevent it from being shutdown by the Android OS
 * unnecessarily. The process to properly begin sensor collection and then eventually stop is as
 * follows:
 *              BandCollectionService.connect(Context)
 *              BandCollectionService.startStreaming(Context)
 *              ...
 *              BandCollectionService.stopStreaming(Context)    [optional]
 *              BandCollectionService.disconnect(Context)
 *
 */

public class BandCollectionService extends Service {
    //Actions provided by this Service
    /**
     * When an Intent is received with this action, BandCollectionService attempts to open a
     * connection with a Microsoft Band.
     */
    public static final String ACTION_CONNECT = "uri.wbl.ear.action_connect";
    /**
     * When an Intent is received with this action, BandCollectionService attempts to close any
     * currently open connections to Microsoft Bands.
     */
    public static final String ACTION_DISCONNECT = "uri.wbl.ear.action_disconnect";
    /**
     * When an Intent is received with this action, BandCollectionService attempts to begin
     * collecting sensor data from the connected Microsoft Band.
     */
    public static final String ACTION_START_STREAMING = "uri.wbl.ear.action_start_streaming";
    /**
     * When an Intent is received with this action, BandCollectionService attempts to stop
     * collecting data from the connected Microsoft Band.
     */
    public static final String ACTION_STOP_STREAMING = "uri.wbl.ear.action_stop_streaming";
    /**
     * When an Intent is received with this action, BandCollectionService retrieves the connected
     * Band's name and address and broadcasts this information in a String array to all registered
     * BandUpdateReceivers.
     */
    public static final String ACTION_GET_INFO = "uri.wbl.ear.action_get_info";
    /**
     * When an Intent is received with this action, BandCollectionService restarts itself and sends
     * a broadcast to all registered TestBandReceivers to notify that the service is still working
     */
    public static final String ACTION_TEST_SERVICE = "uri.wbl.ear.action_test_service";

    public static final String EXTRA_AUTO_STREAM = "uri.egr.wbl.extra_auto_stream";

    //States
    public static final int STATE_CONNECTED = 0;
    public static final int STATE_STREAMING = 1;
    public static final int STATE_DISCONNECTED = 2;
    public static final int STATE_NOT_WORN = 3;
    public static final int STATE_PAUSED = 4;
    public static final int STATE_OTHER = 5;
    private static final int REQUEST_CODE = 114;

    public static void connect(Context context) {
        Intent intent = new Intent(context, BandCollectionService.class);
        intent.setAction(ACTION_CONNECT);
        context.startService(intent);
    }

    public static void connect(Context context, boolean autoStream) {
        Intent intent = new Intent(context, BandCollectionService.class);
        intent.setAction(ACTION_CONNECT);
        intent.putExtra(EXTRA_AUTO_STREAM, autoStream);
        context.startService(intent);
    }

    /** startStream **
     *
     * Called by one of the following...
     * 1. Enabling Sensor Recordings in the settings
     * 2. When the phone is turned back on after being shut off while streaming
     * 3. Testing the application by using "My Nightly Check"
     *
     * This starts the streaming process from the beginning, setting up the device
     * to update its streaming status and to activate the data aggregation.
     *
     */
    public static void startStream(Context context)
    {
        Intent intent = new Intent(context, BandCollectionService.class);
        intent.setAction(ACTION_START_STREAMING);
//        PendingIntent pendIntent = PendingIntent.getBroadcast(context, REQUEST_CODE, intent, PendingIntent.FLAG_UPDATE_CURRENT);
//        AlarmManager alarmManager = (AlarmManager) context.getSystemService(ALARM_SERVICE);
//        alarmManager.setInexactRepeating(AlarmManager.ELAPSED_REALTIME_WAKEUP, SystemClock.elapsedRealtime(), 360000, pendIntent);
        context.startService(intent);
    }

//    public static void stopStream(Context context) {
//        Intent intent = new Intent(context, BandCollectionService.class);
//        intent.setAction(ACTION_STOP_STREAMING);
//        context.startService(intent);
//    }

    /** requestBandInfo
     *
     * Starts the process of gathering the band's information.
     * This is called after the band is already connected, and is
     * mainly for the user to see when starting to stream sensor data.
     *
     */
    public static void requestBandInfo(Context context) {
        Intent intent = new Intent(context, BandCollectionService.class);
        intent.setAction(ACTION_GET_INFO);
        context.startService(intent);
    }

    /** disconnect
     *
     * Starts the process of disconnecting the band.
     * This completely stops periodic recording of the band's sensors.
     *
     * Called by one of the following:
     * 1. Disable Sensor Recordings from the settings
     * 2. Either the microsoft band or the phone dies or gets shut off
     *
     */
    public static void disconnect(Context context) {
        Intent intent = new Intent(context, BandCollectionService.class);
        intent.setAction(ACTION_DISCONNECT);
        context.startService(intent);
    }

    /** test
     *
     * Tests the sensor data aggregation
     *
     * This is called from the My Nightly Check, and is used to determine
     * whether or not the application is receiving sensor data from the
     * Microsoft Band.
     */
    public static void test(Context context) {
        Intent intent = new Intent(context, BandCollectionService.class);
        intent.setAction(ACTION_TEST_SERVICE);
        context.startService(intent);
    }

    private final int NOTIFICATION_ID = 43;

    private BandClientManager mBandClientManager;
    private BandClient mBandClient;
    private BandAccelerometerListener mBandAccelerometerListener;
    private BandAmbientLightListener mBandAmbientLightListener;
    private BandContactListener mBandContactListener;
    private BandGsrListener mBandGsrListener;
    private BandHeartRateListener mBandHeartRateListener;
    private BandRRIntervalListener mBandRRIntervalListener;
    private BandSkinTemperatureListener mBandSkinTemperatureListener;

    private Context mContext;
    private String mBandName;
    private String mBandAddress;
    private boolean mAutoStream;
    private PowerManager.WakeLock mWakeLock;

    private int mState;

    private CountDownTimer  startTimer,
                            stopTimer;

    /** mBandContactStateReceiver
     *
     * Receives information from the Microsoft Band regarding its contact state. This executes
     * code whenever the band detects that its contact state (being worn / not being worn) has changed.
     */
    private BandContactStateReceiver mBandContactStateReceiver = new BandContactStateReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getBooleanExtra(BAND_STATE, false)) {
                resumeFromDynamicBlackout();
            } else {
                enterDynamicBlackout();
            }
        }
    };

    /** mBandConnectResultCallback
     *
     * This handles the actions that should take place depending on the connection
     * state of the MicrosoftBand. These various actions will update the phone's UI,
     * influence the sensor data aggregation, and influence the connectivity between
     * the phone and the MicrosoftBand.
     */
    private BandResultCallback<ConnectionState> mBandConnectResultCallback = new BandResultCallback<ConnectionState>() {
        @Override
        public void onResult(ConnectionState connectionState, Throwable throwable) {
            switch (connectionState) {
                case CONNECTED:
                    log("Connected");
                    mState = STATE_CONNECTED;
                    updateNotification("CONNECTED", android.R.drawable.presence_away);
                    try {
                        mBandClient.getNotificationManager().vibrate(VibrationType.RAMP_UP);
                    } catch (BandException e) {
                        e.printStackTrace();
                    }

                    //  AutoStream is called if the device reconnects after a disconnect
                    //  it quickly starts streaming if the device was streaming
                    //  before it disconnected
                    if (mAutoStream) {
                        startStreaming();
                    }

                    mBandClient.registerConnectionCallback(mBandConnectionCallback);

                    mState = STATE_CONNECTED;

                    //Broadcast Update
                    log("Broadcasting Update");
                    Intent intent = new Intent(BandUpdateReceiver.INTENT_FILTER.getAction(0));
                    intent.putExtra(BandUpdateReceiver.UPDATE_BAND_CONNECTED, true);
                    sendBroadcast(intent);
                    break;
                case BOUND:
                    log("Bound");
                    //  Completely disconnects the streaming
                    //  Ex. This is called when the sensor test fails
                    mState = STATE_DISCONNECTED;
                    updateNotification("DISCONNECTED", android.R.drawable.presence_offline);
                    Toast.makeText(mContext, "Could not connect to Band", Toast.LENGTH_LONG).show();
//                    disconnect();
                    break;
                case BINDING:
                    log("Binding");
                    break;
                case UNBOUND:
                    log("Unbound");
                    //  Similar to BOUND, but does not completely disconnect the device
                    mState = STATE_DISCONNECTED;
                    updateNotification("UNBOUND", android.R.drawable.presence_busy);
                    Toast.makeText(mContext, "Could not connect to Band", Toast.LENGTH_LONG).show();
                    break;
                case UNBINDING:
                    log("Unbinding");
                    mState = STATE_OTHER;
                    break;
                default:
                    mState = STATE_OTHER;
                    log("Unknown State");
                    updateNotification("ERROR", android.R.drawable.presence_busy);
                    break;
            }
        }
    };

    /** mBandConnetionCallback
     *
     * This is called by actions given by the MicrosoftBand itself.
     *
     * These actions are similar to the actions of the mBandConnectResultCallback.
     * However, these are called by the MicrosoftBand when it detects that its connection
     * state has changed.
     */
    private BandConnectionCallback mBandConnectionCallback = new BandConnectionCallback() {
        @Override
        public void onStateChanged(ConnectionState connectionState) {
            switch (connectionState) {
                case BINDING:
                    log("Binding");
                    break;
                case BOUND:
                    log("Bound");
                    mState = STATE_DISCONNECTED;
                    updateNotification("DISCONNECTED", android.R.drawable.presence_offline);
                    break;
                case CONNECTED:
                    log("Connected");
                    mState = STATE_CONNECTED;
                    updateNotification("CONNECTED", android.R.drawable.presence_away);

                    if (mAutoStream || mState != STATE_PAUSED) {
                        startStreaming();
                    }

                    try {
                        mBandClient.getNotificationManager().vibrate(VibrationType.TWO_TONE_HIGH);
                    } catch (BandIOException e) {
                        e.printStackTrace();
                    }

                    mState = STATE_CONNECTED;

                    //Broadcast Update
                    log("Broadcasting Update");
                    Intent intent = new Intent(BandUpdateReceiver.INTENT_FILTER.getAction(0));
                    intent.putExtra(BandUpdateReceiver.UPDATE_BAND_CONNECTED, true);
                    sendBroadcast(intent);
                    break;
                case UNBINDING:
                    log("Unbinding");
                    break;
                case UNBOUND:
                    log("Unbound");
                    mState = STATE_DISCONNECTED;
                    updateNotification("DISCONNECTED", android.R.drawable.presence_offline);
                    break;
                case INVALID_SDK_VERSION:
                    log("Invalid SDK Version");
                    mState = STATE_OTHER;
                    break;
                case DISPOSED:
                    log("Disposed");
                    mState = STATE_OTHER;
                    break;
            }
        }
    };

    private BandResultCallback<Void> mBandDisconnectResultCallback = new BandResultCallback<Void>() {
        @Override
        public void onResult(Void aVoid, Throwable throwable) {
            log("Disconnected");
            mState = STATE_DISCONNECTED;
            updateNotification("DISCONNECTED", android.R.drawable.presence_offline);
            stopSelf();
        }
    };

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();

        log("Service Created");
        mContext = this;
        mState = STATE_OTHER;
        mBandName = null;
        mBandAddress = null;
        mAutoStream = false;

        //  Allows the application to wake up the phone when needed
        mWakeLock = ((PowerManager) getSystemService(POWER_SERVICE)).newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "BandWakeLock");
        mWakeLock.acquire();
        if (mWakeLock.isHeld()) {
            log("Wake Lock Acquired");
        }

        //Declare as Foreground Service
        Notification notification = new Notification.Builder(this)
                .setContentTitle("ED EAR Active")
                .setSmallIcon(android.R.drawable.presence_invisible)
                .setContentText("EAR is Starting")
                .build();
        startForeground(NOTIFICATION_ID, notification);

        //  Receives the sensor information of the sensors we will aggregate data from
        mBandClientManager = BandClientManager.getInstance();
        mBandAccelerometerListener = new BandAccelerometerListener(this);
        mBandAmbientLightListener = new BandAmbientLightListener(this);
        mBandContactListener = new BandContactListener(this);
        mBandGsrListener = new BandGsrListener(this);
        mBandHeartRateListener = new BandHeartRateListener(this);
        mBandRRIntervalListener = new BandRRIntervalListener(this);
        mBandSkinTemperatureListener = new BandSkinTemperatureListener(this);

        registerReceiver(mBandContactStateReceiver, BandContactStateReceiver.INTENT_FILTER);
    }

    /** onStartCommand
     *
     * When the service is started, this method will handle different actions
     * depending on what the service was started for.
     */
    @Override
    public int onStartCommand(Intent intent, int flags, int startID) {
        if (flags == START_FLAG_REDELIVERY) {
            new Thread(new Runnable() {
                @Override
                public void run() {
                    try {
                        connect();
                        Thread.sleep(250);
                        startStreaming();
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }).start();
        } else {

            if (intent == null || intent.getAction() == null) {
                return START_NOT_STICKY;
            }

            log("Handling Intent");

            switch (intent.getAction()) {
                case ACTION_CONNECT:
                    //  Will automatically stream if its connecting to
                    //  a device that was already streaming
                    if (intent.hasExtra(EXTRA_AUTO_STREAM)) {
                        mAutoStream = true;
                    }
                    connect();
                    break;
                case ACTION_DISCONNECT:
                    disconnect();
                    break;
                case ACTION_START_STREAMING:
                    log("Stream Intent");
                    startStreaming();
                    break;
                case ACTION_STOP_STREAMING:
                    fullStopStreaming();
                    break;
                case ACTION_GET_INFO:
                    getInfo();
                    break;
                case ACTION_TEST_SERVICE:
                    Intent testIntent = new Intent(TestBandReceiver.INTENT_FILTER.getAction(0));
                    testIntent.putExtra(TestBandReceiver.EXTRA_STATE, mState);
                    sendBroadcast(testIntent);
                    if (mState == STATE_OTHER) {
                        stopSelf();
                    }
                default:
                    break;
            }
        }

        return START_REDELIVER_INTENT;
    }

    @Override
    public void onDestroy() {
        mWakeLock.release();
        unregisterReceiver(mBandContactStateReceiver);
        Vibrator vibrator = (Vibrator) getSystemService(VIBRATOR_SERVICE);
        vibrator.vibrate(500);
        log("Service Destroyed");
        super.onDestroy();
    }

    /*  Gathers the information from the MicrosoftBand
     *  that is necessary to connect to it.
     */
    private void connect() {
        if (mBandClientManager == null) {
            log("Connect Failed (Band Client Manager not Initialized)");
            return;
        }

        BandInfo[] pairedBands = mBandClientManager.getPairedBands();
        if (pairedBands == null || pairedBands.length == 0) {
            log("Connect Failed (No Bands are Paired with this Device)");
        } else if (pairedBands.length > 1) {
            /*
             * TODO
             * Implement UI to allow User to choose Band to pair to.
             * For now, always choose pairedBands[0]
             */
            connect(pairedBands[0]);
        } else {
            connect(pairedBands[0]);
        }
    }

    // Uses the information just gathered in order to connect to the band
    private void connect(BandInfo bandInfo) {
        log("Attempting to Connect to " + bandInfo.getMacAddress() + "...");
        mBandName = bandInfo.getName();
        mBandAddress = bandInfo.getMacAddress();
        mBandClient = mBandClientManager.create(this, bandInfo);
        mBandClient.connect().registerResultCallback(mBandConnectResultCallback);
    }

    //  Sends the information to the Settings for the user to see
    private void getInfo() {
        if (mBandClient == null || !mBandClient.isConnected()) {
            return;
        }

        String[] bandInfo = new String[2];
        bandInfo[0] = mBandName;
        bandInfo[1] = mBandAddress;

        //Broadcast Update
        Intent intent = new Intent(BandUpdateReceiver.INTENT_FILTER.getAction(0));
        intent.putExtra(BandUpdateReceiver.UPDATE_BAND_INFO, true);
        intent.putExtra(BandUpdateReceiver.EXTRA_BAND_INFO, bandInfo);
        this.sendBroadcast(intent);
    }

    /** startStreaming
     *
     * Starts streaming data from the Microsoft Band to the phone, and begins the periodic
     * recording timer. The streaming continues for three minutes before calling the
     * powerSaveMode method.
     */
    private void startStreaming() {
        log("Starting Stream");
        if (mBandClient == null || !mBandClient.isConnected()) {
            log("Band is not Connected");
            return;
        }

        if (mState != STATE_STREAMING) {

            try {Thread.sleep(250);}
            catch (InterruptedException e) {e.printStackTrace();}

            log(mBandClient.getSensorManager().getCurrentHeartRateConsent().name());
            BandSensorManager bandSensorManager = mBandClient.getSensorManager();
            try
            {
                log(bandSensorManager.getCurrentHeartRateConsent().name());
                mState = STATE_STREAMING;
                updateNotification("STREAMING", android.R.drawable.presence_online);
                bandSensorManager.registerAccelerometerEventListener(mBandAccelerometerListener, SampleRate.MS128);
                bandSensorManager.registerAmbientLightEventListener(mBandAmbientLightListener);
                bandSensorManager.registerContactEventListener(mBandContactListener);
                bandSensorManager.registerGsrEventListener(mBandGsrListener, GsrSampleRate.MS200);
                bandSensorManager.registerHeartRateEventListener(mBandHeartRateListener);
                bandSensorManager.registerRRIntervalEventListener(mBandRRIntervalListener);
                bandSensorManager.registerSkinTemperatureEventListener(mBandSkinTemperatureListener);
            }
            catch (BandException | InvalidBandVersionException e) {
                e.printStackTrace();
            }
            startTimer = new CountDownTimer(180000,180000)
            {
                @Override
                public void onTick(long millisUntilFinished) {}

                @Override
                public void onFinish() { powerSaveMode(); }
            };
            startTimer.start();
            log("Start Timer Started");
        }
    }

    /** powerSaveMode
     *
     * Stops recording most data. This unregisters every sensor besides the Ambient Light and Contact sensors for three minutes.
     * After the three minutes has passed, startStreaming is called again to start recording data from every sensor.
     */
    private void powerSaveMode() {
        if (mBandClient == null) {
            return;
        }

        if (mState == STATE_STREAMING || mState == STATE_NOT_WORN) {
            try {Thread.sleep(250);}
            catch (InterruptedException e) {e.printStackTrace();}

            BandSensorManager bandSensorManager = mBandClient.getSensorManager();
            try {
                bandSensorManager.unregisterAccelerometerEventListener(mBandAccelerometerListener);
//                bandSensorManager.unregisterAmbientLightEventListener(mBandAmbientLightListener);
//                bandSensorManager.unregisterContactEventListener(mBandContactListener);
                bandSensorManager.unregisterGsrEventListener(mBandGsrListener);
                bandSensorManager.unregisterHeartRateEventListener(mBandHeartRateListener);
                bandSensorManager.unregisterRRIntervalEventListener(mBandRRIntervalListener);
                bandSensorManager.unregisterSkinTemperatureEventListener(mBandSkinTemperatureListener);
                mState = STATE_CONNECTED;
                updateNotification("POWER-SAVING", android.R.drawable.presence_away);
            } catch (BandIOException | IllegalArgumentException e) {
                e.printStackTrace();
            }
            stopTimer = new CountDownTimer(180000,180000)
            {
                @Override
                public void onTick(long millisUntilFinished) {}

                @Override
                public void onFinish() {startStreaming();}
            };
            stopTimer.start();
            log("Stop Timer Started");
        }
    }

    /** fullStopStreaming
     *
     * Stops the streaming of every sensor without starting a timer to turn them back on. This
     * is called whenever the device has to stop streaming altogether and exit the periodic recording
     * protocol.
     */
    private void fullStopStreaming() {
        if (mBandClient == null) {
            return;
        }

        if (mState == STATE_STREAMING || mState == STATE_NOT_WORN) {
            try {
                Thread.sleep(250);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            BandSensorManager bandSensorManager = mBandClient.getSensorManager();
            try {
                bandSensorManager.unregisterAccelerometerEventListener(mBandAccelerometerListener);
                bandSensorManager.unregisterAmbientLightEventListener(mBandAmbientLightListener);
                bandSensorManager.unregisterContactEventListener(mBandContactListener);
                bandSensorManager.unregisterGsrEventListener(mBandGsrListener);
                bandSensorManager.unregisterHeartRateEventListener(mBandHeartRateListener);
                bandSensorManager.unregisterRRIntervalEventListener(mBandRRIntervalListener);
                bandSensorManager.unregisterSkinTemperatureEventListener(mBandSkinTemperatureListener);
                mState = STATE_CONNECTED;
                updateNotification("POWER-SAVING", android.R.drawable.presence_away);
            } catch (BandIOException | IllegalArgumentException e) {
                e.printStackTrace();
            }
        }
    }

    /**             NOTE: Dynamic Blackout does not refer to the blackout period!
     *
     * enterDynamicBlackout executes when the band is not being worn (hence the notification message).
     * This temporarily unregisters the sensors while the band is not being worn so that it can
     * save some of the band's battery.
     *
     * When the band is being worn again, resumeFromDynamicBlackout is called, turning the sensors
     * on again and resuming the data collection.
     */

    private void enterDynamicBlackout()
    {
        if (mBandClient.isConnected()) {
            updateNotification("Band is not being worn", android.R.drawable.presence_away);
        }
        BandSensorManager bandSensorManager = mBandClient.getSensorManager();
        try {
            bandSensorManager.unregisterAccelerometerEventListener(mBandAccelerometerListener);
            bandSensorManager.unregisterAmbientLightEventListener(mBandAmbientLightListener);
            bandSensorManager.unregisterGsrEventListener(mBandGsrListener);
            bandSensorManager.unregisterHeartRateEventListener(mBandHeartRateListener);
            bandSensorManager.unregisterRRIntervalEventListener(mBandRRIntervalListener);
            bandSensorManager.unregisterSkinTemperatureEventListener(mBandSkinTemperatureListener);
            mState = STATE_NOT_WORN;
        } catch (BandIOException | IllegalArgumentException e) {
            e.printStackTrace();
        }
    }

    private void resumeFromDynamicBlackout()
    {
        if (mBandClient.isConnected()) {
            updateNotification("STREAMING", android.R.drawable.presence_online);
        }
        BandSensorManager bandSensorManager = mBandClient.getSensorManager();
        try {
            bandSensorManager.registerAccelerometerEventListener(mBandAccelerometerListener, SampleRate.MS128);
            bandSensorManager.registerAmbientLightEventListener(mBandAmbientLightListener);
            bandSensorManager.registerGsrEventListener(mBandGsrListener, GsrSampleRate.MS200);
            bandSensorManager.registerHeartRateEventListener(mBandHeartRateListener);
            bandSensorManager.registerRRIntervalEventListener(mBandRRIntervalListener);
            bandSensorManager.registerSkinTemperatureEventListener(mBandSkinTemperatureListener);
            mState = STATE_STREAMING;
        } catch (BandException | InvalidBandVersionException e) {
            e.printStackTrace();
        }
    }

    // Completely disconnects the sensor connection from the phone
    private void disconnect() {
        if (mBandClientManager == null) {
            log("Disconnect Failed (Band Client Manager not Initialized)");
            return;
        }

        if (mBandClient == null || !mBandClient.isConnected()) {
            log("Disconnect Failed (Band is not Connected)");
            SettingsActivity.putBoolean(this, SettingsActivity.KEY_SENSOR_ENABLE, false);
            stopSelf();
            return;
        }

        try {
            mBandClient.getNotificationManager().vibrate(VibrationType.RAMP_DOWN);
        } catch (BandException e) {
            e.printStackTrace();
        }

        if (mState == STATE_STREAMING) {

            fullStopStreaming();
        }

        mBandClient.disconnect().registerResultCallback(mBandDisconnectResultCallback, 10, TimeUnit.SECONDS);
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

    private void log(String message) {
        Log.d("BandCollectionService", message);
    }
}
