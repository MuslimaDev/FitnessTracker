package com.example.fitnesstracker.activities;

import android.annotation.SuppressLint;
import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.graphics.Color;
import android.location.Location;
import android.os.IBinder;
import android.os.SystemClock;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.Chronometer;
import android.widget.TextView;
import android.widget.Toast;

import com.akexorcist.googledirection.util.DirectionConverter;
import com.example.fitnesstracker.R;
import com.example.fitnesstracker.models.CoordinateModel;
import com.example.fitnesstracker.models.Routes;
import com.example.fitnesstracker.utils.Constants;
import com.example.fitnesstracker.utils.PermissionUtils;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;

import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;
import java.util.Objects;
import java.util.Observable;
import java.util.Observer;

import io.realm.Realm;

public class MapActivity extends AppCompatActivity implements OnMapReadyCallback, View.OnClickListener, Observer {
    private ArrayList<CoordinateModel> mWalkedList = new ArrayList<>();
    private GoogleMap mGoogleMap;
    private Button stopButton;
    private Chronometer chronometer;
    private TextView distance;
    private long pauseOffset;
    private Realm realm;

    @SuppressLint("SetTextI18n")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map);
        realm = Realm.getDefaultInstance();
        initMap();

        Button startButton = findViewById(R.id.startButton);
        startButton.setOnClickListener(this);

        Button continueButton = findViewById(R.id.continueButton);
        continueButton.setOnClickListener(this);

        stopButton = findViewById(R.id.stopButton);
        stopButton.setOnClickListener(this);

        Button saveButton = findViewById(R.id.saveButton);
        saveButton.setOnClickListener(this);

        distance = findViewById(R.id.distance);
        distance.setText("  Distance: 0,00 km  ");

        chronometer = findViewById(R.id.chronometer);
        chronometer.setFormat("  Time: %s  ");
    }

    private void initMap() {
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        if (mapFragment != null) {
            mapFragment.getMapAsync(this);
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        EventBus.getDefault().register(this);
    }

    @Override
    protected void onStop() {
        super.onStop();
        EventBus.getDefault().unregister(this);
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void locationRecived(Location location) {
        saveRoad(location);
        countDistance();
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.startButton:
                stopButton.setVisibility(View.VISIBLE);
                chronometer.setVisibility(View.VISIBLE);
                distance.setVisibility(View.VISIBLE);
                chronometer.setBase(SystemClock.elapsedRealtime() - pauseOffset);
                chronometer.start();
                mService.startLocationUpdates();
                DataHandler.getInstance().addObserver(this);
                break;
            case R.id.stopButton:
                chronometer.stop();
                pauseOffset = SystemClock.elapsedRealtime() - chronometer.getBase();
                mService.stopLocationUpdates();
                break;
            case R.id.continueButton:
                chronometer.setBase(SystemClock.elapsedRealtime() - pauseOffset);
                chronometer.start();
                mService.startLocationUpdates();
                break;
            case R.id.saveButton:
                int id;
                if (Realm.getDefaultInstance().where(Routes.class).max("id") != null) {
                    Number number = Realm.getDefaultInstance().where(Routes.class).max("id");
                    id = number.intValue() + 1;
                } else {
                    id = 0;
                }
                SimpleDateFormat sdf = new SimpleDateFormat("MMMM d, HH:mm:ss", Locale.US);
                String currentDateTime = sdf.format(new Date());
                realm.beginTransaction();
                Routes routes = realm.createObject(Routes.class, id);
                routes.setDistance(distance.getText().toString());
                routes.setTime(chronometer.getText().toString());
                routes.setDate(currentDateTime);
                realm.commitTransaction();
                Toast.makeText(this, "Result saved", Toast.LENGTH_LONG).show();
                stopButton.setVisibility(View.INVISIBLE);
                chronometer.setVisibility(View.INVISIBLE);
                distance.setVisibility(View.INVISIBLE);
                mService.stopLocationUpdates();
                chronometer.setBase(SystemClock.elapsedRealtime());
                chronometer.stop();
                pauseOffset = 0;
                mWalkedList.clear();
                mGoogleMap.clear();
                distance.setText("  Distance: 0,00 km  ");
                break;
        }
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mGoogleMap = googleMap;
        startLocationService();
        enableMyLocation();
    }

    LocationUpdateService mService;

    private void startLocationService() {
        ServiceConnection serviceConnection = new ServiceConnection() {
            @Override
            public void onServiceConnected(ComponentName componentName, IBinder iBinder) {
                mService = ((LocationUpdateService.LocalBinder) iBinder).getService();
                mService.getLastLocation(new OnCompleteListener<Location>() {
                    @SuppressLint("NewApi")
                    @Override
                    public void onComplete(@NonNull Task<Location> task) {
                        mGoogleMap.moveCamera(CameraUpdateFactory.newLatLng(new LatLng(Objects.requireNonNull(task.getResult()).getLatitude(), task.getResult().getLongitude())));
                    }
                });
            }

            @Override
            public void onServiceDisconnected(ComponentName componentName) {
                mService = null;
            }
        };
        bindService(new Intent(this, LocationUpdateService.class), serviceConnection, BIND_AUTO_CREATE);
    }

    @Override
    protected void onResume() {
        super.onResume();
        checkLocationService();
    }

    private void checkLocationService() {
        if (PermissionUtils.isLocationServicesEnabled(this)) {
            enableMyLocation();
        }
    }

    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == Constants.REQUEST_CODE_LOCATION_PERMISSION) {
            if (grantResults.length == 0) {
                enableMyLocation();
            }
        }
    }

    private void enableMyLocation() {
        if (PermissionUtils.checkLocationPermission(this) && mGoogleMap != null) {
            mGoogleMap.setMyLocationEnabled(true);
            mGoogleMap.setMaxZoomPreference(18);
            mGoogleMap.setMinZoomPreference(15);
        }
    }

    private void drawRoute(ArrayList<CoordinateModel> routeList) {
        ArrayList<LatLng> latLngs = new ArrayList<>();

        for (Location location : routeList) {
            latLngs.add(new LatLng(location.getLatitude(), location.getLongitude()));
        }

        PolylineOptions options = DirectionConverter.createPolyline(this, latLngs, 4, Color.parseColor("#38c404"));
        mGoogleMap.addPolyline(options);
    }

    @SuppressLint("DefaultLocale")
    private void countDistance() {
        double myDistance = 0;
        for (int i = 0; i < mWalkedList.size() - 1; i++) {
            if (!mWalkedList.get(i).isStop()) {
                myDistance += mWalkedList.get(i).distanceTo(mWalkedList.get(i + 1));
            }
        }
        distance.setText(String.format("  Distance: %.2f km  ", myDistance / 1000));
    }

    private void saveRoad(Location location) {
        mWalkedList.add((new CoordinateModel(location)));
        drawRoute(mWalkedList);
    }

    protected void onDestroy() {
        super.onDestroy();
        DataHandler.getInstance().deleteObserver(this);
    }

    @Override
    public void update(Observable observable, Object o) {}

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.itemRoutes:
                Intent intent = new Intent(this, RoutesListActivity.class);
                startActivityForResult(intent, 0);
                break;
            case R.id.itemBluetooth:
                Intent intentBluetooth = new Intent(this, BluetoothActivity.class);
                startActivityForResult(intentBluetooth, 0);
                break;
        }
        return super.onOptionsItemSelected(item);
    }
}