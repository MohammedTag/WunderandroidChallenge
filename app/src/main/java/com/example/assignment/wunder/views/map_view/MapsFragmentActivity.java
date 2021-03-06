package com.example.assignment.wunder.views.map_view;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.example.assignment.wunder.R;
import com.example.assignment.wunder.models.Vehicle;
import com.example.assignment.wunder.presenters.map_presenter.MapViewPresenter;
import com.example.assignment.wunder.utils.Util;
import com.example.assignment.wunder.views.car_details_view.MainActivity;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;

import java.util.HashMap;
import java.util.List;

import retrofit2.Response;


public class MapsFragmentActivity extends AppCompatActivity implements OnMapReadyCallback, mapView {

    private static final String FINE_LOCATION = Manifest.permission.ACCESS_FINE_LOCATION;
    private static final String COURSE_LOCATION = Manifest.permission.ACCESS_COARSE_LOCATION;
    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1234;

    private Boolean mLocationPermissionsGranted = false;

    private GoogleMap mMap;
    private MapViewPresenter presenter;

    boolean doubleBackToExitPressedOnce = false;

    private HashMap<String, Integer> CarIdentifier = new HashMap<String, Integer>();

    double currentLongitude;
    double currentLatitude;
    String clickedMarkerTitle;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);

        presenter = new MapViewPresenter(this);
        getLocationPermission();

        if (mLocationPermissionsGranted){
            initView();
        }

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        if (mapFragment != null) {
            mapFragment.getMapAsync(this);
        }


    }


    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        mMap.getUiSettings().setZoomControlsEnabled(true);
        mMap.getUiSettings().setZoomGesturesEnabled(true);

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        mMap.setOnMyLocationButtonClickListener(new GoogleMap.OnMyLocationButtonClickListener() {
            @Override
            public boolean onMyLocationButtonClick() {

                LatLng latLng = new LatLng(currentLatitude, currentLongitude);

                MarkerOptions marker2 = new MarkerOptions();

                marker2.position(latLng);
                mMap.addMarker(marker2);
                mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, 14f));

                return false;
            }
        });



        mMap.setOnMarkerClickListener(new GoogleMap.OnMarkerClickListener() {
            @Override
            public boolean onMarkerClick(Marker marker) {
                onMarkerClickLogic(marker);
                return true;
            }
        });
        mMap.setMyLocationEnabled(true);
    }

    private void onMarkerClickLogic(Marker marker) {

        if (doubleBackToExitPressedOnce && marker.getTitle().equals(clickedMarkerTitle)) {

            Integer carId = CarIdentifier.get(clickedMarkerTitle);
            Intent carDetailsIntent = new Intent(this, MainActivity.class);
            carDetailsIntent.putExtra("CAR_ID", carId);
            this.startActivity(carDetailsIntent);
        } else {

            this.doubleBackToExitPressedOnce = true;
            clickedMarkerTitle = marker.getTitle();
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    doubleBackToExitPressedOnce = false;
                }
            }, 5000);
            marker.showInfoWindow();
        }

    }

    private void setMarkerSelected(Vehicle i) {
        MarkerOptions marker = new MarkerOptions();
        marker.title(i.getTitle());
        marker.snippet(i.getAddress());
        marker.position(new LatLng(i.getLat(), i.getLon()));
        mMap.addMarker(marker).showInfoWindow();
    }


    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        Log.d("RequestPermissions", "onRequestPermissionsResult: called.");
        mLocationPermissionsGranted = false;

        switch(requestCode){
            case LOCATION_PERMISSION_REQUEST_CODE:{
                if(grantResults.length > 0){
                    for(int i = 0; i < grantResults.length; i++){
                        if(grantResults[i] != PackageManager.PERMISSION_GRANTED){
                            mLocationPermissionsGranted = false;
                            Log.d("RequestPermissions", "onRequestPermissionsResult: permission failed");
                            return;
                        }
                    }
                    Log.d("RequestPermissions", "onRequestPermissionsResult: permission granted");
                    mLocationPermissionsGranted = true;
                    initView();
                }
            }
        }
    }


    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }

    void initView() {
        if (Util.isNetworkAvailable(this)) {
            presenter.getCarsFeed();
        } else {
            Toast.makeText(this, "No connection", Toast.LENGTH_LONG).show();
        }
    }

    @Override
    public void showSuccess(retrofit2.Call<List<Vehicle>> call, Response<List<Vehicle>> response) {
        List<Vehicle> vehicles = response.body();

        MarkerOptions marker = new MarkerOptions();

        for (int i = 0; i < vehicles.size(); i++) {
            if (vehicles.get(i).getTitle() != null) {
                marker.title(vehicles.get(i).getTitle());
            }
            if (vehicles.get(i).getAddress() != null) {
                marker.snippet(vehicles.get(i).getCarId().toString());
            }
            if (vehicles.get(i).getLat() != null && vehicles.get(i).getLon() != null) {
                marker.position(new LatLng(vehicles.get(i).getLat(), vehicles.get(i).getLon()));
            }
            mMap.addMarker(marker);

            CarIdentifier.put(marker.getTitle(), vehicles.get(i).getCarId());

        }

        for (int i = 0; i < vehicles.size(); i++) {

            setMarkerSelected(vehicles.get(i));
        }

    }

    private void getLocationPermission(){
        Log.d("getLocationPermission", "getLocationPermission: getting location permissions");
        String[] permissions = {Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION};

        if(ContextCompat.checkSelfPermission(this.getApplicationContext(),
                FINE_LOCATION) == PackageManager.PERMISSION_GRANTED){
            if(ContextCompat.checkSelfPermission(this.getApplicationContext(),
                    COURSE_LOCATION) == PackageManager.PERMISSION_GRANTED){
                mLocationPermissionsGranted = true;
            }else{
                ActivityCompat.requestPermissions(this,
                        permissions,
                        LOCATION_PERMISSION_REQUEST_CODE);
            }
        }else{
            ActivityCompat.requestPermissions(this,
                    permissions,
                    LOCATION_PERMISSION_REQUEST_CODE);
        }
    }

    @Override
    public void showError(retrofit2.Call<List<Vehicle>> call, Throwable t) {
        Toast.makeText(this, "Get list repo failed", Toast.LENGTH_SHORT).show();
    }
}
