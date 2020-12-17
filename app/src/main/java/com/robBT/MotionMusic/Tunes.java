package com.robBT.MotionMusic;

import android.Manifest;
import android.content.Context;
import android.graphics.Color;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Build;
import android.support.annotation.RequiresApi;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.content.Intent;
import android.os.Environment;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;

import com.karumi.dexter.Dexter;
import com.karumi.dexter.PermissionToken;
import com.karumi.dexter.listener.PermissionDeniedResponse;
import com.karumi.dexter.listener.PermissionGrantedResponse;
import com.karumi.dexter.listener.PermissionRequest;
import com.karumi.dexter.listener.single.PermissionListener;


import java.io.File;
import java.util.ArrayList;
import java.util.Random;

//public class Tunes extends AppCompatActivity implements SensorEventListener {
public class Tunes extends AppCompatActivity {

    ListView listView;
    String[] items;
    Button shuffle;
    public int songLen = 0;


    //shake to play/pause
//    private SensorManager sensorManager;
//    private Sensor accelerometer;
//    private Boolean accAvailable,
    private Boolean accFirst = true, acc2 = true;
//    private float currX, currY, currZ, lastX, lastY, lastZ, xDiff, yDiff, zDiff;
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
                Random r = new Random();
                int pos = r.nextInt(songLen);

                listView.performItemClick(
                        listView.getAdapter().getView(pos, null, null), pos, pos);
            }
        });

        //initialise sensor
//        sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
//        if (sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER) != null){
//            accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
//            accAvailable = true;
//        }else { accAvailable = false;}
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
                        if ((xDiff >15 && yDiff >15) || (xDiff >15 && zDiff >15) || (yDiff >15 && zDiff >15)){
                            shuffle.performClick();
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
        getMenuInflater().inflate(R.menu.list_menu, menu);
        return super.onCreateOptionsMenu(menu);
    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        startActivity(new Intent(Tunes.this, listPop.class));
        return super.onOptionsItemSelected(item);
    }

    public ArrayList<File> findSong(File root){
        ArrayList<File> songList = new ArrayList<File>();
        File[] files = root.listFiles();
        for(File singleFile : files){
            if(singleFile.isDirectory() && !singleFile.isHidden()){
                songList.addAll(findSong(singleFile));
            }
            else{
                if((singleFile.getName().endsWith(".mp3") || singleFile.getName().endsWith(".wav")) &&
                                !(singleFile.getName().contains("._")
                                || singleFile.getName().contains("Slack")
                                || singleFile.getName().contains("soundscape")) ){
                    songList.add(singleFile);
                }
            }
        }
        return songList;
    }

    public void display(){
        final ArrayList<File> mySongs = findSong(Environment.getExternalStorageDirectory());
        songLen = mySongs.size();
        items = new String[ songLen ];
        for(int i=0;i<mySongs.size();i++){
            items[i] = mySongs.get(i).getName().replace(".mp3","").replace(".wav","");
        }
        ArrayAdapter<String> adp = new ArrayAdapter<String>(this,android.R.layout.simple_list_item_1,items);
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


    //shake to play/pause
//    @Override
//    public void onSensorChanged(SensorEvent sensorEvent) {
//        currX = sensorEvent.values[0];
//        currY = sensorEvent.values[1];
//        currZ = sensorEvent.values[2];
//
//        //make sure its not the first set of values
//        if(!accFirst) {
//            xDiff = Math.abs(lastX - currX);
//            yDiff = Math.abs(lastY - currY);
//            zDiff = Math.abs(lastZ - currZ);
//            //if ((xDiff > 8 && yDiff > 8) || (xDiff > 8 && zDiff > 8) || (yDiff > 8 && zDiff > 8) || (xDiff > 1)) {
//            if ((xDiff >8 && yDiff >8) || (xDiff >8 && zDiff >8) || (yDiff >8 && zDiff >8)){
//                shuffle.performClick();
//                //ensure next pass doesn't activate again
//                accFirst = true;
//            }
//        } else {accFirst = false;}
//
//        lastX = currX;
//        lastY = currY;
//        lastZ = currZ;
//    }
//    @Override
//    public void onAccuracyChanged(Sensor sensor, int i) {
//    }
//    @Override
//    protected void onResume() {
//        super.onResume();
//        if (accAvailable)
//            sensorManager.registerListener(this, accelerometer, SensorManager.SENSOR_DELAY_NORMAL);
//    }
//    @Override
//    protected void onPause() {
//        super.onPause();
//        if (accAvailable)
//            sensorManager.unregisterListener(this);
//    }
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
