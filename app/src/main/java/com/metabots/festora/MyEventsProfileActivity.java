package com.metabots.festora;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.google.android.material.card.MaterialCardView;

public class MyEventsProfileActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // keep your existing layout; no need to rename the XML
        setContentView(R.layout.activity_my_event_profile);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle("Events");
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        /*MaterialCardView rowMyEvents = findViewById(R.id.rowMyEvents);
        MaterialCardView rowUpcomingEvents = findViewById(R.id.rowUpcomingEvents);

        rowMyEvents.setOnClickListener(v ->
                startActivity(new Intent(MyEventsProfileActivity.this, MyEventsActivity.class))
        );

        rowUpcomingEvents.setOnClickListener(v ->
                startActivity(new Intent(MyEventsProfileActivity.this, UpcomingEventsActivity.class))
        );*/
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
