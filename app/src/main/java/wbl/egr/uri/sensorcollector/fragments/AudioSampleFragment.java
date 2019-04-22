package wbl.egr.uri.sensorcollector.fragments;

import android.app.Fragment;
import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;
import wbl.egr.uri.sensorcollector.R;
import wbl.egr.uri.sensorcollector.activities.AudioSampleActivity;
import wbl.egr.uri.sensorcollector.services.AudioRecordManager;

public class AudioSampleFragment extends Fragment
{
    private Context mContext;
    private Button audioRecordBtn;
    private boolean toggled = false;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_audio_sample, container, false);
        mContext = AudioSampleActivity.getContext();

        audioRecordBtn = (Button) view.findViewById(R.id.record_btn);
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
                    audioRecordBtn.setText(R.string.toggleBtn_start);
                    AudioRecordManager.start(mContext, AudioRecordManager.ACTION_AUDIO_CANCEL);
                    Toast.makeText(mContext, "Audio file created.", Toast.LENGTH_SHORT).show();
                }
            }
        });
        return view;
    }

    public static Fragment getInstance() {
        return new AudioSampleFragment();
    }
}
