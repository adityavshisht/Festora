package com.metabots.festora;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

public class HomeActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_home);

        // Toolbar first so menu inflates properly
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        // Edge-to-edge insets
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.home_root), (v, insets) -> {
            Insets bars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(bars.left, bars.top, bars.right, bars.bottom);
            return insets;
        });

        // RecyclerView with sample items
        RecyclerView rv = findViewById(R.id.rvEvents);
        rv.setLayoutManager(new LinearLayoutManager(this));

        List<Event> sample = new ArrayList<>();
        sample.add(new Event("1", "Tech Meetup: AI & Startups", "Sat, Sep 21 • 6:00 PM", "Montreal, QC", null));
        sample.add(new Event("2", "Indie Music Night", "Sun, Sep 22 • 8:00 PM", "Laval, QC", null));
        sample.add(new Event("3", "Startup Pitch Day", "Mon, Sep 23 • 5:00 PM", "Longueuil, QC", null));

        EventAdapter adapter = new EventAdapter(this, sample, e ->
                Toast.makeText(this, "Open: " + e.title, Toast.LENGTH_SHORT).show()
        );
        rv.setAdapter(adapter);

        // FAB
        findViewById(R.id.fabCreate).setOnClickListener(v ->
                Toast.makeText(this, "Create new event", Toast.LENGTH_SHORT).show()
        );
    }

    @Override
    public boolean onCreateOptionsMenu(android.view.Menu menu) {
        getMenuInflater().inflate(R.menu.home_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(android.view.MenuItem item) {
        if (item.getItemId() == R.id.action_profile) {
            startActivity(new Intent(this, ProfileActivity.class));
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
