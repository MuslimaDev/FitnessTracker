/*
package com.example.fitnesstracker.activities;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.Chronometer;
import android.widget.ImageView;
import android.widget.LinearLayout;

import com.example.fitnesstracker.R;
import com.mapbox.android.core.permissions.PermissionsListener;
import com.mapbox.android.core.permissions.PermissionsManager;
import com.mapbox.mapboxsdk.Mapbox;
import com.mapbox.mapboxsdk.annotations.Icon;
import com.mapbox.mapboxsdk.annotations.IconFactory;
import com.mapbox.mapboxsdk.annotations.Marker;
import com.mapbox.mapboxsdk.annotations.MarkerOptions;
import com.mapbox.mapboxsdk.camera.CameraPosition;
import com.mapbox.mapboxsdk.camera.CameraUpdateFactory;
import com.mapbox.mapboxsdk.geometry.LatLng;
import com.mapbox.mapboxsdk.location.LocationComponent;
import com.mapbox.mapboxsdk.location.OnCameraTrackingChangedListener;
import com.mapbox.mapboxsdk.location.OnLocationClickListener;
import com.mapbox.mapboxsdk.location.modes.CameraMode;
import com.mapbox.mapboxsdk.location.modes.RenderMode;
import com.mapbox.mapboxsdk.maps.MapView;
import com.mapbox.mapboxsdk.maps.MapboxMap;
import com.mapbox.mapboxsdk.maps.OnMapReadyCallback;
import com.mapbox.mapboxsdk.maps.Style;

import java.util.List;

public class MapActivity extends AppCompatActivity implements OnMapReadyCallback, View.OnClickListener, OnLocationClickListener, LocationListener, PermissionsListener, OnCameraTrackingChangedListener {

    private PermissionsManager permissionsManager;
    private MapView mapView;
    private MapboxMap mapboxMap;
    private LocationComponent locationComponent;
    private Button stopButton, startButton;
    private double lat, lng;
    private ImageView imageView;
    private LocationManager locationManager;
    private LocationListener locationListener;
    private Location location;
    private LinearLayout firstLL, secondLL;
    private Chronometer chronometer;

    @SuppressLint("MissingPermission")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Mapbox.getInstance(this, getString(R.string.mapbox_access_token));
        setContentView(R.layout.activity_map);

        mapView = findViewById(R.id.mapView);
        mapView.onCreate(savedInstanceState);
        mapView.getMapAsync(this);

        imageView = findViewById(R.id.myLocation);
        imageView.setOnClickListener(this);

        startButton = findViewById(R.id.startButton);
        startButton.setOnClickListener(this);

        stopButton = findViewById(R.id.stopButton);
        stopButton.setOnClickListener(this);

        firstLL = findViewById(R.id.linearLayoutOne);
        secondLL = findViewById(R.id.linearLayoutTwo);
        chronometer = findViewById(R.id.chronometer);

        showLocation(location);
    }

    @Override
    public void onMapReady(@NonNull MapboxMap mapboxMap) {
        this.mapboxMap = mapboxMap;
        mapboxMap.setStyle(Style.MAPBOX_STREETS, new Style.OnStyleLoaded() {
            @Override
            public void onStyleLoaded(@NonNull Style style) {
                enableLocationComponent(style);
            }
        });

        Icon icon = IconFactory.getInstance(MapActivity.this).fromResource(R.drawable.placeholder);
        Marker marker = mapboxMap.addMarker(new MarkerOptions()
                .position(new LatLng(42.8700000, 74.5900000))
                .title("Current Location")
                .icon(icon));
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.myLocation:
                cameraUpdate(locationComponent);
                break;
            case R.id.startButton:
                stopButton.setVisibility(View.VISIBLE);
                firstLL.setVisibility(View.VISIBLE);
                secondLL.setVisibility(View.VISIBLE);
                chronometer.start();
                break;
            case R.id.stopButton:
                stopButton.setVisibility(View.INVISIBLE);
                firstLL.setVisibility(View.INVISIBLE);
                secondLL.setVisibility(View.INVISIBLE);
                chronometer.setFormat(null);
                break;
        }
    }


    private void cameraUpdate(LocationComponent locationComponent) {
        if (mapboxMap != null) {
            CameraPosition position = new CameraPosition.Builder()
                    .target(new LatLng(42.8700000, 74.5900000))
                    .bearing(0)
                    .zoom(13).tilt(15).build();
            mapboxMap.animateCamera(CameraUpdateFactory.newCameraPosition(position));
        }
    }

    @SuppressLint("MissingPermission")
    private void enableLocationComponent(@NonNull Style loadedMapStyle) {
        if (PermissionsManager.areLocationPermissionsGranted(this)) {
            locationComponent = mapboxMap.getLocationComponent();
            locationComponent.activateLocationComponent(this, loadedMapStyle);
            locationComponent.setLocationComponentEnabled(true);
            locationComponent.setCameraMode(CameraMode.TRACKING);
            locationComponent.setRenderMode(RenderMode.COMPASS);
            locationComponent.addOnLocationClickListener(this);
            locationComponent.addOnCameraTrackingChangedListener(this);
        } else {
            permissionsManager = new PermissionsManager(this);
            permissionsManager.requestLocationPermissions(this);
        }
    }

    @SuppressWarnings({"MissingPermission"})
    @Override
    public void onLocationComponentClick() {
        locationComponent.getLastKnownLocation().getLatitude();
        locationComponent.getLastKnownLocation().getLongitude();
    }

    @Override
    public void onCameraTrackingDismissed() {
        boolean isInTrackingMode = false;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == 10) {
            showLocation(location);
        }
    }

    @Override
    public void onPermissionResult(boolean granted) {
        if (granted) {
            mapboxMap.getStyle(new Style.OnStyleLoaded() {
                @Override
                public void onStyleLoaded(@NonNull Style style) {
                    enableLocationComponent(style);
                }
            });
        } else {
            finish();
        }
    }

    @SuppressLint("MissingPermission")
    public void showLocation(Location location) {
        locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
        //   locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 1000 * 10, 10, locationListener);
        if (location != null) {
            Intent intent = new Intent();
            intent.putExtra("location1", location.getLatitude());
            intent.putExtra("location2", location.getLongitude());
            startActivity(intent);
            finish();
        }
    }


    @Override
    public void onCameraTrackingChanged(int currentMode) {
    }

    @Override
    public void onExplanationNeeded(List<String> permissionsToExplain) {
    }

    @Override
    public void onLocationChanged(Location location) {

    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {

    }

    @SuppressLint("MissingPermission")
    @Override
    public void onProviderEnabled(String provider) {

        showLocation(locationManager.getLastKnownLocation(provider));
    }

    @Override
    public void onProviderDisabled(String provider) {

    }

    protected void onStart() {
        super.onStart();
        mapView.onStart();
    }

    @Override
    protected void onResume() {
        super.onResume();
        mapView.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
        mapView.onPause();
    }

    @Override
    protected void onStop() {
        super.onStop();
        mapView.onStop();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        mapView.onSaveInstanceState(outState);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mapView.onDestroy();
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
        mapView.onLowMemory();
    }
}*/
