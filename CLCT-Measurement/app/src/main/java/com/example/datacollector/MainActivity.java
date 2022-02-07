package com.example.datacollector;

import androidx.appcompat.app.AppCompatActivity;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.TextView;

public class MainActivity extends AppCompatActivity {
    DataCollector dataCollector = new DataCollector(this);
    boolean isCollecting = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M)
            if (checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED)
                requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 1);

        TextView textView = findViewById(R.id.text);

        Button startButton = findViewById(R.id.start_button);
        startButton.setOnClickListener(view -> {
            if (isCollecting) return;
            isCollecting = true;
            textView.setText(R.string.collecting);
            new Thread(() -> dataCollector.start()).start();
        });

        Button stopButton = findViewById(R.id.stop_button);
        stopButton.setOnClickListener(view -> {
            if (!isCollecting) return;
            isCollecting = false;
            textView.setText(R.string.waiting);
            dataCollector.stop();
        });
    }
}