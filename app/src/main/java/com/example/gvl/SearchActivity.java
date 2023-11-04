package com.example.gvl;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.preference.PreferenceManager;

import android.Manifest;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;

import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnSuccessListener;

import java.io.IOException;
import java.util.List;
import java.util.Locale;
import java.util.Random;

public class SearchActivity extends AppCompatActivity {
    private static final String ADDRESS_TEXT_KEY = "address_text";
    EditText address;
    FusedLocationProviderClient fusedLocationProviderClient;
    TextView loc;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search);

        loc = findViewById(R.id.current_location2);

        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);

        getLocation();

        address = (EditText)findViewById(R.id.type_address);

        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        String addy1 = prefs.getString("address", "");
        address.setText(addy1);

        ImageButton search_btn = (android.widget.ImageButton) findViewById(R.id.search_btn);
        TextView location = findViewById(R.id.current_location2);
        TextView deathsText = findViewById(R.id.total_dni2);
        TextView incidentsText = findViewById(R.id.total_count2);

        search_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                String loc = address.getText().toString();
                location.setText(loc);
                Random random = new Random();
                int deaths = random.nextInt(10000);
                int incidents = random.nextInt(10000);
                deathsText.setText("Total Deaths:\n"+deaths);
                incidentsText.setText("Total Incidents:\n"+incidents);
            }
        });


        ImageButton back_btn = (android.widget.ImageButton) findViewById(R.id.back_btn);
        back_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                String addy = address.getText().toString();

                SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(SearchActivity.this);
                SharedPreferences.Editor editor = prefs.edit();

                editor.putString("address", addy);
                editor.apply();

                Intent intent = new Intent(SearchActivity.this, MainActivity.class);
                startActivity(intent);
            }
        });
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        // Save the text entered by the user to the savedInstanceState bundle
        String addressText = address.getText().toString();
        outState.putString(ADDRESS_TEXT_KEY, addressText);
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);

        // Restore the text entered by the user from the savedInstanceState bundle
        if (savedInstanceState != null) {
            String savedAddressText = savedInstanceState.getString(ADDRESS_TEXT_KEY);
            address.setText(savedAddressText);
        }
    }

    private void getLocation() {
        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED || ActivityCompat.checkSelfPermission(this,
                android.Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {

            fusedLocationProviderClient.getLastLocation().addOnSuccessListener(new OnSuccessListener<Location>() {
                @Override
                public void onSuccess(Location location) {
                    if (location != null) {
                        Geocoder geocoder = new Geocoder(SearchActivity.this, Locale.US);
                        List<Address> addresses = null;
                        try {
                            addresses = geocoder.getFromLocation(location.getLatitude(), location.getLongitude(), 1);
                            String cityName = addresses.get(0).getLocality();
                            String stateName = addresses.get(0).getAdminArea();
                            loc.setText(cityName + ", " + stateName);
                        } catch (IOException e) {
                            throw new RuntimeException(e);
                        }
                    }
                }
            });
        }
        else {
            askPermission();
        }
    }

    private void askPermission() {
        ActivityCompat.requestPermissions(this, new String[]
                {Manifest.permission.ACCESS_FINE_LOCATION}, 100);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {

        if (requestCode==100){
            if (grantResults.length>0 && grantResults[0] == PackageManager.PERMISSION_GRANTED){
                getLocation();
            }
            else {
                Toast.makeText(this, "Required Permission", Toast.LENGTH_SHORT).show();
            }
        }

        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }
}