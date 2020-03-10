package com.example.roadsafety;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import android.content.Context;
import androidx.core.content.ContextCompat;
import android.Manifest;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.media.MediaRecorder;
import android.os.Build;
import android.os.Bundle;
import android.os.Vibrator;
import android.widget.TextView;

import com.jjoe64.graphview.GraphView;
import com.jjoe64.graphview.series.DataPoint;
import com.jjoe64.graphview.series.LineGraphSeries;

import java.io.IOException;

public class MainActivity extends AppCompatActivity {
    private  final String APP_BROADCAST_RECEIVER ="AMPLITUDE_BROADCAST" ;
    private int MY_PERMISSIONS_REQUEST_RECORD_AUDIO = 1;
    static final private double EMA_FILTER = 0.6;
    private static double mEMA = 0.0;
    MediaRecorder mRecorder;
    TextView textView;
    Thread thread;
    double value;
    String s;

    NotificationManagerCompat managerCompat;
    private BroadcastReceiver receiver;
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

          Intent intent = new Intent(this,MainActivity.class);

         PendingIntent pIntent = PendingIntent.getActivity(this,
               (int) System.currentTimeMillis(), intent, 0);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel("RoadSafaty", "RoadSafaty", NotificationManager.IMPORTANCE_DEFAULT);
            NotificationManager manager = getSystemService(NotificationManager.class);
            manager.createNotificationChannel(channel);
        }

        final NotificationCompat.Builder builder = new NotificationCompat.Builder(this, "RoadSafaty")
                .setContentTitle("Road Safety mode ON")
                .setSmallIcon(R.drawable.ic_launcher_background)
                .addAction(R.drawable.ic_launcher_background, "Trun Off", pIntent)
                .setPriority(Notification.PRIORITY_MAX);


        managerCompat = NotificationManagerCompat.from(this);
        managerCompat.notify(900, builder.build());


        textView = (TextView) findViewById(R.id.textView);
        if (ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.RECORD_AUDIO)) {
                new AlertDialog.Builder(this)
                        .setTitle("Permission needed")
                        .setMessage("This permission is needed for work")
                        .setPositiveButton("ok", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.RECORD_AUDIO}, MY_PERMISSIONS_REQUEST_RECORD_AUDIO);
                            }
                        })
                        .setNegativeButton("cancel", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                dialogInterface.dismiss();
                            }
                        }).create().show();
            } else {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.RECORD_AUDIO}, MY_PERMISSIONS_REQUEST_RECORD_AUDIO);
            }
        } else {
            // Toast.makeText(MainActivity.this, "You have already granted this permission", Toast.LENGTH_SHORT).show();
//            mRecorder = new MediaRecorder();
////            mRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
////            mRecorder.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP);
////            mRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);
////            mRecorder.setOutputFile("/dev/null");
////            try {
////                mRecorder.prepare();
////            } catch (IOException e) {
////                e.printStackTrace();
////            }
////            mRecorder.start();
            IntentFilter filter = new IntentFilter("APP_BROADCAST_RECEIVER");
       //     filter.addAction(APP_BROADCAST_RECEIVER);

            receiver = new BroadcastReceiver() {
                @Override
                public void onReceive(Context context, Intent intent) {
                    Bundle bundle = intent.getExtras();
                    if(builder != null){
                        String data=bundle.getString("data");

                    }
                }
            };

            registerReceiver(receiver,filter);



            thread = new Thread() {
                @Override
                public void run() {
                    try {
                        while (!thread.isInterrupted()) {
                            Thread.sleep(500);
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    // update TextView here!
                                    double amplitude = mRecorder.getMaxAmplitude();
                                    if (amplitude > 0 && amplitude < 1000000) {
                                        value = convertdDb(amplitude);
                                        s = String.valueOf(value);
                                        updateTextView(s);

                                        if (value >= 60) {
                                            if (value >70) {
                                                if (value >=71) {
                                                    if (value >=75) {
                                                        Vibrator v = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
                                                        long vibratePattern[] = {0, 5000};
                                                        v.vibrate(vibratePattern, 1);
                                                    }
                                                }
                                            }

                                        }
                                    }
                                }
                            });
                        }
                    } catch (InterruptedException e) {
                    }
                }
            };
            thread.start();
        }

    }

    @Override
    protected void onResume() {

        super.onResume();
     //   registerReceiver(receiver,APP_BROADCAST_RECEIVER);

    }


    @Override
    public void onBackPressed() {
        super.onBackPressed();
        mRecorder.stop();
        mRecorder.release();
        mRecorder = null;
    }

    private void updateTextView(final String a) {
        MainActivity.this.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                textView.setText(a+" Db");
            }
        });
    }

    public double convertdDb(double amplitude) {
        double mEMAValue = EMA_FILTER * amplitude + (1.0 - EMA_FILTER) * mEMA;
        return 20 * (float) Math.log10((mEMAValue/51805.5336)/ 0.000028251);
    }
}
