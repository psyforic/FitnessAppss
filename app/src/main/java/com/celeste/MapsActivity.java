package com.celeste;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import androidx.fragment.app.FragmentActivity;

import com.celeste.fitnessapp.R;
import com.celeste.helpers.FetchUrl;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback {

    Button btnDir;
    MarkerOptions markerOptions1, markerOptions2;
    Polyline currentPolyline;
    private GoogleMap mMap;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps_route);

        //initialize widgets
        btnDir = findViewById(R.id.btnDirection);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        String url = getUrl(markerOptions1.getPosition(), markerOptions2.getPosition(), "driving");

//        new FetchUrl(MapsActivity.this).execute(url, "driving");

        btnDir.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(MapsActivity.this, "Direct me", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private String getUrl(LatLng origin, LatLng dest, String directionMode) {
        String str_origin = "origin" + origin.latitude + "," + origin.longitude;
        String str_dest = "destination" + dest.latitude + "," + dest.longitude;
        String mode = "mode" + directionMode;
        String parameter = str_origin + "&" + str_dest + mode;
        String output = "json";

        String url = "https://maps.googleapis.com/maps/api/directions/" + output + "?" + parameter + "&key=" +
                getString(R.string.google_maps_key);
        return url;
    }


    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        // Add a marker in Sydney and move the camera
        LatLng PE = new LatLng(-34, 151);
        mMap.addMarker(new MarkerOptions().position(PE).title("Marker in PE"));
//        mMap.moveCamera(CameraUpdateFactory.newLatLng(sydney));
    }
}