package com.example.fitnesstracker.activities;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;

import com.example.fitnesstracker.R;
import com.example.fitnesstracker.models.Routes;

import io.realm.Realm;
import io.realm.RealmResults;

public class RoutesListActivity extends AppCompatActivity {

    Realm mRealm;
    RoutesAdapter mAdapter;
    RealmResults<Routes> results;
    RecyclerView recyclerView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mRealm = Realm.getDefaultInstance();
        setContentView(R.layout.activity_routes);
        recyclerView = findViewById(R.id.recyclerView);
        results = Realm.getDefaultInstance().where(Routes.class).findAllSorted("id");
        recyclerView.setAdapter(mAdapter = new RoutesAdapter(results, RoutesListActivity.this));
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
    }
}