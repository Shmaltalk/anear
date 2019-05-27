package wbl.egr.uri.sensorcollector.services;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.os.CountDownTimer;
import android.os.IBinder;
import android.os.PowerManager;
import android.os.Vibrator;
import android.util.Log;
import android.widget.Toast;
import com.microsoft.band.*;
import com.microsoft.band.notifications.VibrationType;
import com.microsoft.band.sensors.BandSensorManager;
import com.microsoft.band.sensors.GsrSampleRate;
import com.microsoft.band.sensors.SampleRate;
import wbl.egr.uri.sensorcollector.activities.SettingsActivity;
import wbl.egr.uri.sensorcollector.band_listeners.*;
import wbl.egr.uri.sensorcollector.collector_server.CollectorServer;
import wbl.egr.uri.sensorcollector.fitbit.FBClient;
import wbl.egr.uri.sensorcollector.fitbit.FBClientManager;
import wbl.egr.uri.sensorcollector.fitbit.FBInfo;
import wbl.egr.uri.sensorcollector.fitbit.FBSensorManager;
import wbl.egr.uri.sensorcollector.receivers.BandContactStateReceiver;
import wbl.egr.uri.sensorcollector.receivers.BandUpdateReceiver;
import wbl.egr.uri.sensorcollector.receivers.TestBandReceiver;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.TimeUnit;

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

    private static CollectorServer server = null;
    private static int state = STATE_OTHER;

    private static CollectorServer getServer(FBClient cli) {
        if (server == null) {
            server = new CollectorServer(cli);
        }
        return server;
    }

    public static void connect(Context context) {
        Intent intent = new Intent(context, BandCollectionService.class);
        intent.setAction(ACTION_CONNECT);
        //intent.setComponent(new ComponentName("wbl.egr.uri.sensorcollector", "wbl.egr.uri.sensorcollector.services.BandCollectionService"));
        context.startService(intent);
        startStream(context);
    }

    public static void connect(Context context, boolean autoStream) {
        Intent intent = new Intent(context, BandCollectionService.class);
        intent.setAction(ACTION_CONNECT);
        intent.putExtra(EXTRA_AUTO_STREAM, autoStream);
        //intent.setComponent(new ComponentName("wbl.egr.uri.sensorcollector", "wbl.egr.uri.sensorcollector.services.BandCollectionService"));
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
        Log.d("BCService", "Start stream");
        Intent intent = new Intent(context, BandCollectionService.class);
        intent.setAction(ACTION_START_STREAMING);
        //intent.setComponent(new ComponentName("wbl.egr.uri.sensorcollector", "wbl.egr.uri.sensorcollector.services.BandCollectionService"));
        context.startService(intent);
    }

    /** requestBandInfo
     *
     * Starts the process of gathering the band's information.
     * This is called after the band is already connected, and is
     * mainly for the user to see when starting to stream sensor data.
     *
     */
    public static void requestBandInfo(Context context) {
        Log.d("BCService", "Req info");
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
        Log.d("BCService", "DC");
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

    private FBClientManager mBandClientManager;
    private FBClient mBandClient;
    private CollectorServer mServer;
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

    private CountDownTimer  startTimer,
                            stopTimer;

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        Log.d("SAVEME", "FROMTHENOTHINGBLAHBLAH");
        super.onCreate();
        Log.d("SERVICE", "SERVICE CCREATE CERVICE CRTETEATE");
        log("Service Created");
        mContext = this;
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
        mBandClientManager = FBClientManager.getInstance();
        mBandClientManager.addBand();
        mBandClient = mBandClientManager.create(mBandClientManager.getConnectedBands().get(0));
        mServer = getServer(mBandClient);
        mBandAccelerometerListener = new BandAccelerometerListener(this);
        mBandAmbientLightListener = new BandAmbientLightListener(this);
        mBandContactListener = new BandContactListener(this);
        mBandGsrListener = new BandGsrListener(this);
        mBandHeartRateListener = new BandHeartRateListener(this);
        mBandRRIntervalListener = new BandRRIntervalListener(this);
        mBandSkinTemperatureListener = new BandSkinTemperatureListener(this);
    }

    /** onStartCommand
     *
     * When the service is started, this method will handle different actions
     * depending on what the service was started for.
     */
    @Override
    public int onStartCommand(Intent intent, int flags, int startID) {
        /*if (this != getInstance()) {
            return getInstance().onStartCommand(intent, flags, startID);
        }*/
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
                    testIntent.putExtra(TestBandReceiver.EXTRA_STATE, state);
                    sendBroadcast(testIntent);
                    if (state == STATE_OTHER) {
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
        //mServer.stop();
        Vibrator vibrator = (Vibrator) getSystemService(VIBRATOR_SERVICE);
        vibrator.vibrate(500);
        log("Service Destroyed");
        super.onDestroy();
    }

    /*  Gathers the information from the MicrosoftBand
     *  that is necessary to connect to it.
     */
    private void connect() {
        if (mBandClientManager == null || mBandClient == null) {
            log("Connect Failed (Band Client Manager not Initialized)");
            return;
        }

        List<FBInfo> pairedBands = mBandClientManager.getConnectedBands();
        if (pairedBands == null || pairedBands.size() == 0) {
            log("Connect Failed (No Bands are Paired with this Device)");
        } else if (pairedBands.size() > 1) {
            /*
             * TODO
             * Implement UI to allow User to choose Band to pair to.
             * For now, always choose pairedBands[0]
             */
            connect(pairedBands.get(0));
        } else {
            connect(pairedBands.get(0));
        }
    }

    // Uses the information just gathered in order to connect to the band
    private void connect(FBInfo bandInfo) {
        log("Attempting to Connect to " + bandInfo.getMacAddress() + "...");
        mBandName = bandInfo.getName();
        mBandAddress = bandInfo.getMacAddress();
    }

    //  Sends the information to the Settings for the user to see
    private void getInfo() {
        if (mBandClient == null) {
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
     * recording timer.
     */
    private void startStreaming() {
        log("Starting Stream");
        if (mBandClient == null) {
            log("Band is not Connected");
            return;
        }

        if (state != STATE_STREAMING) {

            try {Thread.sleep(250);}
            catch (InterruptedException e) {e.printStackTrace();}

            FBSensorManager bandSensorManager = mBandClient.getSensorManager();
            try
            {
                state = STATE_STREAMING;
                updateNotification("STREAMING", android.R.drawable.presence_online);
                // XXX accel listener does nothing currently
                bandSensorManager.registerAccelerometerEventListener(mBandAccelerometerListener);
                bandSensorManager.registerHeartRateEventListener(mBandHeartRateListener);
                mServer.start();
            }
            catch (Exception e) {
                e.printStackTrace();
            }
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

        if (state == STATE_STREAMING || state == STATE_NOT_WORN || state == STATE_CONNECTED) {
            try {
                Thread.sleep(250);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            FBSensorManager bandSensorManager = mBandClient.getSensorManager();
            try {
                bandSensorManager.stop();
                state = STATE_DISCONNECTED;
                updateNotification("OFF", android.R.drawable.presence_offline);
                mServer.stop();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    // Completely disconnects the sensor connection from the phone
    private void disconnect() {
        if (mBandClientManager == null) {
            log("Disconnect Failed (Band Client Manager not Initialized)");
            return;
        }

        if (mBandClient == null) {
            log("Disconnect Failed (Band is not Connected)");
            SettingsActivity.putBoolean(this, SettingsActivity.KEY_SENSOR_ENABLE, false);
            stopSelf();
            return;
        }

        try {
            mBandClient.getNotificationManager().vibrate(); //VibrationType.RAMP_DOWN);
        } catch (Exception e) {
            e.printStackTrace();
        }

        if (state == STATE_STREAMING || state == STATE_CONNECTED) {

            fullStopStreaming();
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

    private void log(String message) {
        Log.d("BandCollectionService", message);
    }
}
