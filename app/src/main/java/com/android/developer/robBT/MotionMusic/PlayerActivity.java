package com.android.developer.robBT.MotionMusic;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
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

public class PlayerActivity extends AppCompatActivity implements SensorEventListener {

    static MediaPlayer mp;//assigning memory loc once or else multiple songs will play at once
    int position;
    ArrayList<File> mySongs;
    Button pause,next,previous;
    TextView songNameText;
    String sname;

    //shake to play/pause
    private SensorManager sensorManager;
    private Sensor light;
    private Boolean lightAvailable;
    private float currX, lastX,  xDiff;

    @SuppressLint("NewApi")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.music_player_ui);

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
               // songNameText.setText(getSongName);
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

        //shake to play random
        sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        if (sensorManager.getDefaultSensor(Sensor.TYPE_LIGHT) != null){
            light = sensorManager.getDefaultSensor(Sensor.TYPE_LIGHT);
            lightAvailable = true;
        }else { lightAvailable = false;}


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
    @Override
    public void onSensorChanged(SensorEvent sensorEvent) {
        currX = sensorEvent.values[0];
        xDiff = Math.abs(lastX - currX);

        if (2*currX < lastX){
            pause.performClick();
        }
        lastX = currX;
    }
    @Override
    public void onAccuracyChanged(Sensor sensor, int i) {
    }
    @Override
    protected void onResume() {
        super.onResume();
        if (lightAvailable)
            sensorManager.registerListener(this, light, SensorManager.SENSOR_DELAY_NORMAL);
    }
    @Override
    protected void onPause() {
        super.onPause();
        if (lightAvailable)
            sensorManager.unregisterListener(this);
    }
}