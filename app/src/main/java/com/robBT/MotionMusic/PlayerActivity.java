package com.robBT.MotionMusic;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.util.ArrayList;
import java.util.Random;

public class PlayerActivity extends AppCompatActivity{

    static MediaPlayer mp;//assigning memory loc once or else multiple songs will play at once
    int position;
    ArrayList<File> mySongs;
    Button pause,next,previous;
    TextView songNameText;
    String sname;

    private Proximity proximity;
    //shake to play/pause
//    private SensorManager sensorManager;
//    private Sensor prox;
//    private Boolean proxAvailable;
//    private float currX, lastX,  xDiff;
    private Boolean firstProx = true;
    private float lastProx;
    private Boolean accFirst = true, acc2 = true;
    private float lastX, lastY, lastZ, xDiff, yDiff, zDiff;
    private Accelerometer accelerometer;

    @SuppressLint("NewApi")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.music_player_ui);

        accelerometer = new Accelerometer( this);
        proximity = new Proximity(this);

        songNameText = findViewById(R.id.txtSongLabel);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeButtonEnabled(true);
        getSupportActionBar().setTitle("Now Playing");

        pause = findViewById(R.id.pause);
        previous = findViewById(R.id.previous);
        next = findViewById(R.id.next);

        if(mp != null){
            mp.stop();
            mp.release();
        }
        Intent i = getIntent();
        Bundle b = i.getExtras();


        mySongs = (ArrayList) b.getParcelableArrayList("songs");

        sname = mySongs.get(position).getName();

        String SongName = i.getStringExtra("songname");
        songNameText.setText(SongName);
        songNameText.setSelected(true);

        position = b.getInt("pos",0);
        Uri u = Uri.parse(mySongs.get(position).toString());

        mp = MediaPlayer.create(getApplicationContext(),u);
        mp.start();

        pause.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(mp.isPlaying()){
                    pause.setBackgroundResource(R.drawable.play_icon);
                    mp.pause();

                }
                else {
                    pause.setBackgroundResource(R.drawable.pause_icon);
                    mp.start();
                }
            }
        });

        next.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mp.stop();
                mp.release();
                position=((position+1)%mySongs.size());
                Uri u = Uri.parse(mySongs.get( position).toString());
                songNameText.setText(sname);
                mp = MediaPlayer.create(getApplicationContext(),u);

                sname = mySongs.get(position).getName();
                songNameText.setText(sname);

                pause.setBackgroundResource(R.drawable.pause_icon);
                mp.start();
            }
        });

        previous.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //songNameText.setText(getSongName);
                mp.stop();
                mp.release();

                position=((position-1)<0)?(mySongs.size()-1):(position-1);
                Uri u = Uri.parse(mySongs.get(position).toString());
                mp = MediaPlayer.create(getApplicationContext(),u);
                sname = mySongs.get(position).getName();
                songNameText.setText(sname);
                pause.setBackgroundResource(R.drawable.pause_icon);
                mp.start();
            }
        });

        mp.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mediaPlayer) {
                next.performClick();
            }
        });

        //shake to play random
//        sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
//        if (sensorManager.getDefaultSensor(Sensor.TYPE_PROXIMITY) != null){
//            prox = sensorManager.getDefaultSensor(Sensor.TYPE_PROXIMITY);
//            proxAvailable = true;
//        }else { proxAvailable = false;}

        proximity.setListener(new Proximity.Listener() {
            @Override
            public void onRotation(float prox) {
                if (!firstProx) {
                    if (prox < lastProx * 0.8) {
                        pause.performClick();
                        firstProx = true;
                    }
                } else { firstProx = false; }
                lastProx = prox;
            }
        });

        accelerometer.setListener(new Accelerometer.Listener() {
            @Override
            public void onTranslation(float currX, float currY, float currZ) {
                //ensure first pass is missed to ensure lastX, lastY and lastZ have values
                if(!accFirst) {
                    //ensure this does not happen duplicate times
                    if(!acc2) {
                        xDiff = Math.abs(lastX - currX);
                        yDiff = Math.abs(lastY - currY);
                        zDiff = Math.abs(lastZ - currZ);
                        if ((xDiff >10 && yDiff >10) || (xDiff >10&& zDiff >10) || (yDiff >10 && zDiff >10)){
                            // stuff from next

                            Random r = new Random();
                            mp.stop();
                            mp.release();
                            position=((position+(r.nextInt(mySongs.size())))%mySongs.size());
                            Uri u = Uri.parse(mySongs.get( position).toString());
                            songNameText.setText(sname);
                            mp = MediaPlayer.create(getApplicationContext(),u);

                            sname = mySongs.get(position).getName();
                            songNameText.setText(sname);

                            pause.setBackgroundResource(R.drawable.pause_icon);
                            mp.start();


                            accFirst = true;
                            acc2 = true;
                        }
                    }else {acc2 = false;}
                }else {accFirst = false;}
                lastX = currX;
                lastY = currY;
                lastZ = currZ;
            }
        });

    }



    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.player_menu, menu);
        return super.onCreateOptionsMenu(menu);
    }
     @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if(item.getItemId()==android.R.id.home){
            onBackPressed();
        } else if (item.getItemId()==R.id.pInfo)
        {
            startActivity(new Intent(PlayerActivity.this, playerPop.class));
        }
        return super.onOptionsItemSelected(item);
    }




    //cover to play/pause
//    @Override
//    public void onSensorChanged(SensorEvent sensorEvent) {
//        currX = sensorEvent.values[0];
//        if (currX < lastX * 0.8){
//            pause.performClick();
//        }
//        lastX = currX;
//    }
//    @Override
//    public void onAccuracyChanged(Sensor sensor, int i) {
//    }
    @Override
    protected void onResume() {
        super.onResume();
        proximity.register();
        accelerometer.register();
    }

    @Override
    protected void onPause() {
        super.onPause();
        proximity.unregister();
        accelerometer.unregister();
    }
}