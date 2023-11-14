package com.example.gvl;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.gvl.ui.login.LoginActivity;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.CollectionReference;
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
import java.util.Map;

public class MainActivity extends AppCompatActivity {

    FusedLocationProviderClient fusedLocationProviderClient;
    TextView loc;
    TextView totalInjuredTV;
    TextView totalDeathsTV;
    TextView lastReportedTV;
    TextView riskLevelTV;
    ImageView riskIcon;
    String state;
    String city;
    int risk;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        loc = findViewById(R.id.loc);
        totalInjuredTV = findViewById(R.id.total_count);
        totalDeathsTV = findViewById(R.id.total_dni);
        lastReportedTV = findViewById(R.id.last_reported);
        riskLevelTV = findViewById(R.id.alert_txt);
        riskIcon = findViewById(R.id.alert_img);

        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);

        getLocation();

        Button refresh = (android.widget.Button) findViewById(R.id.logo);
        refresh.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                state = "";
                city = "";
                risk = 0;
                getLocation();
            }
        });

        ImageButton search_btn = (android.widget.ImageButton) findViewById(R.id.search_btn);
        search_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, SearchActivity.class);
                startActivity(intent);
            }
        });

        ImageButton map_btn = (android.widget.ImageButton) findViewById(R.id.map_btn);
        map_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, MapActivity.class);
                startActivity(intent);
            }
        });

    }

    private void getLocation() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED || ActivityCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {

            fusedLocationProviderClient.getLastLocation().addOnSuccessListener(new OnSuccessListener<Location>() {
                @Override
                public void onSuccess(Location location) {
                    if (location != null) {
                        Geocoder geocoder = new Geocoder(MainActivity.this, Locale.US);
                        List<Address> addresses = null;
                        try {
                            addresses = geocoder.getFromLocation(location.getLatitude(), location.getLongitude(), 1);
                            state = addresses.get(0).getLocality();
                            city = addresses.get(0).getAdminArea();
//                            city = "Detroit";
                            loc.setText(city + ", " + state);
                            risk = 0;
                            // Call a method to fetch data from Firestore based on state and city
                            fetchDataFromFirestore(state, city);

                        } catch (IOException e) {
                            throw new RuntimeException(e);
                        }
                    }
                }
            });
        } else {
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

        // Update risk based on the time elapsed since the last incident date
        calculateRisk(victimsDeaths, victimsInjured, lastIncidentDate);

        String risklevel;
        String iconname;
        String color;

        if (risk < 5)
        {
            iconname = "safe2";
            risklevel = "Low Risk Area";
            color = "#00d169";
        }
        else if (5 <= risk && risk < 10)
        {
            iconname = "medium2";
            risklevel = "Medium Risk Area";
            color = "#e8e409";

        }
        else {
            iconname = "high_risk2";
            risklevel = "High Risk Area";
            color = "#f54251";

        }

        int iconID = getResources().getIdentifier(iconname, "drawable", getPackageName());
        riskIcon.setImageResource(iconID);

        riskLevelTV.setText(risklevel);
        riskLevelTV.setTextColor(Color.parseColor(color));

    }

    private void calculateRisk(Long victimsKilled, Long victimsInjured, String lastIncidentDate) {
        try {
            SimpleDateFormat dateFormat = new SimpleDateFormat("MMMM d, yyyy", Locale.ENGLISH);
            Date incidentDate = dateFormat.parse(lastIncidentDate);

            // Current date
            Calendar currentDate = Calendar.getInstance();
            currentDate.setTime(new Date());

            // One month ago
            Calendar oneMonthAgo = Calendar.getInstance();
            oneMonthAgo.add(Calendar.MONTH, -1);

            // One week ago
            Calendar oneWeekAgo = Calendar.getInstance();
            oneWeekAgo.add(Calendar.WEEK_OF_YEAR, -1);

            if (incidentDate.before(oneMonthAgo.getTime())) {
                // Date is more than a month ago, increment risk by 1
                risk += 1;
            } else if (incidentDate.before(oneWeekAgo.getTime())) {
                // Date is more than a week ago, but less than a month ago, increment risk by 2
                risk += 2;
            } else {
                // Date is within the last week, increment risk by 3
                risk += 3;
            }

            // Increment risk based on victimsInjured count
            if (victimsInjured < 5) {
                risk += 1;
            } else if (victimsInjured < 10) {
                risk += 2;
            } else {
                risk += 3;
            }

            // Increment risk based on another condition
            if (victimsKilled <= 3) {
                risk += 2;
            } else if (victimsKilled < 10) {
                risk += 4;
            } else {
                risk += 6;
            }

        } catch (ParseException e) {
            // Handle parsing exception
            e.printStackTrace();
        }
    }


    private void askPermission() {
        ActivityCompat.requestPermissions(MainActivity.this, new String[]
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