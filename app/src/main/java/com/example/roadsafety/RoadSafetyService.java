package com.example.roadsafety;

import android.app.Service;
import android.content.Intent;
import android.media.MediaRecorder;
import android.os.IBinder;

import androidx.annotation.Nullable;

import java.io.IOException;


public class RoadSafetyService extends Service {
    public final String APP_BROADCAST_RECEIVER = "AMPLITUDE_BROADCAST";
    MediaRecorder mRecorder;
    double value;
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        mRecorder = new MediaRecorder();
        mRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
        mRecorder.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP);
        mRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);
        mRecorder.setOutputFile("/dev/null");
        try {
            mRecorder.prepare();
        } catch (IOException e) {
            e.printStackTrace();
        }
        mRecorder.start();
        value = mRecorder.getMaxAmplitude();
        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    public void sendBroadcast(){

        Intent intent = new Intent(APP_BROADCAST_RECEIVER);
        intent.putExtra("data","value");
        sendBroadcast();
    }
}