package activities;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Location;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.Spinner;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.FragmentActivity;

import com.celeste.JsonParser;
import com.celeste.Landmarks;
import com.celeste.LocationHelper;
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
import com.google.android.gms.tasks.Task;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.sdsmdg.tastytoast.TastyToast;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.text.DateFormat;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;

import utils.ViewAnimation;

public class ActivityHome extends FragmentActivity implements OnMapReadyCallback,
        GoogleApiClient.OnConnectionFailedListener, RoutingListener {

    //to get location permissions.
    private final static int LOCATION_REQUEST_CODE = 23;
    final String[] placesType = {"school",
            "shopping_mall",
            "museum",
            "railway_construction",
            "movie_theatre",
            "bus_station",
            "atm",
            "mosque",
            "bridge",
            "park",
            "hospital",
            "gas_station",
            "cemetery",
            "church",
            "city_hall",
            "restaurant"};
    private final FirebaseFirestore firebaseFirestore = FirebaseFirestore.getInstance();
    private final CollectionReference landmarksRef = firebaseFirestore.collection("Landmarks");
    protected LatLng start = null;
    protected LatLng end = null;
    MarkerOptions options = null;
    GoogleMap map;
    Button btnDir;
    Location myLocation = null;
    boolean locationPermission = false;
    FloatingActionButton fab_add,
            fab_save, fab_calculate,
            fab_show_route,
            fab_logout,
            fab_share,
            fab_favs;
    FirebaseFirestore db;
    FirebaseDatabase database = FirebaseDatabase.getInstance();
    DatabaseReference myRef = database.getReference("Landmarks");
    LocationHelper helper;
    String TAG = "";
    Landmarks landmarks = new Landmarks();
    Spinner spinner;
    Button btnFind;
    FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
    String userId = user.getUid();
    FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
    DocumentReference userDocument =
            firebaseFirestore.collection("users").document(firebaseUser.getUid()).collection("Landmarks").document();
    String[] names = {"School",
            "Shopping Mall",
            "Museum",
            "Railway Construction",
            "Movie Theatre",
            "Bus Station",
            "Atm",
            "Mosque","Bridge" ,"Park", "Hospital", "Gas Station", "Cemetery", "Church", "City Hall", "Restaurant"};
    private GoogleMap mMap;
    private List<Polyline> polylines = null;
    private View parent_view;
    private View back_drop;
    private boolean rotate = false;
    private View lyt_save;
    private View lyt_calc;
    private View lyt_show_route;

    private View lyt_favs;
    private View lyt_action_share;
    private View lyt_logout;

    private View lyt_main_parent;
    private View lyt_options;
    private ImageButton list_button;
    private boolean state1;
    private boolean state2;
    private ProgressBar progress_bar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);
        db = FirebaseFirestore.getInstance();
        initComponents();
        initToolbar();
        requestPermision();
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
        onClickListeners();
    }

    @Override
    protected void onStart() {
        super.onStart();
    }

    private void initToolbar() {
        Toolbar toolbar = findViewById(R.id.toolbar);
        toolbar.setTitle("Maps");
    }

    private void initComponents() {
        helper = new LocationHelper(0, 0);
        parent_view = findViewById(android.R.id.content);
        back_drop = findViewById(R.id.back_drop);
        fab_save = findViewById(R.id.fab_save);
        fab_add = findViewById(R.id.fab_add);
        fab_calculate = findViewById(R.id.fab_calculate);
        fab_show_route = findViewById(R.id.fab_show_route);
        progress_bar = findViewById(R.id.progress_bar);
        list_button = findViewById(R.id.list_button);

        fab_logout = findViewById(R.id.fab_logout);
        fab_share = findViewById(R.id.fab_share);
        fab_favs = findViewById(R.id.fab_favs);

        btnFind = findViewById(R.id.btn_find);
        lyt_save = findViewById(R.id.lyt_save);
        lyt_calc = findViewById(R.id.lyt_calc);
        lyt_show_route = findViewById(R.id.lyt_show_route);

        lyt_action_share = findViewById(R.id.lyt_action_share);
        lyt_favs = findViewById(R.id.lyt_favs);
        lyt_logout = findViewById(R.id.lyt_logout);

        spinner = findViewById(R.id.spinner);

        lyt_main_parent = findViewById(R.id.lyt_main_parent);
        lyt_options = findViewById(R.id.lyt_options);

        ViewAnimation.initShowOut(lyt_save);
        ViewAnimation.initShowOut(lyt_calc);
        ViewAnimation.initShowOut(lyt_show_route);

        ViewAnimation.initShowOut(lyt_favs);
        ViewAnimation.initShowOut(lyt_action_share);
        ViewAnimation.initShowOut(lyt_logout);

        back_drop.setVisibility(View.GONE);

    }

    private void saveLandmarks() {
        userDocument.set(landmarks).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                TastyToast.makeText(getApplicationContext(), "Saved", TastyToast.LENGTH_SHORT, TastyToast.SUCCESS).show();
            }
        });
    }

    private void onClickListeners() {
        fab_add.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!state2) {
                    toggleFabMode(v);
                }
            }
        });
        list_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!state1) {
                    toggleFabOptions(v);
                }
            }
        });
        fab_calculate.setOnClickListener(v -> {
            if (end != null) {
                CalculationByDistance(start, end);
            } else {
                TastyToast.makeText(getApplicationContext(), "Please select your destination to calculate distance and time", TastyToast.LENGTH_LONG, TastyToast.CONFUSING).show();
            }

        });
        fab_show_route.setOnClickListener(v -> {
            progress_bar.setVisibility(View.VISIBLE);
            findRoutes(start, end);
            toggleFabMode(fab_add);
        });
        fab_save.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                toggleFabMode(fab_add);
                if (end != null) {
                    saveLandmarks();
                } else {
                    TastyToast.makeText(ActivityHome.this, "You have not selected any landmark on the map", TastyToast.LENGTH_LONG, TastyToast.CONFUSING).show();
                }

            }
        });
        back_drop.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                toggleFabMode(fab_add);
            }
        });
        fab_logout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FirebaseAuth auth = FirebaseAuth.getInstance();
                auth.getCurrentUser();
                auth.signOut();
                finish();
            }
        });
        fab_share.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                PackageManager packageManager = getApplicationContext().getPackageManager();
                Intent i = new Intent(Intent.ACTION_VIEW);
                try {
                    Location geoPoint = new Location(myLocation.getLatitude() / 1E6 + "," + myLocation.getLongitude() / 1E6);
                    String url = "https://api.whatsapp.com/send?= " + "&text=" + URLEncoder.encode(String.valueOf(geoPoint));
                    i.setPackage("com.whatsapp");
                    i.setData(Uri.parse(url));
                    if (i.resolveActivity(packageManager) != null) {
                        getApplicationContext().startActivity(i);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }

            }
        });
        fab_favs.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(ActivityHome.this, ActivityFavoritePlaces.class));
            }
        });
        spinner.setAdapter(new ArrayAdapter<>(ActivityHome.this, android.R.layout.simple_spinner_dropdown_item, names));
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
            state1 = true;
            ViewAnimation.showIn(lyt_calc);
            ViewAnimation.showIn(lyt_save);
            ViewAnimation.showIn(lyt_show_route);
            back_drop.setVisibility(View.VISIBLE);
        } else {
            ViewAnimation.showOut(lyt_calc);
            ViewAnimation.showOut(lyt_save);
            ViewAnimation.showOut(lyt_show_route);
            back_drop.setVisibility(View.GONE);
            state1 = false;
        }
    }

    private void toggleFabOptions(View v) {
        rotate = ViewAnimation.rotateFab(v, !rotate);
        if (rotate) {
            state2 = true;
            ViewAnimation.showIn(lyt_favs);
            ViewAnimation.showIn(lyt_action_share);
            ViewAnimation.showIn(lyt_logout);
            back_drop.setVisibility(View.VISIBLE);
        } else {
            ViewAnimation.showOut(lyt_favs);
            ViewAnimation.showOut(lyt_action_share);
            ViewAnimation.showOut(lyt_logout);
            back_drop.setVisibility(View.GONE);
            state2 = false;
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
        mMap.setOnMarkerClickListener(new GoogleMap.OnMarkerClickListener() {
            @Override
            public boolean onMarkerClick(Marker marker) {
                helper.setLongitude(marker.getPosition().longitude);
                helper.setLatitude(marker.getPosition().latitude);
                landmarks = new Landmarks(marker.getPosition().latitude, marker.getPosition().longitude, marker.getTitle());
                marker.setIcon(getMarkerIcon(getResources().getColor(R.color.black)));
                if (marker.isInfoWindowShown()) {
                    marker.hideInfoWindow();
                } else {
                    marker.showInfoWindow();
                }
                return true;
            }
        });
    }

    // function to find Routes.
    public void findRoutes(LatLng Start, LatLng End) {
        if (Start == null || End == null) {
            TastyToast.makeText(ActivityHome.this, "Unable to get location", TastyToast.LENGTH_LONG, TastyToast.ERROR).show();
            progress_bar.setVisibility(View.GONE);
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
            progress_bar.setVisibility(View.GONE);
        }
    }

    @Override
    public void onRoutingFailure(RouteException e) {
        View parentLayout = findViewById(android.R.id.content);
        Snackbar snackbar = Snackbar.make(parentLayout, e.toString(), Snackbar.LENGTH_LONG);
        snackbar.show();
        Log.d("TAG", "onRoutingFailure: " + e.toString());
    }

    @Override
    public void onRoutingStart() {
        TastyToast.makeText(ActivityHome.this, "Finding Route...", TastyToast.LENGTH_LONG, TastyToast.INFO).show();
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
        progress_bar.setVisibility(View.GONE);
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        findRoutes(start, end);
        progress_bar.setVisibility(View.GONE);
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
        @SuppressLint("DefaultLocale")
        double meter = valueResult % 1000;
        int meterInDec = Integer.parseInt(newFormat.format(meter));

        double miles = kmInDec / 1.621371;
        String newMiles = String.format("%.2f", miles);
        Location locationA = new Location("A");
        locationA.setLatitude(StartP.latitude);
        locationA.setLongitude(StartP.longitude);

        Location locationB = new Location("B");
        locationB.setLatitude(EndP.latitude);
        locationB.setLongitude(EndP.longitude);

        float dist = locationA.distanceTo(locationB) / 1000;
        @SuppressLint("DefaultLocale") String strDouble = String.format("%.2f", dist);
        int speedIs1KmMinute = 100;
        double estimatedDriveTimeInMinutes = dist / speedIs1KmMinute;
        DateFormat.getTimeInstance(DateFormat.MEDIUM).format(estimatedDriveTimeInMinutes);
        final AlertDialog.Builder dialog1 = new AlertDialog.Builder(ActivityHome.this);
        dialog1.setTitle("Distance");
        dialog1.setMessage("KILOMETERS : " + strDouble + "(km)" + "\n"+ "\n" +"MILES : "+ newMiles + "(mi)");
        dialog1.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        dialog1.setCancelable(true);
        dialog1.show();
    }

    public void clickAction(View view) {
        int id = view.getId();
        switch (id) {
            case R.id.map_button:
                if (mMap != null) { //prevent crashing if the map doesn't exist yet (eg. on starting activity)
                    mMap.clear();
                }
                break;
            case R.id.list_button:
                toggleFabOptions(view);
                break;
            case R.id.fab_add:
                toggleFabMode(view);
                break;
        }
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

    @SuppressLint("StaticFieldLeak")
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
            progress_bar.setVisibility(View.GONE);
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
            }
            progress_bar.setVisibility(View.GONE);
        }
    }
}