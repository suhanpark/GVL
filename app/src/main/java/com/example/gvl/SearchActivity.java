package com.example.gvl;

import androidx.appcompat.app.AppCompatActivity;
import androidx.preference.PreferenceManager;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;

import android.widget.EditText;
import android.widget.TextView;

import java.util.Random;

public class SearchActivity extends AppCompatActivity {
    private static final String ADDRESS_TEXT_KEY = "address_text";
    EditText address;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search);

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
}