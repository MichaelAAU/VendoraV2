package com.aaufolks.android.vendora.Controller_Classes;

import android.content.Intent;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

import com.aaufolks.android.vendora.Model_Classes.Products;
import com.aaufolks.android.vendora.R;

import java.util.Timer;
import java.util.TimerTask;

/**
 * Created by michalisgratsias on 08/11/2016.
 */

public class LogoActivity extends AppCompatActivity {

    @Override
    public void onCreate(Bundle savedInstanceState) {   // it is Public because it can be called by various activities hosting it
        super.onCreate(savedInstanceState);
        setContentView(R.layout.fragment_logo);
    }

    @Override
    public void onResume() {
        super.onResume();
        final MediaPlayer mp = MediaPlayer.create(getApplicationContext(), R.raw.success);
        mp.start();
        Products.get(this).getProducts();
        // Execute some code after 1 seconds have passed 
        new Timer().schedule(new TimerTask() {
            @Override
            public void run() {
                Intent intent = new Intent(getApplicationContext(), ProductActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);
            }
        }, 1000);
    }
}
