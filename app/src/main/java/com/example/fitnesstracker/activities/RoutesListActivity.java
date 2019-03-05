package com.example.fitnesstracker.activities;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.widget.ListView;

import com.example.fitnesstracker.R;
import com.example.fitnesstracker.models.Routes;

import io.realm.RealmResults;

public class RoutesListActivity extends AppCompatActivity {
    RoutesAdapter adapter;
    RealmResults<Routes> results;
    ListView listView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_routes);
        listView = findViewById(R.id.listView);
        listView.setAdapter(adapter = new RoutesAdapter(RoutesListActivity.this, results));
    }
}
