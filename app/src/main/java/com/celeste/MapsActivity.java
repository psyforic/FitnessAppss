package com.celeste;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import androidx.core.content.ContextCompat;
import androidx.fragment.app.FragmentActivity;

import com.celeste.fitnessapp.R;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.sdsmdg.tastytoast.TastyToast;

import java.text.DecimalFormat;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback {

    Button btnDir;
    MarkerOptions markerOptions1, markerOptions2;
    Polyline currentPolyline;
    private GoogleMap mMap;
    private FloatingActionButton btn_calc_dist;
    private View lyt_save;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);

        //initialize widgets
        btn_calc_dist = findViewById(R.id.fab_calculate);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        String url = getUrl(markerOptions1.getPosition(), markerOptions2.getPosition(), "driving");

        btnDir.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(MapsActivity.this, "Direct me", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void initComponent() {

    }

    private String getUrl(LatLng origin, LatLng dest, String directionMode) {
        String str_origin = "origin" + origin.latitude + "," + origin.longitude;
        String str_dest = "destination" + dest.latitude + "," + dest.longitude;
        String mode = "mode" + directionMode;
        String parameter = str_origin + "&" + str_dest + mode;
        String output = "json";
        CalculationByDistance(origin, dest);
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
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            LatLng PE = new LatLng(-34, 151);
            mMap.addMarker(new MarkerOptions().position(PE).title("Marker in PE"));
            mMap.moveCamera(CameraUpdateFactory.newLatLng(PE));
            mMap.setMyLocationEnabled(true);
        }
        // Add a marker in Sydney and move the camera

    }

    public void CalculationByDistance(LatLng StartP, LatLng EndP) {
        int Radius = 6371;// radius of earth in Km
        double lat1 = StartP.latitude;
        double lat2 = EndP.latitude;
        double lon1 = StartP.longitude;
        double lon2 = EndP.longitude;
        double dLat = Math.toRadians(lat2 - lat1);
        double dLon = Math.toRadians(lon2 - lon1);
        double a = Math.sin(dLat / 2) * Math.sin(dLat / 2)
                + Math.cos(Math.toRadians(lat1))
                * Math.cos(Math.toRadians(lat2)) * Math.sin(dLon / 2)
                * Math.sin(dLon / 2);
        double c = 2 * Math.asin(Math.sqrt(a));
        double valueResult = Radius * c;
        double km = valueResult / 1;
        DecimalFormat newFormat = new DecimalFormat("####");
        int kmInDec = Integer.parseInt(newFormat.format(km));
        double meter = valueResult % 1000;
        int meterInDec = Integer.parseInt(newFormat.format(meter));
        Log.i("Radius Value", "" + valueResult + "   KM  " + kmInDec
                + " Meter   " + meterInDec);

        TastyToast.makeText(getApplicationContext(), "The distance is " + valueResult,
                TastyToast.LENGTH_LONG, TastyToast.SUCCESS).show();
    }
}