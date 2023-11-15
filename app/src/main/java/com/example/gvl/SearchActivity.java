package com.example.gvl;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.preference.PreferenceManager;

import android.Manifest;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;

import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Random;

public class SearchActivity extends AppCompatActivity {
    private static final String ADDRESS_TEXT_KEY = "address_text";
    EditText address;
    FusedLocationProviderClient fusedLocationProviderClient;
    TextView loc;
    int risk;
    TextView totalInjuredTV;
    TextView totalDeathsTV;
    TextView lastReportedTV;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search);

        loc = findViewById(R.id.current_location2);
        totalInjuredTV = findViewById(R.id.total_count2);
        totalDeathsTV = findViewById(R.id.total_dni2);
        lastReportedTV = findViewById(R.id.last_reported2);

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

                // Split string to 2 separate parts, to get city specific data
                String[] parts = loc.split(", ");
                String temp_state = parts[1];
                String temp_city = parts[0];
                System.out.print(temp_state);
                System.out.print(temp_city);

                try {
                    fetchDataFromFirestore(temp_state, temp_city);
                }
                catch (Exception e) {
                    deathsText.setText("Total Deaths:\n"+0);
                    incidentsText.setText("Total Incidents:\n"+0);
                }
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

    private void fetchDataFromFirestore(String state, String city) {
        // Initialize Firestore
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        // Reference to the Firestore collection and document for Michigan and Detroit
        DocumentReference documentRef = db.collection("cases")
                .document(state)
                .collection("cities")
                .document(city);

        documentRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    DocumentSnapshot document = task.getResult();
                    if (document.exists()) {
                        Long victimsInjured = document.getLong("Victims Injured");
                        Long victimsDeaths = document.getLong("Victims Killed");
                        String lastIncidentDate = document.getString("Latest Incident Date");

                        updateUI(victimsInjured, victimsDeaths, lastIncidentDate);
                    } else {
//                        Toast.makeText(MainActivity.this, "Firestore document does not exist for Michigan and Detroit", Toast.LENGTH_SHORT).show();
                        risk = 0;
                        Long victimsInjured = 0L;
                        Long victimsDeaths = 0L;
                        String lastIncidentDate = "N/A";
                        updateUI(victimsInjured, victimsDeaths, lastIncidentDate);
                    }
                } else {
//                    Toast.makeText(MainActivity.this, "Error getting Firestore document: " + task.getException(), Toast.LENGTH_SHORT).show();
                    risk = 0;
                    Long victimsInjured = 0L;
                    Long victimsDeaths = 0L;
                    String lastIncidentDate = "N/A";
                    updateUI(victimsInjured, victimsDeaths, lastIncidentDate);
                }
            }
        });
    }

    private void updateUI(Long victimsInjured, Long victimsDeaths, String lastIncidentDate) {
        // Update UI elements with Firestore data
        totalInjuredTV.setText("Total Injured:\n" + victimsInjured);
        totalDeathsTV.setText("Total Deaths:\n" + victimsDeaths);
        lastReportedTV.setText("Latest Incident Date:\n" + lastIncidentDate);

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