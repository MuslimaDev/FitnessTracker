package com.example.fitnesstracker.activities;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import com.example.fitnesstracker.R;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {
    Button history, connectedDevices, plusButton;

    @SuppressLint("WrongViewCast")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

     /*   history = findViewById(R.id.history);
        history.setOnClickListener(this);

        connectedDevices = findViewById(R.id.connectedDevices);
        connectedDevices.setOnClickListener(this);*/

        plusButton = findViewById(R.id.plusButton);

        plusButton.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.history:
                Intent intent = new Intent(MainActivity.this, RoutesHistoryActivity.class);
                startActivity(intent);
                break;
            case R.id.connectedDevices:
                Intent intentTwo = new Intent(MainActivity.this, ConnectedDevicesActivity.class);
                startActivity(intentTwo);
                break;
            case R.id.plusButton:
                Intent intentThree = new Intent(MainActivity.this, MapActivity.class);
                startActivity(intentThree);
                break;
        }
    }

}
