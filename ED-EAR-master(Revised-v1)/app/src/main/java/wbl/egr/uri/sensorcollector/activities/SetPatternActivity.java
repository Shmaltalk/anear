package wbl.egr.uri.sensorcollector.activities;

import android.content.Intent;

import java.util.List;

import me.zhanghai.android.patternlock.PatternUtils;
import me.zhanghai.android.patternlock.PatternView;

/**
 * Created by mconstant on 2/24/17.
 *
 * SetPatternActivity
 *
 * Called by the MainActivity once the app is first downloaded and no
 * pattern has been set yet by the user. This will create a pattern lock
 * based off of the pattern that the user enters and confirms. This pattern
 * will have to be used each time the user wants to access the settings.
 */

public class SetPatternActivity extends me.zhanghai.android.patternlock.SetPatternActivity {
    @Override
    protected void onSetPattern(List<PatternView.Cell> pattern) {
        SettingsActivity.putString(this, SettingsActivity.KEY_PATTERN, PatternUtils.patternToSha1String(pattern));
        Intent intent = new Intent(this, SettingsActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(intent);
        finish();
    }
}
