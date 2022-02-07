package com.example.bts_app;

import androidx.appcompat.app.AppCompatActivity;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.orhanobut.logger.AndroidLogAdapter;
import com.orhanobut.logger.Logger;

public class MainActivity extends AppCompatActivity {
    BandwidthTest bandwidthTest = new BandwidthTest(this);
    boolean isTesting = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M)
            if (checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED)
                requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 1);

        Logger.addLogAdapter(new AndroidLogAdapter());

        TextView textView = findViewById(R.id.text);
        Button button = findViewById(R.id.start);
        button.setOnClickListener(view -> {
            if (isTesting) {
                isTesting = false;
                button.setText(R.string.start);
                bandwidthTest.stop();
            } else {
                isTesting = true;
                button.setText(R.string.stop);
                textView.setText(R.string.testing);
                new Thread(() -> {
                    double bandwidth = 0;
                    try {
                        bandwidth = bandwidthTest.SpeedTest();
                        bandwidthTest.stop();
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    double finalBandwidth = bandwidth;
                    runOnUiThread(() -> {
                        isTesting = false;
                        button.setText(R.string.start);
                        String result = finalBandwidth + "Mbps";
                        textView.setText(result);
                    });
                }).start();
            }
        });
    }
}