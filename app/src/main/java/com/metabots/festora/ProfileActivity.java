package com.metabots.festora;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.google.android.material.card.MaterialCardView;

public class ProfileActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle("Profile");
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        MaterialCardView rowPersonal = findViewById(R.id.rowPersonal); // <-- must exist in XML
        rowPersonal.setOnClickListener(v ->
                startActivity(new Intent(ProfileActivity.this, PersonalInfoActivity.class))
        );

        MaterialCardView rowEvents = findViewById(R.id.rowEvents);
        rowEvents.setOnClickListener(v ->
                startActivity(new Intent(ProfileActivity.this, MyEventsProfileActivity.class))
        );

        MaterialCardView rowNotifications = findViewById(R.id.rowNotifications);
        rowNotifications.setOnClickListener(v ->
                startActivity(new Intent(ProfileActivity.this, NotificationsActivity.class))
        );

        MaterialCardView rowPrivacy = findViewById(R.id.rowPrivacy);
        rowPrivacy.setOnClickListener(v ->
                startActivity(new Intent(ProfileActivity.this, LocationPrivacyActivity.class))
        );


        // TODO: wire the rest rows similarly (rowEvents, rowNotifications, etc.)
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
