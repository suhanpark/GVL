package com.example.gvl;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.Toast;
import android.Manifest;

import com.example.gvl.ui.login.LoginActivity;
import com.permissionx.guolindev.PermissionX;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

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
                        .request (
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
}