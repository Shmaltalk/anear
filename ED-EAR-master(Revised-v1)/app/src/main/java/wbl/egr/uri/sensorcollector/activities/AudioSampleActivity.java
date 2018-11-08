package wbl.egr.uri.sensorcollector.activities;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.support.design.widget.CoordinatorLayout;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;

import wbl.egr.uri.sensorcollector.R;
import wbl.egr.uri.sensorcollector.fragments.AudioSampleFragment;

public class AudioSampleActivity extends AppCompatActivity
{
    public static final String[] PERMISSIONS = {
            Manifest.permission.WRITE_EXTERNAL_STORAGE,
            Manifest.permission.RECORD_AUDIO
    };

    private final int REQUEST_CODE = 4497; // Changed from 3497

    private CoordinatorLayout mMessageContainer;
    private static Context mContext;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_single_fragment);
        mContext = this;

        mMessageContainer = (CoordinatorLayout) findViewById(R.id.message_container);

        //Request Permissions
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            requestPermissions(PERMISSIONS, REQUEST_CODE);
        }
    }

    @Override
    protected void onStart() {
        super.onStart();

        getFragmentManager().beginTransaction()
                .replace(R.id.fragment_container, AudioSampleFragment.getInstance(), "audiosample_fragment")
                .addToBackStack("audiosample_fragment")
                .commit();
    }

    public static Context getContext()
    {
        return mContext;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu)
    {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.main_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item)
    {
        Intent intent;
        switch (item.getItemId())
        {
            case R.id.menu_audio_sample:
                return true;
            case R.id.menu_settings:
                intent = new Intent(this, SettingsActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(intent);
                finish();
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    protected void onStop() {
        super.onStop();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }
}
