package com.example.camerax;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.os.Environment;
import android.os.SystemClock;
import android.view.View;
import android.widget.Button;

import com.arthenica.mobileffmpeg.ExecuteCallback;
import com.arthenica.mobileffmpeg.FFmpeg;
import com.arthenica.mobileffmpeg.FFmpegExecution;
import com.google.android.exoplayer2.ExoPlayer;
import com.google.android.exoplayer2.MediaItem;
import com.google.android.exoplayer2.ui.PlayerView;
import com.google.android.exoplayer2.util.Log;

import java.util.Date;

public class EditVideoActivity extends AppCompatActivity {
    ExoPlayer player;
    PlayerView playerView;
    Button mergeBtn;
    String mergedVideoPath;
    String vurl;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_video);

        playerView = findViewById(R.id.playerView);

        mergeBtn = findViewById(R.id.mergeVideoWithAudio);

        player = new ExoPlayer.Builder(this).build();

        playerView.setPlayer(player);

        vurl = getIntent().getStringExtra("vpath");

        MediaItem mediaItem = MediaItem.fromUri(vurl);
// Set the media item to be played.
        player.setMediaItem(mediaItem);
// Prepare the player.
        player.prepare();
// Start the playback.
        player.play();

        mergeBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
              Merge();
            }
        });

    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        if(player.isPlaying()){
            player.stop();
        }
    }

    public void Merge() {
        Date date = new Date();
        String timestamp = String.valueOf(date.getTime());

        String[] c = {"-i", vurl
                , "-i", Environment.getExternalStorageDirectory().getPath()
                + "/Download/H.mp3"
                , "-c:v", "copy", "-c:a", "aac", "-map", "0:v:0", "-map", "1:a:0", "-shortest",
                Environment.getExternalStorageDirectory().getPath()
                        + "/Download/"+timestamp+".mp4"};
        MergeVideo(c);

    }
    private void MergeVideo(String[] co){
        FFmpeg.executeAsync(co, new ExecuteCallback() {
            @Override
            public void apply( long executionId, int returnCode ) {
                Log.d("hello" , "return  " + returnCode);
                Log.d("hello" , "executionID  " + executionId);
                Log.d("hello" , "FFMPEG  " +  new FFmpegExecution(executionId,co));

            }
        });
    }
}