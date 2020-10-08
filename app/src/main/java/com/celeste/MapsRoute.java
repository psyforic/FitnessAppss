package com.celeste;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.FragmentActivity;

import com.celeste.fitnessapp.R;
import com.directions.route.AbstractRouting;
import com.directions.route.Route;
import com.directions.route.RouteException;
import com.directions.route.Routing;
import com.directions.route.RoutingListener;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.firestore.FirebaseFirestore;
import com.sdsmdg.tastytoast.TastyToast;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

import utils.ViewAnimation;

public class MapsRoute extends FragmentActivity implements OnMapReadyCallback,
        GoogleApiClient.OnConnectionFailedListener, RoutingListener {

    //to get location permissions.
    private final static int LOCATION_REQUEST_CODE = 23;
    protected LatLng start = null;
    protected LatLng end = null;
    GoogleMap map;
    Button btnDir;
    MarkerOptions markerOptions1, markerOptions2;
    //current and destination location objects
    Location myLocation = null;
    Location destinationLocation = null;
    boolean locationPermission = false;
    FloatingActionButton fab_add, fab_save, fab_calculate, fab_show_route;
    FirebaseFirestore db;
    FirebaseDatabase database = FirebaseDatabase.getInstance();
    DatabaseReference myRef = database.getReference("landmark");
    LocationHelper helper;
    //google map object
    private GoogleMap mMap;
    //polyline object
    private List<Polyline> polylines = null;
    private View parent_view;
    private View back_drop;
    private boolean rotate = false;
    private View lyt_save;
    private View lyt_calc;
    private View lyt_show_route;

    @Override
    protected void onStart() {
        super.onStart();

    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps_route);
        db = FirebaseFirestore.getInstance();
        initComponents();

        //request location permission.
        requestPermision();

        //init google map fragment to show map.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
        onClickListeners();
    }

    private void initComponents() {
        helper = new LocationHelper(0, 0);
        parent_view = findViewById(android.R.id.content);
        back_drop = findViewById(R.id.back_drop);
        fab_save = findViewById(R.id.fab_save);
        fab_add = findViewById(R.id.fab_add);
        fab_calculate = findViewById(R.id.fab_calculate);
        fab_show_route = findViewById(R.id.fab_show_route);
        lyt_save = findViewById(R.id.lyt_save);
        lyt_calc = findViewById(R.id.lyt_calc);
        lyt_show_route = findViewById(R.id.lyt_show_route);
        ViewAnimation.initShowOut(lyt_save);
        ViewAnimation.initShowOut(lyt_calc);
        ViewAnimation.initShowOut(lyt_show_route);
        back_drop.setVisibility(View.GONE);
    }

    private void onClickListeners() {
        fab_add.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                toggleFabMode(v);
            }
        });
        fab_calculate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (end != null) {
                    CalculationByDistance(start, end);
                } else {
                    TastyToast.makeText(getApplicationContext(), "Please select your destination to calculate distance and time", TastyToast.LENGTH_LONG, TastyToast.CONFUSING).show();
                }

            }
        });
        fab_show_route.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Findroutes(start, end);
                toggleFabMode(fab_add);
            }
        });
        fab_save.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                toggleFabMode(fab_add);
                myRef.setValue(helper).addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {
                            TastyToast.makeText(getApplicationContext(), "Your landmark has been saved", TastyToast.LENGTH_LONG, TastyToast.SUCCESS).show();
                        } else {
                            TastyToast.makeText(getApplicationContext(), "Your landmark has not been saved", TastyToast.LENGTH_LONG, TastyToast.SUCCESS).show();
                        }
                    }
                });
            }
        });
        back_drop.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                toggleFabMode(fab_add);
            }
        });
    }

    private void toggleFabMode(View v) {
        rotate = ViewAnimation.rotateFab(v, !rotate);
        if (rotate) {
            ViewAnimation.showIn(lyt_calc);
            ViewAnimation.showIn(lyt_save);
            ViewAnimation.showIn(lyt_show_route);
            back_drop.setVisibility(View.VISIBLE);
        } else {
            ViewAnimation.showOut(lyt_calc);
            ViewAnimation.showOut(lyt_save);
            ViewAnimation.showOut(lyt_show_route);
            back_drop.setVisibility(View.GONE);
        }
    }

    private void requestPermision() {
        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_COARSE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_COARSE_LOCATION},
                    LOCATION_REQUEST_CODE);
        } else {
            locationPermission = true;
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode) {
            case LOCATION_REQUEST_CODE: {
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    //if permission granted.
                    locationPermission = true;
                    getMyLocation();

                } else {
                    // permission denied, boo! Disable the
                    // functionality that depends on this permission.
                }
                return;
            }
        }
    }

    //to get user location
    @SuppressLint("MissingPermission")
    private void getMyLocation() {

        mMap.setMyLocationEnabled(true);
        mMap.setOnMyLocationChangeListener(new GoogleMap.OnMyLocationChangeListener() {
            @Override
            public void onMyLocationChange(Location location) {

                myLocation = location;
                LatLng ltlng = new LatLng(location.getLatitude(), location.getLongitude());
                CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLngZoom(
                        ltlng, 16f);
                mMap.animateCamera(cameraUpdate);
            }
        });
        //get destination location when user click on map
        mMap.setOnMapClickListener(new GoogleMap.OnMapClickListener() {
            @Override
            public void onMapClick(LatLng latLng) {
                end = latLng;
                mMap.clear();
                start = new LatLng(myLocation.getLatitude(), myLocation.getLongitude());
                helper.setLongitude(end.longitude);
                helper.setLatitude(end.latitude);
                //start route finding

            }
        });

    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        getMyLocation();
    }

    // function to find Routes.
    public void Findroutes(LatLng Start, LatLng End) {
        if (Start == null || End == null) {
            TastyToast.makeText(MapsRoute.this, "Unable to get location", TastyToast.LENGTH_LONG, TastyToast.ERROR).show();
        } else {

            Routing routing = new Routing.Builder()
                    .travelMode(AbstractRouting.TravelMode.DRIVING)
                    .withListener(this)
                    .alternativeRoutes(true)
                    .waypoints(Start, End)
                    .key("AIzaSyCWSGKaqi-ksbt79RmDc7RCZ0HES7EWyqg")  //also define your api key here.
                    .build();
            routing.execute();
        }
    }

    @Override
    public void onRoutingFailure(RouteException e) {
        View parentLayout = findViewById(android.R.id.content);
        Snackbar snackbar = Snackbar.make(parentLayout, e.toString(), Snackbar.LENGTH_LONG);
        snackbar.show();
        Log.d("TAG", "onRoutingFailure: " + e.toString());
//        Findroutes(start,end);
    }

    @Override
    public void onRoutingStart() {
        TastyToast.makeText(MapsRoute.this, "Finding Route...", TastyToast.LENGTH_LONG, TastyToast.INFO).show();
    }

    @Override
    public void onRoutingSuccess(ArrayList<Route> route, int shortestRouteIndex) {
        CameraUpdate center = CameraUpdateFactory.newLatLng(start);
        CameraUpdate zoom = CameraUpdateFactory.zoomTo(16);
        if (polylines != null) {
            polylines.clear();
        }
        PolylineOptions polyOptions = new PolylineOptions();
        LatLng polylineStartLatLng = null;
        LatLng polylineEndLatLng = null;


        polylines = new ArrayList<>();
        //add route(s) to the map using polyline
        for (int i = 0; i < route.size(); i++) {

            if (i == shortestRouteIndex) {
                polyOptions.color(getResources().getColor(R.color.colorPrimary));
                polyOptions.width(7);
                polyOptions.addAll(route.get(shortestRouteIndex).getPoints());
                Polyline polyline = mMap.addPolyline(polyOptions);
                polylineStartLatLng = polyline.getPoints().get(0);
                int k = polyline.getPoints().size();
                polylineEndLatLng = polyline.getPoints().get(k - 1);
                polylines.add(polyline);

            } else {

            }

        }
        //Add Marker on route starting position
        MarkerOptions startMarker = new MarkerOptions();
        startMarker.position(polylineStartLatLng);
        startMarker.title("My Location");
        mMap.addMarker(startMarker);

        //Add Marker on route ending position
        MarkerOptions endMarker = new MarkerOptions();
        endMarker.position(polylineEndLatLng);
        endMarker.title("Destination");
        mMap.addMarker(endMarker);
    }

    @Override
    public void onRoutingCancelled() {
        Findroutes(start, end);
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        Findroutes(start, end);
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
        @SuppressLint("DefaultLocale") String strDouble = String.format("%.2f", km);
        double meter = valueResult % 1000;
        int meterInDec = Integer.parseInt(newFormat.format(meter));
        Log.i("Radius Value", "" + valueResult + "   KM  " + kmInDec
                + " Meter   " + meterInDec);
        Context context;
        final AlertDialog.Builder dialog1 = new AlertDialog.Builder(MapsRoute.this);
        dialog1.setTitle("Distance and Time");
        dialog1.setMessage("DISTANCE : " + strDouble + "(km)" + "\n" + "ESTIMATED TIME : ");
        dialog1.setCancelable(true);
        dialog1.show();
        TastyToast.makeText(getApplicationContext(), "The distance is " + valueResult,
                TastyToast.LENGTH_LONG, TastyToast.SUCCESS).show();
    }
}