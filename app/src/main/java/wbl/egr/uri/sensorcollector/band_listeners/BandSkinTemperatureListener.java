package wbl.egr.uri.sensorcollector.band_listeners;

import android.content.Context;
import com.microsoft.band.sensors.BandSkinTemperatureEvent;
import com.microsoft.band.sensors.BandSkinTemperatureEventListener;
import wbl.egr.uri.sensorcollector.activities.MainActivity;
import wbl.egr.uri.sensorcollector.activities.SettingsActivity;
import wbl.egr.uri.sensorcollector.services.DataLogService;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

/**
 * Created by mconstant on 2/22/17.
 */

public class BandSkinTemperatureListener implements BandSkinTemperatureEventListener {
    private static final String HEADER = "Patient ID, Date,Time,Temperature (Celsius)";

    private Context mContext;

    public BandSkinTemperatureListener(Context context) {
        mContext = context;
    }

    @Override
    public void onBandSkinTemperatureChanged(BandSkinTemperatureEvent bandSkinTemperatureEvent) {
        Date date = Calendar.getInstance().getTime();
        String p_id = SettingsActivity.getString(mContext, SettingsActivity.KEY_IDENTIFIER, null);
        String dateString = new SimpleDateFormat("MM/dd/yyyy", Locale.US).format(date);
        String timeString = new SimpleDateFormat("HH:mm:ss.SSS", Locale.US).format(date);
        String data = p_id + "," + dateString + "," + timeString + "," +
                bandSkinTemperatureEvent.getTemperature();
        DataLogService.log(mContext, new File(MainActivity.getRootFile(mContext), "temp.csv"), data, HEADER);
    }
}
