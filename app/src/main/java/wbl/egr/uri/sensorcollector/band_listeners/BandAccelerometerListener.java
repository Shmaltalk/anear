package wbl.egr.uri.sensorcollector.band_listeners;

import android.content.Context;

import com.microsoft.band.sensors.BandAccelerometerEvent;
import com.microsoft.band.sensors.BandAccelerometerEventListener;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

import wbl.egr.uri.sensorcollector.activities.MainActivity;
import wbl.egr.uri.sensorcollector.activities.SettingsActivity;
import wbl.egr.uri.sensorcollector.services.DataLogService;

/**
 * Created by mconstant on 2/22/17.
 */

public class BandAccelerometerListener implements BandAccelerometerEventListener {
    private static final String HEADER = "Patient ID, Date,Time,X-Acceleration (m/s*s),Y-Acceleration (m/s*s),Z-Acceleration (m/s*s)";

    private Context mContext;

    public BandAccelerometerListener(Context context) {
        mContext = context;
    }

    @Override
    public void onBandAccelerometerChanged(BandAccelerometerEvent bandAccelerometerEvent) {
        Date date = Calendar.getInstance().getTime();
        String p_id = SettingsActivity.getString(mContext, SettingsActivity.KEY_IDENTIFIER, null);
        String dateString = new SimpleDateFormat("MM/dd/yyyy", Locale.US).format(date);
        String timeString = new SimpleDateFormat("HH:mm:ss.SSS", Locale.US).format(date);
        String data = p_id + "," + dateString + "," + timeString + "," +
                (bandAccelerometerEvent.getAccelerationX() * (long)9.81) + "," +
                (bandAccelerometerEvent.getAccelerationY() * (long)9.81) + "," +
                (bandAccelerometerEvent.getAccelerationZ() * (long)9.81);
        DataLogService.log(mContext, new File(MainActivity.getRootFile(mContext), "/acc.csv"), data, HEADER);
    }
}
