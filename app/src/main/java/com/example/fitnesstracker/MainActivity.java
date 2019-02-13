package com.example.fitnesstracker;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import java.util.Map;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {
    private Button button;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        button = findViewById(R.id.plusButton);
        button.setOnClickListener(this);
    }

    public void onClick(View view) {
        Intent intent = new Intent(getApplicationContext(), MapActivity.class);
        startActivity(new Intent(this, MapActivity.class));
    }
}
