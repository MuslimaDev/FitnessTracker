package com.example.fitnesstracker.activities;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;

import com.example.fitnesstracker.R;
import com.example.fitnesstracker.models.Routes;

import java.util.List;

import io.realm.Realm;
import io.realm.RealmResults;

public class RoutesListActivity extends AppCompatActivity  implements CallBackRealm {

    Realm mRealm;
    RoutesAdapter mAdapter;
    RealmResults<Routes> results;
    RecyclerView recyclerView;
    Toolbar toolbar;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mRealm = Realm.getDefaultInstance();
        setContentView(R.layout.activity_routes);

        ActionBar actionBar = getSupportActionBar();
        actionBar.setHomeButtonEnabled(true);
        actionBar.setDisplayHomeAsUpEnabled(true);

        recyclerView = findViewById(R.id.recyclerView);
        results = Realm.getDefaultInstance().where(Routes.class).findAllSorted("id");
        recyclerView.setAdapter(mAdapter = new RoutesAdapter(results, RoutesListActivity.this));
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
    }

    public boolean onOptionsItemSelected(MenuItem item){
        Intent myIntent = new Intent(getApplicationContext(), MapActivity.class);
        startActivityForResult(myIntent, 0);
        return true;
    }

    @Override
    public void deleteRealmObject(List<Routes> list, final int position) {
        mRealm.beginTransaction();
        mRealm.getDefaultInstance().where(Routes.class).equalTo("id",position).findAll().deleteAllFromRealm();
        mRealm.commitTransaction();
        mAdapter.notifyDataSetChanged();
    }
}