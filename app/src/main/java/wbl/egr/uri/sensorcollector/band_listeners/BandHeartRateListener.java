package wbl.egr.uri.sensorcollector.band_listeners;

import android.content.Context;
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
    private static final String HEADER = "Patient ID, Date,Time,Heart Rate (BPM),Quality";

    private Context mContext;

    public BandHeartRateListener(Context context) {
        mContext = context;
    }

    @Override
    public void onBandHeartRateChanged(FBHeartRateEvent bandHeartRateEvent) {
        Date date = Calendar.getInstance().getTime();
        String p_id = SettingsActivity.getString(mContext, SettingsActivity.KEY_IDENTIFIER, null);
        String dateString = new SimpleDateFormat("MM/dd/yyyy", Locale.US).format(date);
        String timeString = new SimpleDateFormat("HH:mm:ss.SSS", Locale.US).format(date);
        String data = p_id + "," + dateString + "," + timeString + "," +
                bandHeartRateEvent.getHeartRate(); // + "," +
                //bandHeartRateEvent.getQuality();
        DataLogService.log(mContext, new File(MainActivity.getRootFile(mContext), "/hr.csv"), data, HEADER);

        if (bandHeartRateEvent.getHeartRate() > Integer.parseInt(SettingsActivity.getString(mContext, KEY_HR_TRIGGER, "100")))// &&
                //bandHeartRateEvent.getQuality().name().equals(HeartRateQuality.LOCKED.name()))
        {
            AudioRecordManager.start(mContext, AudioRecordManager.ACTION_AUDIO_TRIGGER);
        }
    }
}
