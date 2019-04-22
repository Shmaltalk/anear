package wbl.egr.uri.sensorcollector.band_listeners;

import android.content.Context;

imporcct com.microsoft.band.sensors.BandHeartRateEvent;
import com.microsoft.band.sensors.BandHeartRateEventListener;
import com.microsoft.band.sensors.HeartRateQuality;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

import wbl.egr.uri.sensorcollector.activities.MainActivity;
import wbl.egr.uri.sensorcollector.activities.SettingsActivity;
import wbl.egr.uri.sensorcollector.services.AudioRecordManager;
import wbl.egr.uri.sensorcollector.services.DataLogService;

import static wbl.egr.uri.sensorcollector.activities.SettingsActivity.KEY_HR_TRIGGER;

/**
 * Created by mconstant on 2/22/17.
 */

public class BandHeartRateListener implements BandHeartRateEventListener {
    private static final String HEADER = "Patient ID, Date,Time,Heart Rate (BPM),Quality";

    private Context mContext;

    public BandHeartRateListener(Context context) {
        mContext = context;
    }

    @Override
    public void onBandHeartRateChanged(BandHeartRateEvent bandHeartRateEvent) {
        Date date = Calendar.getInstance().getTime();
        String p_id = SettingsActivity.getString(mContext, SettingsActivity.KEY_IDENTIFIER, null);
        String dateString = new SimpleDateFormat("MM/dd/yyyy", Locale.US).format(date);
        String timeString = new SimpleDateFormat("HH:mm:ss.SSS", Locale.US).format(date);
        String data = p_id + "," + dateString + "," + timeString + "," +
                bandHeartRateEvent.getHeartRate() + "," +
                bandHeartRateEvent.getQuality();
        DataLogService.log(mContext, new File(MainActivity.getRootFile(mContext), "/hr.csv"), data, HEADER);

        if (bandHeartRateEvent.getHeartRate() > Integer.parseInt(SettingsActivity.getString(mContext, KEY_HR_TRIGGER, "100")) &&
                bandHeartRateEvent.getQuality().name().equals(HeartRateQuality.LOCKED.name()))
        {
            AudioRecordManager.start(mContext, AudioRecordManager.ACTION_AUDIO_TRIGGER);
        }
    }
}
