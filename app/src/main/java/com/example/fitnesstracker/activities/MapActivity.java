package com.example.fitnesstracker.activities;

import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.IBinder;
import android.os.SystemClock;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Chronometer;
import android.widget.LinearLayout;
import android.widget.TextView;


import com.example.fitnesstracker.R;
import com.example.fitnesstracker.models.CoordinateModel;
import com.example.fitnesstracker.utils.Constants;
import com.example.fitnesstracker.utils.PermissionUtils;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;

import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.ArrayList;
import java.util.Observable;
import java.util.Observer;

public class MapActivity extends AppCompatActivity implements OnMapReadyCallback, View.OnClickListener, Observer {
    private ArrayList<CoordinateModel> mWalkedList = new ArrayList<>();
    private FusedLocationProviderClient mFusedLocationProviderClient;
    private GoogleMap mGoogleMap;
    private Marker mMarker;
    private Button startButton, continueButton, stopButton, saveButton;
    private LinearLayout linearLayout;
    private Chronometer chronometer;
    private TextView distance;
    private long lastPause;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map);
        initMap();

        startButton = findViewById(R.id.startButton);
        startButton.setOnClickListener(this);

        continueButton = findViewById(R.id.continueButton);
        continueButton.setOnClickListener(this);

        stopButton = findViewById(R.id.stopButton);
        stopButton.setOnClickListener(this);

        saveButton = findViewById(R.id.saveButton);
        saveButton.setOnClickListener(this);

        linearLayout = findViewById(R.id.linearLayout);

        distance = findViewById(R.id.distance);

        chronometer = findViewById(R.id.chronometer);
    }

    private void initMap() {
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
        mFusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);
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
        MarkerOptions options = new MarkerOptions()
                .position(new LatLng(location.getLatitude(), location.getLongitude()))
                .icon(BitmapDescriptorFactory.defaultMarker());
        mGoogleMap.clear();
        mMarker = mGoogleMap.addMarker(options);

    }

    @Override
    public void onClick(View v) {
        switch(v.getId()){
            case R.id.startButton:
                stopButton.setVisibility(View.VISIBLE);
                linearLayout.setVisibility(View.VISIBLE);
                distance.setVisibility(View.VISIBLE);
                chronometer.start();
                break;
            case R.id.stopButton:
                chronometer.stop();
                break;
            case R.id.continueButton:
                chronometer.start();
                break;
            case R.id.saveButton:
                stopButton.setVisibility(View.INVISIBLE);
                linearLayout.setVisibility(View.INVISIBLE);
                distance.setVisibility(View.INVISIBLE);
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
                    @Override
                    public void onComplete(@NonNull Task<Location> task) {
                        mGoogleMap.moveCamera(CameraUpdateFactory.newLatLng(new LatLng
                                (task.getResult().getLatitude(), task.getResult().getLongitude())));
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
            if (grantResults == null) {
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    startLocationService();
                    enableMyLocation();
                }
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

    protected void onDestroy() {
        super.onDestroy();
    }

    @Override
    public void update(Observable o, Object arg) {
    }
}