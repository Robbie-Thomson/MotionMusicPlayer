package com.robBT.MotionMusic;

import android.Manifest;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.support.annotation.RequiresApi;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;

import com.karumi.dexter.Dexter;
import com.karumi.dexter.PermissionToken;
import com.karumi.dexter.listener.PermissionDeniedResponse;
import com.karumi.dexter.listener.PermissionGrantedResponse;
import com.karumi.dexter.listener.PermissionRequest;
import com.karumi.dexter.listener.single.PermissionListener;

import java.io.File;
import java.util.ArrayList;
import java.util.Random;

public class Tunes extends AppCompatActivity {

    ListView listView;
    String[] items;
    Button shuffle;
    public int songLen = 0;

    private Boolean accFirst = true, loaded = false;
    private float lastX, lastY, lastZ, xDiff, yDiff, zDiff;
    private Accelerometer accelerometer;

    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tunes);
        listView = (ListView) findViewById(R.id.listView);
        getSupportActionBar().setTitle("Motion Music");

        accelerometer = new Accelerometer( this);

        Dexter.withActivity(this)
                .withPermission(Manifest.permission.READ_EXTERNAL_STORAGE)
                .withListener(new PermissionListener() {
                    @Override
                    public void onPermissionGranted(PermissionGrantedResponse response) {
                        getSupportActionBar().setTitle("Music List");
                        display();
                    }

                    @Override
                    public void onPermissionDenied(PermissionDeniedResponse response) {
                        // check for permanent denial of permission
                        setContentView(R.layout.no_permission);
                        getSupportActionBar().setTitle("Motion Music");
                        if (response.isPermanentlyDenied()) {
                            setContentView(R.layout.no_permission);
                            getSupportActionBar().setTitle("Motion Music");
                        }
                    }

                    @Override
                    public void onPermissionRationaleShouldBeShown(PermissionRequest permission, PermissionToken token) {
                        token.continuePermissionRequest();
                    }
                }).check();

        shuffle = findViewById(R.id.shuffle);
        shuffle.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //generate random number to use as position of what song to play
                Random r = new Random();
                int pos = r.nextInt(songLen);

                listView.performItemClick(
                        listView.getAdapter().getView(pos, null, null), pos, pos);
            }
        });

        accelerometer.setListener(new Accelerometer.Listener() {
            @Override
            public void onTranslation(float currX, float currY, float currZ) {
                //ensure first pass is missed to ensure lastX, lastY and lastZ have values
                if(!accFirst && loaded) {
                    xDiff = Math.abs(lastX - currX);
                    yDiff = Math.abs(lastY - currY);
                    zDiff = Math.abs(lastZ - currZ);

//                    int threshH = 2;        //Low
//                    int threshH = 16;       //Mid
                    int threshH = 50;       //High

                    if ((xDiff > threshH && yDiff > threshH) || (xDiff > threshH && zDiff > threshH) || (yDiff > threshH && zDiff > threshH)){
                        shuffle.performClick();
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
        getMenuInflater().inflate(R.menu.list_menu, menu);
        return super.onCreateOptionsMenu(menu);
    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        startActivity(new Intent(Tunes.this, listPop.class));
        return super.onOptionsItemSelected(item);
    }

    public ArrayList<File> findSong(File root){
        ArrayList<File> songList = new ArrayList<>();
        File[] files = root.listFiles();
        for(File singleFile : files){
            if(singleFile.isDirectory() && !singleFile.isHidden()){
                songList.addAll(findSong(singleFile));
            }
            else{
                if((singleFile.getName().endsWith(".mp3") || singleFile.getName().endsWith(".wav")) &&
                                !(singleFile.getName().contains("._")
                                || singleFile.getName().contains("Slack")
                                || singleFile.getName().contains("soundscape")) )
                {   songList.add(singleFile);   }
            }
        }
        return songList;
    }

    public void display(){
        final ArrayList<File> mySongs = findSong(Environment.getExternalStorageDirectory());
        songLen = mySongs.size();
        if (songLen >0) { loaded = true; }
        items = new String[ songLen ];
        for(int i=0;i<mySongs.size();i++){
            items[i] = mySongs.get(i).getName().replace(".mp3","").replace(".wav","");
        }
        ArrayAdapter<String> adp = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, items);
        listView.setAdapter(adp);


        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int
                    position, long l) {

                String songName = listView.getItemAtPosition(position).toString();
                startActivity(new Intent(getApplicationContext(),PlayerActivity.class)
                        .putExtra("pos",position).putExtra("songs",mySongs).putExtra("songname",songName));
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        accelerometer.register();
    }

    @Override
    protected void onPause() {
        super.onPause();
        accelerometer.unregister();
    }

}
