package wbl.egr.uri.sensorcollector.receivers;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import wbl.egr.uri.sensorcollector.activities.SettingsActivity;
import wbl.egr.uri.sensorcollector.activities.TestingActivity;
import wbl.egr.uri.sensorcollector.services.AudioRecordManager;
import wbl.egr.uri.sensorcollector.services.BandCollectionService;

/**
 * Created by mconstant on 2/23/17.
 */


/** BootReceiver
 *
 * This class is called when the device is turned on. It activates anEAR automatically.
 * If the device shut off while data was being collected, this class reactivates the
 * appropriate data collecting services that will allow data to continue being collected.
 *
 */
public class BootReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(final Context context, Intent intent)
    {
        if (intent.getAction().equals(Intent.ACTION_BOOT_COMPLETED))
        {
            Intent i = new Intent(context, TestingActivity.class);
            i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);

            if (SettingsActivity.getBoolean(context, SettingsActivity.KEY_SENSOR_ENABLE, false))
            {
                BandCollectionService.connect(context, true);
            }

            if (SettingsActivity.getBoolean(context, SettingsActivity.KEY_AUDIO_ENABLE, false))
            {
                AudioRecordManager.start(context, AudioRecordManager.ACTION_AUDIO_CANCEL);
                AudioRecordManager.start(context, AudioRecordManager.ACTION_AUDIO_CREATE);
            }
        }
    }
}