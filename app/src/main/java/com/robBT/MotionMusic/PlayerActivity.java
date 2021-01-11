package com.robBT.MotionMusic;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
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
    public static final String EXTRA_NUMBER = "com.robBT.MotionMusic.EXTRA_NUMBER";


    private Proximity proximity;
    private Boolean firstProx = true;
    private float lastProx;
    private Boolean accFirst = true;
    private float lastX, lastY, lastZ, xDiff, yDiff, zDiff;
    private Accelerometer accelerometer;
    public float threshH = 0;


    @SuppressLint("NewApi")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.music_player_ui);

        accelerometer = new Accelerometer( this);
        proximity = new Proximity(this);

        Intent intent = getIntent();
        threshH = (intent.getIntExtra(Calibrator.EXTRA_NUMBER, 0));
//        String Maxi = String.valueOf(threshH);
//        Toast.makeText(getApplicationContext(),Maxi,Toast.LENGTH_SHORT).show();

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
        sname = mySongs.get(position).getName().replace(".mp3","").replace(".wav","");
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
                } else {
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
                Uri u = Uri.parse(mySongs.get(position).toString());
                mp = MediaPlayer.create(getApplicationContext(),u);

                sname = mySongs.get(position).getName().replace(".mp3","").replace(".wav","");
                songNameText.setText(sname);

                pause.setBackgroundResource(R.drawable.pause_icon);
                mp.start();
                mp.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                    @Override
                    public void onCompletion(MediaPlayer mediaPlayer) {
                        next.performClick();
                    }
                });
            }
        });

        previous.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mp.stop();
                mp.release();

                position=((position-1)<0)?(mySongs.size()-1):(position-1);
                Uri u = Uri.parse(mySongs.get(position).toString());
                mp = MediaPlayer.create(getApplicationContext(),u);

                sname = mySongs.get(position).getName().replace(".mp3","").replace(".wav","");
                songNameText.setText(sname);

                pause.setBackgroundResource(R.drawable.pause_icon);
                mp.start();
                mp.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                    @Override
                    public void onCompletion(MediaPlayer mediaPlayer) {
                        next.performClick();
                    }
                });
            }
        });

        mp.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mediaPlayer) {
                next.performClick();
            }
        });

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
                    xDiff = Math.abs(lastX - currX);
                    yDiff = Math.abs(lastY - currY);
                    zDiff = Math.abs(lastZ - currZ);

//                    int threshH = 2;        //Low
//                    int threshH = 50;       //High

                    if ((xDiff > threshH && yDiff > threshH) || (xDiff > threshH && zDiff > threshH) || (yDiff > threshH && zDiff > threshH)){

                        //skip to random position
                        Random r = new Random();
                        mp.stop();
                        mp.release();
                        position=((position+(r.nextInt(mySongs.size())))%mySongs.size());
                        Uri u = Uri.parse(mySongs.get( position).toString());
                        songNameText.setText(sname);
                        mp = MediaPlayer.create(getApplicationContext(),u);

                        sname = mySongs.get(position).getName().replace(".mp3","").replace(".wav","");
                        songNameText.setText(sname);

                        pause.setBackgroundResource(R.drawable.pause_icon);
                        mp.start();

                        accFirst = true;
                    }
                }else {accFirst = false;}

                lastX = currX;
                lastY = currY;
                lastZ = currZ;
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.cal_menu, menu);
        getMenuInflater().inflate(R.menu.player_menu, menu);
        return super.onCreateOptionsMenu(menu);
    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if(item.getItemId()==android.R.id.home){
            onBackPressed();
        } else if (item.getItemId()==R.id.calInfo) {
            startActivity(new Intent(PlayerActivity.this, calPop.class));
        } else if (item.getItemId()==R.id.pInfo) {
            startActivity(new Intent(PlayerActivity.this, playerPop.class));
        }
        return super.onOptionsItemSelected(item);
    }

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