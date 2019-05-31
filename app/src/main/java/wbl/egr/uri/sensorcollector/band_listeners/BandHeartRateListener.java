package wbl.egr.uri.sensorcollector.band_listeners;

import android.app.Application;
import android.app.Notification;
import android.app.NotificationManager;
import android.content.Context;
import android.support.v7.app.NotificationCompat;
import android.util.Log;
import android.view.View;

import wbl.egr.uri.sensorcollector.activities.MainActivity;
import wbl.egr.uri.sensorcollector.activities.SettingsActivity;
import wbl.egr.uri.sensorcollector.fitbit.events.FBHeartRateEvent;
import wbl.egr.uri.sensorcollector.fitbit.listeners.FBHeartRateEventListener;
import wbl.egr.uri.sensorcollector.services.AudioRecordManager;
import wbl.egr.uri.sensorcollector.services.DataLogService;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

import static wbl.egr.uri.sensorcollector.activities.SettingsActivity.KEY_HR_TRIGGER;

/*imporcct com.microsoft.band.sensors.BandHeartRateEvent;
import com.microsoft.band.sensors.BandHeartRateEventListener;
import com.microsoft.band.sensors.HeartRateQuality;
*/

/**
 * Created by mconstant on 2/22/17.
 */

public class BandHeartRateListener implements FBHeartRateEventListener {
    private static final String HEADER = "Patient ID,Date,Time,Heart Rate (BPM)";

    private Context mContext;
    private static final int NOTIFICATION_ID = 7903;
    public BandHeartRateListener(Context context) {
        mContext = context;
        //updateNotification("???");
    }

    @Override
    public void onBandHeartRateChanged(FBHeartRateEvent bandHeartRateEvent) {
        Date date = Calendar.getInstance().getTime();
        String p_id = SettingsActivity.getString(mContext, SettingsActivity.KEY_IDENTIFIER, null);
        String dateString = new SimpleDateFormat("MM/dd/yyyy", Locale.US).format(date);
        String timeString = new SimpleDateFormat("HH:mm:ss.SSS", Locale.US).format(date);
        String data = p_id + "," + dateString + "," + timeString + "," +
                bandHeartRateEvent.getHeartRate();
        DataLogService.log(mContext, new File(MainActivity.getRootFile(mContext), "/hr.csv"), data, HEADER);
        //updateNotification(Double.toString(bandHeartRateEvent.getHeartRate()));
        if (bandHeartRateEvent.getHeartRate() > Integer.parseInt(SettingsActivity.getString(mContext, KEY_HR_TRIGGER, "100")))
        {
            AudioRecordManager.start(mContext, AudioRecordManager.ACTION_AUDIO_TRIGGER);
        }
    }

    private void updateNotification(String hr) {
        Notification notification = new Notification.Builder(mContext)
                .setContentTitle("ED EAR Heart Rate")
                .setSmallIcon(android.R.drawable.presence_invisible)
                .setContentText("Heart Rate: " + hr)
                .build();
        NotificationManager man = (NotificationManager) mContext.getSystemService(Context.NOTIFICATION_SERVICE);
        man.notify(NOTIFICATION_ID, notification);
    }
}
