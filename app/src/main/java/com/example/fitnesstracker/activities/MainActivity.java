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

        plusButton = findViewById(R.id.plusButton);
        plusButton.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
                Intent intentThree = new Intent(MainActivity.this, ActivityOne.class);
                startActivity(intentThree);
    }

}
