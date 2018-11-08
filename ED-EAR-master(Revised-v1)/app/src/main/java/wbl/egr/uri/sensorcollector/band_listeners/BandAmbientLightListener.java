package wbl.egr.uri.sensorcollector.band_listeners;

import android.content.Context;

import com.microsoft.band.sensors.BandAmbientLightEvent;
import com.microsoft.band.sensors.BandAmbientLightEventListener;

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

public class BandAmbientLightListener implements BandAmbientLightEventListener {
    private static final String HEADER = "Patient ID, Date,Time,Brightness (LUX)";

    private Context mContext;

    public BandAmbientLightListener(Context context) {
        mContext = context;
    }

    @Override
    public void onBandAmbientLightChanged(BandAmbientLightEvent bandAmbientLightEvent) {
        Date date = Calendar.getInstance().getTime();
        String dateString = new SimpleDateFormat("MM/dd/yyyy", Locale.US).format(date);
        String timeString = new SimpleDateFormat("HH:mm:ss.SSS", Locale.US).format(date);
        String p_id = SettingsActivity.getString(mContext, SettingsActivity.KEY_IDENTIFIER, null);
        String data = p_id + "," + dateString + "," + timeString + "," +
                bandAmbientLightEvent.getBrightness();
        DataLogService.log(mContext, new File(MainActivity.getRootFile(mContext), "light.csv"), data, HEADER);
    }
}
