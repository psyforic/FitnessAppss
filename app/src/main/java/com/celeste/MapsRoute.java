package com.celeste;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Location;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
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
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.sdsmdg.tastytoast.TastyToast;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;

import utils.ViewAnimation;

public class MapsRoute extends FragmentActivity implements OnMapReadyCallback,
        GoogleApiClient.OnConnectionFailedListener, RoutingListener {

    //to get location permissions.
    private final static int LOCATION_REQUEST_CODE = 23;
    private static final String KEY_TITLE = "title";
    private static final String KEY_DESCRIPTION = "description";
    protected LatLng start = null;
    protected LatLng end = null;
    MarkerOptions options = null;
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
    String TAG = "";
    Landmarks landmarks = new Landmarks();
    Spinner spinner;
    Button btnFind;
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
    private final FirebaseFirestore firebaseFirestore = FirebaseFirestore.getInstance();
    private final CollectionReference landmarksRef = firebaseFirestore.collection("Landmarks");
    private DocumentReference lanmarkRef = firebaseFirestore.document("Landmarks/favorite_places");
    final String[] placesType = {"stadium", "school",
            "shopping_mall",
            "museum",
            "railway_construction",
            "movie_theatre",
            "bus_station",
            "atm",
            "mosque",
            "park",
            "hospital",
            "gas_station",
            "cemetery",
            "church",
            "city_hall",
            "restaurant"};
    String[] names = {"Stadium", "School",
            "Shopping Mall",
            "Museum",
            "Railway Construction",
            "Movie Theatre",
            "Bus Station",
            "Atm",
            "Mosque", "Park", "Hospital", "Gas Station", "Cemetery", "Church", "City Hall", "Restaurant"};

    @Override
    protected void onStart() {
        super.onStart();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps_route);
        db = FirebaseFirestore.getInstance();
        double latitude = getIntent().getDoubleExtra("latitude", 0);
        double longitude = getIntent().getDoubleExtra("longitude", 0);
        TastyToast.makeText(getApplicationContext(), "" + longitude, TastyToast.LENGTH_LONG, TastyToast.SUCCESS).show();

        if (latitude != 0 && longitude != 0) {
            LatLng latLng = new LatLng(latitude, longitude);
            findRoutes(start, latLng);
        }
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
        btnFind = findViewById(R.id.btn_find);
        lyt_save = findViewById(R.id.lyt_save);
        lyt_calc = findViewById(R.id.lyt_calc);
        lyt_show_route = findViewById(R.id.lyt_show_route);
        spinner = findViewById(R.id.spinner);
        ViewAnimation.initShowOut(lyt_save);
        ViewAnimation.initShowOut(lyt_calc);
        ViewAnimation.initShowOut(lyt_show_route);
        back_drop.setVisibility(View.GONE);

    }

    private void saveLandmarks() {
        landmarksRef.addSnapshotListener(this, new EventListener<QuerySnapshot>() {
            @Override
            public void onEvent(@Nullable QuerySnapshot queryDocumentSnapshots, @Nullable FirebaseFirestoreException error) {
                if (error != null) {
                    return;
                }
                String data = "";
                for (QueryDocumentSnapshot documentSnapshot : queryDocumentSnapshots) {
                    Landmarks landmark = documentSnapshot.toObject(Landmarks.class);
                    landmark.setDocumentId(documentSnapshot.getId());
                    String documentId = landmarks.getDocumentId();
                    String latitude = String.valueOf(landmarks.getLatitude());
                    String longitude = String.valueOf(landmark.getLongitude());
                    data += "ID: " + documentId
                            + "\nLatitude: " + latitude + "\nLongitude: " + longitude + "\n\n";
                }
            }
        });
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
                findRoutes(start, end);
                toggleFabMode(fab_add);
            }
        });
        fab_save.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                toggleFabMode(fab_add);
                saveLandmarks();
                myRef.setValue(helper).addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {
                            TastyToast.makeText(getApplicationContext(), "Your landmark has been saved", TastyToast.LENGTH_LONG, TastyToast.SUCCESS).show();
                        } else {
                            TastyToast.makeText(getApplicationContext(), "Your landmark has not been saved", TastyToast.LENGTH_LONG, TastyToast.ERROR).show();
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

        spinner.setAdapter(new ArrayAdapter<>(MapsRoute.this, android.R.layout.simple_spinner_dropdown_item, names));
        //init fused loc provider
        btnFind.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //Get Selected position
                int i = spinner.getSelectedItemPosition();
                String url = "https://maps.googleapis.com/maps/api/place/nearbysearch/json" +
                        "?location=" + myLocation.getLatitude() + "," + myLocation.getLongitude() +
                        "&radius=15000" +
                        "&types=" + placesType[i] +
                        "&sensor=true" +
                        "&key=" + getResources().getString(R.string.google_maps_key);
                //Execute place task method to download json data
                new PlaceTask().execute(url);
            }
        });
    }

    private String downloadUrl(String string) throws IOException {
        //init url
        URL url = new URL(string);
        HttpURLConnection httpURLConnection = (HttpURLConnection) url.openConnection();
        //connect connectionn
        httpURLConnection.connect();
        InputStream stream = httpURLConnection.getInputStream();
        BufferedReader reader = new BufferedReader(new InputStreamReader(stream));
        //Initialize string builder
        StringBuilder builder = new StringBuilder();
        String line = "";
        while ((line = reader.readLine()) != null) {
            builder.append(line);
        }
        String data = builder.toString();
        //Close reader
        reader.close();
        return data;
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

    private void getMyLocation() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
        mMap.setMyLocationEnabled(true);
        mMap.setOnMyLocationChangeListener(new GoogleMap.OnMyLocationChangeListener() {
            @Override
            public void onMyLocationChange(Location location) {
                myLocation = location;

            }
        });
        // get destination location when user click on map
        mMap.setOnMapClickListener(latLng -> {
            end = latLng;
            mMap.clear();
            start = new LatLng(myLocation.getLatitude(), myLocation.getLongitude());
            helper.setLongitude(end.longitude);
            helper.setLatitude(end.latitude);
            landmarks = new Landmarks(end.latitude, end.longitude, "");
            landmarksRef.add(landmarks);
            mMap.addMarker(new MarkerOptions()
                    .position(latLng)
                    .icon(getMarkerIcon(getResources().getColor(R.color.black))));
        });
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        googleMap.getUiSettings().setCompassEnabled(true);
        googleMap.getUiSettings().setMyLocationButtonEnabled(true);
        googleMap.getUiSettings().setZoomGesturesEnabled(true);
        googleMap.getUiSettings().setScrollGesturesEnabled(true);
        googleMap.getUiSettings().setTiltGesturesEnabled(true);
        googleMap.getUiSettings().setRotateGesturesEnabled(true);
        mMap = googleMap;
        getMyLocation();
//        mMap.setOnMarkerClickListener(new GoogleMap.OnMarkerClickListener() {
//            @Override
//            public boolean onMarkerClick(Marker marker) {
//                helper.setLongitude(marker.getPosition().longitude);
//                helper.setLatitude(marker.getPosition().latitude);
//                landmarks = new Landmarks(marker.getPosition().latitude, marker.getPosition().longitude, "");
////                landmarksRef.add(landmarks);
//                marker.setIcon(getMarkerIcon(getResources().getColor(R.color.black)));
//                if (marker.isInfoWindowShown()) {
//                    marker.hideInfoWindow();
//                } else {
//                    marker.showInfoWindow();
//                }
//                return true;
//            }
//        });
    }

    // function to find Routes.
    public void findRoutes(LatLng Start, LatLng End) {
        if (Start == null || End == null) {
            TastyToast.makeText(MapsRoute.this, "Unable to get location", TastyToast.LENGTH_LONG, TastyToast.ERROR).show();
        } else {
            mMap.clear();
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

    public BitmapDescriptor getMarkerIcon(int color) {
        float[] hsv = new float[3];
        Color.colorToHSV(color, hsv);
        return BitmapDescriptorFactory.defaultMarker(hsv[0]);
    }


    @Override
    public void onRoutingCancelled() {
        findRoutes(start, end);
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        findRoutes(start, end);
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
    }

    @SuppressLint("StaticFieldLeak")
    private class PlaceTask extends AsyncTask<String, Integer, String> {


        @Override
        protected String doInBackground(String... strings) {
            //Init data
            String data = null;
            try {
                data = downloadUrl(strings[0]);
            } catch (IOException e) {
                e.printStackTrace();
            }
            return data;
        }

        @Override
        protected void onPostExecute(String s) {
            new ParserTask().execute(s);
        }
    }

    private class ParserTask extends AsyncTask<String, Integer, List<HashMap<String, String>>> {
        @Override
        protected List<HashMap<String, String>> doInBackground(String... strings) {
            //Create json parser class
            JsonParser jsonParser = new JsonParser();
            List<HashMap<String, String>> mapList = null;
            JSONObject object = null;
            try {
                object = new JSONObject(strings[0]);
                mapList = jsonParser.parseResult(object);
            } catch (JSONException e) {
                e.printStackTrace();
            }
            return mapList;
        }

        @Override
        protected void onPostExecute(List<HashMap<String, String>> hashMaps) {
            //Clear Map
            mMap.clear();
            for (int m = 0; m < hashMaps.size(); m++) {
                HashMap<String, String> hashMapList = hashMaps.get(m);
                double lat = Double.parseDouble(Objects.requireNonNull(hashMapList.get("lat")));
                double lng = Double.parseDouble(Objects.requireNonNull(hashMapList.get("lng")));

                String name = hashMapList.get("name");
                String snippet = hashMapList.get("snippet");
                LatLng latLng = new LatLng(lat, lng);
                //initialize marker options
                options = new MarkerOptions();
                options.position(latLng);
                //set title
                options.snippet(snippet);
                options.title(name);
                //add marker
                mMap.addMarker(options);
                start = new LatLng(myLocation.getLatitude(), myLocation.getLongitude());
                end = new LatLng(options.getPosition().latitude, options.getPosition().longitude);
                helper.setLongitude(options.getPosition().longitude);
                helper.setLatitude(options.getPosition().latitude);
                landmarks = new Landmarks(options.getPosition().latitude, options.getPosition().longitude, name);
                landmarksRef.add(landmarks);
            }
        }
    }
}