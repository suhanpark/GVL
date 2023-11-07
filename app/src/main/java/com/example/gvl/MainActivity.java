package com.example.gvl;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import android.Manifest;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnSuccessListener;

import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.permissionx.guolindev.PermissionX;

public class MainActivity extends AppCompatActivity {

    FusedLocationProviderClient fusedLocationProviderClient;
    TextView currentLoc;
    Button getLocation;
    ImageView riskIcon;
    TextView alertText;
    String city;
    String state;
    int injured;
    String risk;
    int deaths;
    String last_incident;
    FirebaseFirestore db = FirebaseFirestore.getInstance();
    CollectionReference stateCollection;
    DocumentReference localityDocument;
    TextView totalDeaths;
    TextView totalInjured;
    TextView lastIncidentDate;


    private final static int REQUEST_CODE = 100;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        riskIcon = findViewById(R.id.alert_img);
        alertText = findViewById(R.id.alert_txt);
        totalInjured = findViewById(R.id.total_count);
        totalDeaths = findViewById(R.id.total_dni);
        lastIncidentDate = findViewById(R.id.last_reported);

        currentLoc = findViewById(R.id.loc);
        getLocation = findViewById(R.id.locationFinder);

        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);
        getLastLocation();

        try {
            // Create a reference to the top-level collection (e.g., "states")
            stateCollection = db.collection(state);
            // Create a reference to the document within the state collection (e.g., "localities")
            localityDocument = stateCollection.document("Detroit");
            updateRisk(localityDocument, "Victims Injured", "Victims Killed", "Latest Incident Date", riskIcon, alertText);
            totalDeaths.setText("Total Deaths\n"+deaths);
            totalInjured.setText("Total Injured\n"+injured);
            lastIncidentDate.setText("Last Reported\n"+lastIncidentDate);

        } catch (Exception e) {
            riskIcon.setImageResource(R.drawable.safe2);
            alertText.setText("Safe Area");
            alertText.setTextColor(Color.rgb(0, 196, 23));
            totalDeaths.setText("Total Deaths\n"+0);
            totalInjured.setText("Total Injured\n"+0);
            lastIncidentDate.setText("Last Reported\nNo Data");
        }

        getLocation.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getLastLocation();
                try {
                    // Create a reference to the top-level collection (e.g., "states")
                    stateCollection = db.collection(state);
                    // Create a reference to the document within the state collection (e.g., "localities")
                    localityDocument = stateCollection.document(city);
                    updateRisk(localityDocument, "Victims Injured", "Victims Killed", "Latest Incident Date", riskIcon, alertText);
                    totalDeaths.setText("Total Deaths\n"+deaths);
                    totalInjured.setText("Total Injured\n"+injured);
                    lastIncidentDate.setText("Last Reported\n"+lastIncidentDate);

                } catch (Exception e) {
                    riskIcon.setImageResource(R.drawable.safe2);
                    alertText.setText("Safe Area");
                    alertText.setTextColor(Color.rgb(0, 196, 23));
                    totalDeaths.setText("Total Deaths\n"+0);
                    totalInjured.setText("Total Injured\n"+0);
                    lastIncidentDate.setText("Last Reported\nNo Data");
                }
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

        AppCompatActivity ma = this;
        ImageButton map_btn = (android.widget.ImageButton) findViewById(R.id.map_btn);
        map_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                PermissionX.init(ma)
                        .permissions(Manifest.permission.ACCESS_FINE_LOCATION)
                        .request(
                                (allGranted, grantedList, deniedList) ->
                                {
                                    if (allGranted) {
                                        Intent intent = new Intent(MainActivity.this, MapActivity.class);
                                        startActivity(intent);
                                    } else {
                                        Toast.makeText(ma, "Can't get precise location: permission denied", Toast.LENGTH_LONG).show();
                                    }
                                }
                        );

                /*Intent intent = new Intent(MainActivity.this, MapActivity.class);
                startActivity(intent);*/
            }
        });
    }

    @SuppressLint("MissingPermission")
    private void getLastLocation() {
        AppCompatActivity ma = this;
        PermissionX.init(ma)
                .permissions(Manifest.permission.ACCESS_FINE_LOCATION)
                .request(
                        (allGranted, grantedList, deniedList) ->
                        {
                            if (allGranted) {
                                fusedLocationProviderClient.getLastLocation().addOnSuccessListener(new OnSuccessListener<Location>() {
                                    @Override
                                    public void onSuccess(Location location) {
                                        if (location != null) {
                                            Geocoder geocoder = new Geocoder(MainActivity.this, Locale.getDefault());
                                            List<Address> addresses = null;
                                            try {
                                                addresses = geocoder.getFromLocation(location.getLatitude(), location.getLongitude(), 1);
                                                city = addresses.get(0).getLocality();
                                                state = addresses.get(0).getAdminArea();
                                                currentLoc.setText(addresses.get(0).getLocality() + ", " + addresses.get(0).getAdminArea() /*+ ", " + addresses.get(0).getCountryName()*/);
                                            } catch (IOException e) {
                                                e.printStackTrace();
                                            }
                                        }
                                    }
                                });
                            } else {
                                Toast.makeText(ma, "Can't get precise location: permission denied", Toast.LENGTH_LONG).show();
                            }
                        }
                );

    }
    private void updateRisk(DocumentReference localityDocument, String victimsInjuredField, String victimsDeathsField, String lastIncidentField, ImageView riskIcon, TextView alertText) {
        localityDocument.get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        Long victimsInjured = documentSnapshot.getLong(victimsInjuredField);
                        Long victimsDeaths = documentSnapshot.getLong(victimsDeathsField);
                        String lastReported = documentSnapshot.getString(lastIncidentField);
                        if (victimsInjured != null) {
                            injured = victimsInjured.intValue();
                        }
                        if (victimsDeaths != null) {
                            deaths = victimsDeaths.intValue();
                        }
                        if (lastReported != null) {
                            last_incident = lastReported;

                            SimpleDateFormat dateFormat = new SimpleDateFormat("MMMM d, yyyy", Locale.ENGLISH);

                            try {
                                Date date = dateFormat.parse(last_incident); // Parse the date string
                                Calendar currentDate = Calendar.getInstance();
                                currentDate.setTime(new Date());

                                Calendar oneMonthAgo = Calendar.getInstance();
                                oneMonthAgo.add(Calendar.MONTH, -1);

                                Calendar oneWeekAgo = Calendar.getInstance();
                                oneWeekAgo.add(Calendar.WEEK_OF_YEAR, -1);

                                if (date.before(oneMonthAgo.getTime())) {
                                    // Date is more than a month ago, set "risk" to "low"
                                    risk = "low";
                                    riskIcon.setImageResource(R.drawable.safe2);
                                    alertText.setText("Safe Area");
                                    alertText.setTextColor(Color.rgb(0, 196, 23));

                                } else if (date.before(oneWeekAgo.getTime())) {
                                    // Date is more than a week ago, but less than a month ago, set "risk" to "medium"
                                    risk = "medium";
                                    riskIcon.setImageResource(R.drawable.medium2);
                                    alertText.setText("Medium Risk Area");
                                    alertText.setTextColor(Color.rgb(252, 198, 3));
                                } else {
                                    // Date is within the last week, set "risk" to "high"
                                    risk = "high";
                                    riskIcon.setImageResource(R.drawable.high_risk2);
                                    alertText.setText("High Risk Area");
                                    alertText.setTextColor(Color.rgb(196, 0, 33));
                                }
                            } catch (ParseException e) {
                                risk = "low";
                            }
                        } else {
                            risk = "low";
                        }
                    } else {
                        risk = "low";
                    }
                })
                .addOnFailureListener(e -> {
                    risk = "low";
                });
    }

    /*private void askPermission() {

}*/

}