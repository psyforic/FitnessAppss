package com.celeste.fitnessapp;

import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.libraries.places.api.Places;
import com.google.android.libraries.places.api.model.Place;
import com.google.android.libraries.places.widget.Autocomplete;
import com.google.android.libraries.places.widget.model.AutocompleteActivityMode;

import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.List;

public class MainActivity extends AppCompatActivity {
    EditText etSource, etDestination;
    Button btTrack;
    String sType;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initComponent();
    }

    private void initComponent() {
        etSource = findViewById(R.id.etSource);
        Places.initialize(getApplicationContext(), "");
        etSource.setFocusable(false);
        etSource.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sType = "source";
                List<Place.Field> fieldList = Arrays.asList(Place.Field.ADDRESS, Place.Field.LAT_LNG);
                Intent intent = new Autocomplete.IntentBuilder(AutocompleteActivityMode.OVERLAY, fieldList).build(MainActivity.this);
                startActivityForResult(intent, 100);
            }
        });
        etDestination.setFocusable(false);
        etDestination.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sType="destination";
                List<Place.Field> fields=Arrays.asList(Place.Field.ADDRESS,
                        Place.Field.LAT_LNG);

                Intent intent=new Autocomplete.IntentBuilder(
                       AutocompleteActivityMode.OVERLAY,fields).build(MainActivity.this);
                startActivityForResult(intent,100);

            }
        });
        etDestination = findViewById(R.id.etDestination);
        btTrack = findViewById(R.id.bt_tack);
        btTrack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //Get edit text values
                String text_source = etSource.getText().toString().trim();
                String text_destination = etDestination.getText().toString().trim();
                //  if (!text_source.equals("") && text_destination.equals("")) {
                displayTrack(text_source, text_destination);
                //   }
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
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
        }
        catch (ActivityNotFoundException ex) {
            Toast.makeText(getApplicationContext(), "This does not work", Toast.LENGTH_LONG).show();
            Uri uri = Uri.parse("https://play.google.com/store/apps/details?id=com.google.android.apps.maps");
            Intent intent = new Intent(Intent.ACTION_VIEW, uri);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
        }
    }
}