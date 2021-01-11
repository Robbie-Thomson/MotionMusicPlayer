package com.robBT.MotionMusic;

import android.Manifest;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.support.annotation.Nullable;
import android.support.annotation.RequiresApi;
import android.support.v7.app.AppCompatActivity;
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

public class Calibrator  extends AppCompatActivity {

    private Accelerometer accelerometer;
    private Boolean accFirst = true;
    Button calibrate;
    public float maxX, maxY, maxZ, MAX = 1;
    public static final String EXTRA_NUMBER = "com.robBT.MotionMusic.EXTRA_NUMBER";



    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.calibrate_acc);
        getSupportActionBar().setTitle("Calibrate");

        accelerometer = new Accelerometer( this);
        accelerometer.setListener(new Accelerometer.Listener() {
            @Override
            public void onTranslation(float currX, float currY, float currZ) {
            //ensure first pass is missed to ensure initial values are not read
            if(!accFirst) {
                if (currX > maxX) { maxX = currX; }
                if (currY > maxY) { maxY = currY; }
                if (currZ > maxZ) { maxZ = currZ; }
                MAX = (float) ((maxX+maxY+maxZ)/3);
            }else {accFirst = false;}
            }
        });

        calibrate = findViewById(R.id.calibrate);
        calibrate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (MAX > 0.5){
                    //start next activity
                    openActivity(MAX);
                } else {Toast.makeText(getApplicationContext(),"Please Shake",Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    public void openActivity(float MAX) {
        Intent intent = new Intent(this, Tunes.class);
        intent.putExtra(EXTRA_NUMBER, Math.round(MAX));
        startActivity(intent);
    }

    public float getMax(){
        return this.MAX;
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
