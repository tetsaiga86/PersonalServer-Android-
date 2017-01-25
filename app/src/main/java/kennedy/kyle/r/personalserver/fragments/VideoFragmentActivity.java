package kennedy.kyle.r.personalserver.fragments;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.MediaController;
import android.widget.VideoView;

import kennedy.kyle.r.personalserver.R;

public class VideoFragmentActivity extends AppCompatActivity {
    private VideoView mVideoView;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.video_fragment);
        mVideoView = (VideoView) findViewById(R.id.videoView);
        Intent intent = getIntent();
        String vidAddress = intent.getStringExtra("uri");
        Uri vidUri = Uri.parse(vidAddress);
        mVideoView.setVideoURI(vidUri);
        MediaController vidControl = new MediaController(this);
        vidControl.setAnchorView(mVideoView);
        mVideoView.setMediaController(vidControl);
        mVideoView.start();
    }
}
