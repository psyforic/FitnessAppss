package com.celeste;

import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

import com.celeste.fitnessapp.R;
import com.google.android.gms.common.api.Status;
import com.google.android.libraries.places.api.Places;
import com.google.android.libraries.places.api.model.Place;
import com.google.android.libraries.places.widget.Autocomplete;
import com.google.android.libraries.places.widget.AutocompleteActivity;
import com.google.android.libraries.places.widget.model.AutocompleteActivityMode;
import com.sdsmdg.tastytoast.TastyToast;

import java.util.Arrays;
import java.util.List;
import java.util.Locale;

public class MainActivity extends AppCompatActivity {
    EditText etSource, etDestination;
    Button btTrack;
    String sType;
    TextView tvDistance;
    int flag = 0;
    double lat1, lng1;
    double lat2, lng2;
    double miles, kilometres;
    CardView btn_maps;
    CardView bt_landmarks;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initComponent();
        setDistance();
    }

    private void initComponent() {
        btn_maps = findViewById(R.id.btn_maps);
        bt_landmarks = findViewById(R.id.bt_landmarks);
        etSource = findViewById(R.id.etSource);
        etDestination = findViewById(R.id.etDestination);
        tvDistance = findViewById(R.id.tvDistance);
        btTrack = findViewById(R.id.bt_tack);
        etSource.setFocusable(true);
        etDestination.setFocusable(true);
        btTrack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //Get edit text values
                String text_source = etSource.getText().toString().trim();
                String text_destination = etDestination.getText().toString().trim();
                //  if (!text_source.equals("") && text_destination.equals("")) {
//                displayTrack(text_source, text_destination);
                //   }
            }
        });
        btn_maps.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(MainActivity.this, MapsRoute.class));
            }
        });
        bt_landmarks.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                TastyToast.makeText(getApplicationContext(),"Coming soon...",TastyToast.LENGTH_LONG,TastyToast.DEFAULT).show();
            }
        });
    }

    public void setDistance() {
        Places.initialize(getApplicationContext(), "AIzaSyCWSGKaqi-ksbt79RmDc7RCZ0HES7EWyqg");
        etSource.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sType = "source";
                List<Place.Field> fieldList = Arrays.asList(Place.Field.ADDRESS, Place.Field.LAT_LNG);
                Intent intent = new Autocomplete.IntentBuilder(AutocompleteActivityMode.OVERLAY, fieldList).build(getApplicationContext());
                startActivityForResult(intent, 100);
            }
        });
        etDestination.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sType = "destination";
                List<Place.Field> fields = Arrays.asList(Place.Field.ADDRESS,
                        Place.Field.LAT_LNG);
                Intent intent = new Autocomplete.IntentBuilder(
                        AutocompleteActivityMode.OVERLAY, fields).build(getApplicationContext());
                startActivityForResult(intent, 100);
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 100 && resultCode == RESULT_OK) {
            Place place = Autocomplete.getPlaceFromIntent(data);
            if (sType.equals("source")) {
                flag++;
                etSource.setText(place.getAddress());
                String sSource = String.valueOf(place.getLatLng());
                sSource = sSource.replaceAll("lat/lng: ", "");
                sSource = sSource.replace("(", "");
                sSource = sSource.replace(")", "");
                String[] split = sSource.split(",");
                lat1 = Double.parseDouble(split[0]);
                lng1 = Double.parseDouble(split[1]);
            } else {
                flag++;
                etDestination.setText(place.getAddress());
                String sDestination = String.valueOf(place.getLatLng());
                sDestination = sDestination.replaceAll("lat/lng: ", "");
                sDestination = sDestination.replace("(", "");
                sDestination = sDestination.replace(")", "");
                String[] split = sDestination.split(",");
                lat2 = Double.parseDouble(split[0]);
                lng2 = Double.parseDouble(split[1]);
            }
            if (flag >= 2) {
                displayTrack(lat1, lng1, lat2, lng2);
            }
        } else if (requestCode == AutocompleteActivity.RESULT_ERROR) {
            Status status = Autocomplete.getStatusFromIntent(data);
            TastyToast.makeText(getApplicationContext(), status.getStatusMessage(), TastyToast.LENGTH_LONG, TastyToast.ERROR).show();
        }
    }

    private void displayTrack(double lat1, double lng1, double lat2, double lng2) {
        //Calculate longitude diff
        double longDifference = lng1 - lng2;
        double distance =
                Math.sin(degree2radius(lat1)) * Math.sin(degree2radius(lat2))
                        + Math.cos(degree2radius(lat1)) * Math.cos(degree2radius(lat2))
                        * Math.cos(degree2radius(longDifference));
        distance = Math.acos(distance);
        distance = radius2degree(distance);
        //miles
        miles = distance * 60 * 1.1515;
        //kilometres
        kilometres = distance * 1.609344;
        tvDistance.setText(String.format(Locale.US, "%2f Kilometers"));

    }

    private double radius2degree(double distance) {
        return (distance * 180.0 / Math.PI);
    }

    private double degree2radius(double lat1) {
        return (lat1 * Math.PI / 180.0);
    }

    private void displayTrack(String source, String destination) {
        try {
            Toast.makeText(getApplicationContext(), "This does work", Toast.LENGTH_LONG).show();
            Uri uri = Uri.parse("https://www.google.co.in/maps/dir/" + source + "/" + destination);
            Intent intent = new Intent(Intent.ACTION_VIEW, uri);
            intent.setPackage("com.google.android.app.maps");
            //set flag
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
        } catch (ActivityNotFoundException ex) {
            Toast.makeText(getApplicationContext(), "This does not work", Toast.LENGTH_LONG).show();
            Uri uri = Uri.parse("https://play.google.com/store/apps/details?id=com.google.android.apps.maps");
            Intent intent = new Intent(Intent.ACTION_VIEW, uri);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
        }
    }
}