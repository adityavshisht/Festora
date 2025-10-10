package com.metabots.festora;

import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

// Firestore
import static com.metabots.festora.data.FirestoreContract.Collections.EVENTS;
import static com.metabots.festora.data.FirestoreContract.EventFields.CATEGORY;
import static com.metabots.festora.data.FirestoreContract.EventFields.CREATED_AT;
import static com.metabots.festora.data.FirestoreContract.EventFields.DATE_TEXT;
import static com.metabots.festora.data.FirestoreContract.EventFields.DESCRIPTION;
import static com.metabots.festora.data.FirestoreContract.EventFields.HOST_EMAIL;
import static com.metabots.festora.data.FirestoreContract.EventFields.HOST_UID;
import static com.metabots.festora.data.FirestoreContract.EventFields.IMAGE_URL;
import static com.metabots.festora.data.FirestoreContract.EventFields.LOCATION;
import static com.metabots.festora.data.FirestoreContract.EventFields.TITLE;

import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.DocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

public class MyEventsProfileActivity extends AppCompatActivity {

    private RecyclerView rvHosted, rvUpcoming;
    private TextView tvHostedEmpty, tvUpcomingEmpty;

    private final List<Event> hostedData = new ArrayList<>();
    private final List<Event> upcomingData = new ArrayList<>();

    private EventAdapter hostedAdapter;
    private EventAdapter upcomingAdapter;

    private View root;

    private void banner(String msg) {
        Snackbar.make(root, msg, Snackbar.LENGTH_SHORT).show();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my_event_profile);

        root = findViewById(R.id.events_root);

        // Toolbar + back arrow
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle("My events");
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        // Views
        rvHosted = findViewById(R.id.rvHosted);
        rvUpcoming = findViewById(R.id.rvUpcoming);
        tvHostedEmpty = findViewById(R.id.tvHostedEmpty);
        tvUpcomingEmpty = findViewById(R.id.tvUpcomingEmpty);

        // RecyclerViews inside NestedScrollView: disable nested scrolling
        rvHosted.setNestedScrollingEnabled(false);
        rvUpcoming.setNestedScrollingEnabled(false);

        rvHosted.setLayoutManager(new LinearLayoutManager(this));
        rvUpcoming.setLayoutManager(new LinearLayoutManager(this));

        hostedAdapter = new EventAdapter(this, hostedData, e -> banner("Open: " + e.title));
        upcomingAdapter = new EventAdapter(this, upcomingData, e -> banner("Open: " + e.title));

        rvHosted.setAdapter(hostedAdapter);
        rvUpcoming.setAdapter(upcomingAdapter);

        loadEvents();
    }

    private void loadEvents() {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        final String myUid = user != null ? user.getUid() : null;

        // Grab the latest events (hosted by anyone), then split locally
        FirebaseFirestore.getInstance()
                .collection(EVENTS)
                .orderBy(CREATED_AT, Query.Direction.DESCENDING)
                .limit(100)
                .get()
                .addOnSuccessListener(snap -> {
                    hostedData.clear();
                    upcomingData.clear();

                    for (DocumentSnapshot d : snap.getDocuments()) {
                        String id = d.getId();
                        String title = d.getString(TITLE);
                        String dateText = d.getString(DATE_TEXT);
                        String location = d.getString(LOCATION);
                        String imageUrl = d.getString(IMAGE_URL);
                        String hostUid = d.getString(HOST_UID);
                        // String hostEmail = d.getString(HOST_EMAIL);
                        // String category = d.getString(CATEGORY);
                        // String description = d.getString(DESCRIPTION);

                        Event ev = new Event(id, title, dateText, location, imageUrl);
                        if (myUid != null && myUid.equals(hostUid)) {
                            hostedData.add(ev);
                        } else {
                            upcomingData.add(ev);
                        }
                    }

                    hostedAdapter.notifyDataSetChanged();
                    upcomingAdapter.notifyDataSetChanged();

                    tvHostedEmpty.setVisibility(hostedData.isEmpty() ? View.VISIBLE : View.GONE);
                    tvUpcomingEmpty.setVisibility(upcomingData.isEmpty() ? View.VISIBLE : View.GONE);
                })
                .addOnFailureListener(e -> banner("Failed to load: " + e.getMessage()));
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
