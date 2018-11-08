package com.example.alli.anearaw.activities;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.example.alli.anearaw.R;
import com.example.alli.anearaw.services.AudioRecordManager;

public class AudioSampleActivity extends AppCompatActivity
{
    private static final String TAG = "AudioSampleActivity";
    public static final String[] PERMISSIONS = {
            Manifest.permission.WRITE_EXTERNAL_STORAGE,
            Manifest.permission.RECORD_AUDIO
    };
    private final int REQUEST_CODE = 3497;
    private Context mContext;
    private Button audioRecordBtn;
    private boolean toggled = false;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_audio_sample);
        //Request Permissions
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            requestPermissions(PERMISSIONS, REQUEST_CODE);
        }
        mContext = this;

        audioRecordBtn = findViewById(R.id.record_btn);
        audioRecordBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                toggled = !toggled;
                if(toggled)
                {
                    audioRecordBtn.setText(R.string.toggleBtn_stop);
                    AudioRecordManager.start(mContext, AudioRecordManager.ACTION_AUDIO_SAMPLE);
                }
                else
                {
                    Toast.makeText(mContext, "Audio file created.", Toast.LENGTH_SHORT).show();
                    audioRecordBtn.setText(R.string.toggleBtn_start);
                    AudioRecordManager.start(mContext, AudioRecordManager.ACTION_AUDIO_CANCEL);
                }
            }
        });
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
                intent = new Intent(this, MainActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(intent);
                finish();
            default:
                return super.onOptionsItemSelected(item);
        }
    }
}
