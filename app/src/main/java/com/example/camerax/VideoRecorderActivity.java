package com.example.camerax;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.camera.core.CameraSelector;
import androidx.camera.core.CameraXConfig;
import androidx.camera.core.ImageCapture;
import androidx.camera.core.Preview;
import androidx.camera.core.VideoCapture;
import androidx.camera.lifecycle.ProcessCameraProvider;
import androidx.camera.view.PreviewView;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import androidx.lifecycle.LifecycleOwner;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraManager;
import android.os.Bundle;
import android.os.SystemClock;
import android.util.Log;
import android.view.View;
import android.widget.Chronometer;
import android.widget.ImageButton;
import android.widget.Toast;

import com.example.camerax.Fragment.GalleryVideosFragment;
import com.google.common.util.concurrent.ListenableFuture;

import java.io.File;
import java.util.Date;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executor;

public class VideoRecorderActivity extends AppCompatActivity implements CameraXConfig.Provider {

    private ImageCapture imageCapture = null;
    PreviewView previewView;

    ImageButton startRecording, flashOnOff, flipCamera, closeScreen, openGallery;
    String[] REQUIRED_PERMISSIONS =
            {
              Manifest.permission.CAMERA,Manifest.permission.RECORD_AUDIO,
              Manifest.permission.READ_EXTERNAL_STORAGE,
              Manifest.permission.WRITE_EXTERNAL_STORAGE
            };
    int REQUEST_CODE_PERMISSIONS = 10;
    private ListenableFuture<ProcessCameraProvider> cameraProviderFuture;
    VideoCapture videoCapture;
    CameraSelector cameraSelector;
    Preview preview;
    private CameraManager cameraManager;
    private String getCameraID;
    private boolean flashOff = true;
    private boolean recordingOff = true;
    private boolean backCamera = true;
    private Chronometer chronometer;
    private boolean running;
    long pauseOffset;
    boolean state = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_video_recorder);

        previewView = findViewById(R.id.viewFinder);
        cameraProviderFuture = ProcessCameraProvider.getInstance(this);
        startRecording = findViewById(R.id.startRecording);
        flashOnOff = findViewById(R.id.flashBtn);
        closeScreen = findViewById(R.id.cancelBtn);
        cameraManager = (CameraManager) getSystemService(Context.CAMERA_SERVICE);
        chronometer = findViewById(R.id.chronometer);
        openGallery = findViewById(R.id.openGalleryBtn);
        flipCamera = findViewById(R.id.flipcameraBtn);


        openGallery.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Fragment fragment = new GalleryVideosFragment();
                FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
                transaction.setCustomAnimations(R.anim.slide_in_down,R.anim.slide_out_up,R.anim.slide_in_up, R.anim.slide_out_down);
                transaction.replace(R.id.fraghere, fragment).commit();
            }
        });
        flipCamera.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (!backCamera) {
                    cameraProviderFuture.addListener(() -> {
                        try {
                            ProcessCameraProvider cameraProvider = cameraProviderFuture.get();
                            startFrontCameraX(cameraProvider);
                        } catch (ExecutionException | InterruptedException e) {
                        }
                    }, getExecuter());
                    backCamera = true;
                } else {
                    backCamera = false;
                    cameraProviderFuture.addListener(() -> {
                        try {
                            ProcessCameraProvider cameraProvider = cameraProviderFuture.get();
                            startCameraX(cameraProvider);
                        } catch (ExecutionException | InterruptedException e) {
                        }
                    }, getExecuter());

                }
            }
        });

        closeScreen.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });

        try {
            // O means back camera unit,
            // 1 means front camera unit
            getCameraID = cameraManager.getCameraIdList()[0];
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }

        flashOnOff.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Toast.makeText(VideoRecorderActivity.this, "Flash On", Toast.LENGTH_SHORT).show();
                CameraManager cameraManager = (CameraManager) getSystemService(Context.CAMERA_SERVICE);
                if (!state) {

                    try {
                        String cameraId = cameraManager.getCameraIdList()[0];
                        cameraManager.setTorchMode(cameraId, true);
                        state = true;
                        flashOnOff.setImageResource(R.drawable.flash_on_24);
                    } catch (CameraAccessException e) {
                    }
                } else {

                    try {
                        String cameraId = cameraManager.getCameraIdList()[0];
                        cameraManager.setTorchMode(cameraId, false);
                        state = false;
                        flashOnOff.setImageResource(R.drawable.ic_baseline_flash_off_24);
                    } catch (CameraAccessException e) {
                    }
                }
            }
        });


        startRecording.setOnClickListener(new View.OnClickListener() {
            @SuppressLint("RestrictedApi")
            @Override
            public void onClick(View view) {
                if (recordingOff) {
                    startChronometer();
                    recordVideo();
                    recordingOff = false;
                    startRecording.setBackgroundResource(R.drawable.recording_on);
                } else {
                    stopChronometer();
                    recordingOff = true;
                    videoCapture.stopRecording();
                    startRecording.setBackgroundResource(R.drawable.rounded_btn);
                }
            }
        });


        if (allPermissionsGranted()) {
            // startCamera();

        } else {
            ActivityCompat.requestPermissions(
                    this, REQUIRED_PERMISSIONS, REQUEST_CODE_PERMISSIONS);
        }


        cameraProviderFuture.addListener(() -> {
            try {
                ProcessCameraProvider cameraProvider = cameraProviderFuture.get();
                startFrontCameraX(cameraProvider);
            } catch (ExecutionException | InterruptedException e) {
            }
        }, getExecuter());


    }

    @SuppressLint("RestrictedApi")
    private void startFrontCameraX(ProcessCameraProvider cameraProvider) {
        cameraProvider.unbindAll();

        preview = new Preview.Builder().build();

        cameraSelector = new CameraSelector.Builder()
                .requireLensFacing(CameraSelector.LENS_FACING_FRONT)
                .build();

        preview.setSurfaceProvider(previewView.getSurfaceProvider());

        videoCapture = new VideoCapture.Builder()
                .setVideoFrameRate(30)
                .build();

        cameraProvider.bindToLifecycle((LifecycleOwner) this, cameraSelector, preview, videoCapture);

    }

    private void startChronometer() {
        if (!running) {
            chronometer.setBase(SystemClock.elapsedRealtime() - pauseOffset);
            chronometer.start();
            running = true;
        }
    }

    private void stopChronometer() {
        if (running) {
            chronometer.stop();
            pauseOffset = SystemClock.elapsedRealtime() - chronometer.getBase();
            running = false;
        }
    }

    private void resetChronometer() {
        chronometer.setBase(SystemClock.elapsedRealtime());
        pauseOffset = 0;
    }

    @SuppressLint("RestrictedApi")
    private void startCameraX(ProcessCameraProvider cameraProvider) {
        cameraProvider.unbindAll();

        preview = new Preview.Builder().build();

        cameraSelector = new CameraSelector.Builder()
                .requireLensFacing(CameraSelector.LENS_FACING_BACK)
                .build();

        preview.setSurfaceProvider(previewView.getSurfaceProvider());

        videoCapture = new VideoCapture.Builder()
                .setVideoFrameRate(30)
                .build();

        cameraProvider.bindToLifecycle((LifecycleOwner) this, cameraSelector, preview, videoCapture);

    }

    @SuppressLint("RestrictedApi")
    private void recordVideo() {
        if (videoCapture != null) {
            File videoUrl = new File("mnt/sdcard/Movies/cameraX");

            if (!videoUrl.exists())
                videoUrl.mkdir();

            Date date = new Date();
            String timestamp = String.valueOf(date.getTime());

            String vidFilePath = videoUrl.getAbsolutePath() + "/" + timestamp + ".mp4";

            File vidFile = new File(vidFilePath);

            videoCapture.startRecording(
                    new VideoCapture.OutputFileOptions.Builder(vidFile).build(),
                    getExecuter(),
                    new VideoCapture.OnVideoSavedCallback() {
                        @Override
                        public void onVideoSaved(@NonNull VideoCapture.OutputFileResults outputFileResults) {
                            resetChronometer();
                            Toast.makeText(VideoRecorderActivity.this, "Video Recorded Successfully" + "\n" + outputFileResults.getSavedUri(), Toast.LENGTH_SHORT).show();
                            Log.e("VIDEO_PATH", String.valueOf(outputFileResults.getSavedUri()));
                            Intent intent = new Intent(VideoRecorderActivity.this, EditVideoActivity.class);
                            intent.putExtra("vpath", String.valueOf(outputFileResults.getSavedUri()));
                            startActivity(intent);
                            finish();

                        }

                        @Override
                        public void onError(int videoCaptureError, @NonNull String message, @Nullable Throwable cause) {
                            Log.e("ERROR", String.valueOf(message));

                        }
                    });
        }

    }

    private Executor getExecuter() {
        return ContextCompat.getMainExecutor(this);
    }

    private boolean allPermissionsGranted() {
        if (ContextCompat.checkSelfPermission(
                this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED
                && ContextCompat.checkSelfPermission(this,Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED
                && ContextCompat.checkSelfPermission(this,Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED
                && ContextCompat.checkSelfPermission(this,Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            return false;
        } else {
            return true;
        }
    }


    @NonNull
    @Override
    public CameraXConfig getCameraXConfig() {
        return null;
    }
}