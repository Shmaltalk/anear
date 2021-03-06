package wbl.egr.uri.sensorcollector.tasks;

import android.app.Activity;
import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;
import com.microsoft.band.BandClient;
import com.microsoft.band.BandClientManager;
import com.microsoft.band.BandInfo;
import com.microsoft.band.sensors.HeartRateConsentListener;
import wbl.egr.uri.sensorcollector.activities.SettingsActivity;
import wbl.egr.uri.sensorcollector.fitbit.FBClient;
import wbl.egr.uri.sensorcollector.fitbit.FBClientManager;
import wbl.egr.uri.sensorcollector.fitbit.FBInfo;

import java.lang.ref.WeakReference;
import java.util.List;

/**
 * Created by mconstant on 2/23/17.
 */

public class RequestHeartRateTask extends AsyncTask<WeakReference<Activity>, Void, Void> {
    private WeakReference<Context> mContext;

    private HeartRateConsentListener mHeartRateConsentListener = new HeartRateConsentListener() {
        @Override
        public void userAccepted(boolean b) {
            if (mContext != null && mContext.get() != null) {
                SettingsActivity.putBoolean(mContext.get(), SettingsActivity.KEY_HR_CONSENT, b);
                Log.d("CONSENT", String.valueOf(b));
            } else {
                Log.d("CONSENT", "Could not store HR Consent Value");
            }
        }
    };

    @Override
    protected Void doInBackground(WeakReference...weakReferences) {
        WeakReference<Activity> activityWeakReference = weakReferences[0];
        if (activityWeakReference == null || activityWeakReference.get() == null) {
            return null;
        } else {
            Log.d("CONSENT", "Invalid Context!");
        }

        mContext = new WeakReference<Context>(activityWeakReference.get());

        FBClientManager bandClientManager = FBClientManager.getInstance();
        List<FBInfo> pairedBands = bandClientManager.getConnectedBands();
        if (pairedBands.size() == 0) {
            return null;
        } else {
            try {
                if (activityWeakReference.get() != null) {
                    FBClient bandClient = bandClientManager.create(pairedBands.get(0));
                    //bandClient.connect().await();
                    //bandClient.getSensorManager().requestHeartRateConsent(activityWeakReference.get(), mHeartRateConsentListener);
                    //bandClient.disconnect().await();
                } else {
                    Log.d("CONSENT", "Caller Instance no longer exists");
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        return null;
    }
}
