package com.celeste;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import com.celeste.fitnessapp.R;
import com.directions.route.AbstractRouting;
import com.directions.route.Route;
import com.directions.route.RouteException;
import com.directions.route.Routing;
import com.directions.route.RoutingListener;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;
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

public class NearbyLandMarksActivity extends AppCompatActivity implements OnMapReadyCallback,
        GoogleApiClient.OnConnectionFailedListener, RoutingListener {
    public static final int REQUEST_CODE_MAP = 44;
    static double currentLat = 0, currentLong = 0;
    protected LatLng start = null;
    protected LatLng end = null;
    Spinner spinner;
    Button btnFind;
    SupportMapFragment supportMapFragment;
    FusedLocationProviderClient fusedLocationProviderClient;
    FloatingActionButton fab_directions;
    FloatingActionButton fab_save;
    FloatingActionButton fab_more;
    Location myLocation = null;
    MarkerOptions markerOptions = null;
    private GoogleMap map;
    private View parent_view;
    private View back_drop;
    private boolean rotate = false;
    private View lyt_directions;
    private View lyt_save;
    private View lyt_calc;
    private List<Polyline> polylines = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_nearby_land_marks);
        initComponent();
        //   getCurrentLocation();
    }

    private void initComponent() {
        fab_directions = findViewById(R.id.fab_directions);
        fab_save = findViewById(R.id.fab_save);
        fab_more = findViewById(R.id.fab_add);
        spinner = findViewById(R.id.spinner);
        btnFind = findViewById(R.id.btn_find);
        back_drop = findViewById(R.id.back_drop);
        lyt_directions = findViewById(R.id.lyt_directions);
        lyt_save = findViewById(R.id.lyt_save);
        lyt_calc = findViewById(R.id.lyt_calc);
        ViewAnimation.initShowOut(lyt_directions);
        ViewAnimation.initShowOut(lyt_save);
        ViewAnimation.initShowOut(lyt_calc);
        back_drop.setVisibility(View.GONE);
        fab_more.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                toggleFabMode(v);
            }
        });

        back_drop.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                toggleFabMode(fab_more);
            }
        });

        fab_save.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (ActivityCompat.checkSelfPermission(NearbyLandMarksActivity.this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(NearbyLandMarksActivity.this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                    // TODO: Consider calling
                    //    ActivityCompat#requestPermissions
                    // here to request the missing permissions, and then overriding
                    //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                    //                                          int[] grantResults)
                    // to handle the case where the user grants the permission. See the documentation
                    // for ActivityCompat#requestPermissions for more details.
                    return;
                }
                map.setMyLocationEnabled(true);
                map.setOnMyLocationChangeListener(new GoogleMap.OnMyLocationChangeListener() {
                    @Override
                    public void onMyLocationChange(Location location) {

                        myLocation = location;
                        LatLng ltlng = new LatLng(location.getLatitude(), location.getLongitude());

                        CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLngZoom(
                                ltlng, 10f);
                        map.animateCamera(cameraUpdate);
                    }
                });
                //get destination location when user click on map
                map.setOnMapClickListener(new GoogleMap.OnMapClickListener() {
                    @Override
                    public void onMapClick(LatLng latLng) {

                        end = latLng;

                        map.clear();
                        start = new LatLng(myLocation.getLatitude(), myLocation.getLongitude());
                        //start route finding
                       // start = new LatLng(currentLat, currentLong);
                        Findroutes(start, end);
                    }
                });

                Toast.makeText(getApplicationContext(), "Save clicked", Toast.LENGTH_SHORT).show();
            }
        });

        fab_directions.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(getApplicationContext(), "Directions clicked", Toast.LENGTH_SHORT).show();
            }
        });

        supportMapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.google_map);
        //initialize array of places
        final String[] placesType = {"stadium",
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
        String[] names = {"Stadium",
                "Shopping Mall",
                "Museum",
                "Railway Construction",
                "Movie Theatre",
                "Bus Station",
                "Atm",
                "Mosque", "Park", "Hospital", "Gas Station", "Cemetery", "Church", "City Hall", "Restaurant"};

        spinner.setAdapter(new ArrayAdapter<>(NearbyLandMarksActivity.this, android.R.layout.simple_spinner_dropdown_item, names));
        //init fused loc provider
        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);
        if (ActivityCompat.checkSelfPermission(NearbyLandMarksActivity.this,
                Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            getCurrentLocation();
        } else {
            //permission denied
            ActivityCompat.requestPermissions(NearbyLandMarksActivity.this
                    , new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, REQUEST_CODE_MAP);
        }


        btnFind.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //Get Selected position
                int i = spinner.getSelectedItemPosition();
                String url = "https://maps.googleapis.com/maps/api/place/nearbysearch/json" +
                        "?location=" + currentLat + "," + currentLong +
                        "&radius=50000" +
                        "&types=" + placesType[i] +
                        "&sensor=true" +
                        "&key=" + getResources().getString(R.string.google_maps_key);
                //Execute place task method to download json data
                new PlaceTask().execute(url);
            }
        });
    }

    private void toggleFabMode(View v) {
        rotate = ViewAnimation.rotateFab(v, !rotate);
        if (rotate) {
            ViewAnimation.showIn(lyt_directions);
            ViewAnimation.showIn(lyt_save);
            back_drop.setVisibility(View.VISIBLE);
        } else {
            ViewAnimation.showOut(lyt_directions);
            ViewAnimation.showOut(lyt_save);
            back_drop.setVisibility(View.GONE);
        }
    }

    private void getCurrentLocation() {
        //init task location
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
        @SuppressLint("") Task<Location> task = fusedLocationProviderClient.getLastLocation();
        task.addOnSuccessListener(new OnSuccessListener<Location>() {
            @Override
            public void onSuccess(Location location) {
                if (location != null) {
                    currentLat = location.getLatitude();
                    currentLong = location.getLongitude();
                    supportMapFragment.getMapAsync(new OnMapReadyCallback() {
                        @Override
                        public void onMapReady(GoogleMap googleMap) {
                            map = googleMap;
                            TastyToast.makeText(NearbyLandMarksActivity.this, "Here", TastyToast.LENGTH_SHORT, TastyToast.SUCCESS).show();
                            //Zoom current loc
                            markerOptions = new MarkerOptions().position(new LatLng(currentLat, currentLong)).title("I am here");
                            map.animateCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(currentLat, currentLong), 10));
                            map.addMarker(markerOptions);
                        }
                    });
                }
            }
        });

    }

    public void Findroutes(LatLng Start, LatLng End) {
        if (Start == null || End == null) {
            TastyToast.makeText(NearbyLandMarksActivity.this, "Unable to get location", TastyToast.LENGTH_LONG, TastyToast.ERROR).show();
        } else {

            Routing routing = new Routing.Builder()
                    .travelMode(AbstractRouting.TravelMode.DRIVING)
                    .withListener(this)
                    .alternativeRoutes(true)
                    .waypoints(Start, End)
                    .key("AIzaSyCWSGKaqi-ksbt79RmDc7RCZ0HES7EWyqg")  //also define your api key here.
                    .build();
            routing.execute();
            CalculationByDistance(Start, End);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == REQUEST_CODE_MAP) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                //Call method
                getCurrentLocation();
                TastyToast.makeText(NearbyLandMarksActivity.this, "Show current location", TastyToast.LENGTH_LONG, TastyToast.SUCCESS).show();
            }
        }
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

    @Override
    public void onRoutingFailure(RouteException e) {
        View parentLayout = findViewById(android.R.id.content);
        Snackbar snackbar = Snackbar.make(parentLayout, e.toString(), Snackbar.LENGTH_LONG);
        snackbar.show();
        Log.d("TAG", "onRoutingFailure: " + e.toString());
    }

    @Override
    public void onRoutingStart() {
        //insert spinner
        TastyToast.makeText(NearbyLandMarksActivity.this, "Finding Route...", TastyToast.LENGTH_LONG, TastyToast.INFO).show();
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
        for (int d = 0; d < route.size(); d++) {

            if (d == shortestRouteIndex) {
                polyOptions.color(getResources().getColor(R.color.colorPrimary));
                polyOptions.width(7);
                polyOptions.addAll(route.get(shortestRouteIndex).getPoints());
                Polyline polyline = map.addPolyline(polyOptions);
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
        map.addMarker(startMarker);

        //Add Marker on route ending position
        MarkerOptions endMarker = new MarkerOptions();
        endMarker.position(polylineEndLatLng);
        endMarker.title("Destination");
        map.addMarker(endMarker);
    }

    @Override
    public void onRoutingCancelled() {
        Findroutes(start, end);
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        TastyToast.makeText(NearbyLandMarksActivity.this, "Finding Route...", TastyToast.LENGTH_LONG, TastyToast.INFO).show();
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        map = googleMap;
        getCurrentLocation();
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
            map.clear();
            for (int m = 0; m < hashMaps.size(); m++) {
                HashMap<String, String> hashMapList = hashMaps.get(m);
                double lat = Double.parseDouble(Objects.requireNonNull(hashMapList.get("lat")));
                double lng = Double.parseDouble(Objects.requireNonNull(hashMapList.get("lng")));

                String name = hashMapList.get("name");
                LatLng latLng = new LatLng(lat, lng);
                //initialize marker options
                MarkerOptions options = new MarkerOptions();
                options.position(latLng);
                //set title
                options.title(name);
                //add marker
                map.addMarker(options);
            }
        }
    }
}