package wbl.egr.uri.sensorcollector.activities;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

/**
 * Created by mconstant on 2/23/17.
 */

/**
 * LockScreenActivity determines whether or not the user has already entered
 * a lock screen pattern, then launches the appropriate activity.
 */
public class LockScreenActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Intent intent;

        //  No pattern found. User must set a pattern
        if (SettingsActivity.getString(this, SettingsActivity.KEY_PATTERN, null) == null) {
            intent = new Intent(this, SetPatternActivity.class);
        }
        //  Pattern found. User must confirm pattern
        else {
            intent = new Intent(this, ConfirmPatternActivity.class);
        }
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(intent);
        finish();
    }
}
