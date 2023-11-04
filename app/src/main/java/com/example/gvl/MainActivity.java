package com.example.gvl;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import android.Manifest;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnSuccessListener;

import java.io.IOException;
import java.util.List;
import java.util.Locale;
import com.permissionx.guolindev.PermissionX;

public class MainActivity extends AppCompatActivity {

    FusedLocationProviderClient fusedLocationProviderClient;
    TextView currentLoc;
    Button getLocation;

    private final static int REQUEST_CODE = 100;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        currentLoc = findViewById(R.id.loc);
        getLocation = findViewById(R.id.locationFinder);

        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);
        getLastLocation();
        getLocation.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getLastLocation();
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

    /*private void askPermission() {

}*/

}