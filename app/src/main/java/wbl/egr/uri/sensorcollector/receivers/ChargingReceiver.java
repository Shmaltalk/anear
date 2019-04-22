package wbl.egr.uri.sensorcollector.receivers;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import wbl.egr.uri.sensorcollector.activities.MainActivity;
import wbl.egr.uri.sensorcollector.services.DataLogService;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

/**
 * Created by mconstant on 2/23/17.
 */

//ChargingReciever logs charging information to the charge log CSV file
public class ChargingReceiver extends BroadcastReceiver {
    public static final String HEADER = "date,time,charging";

    @Override
    public void onReceive(Context context, Intent intent) {
        Log.v("ChargingReceiver", "Charging...");
        File directory = MainActivity.getRootFile(context).getParentFile();
        File chargeFile = new File(directory, "charge_log.csv");
        String contents;

        String action = intent.getAction();
        boolean charging = action.equals(Intent.ACTION_POWER_CONNECTED);

        Calendar calendar = Calendar.getInstance();
        String dateString = new SimpleDateFormat("MM/dd/yyyy", Locale.US).format(calendar.getTime());
        String timeString = new SimpleDateFormat("HH:mm:ss.SSS", Locale.US).format(calendar.getTime());
        contents = dateString + "," + timeString + "," + String.valueOf(charging);

        DataLogService.log(context, chargeFile, contents, HEADER);
    }
}
