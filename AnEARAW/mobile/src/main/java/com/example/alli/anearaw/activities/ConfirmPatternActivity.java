package com.example.alli.anearaw.activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import java.util.List;

import me.zhanghai.android.patternlock.PatternUtils;
import me.zhanghai.android.patternlock.PatternView;

/**
 * Created by mconstant on 2/24/17.
 *
 * ConfirmPatternActivity
 *
 * Checks to see whether or not the pattern that the user enters matches
 * the same pattern that the user set. If not, it will ask the user to
 * enter the pattern again until the correct pattern has been inputted.
 * If the user has entered five incorrect patterns, no more patterns can
 * be entered.
 */

public class ConfirmPatternActivity extends me.zhanghai.android.patternlock.ConfirmPatternActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mRightButton.setVisibility(View.GONE);
    }

    //Checks to see if the pattern is correct
    @Override
    protected boolean isPatternCorrect(List<PatternView.Cell> pattern) {
        if (PatternUtils.patternToSha1String(pattern).equals(MainActivity.getString(this, MainActivity.KEY_PATTERN, null))) {
            return true;
        } else {
            return false;
        }
    }

    //Enters the correct pattern, which takes the user to the settings
    @Override
    protected void onConfirmed() {
        setResult(RESULT_OK);
        Intent intent = new Intent(this, MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(intent);
        finish();
    }

    //Counts the amount of wrong entries
    @Override
    protected void onWrongPattern() {
        //If the user entered the pattern incorrectly five times,
        //exit the application
        if (numFailedAttempts == 5) {
            finish();
        } else {
            ++numFailedAttempts;
        }
    }
}
