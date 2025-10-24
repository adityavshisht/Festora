package com.metabots.festora;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

import static com.metabots.festora.data.FirestoreContract.Collections.EVENTS;
import static com.metabots.festora.data.FirestoreContract.EventFields.CREATED_AT;
import static com.metabots.festora.data.FirestoreContract.EventFields.DATE_TEXT;
import static com.metabots.festora.data.FirestoreContract.EventFields.HOST_UID;
import static com.metabots.festora.data.FirestoreContract.EventFields.IMAGE_URL;
import static com.metabots.festora.data.FirestoreContract.EventFields.LOCATION;
import static com.metabots.festora.data.FirestoreContract.EventFields.TITLE;


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

        hostedAdapter = new EventAdapter(this, hostedData, e -> openEvent(e, true));
        upcomingAdapter = new EventAdapter(this, upcomingData, e -> openEvent(e, false));

        rvHosted.setAdapter(hostedAdapter);
        rvUpcoming.setAdapter(upcomingAdapter);

        loadEvents();
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadEvents();
    }

    private void loadEvents() {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        final String myUid = user != null ? user.getUid() : null;

        FirebaseFirestore db = FirebaseFirestore.getInstance();

        Task<QuerySnapshot> eventsTask = db.collection(EVENTS)
                .orderBy(CREATED_AT, Query.Direction.DESCENDING)
                .limit(100)
                .get();

        Task<QuerySnapshot> ticketsTask = (myUid == null)
                ? Tasks.forResult((QuerySnapshot) null)
                : db.collection("users")
                        .document(myUid)
                        .collection("tickets")
                        .orderBy("eventDate", Query.Direction.ASCENDING)
                        .get();

        Tasks.whenAllComplete(eventsTask, ticketsTask)
                .addOnCompleteListener(all -> {
                    hostedData.clear();
                    upcomingData.clear();

                    Date now = new Date();
                    Set<String> seenUpcoming = new HashSet<>();

                    if (eventsTask.isSuccessful() && eventsTask.getResult() != null) {
                        for (DocumentSnapshot d : eventsTask.getResult().getDocuments()) {
                            String id = d.getId();
                            String title = d.getString(TITLE);
                            String dateText = d.getString(DATE_TEXT);
                            String location = d.getString(LOCATION);
                            String imageUrl = d.getString(IMAGE_URL);
                            String hostUid = d.getString(HOST_UID);

                            Event ev = new Event(id, title, dateText, location, imageUrl, "firestore", null);

                            if (myUid != null && myUid.equals(hostUid)) {
                                hostedData.add(ev);
                            } else if (isFutureEvent(dateText, now)) {
                                upcomingData.add(ev);
                                seenUpcoming.add(id);
                            }
                        }
                    } else if (eventsTask.getException() != null) {
                        banner("Host events failed: " + eventsTask.getException().getMessage());
                    }

                    if (ticketsTask.isSuccessful() && ticketsTask.getResult() != null) {
                        for (DocumentSnapshot d : ticketsTask.getResult().getDocuments()) {
                            String upcomingId = d.getString("eventId");
                            if (TextUtils.isEmpty(upcomingId)) {
                                upcomingId = d.getId();
                            }
                            if (seenUpcoming.contains(upcomingId)) {
                                continue;
                            }

                            String title = d.getString("title");
                            String date = d.getString("dateText");
                            String location = d.getString("location");
                            String imageUrl = d.getString("imageUrl");
                            Timestamp ts = d.getTimestamp("eventDate");
                            String source = d.getString("source");
                            String bookingUrl = d.getString("bookingUrl");
                            String ticketType = d.getString("ticketType");
                            String hostUidFromTicket = d.getString("hostUid");

                            if (ts != null && ts.toDate().before(now)) {
                                continue;
                            }
                            if (ts == null && !isFutureEvent(date, now)) {
                                continue;
                            }

                            Event ticketEvent = new Event(upcomingId, title, date, location, imageUrl, source, bookingUrl);
                            upcomingData.add(ticketEvent);
                            seenUpcoming.add(upcomingId);

                            if (myUid != null && myUid.equals(hostUidFromTicket) && "host".equals(ticketType)) {
                                // ensure host list also has this event (if Firestore read failed above)
                                boolean alreadyHosted = false;
                                for (Event hosted : hostedData) {
                                    if (hosted.id.equals(upcomingId)) {
                                        alreadyHosted = true;
                                        break;
                                    }
                                }
                                if (!alreadyHosted) {
                                    hostedData.add(ticketEvent);
                                }
                            }
                        }
                    } else if (ticketsTask.getException() != null) {
                        banner("Tickets failed: " + ticketsTask.getException().getMessage());
                    }

                    hostedAdapter.notifyDataSetChanged();
                    upcomingAdapter.notifyDataSetChanged();

                    tvHostedEmpty.setVisibility(hostedData.isEmpty() ? View.VISIBLE : View.GONE);
                    tvUpcomingEmpty.setVisibility(upcomingData.isEmpty() ? View.VISIBLE : View.GONE);
                })
                .addOnFailureListener(e -> banner("Failed to load events: " + e.getMessage()));
    }

    private boolean isFutureEvent(String dateText, Date now) {
        if (TextUtils.isEmpty(dateText)) {
            return true; // treat as future if unknown
        }
        for (String pattern : new String[]{"EEE, MMM d • h:mm a", "EEEE, MMMM d • h:mm a"}) {
            try {
                SimpleDateFormat sdf = new SimpleDateFormat(pattern, Locale.getDefault());
                sdf.setLenient(false);
                Date parsed = sdf.parse(dateText);
                if (parsed != null) {
                    return parsed.after(now);
                }
            } catch (ParseException ignored) {
            }
        }
        return true;
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void openEvent(Event event, boolean fromHostedList) {
        if (event == null) return;
        String source = event.source != null ? event.source : "";
        if (fromHostedList || "firestore".equals(source)) {
            Intent hosted = new Intent(this, HostedEventDetailsActivity.class);
            hosted.putExtra(HostedEventDetailsActivity.EXTRA_EVENT_ID, event.id);
            hosted.putExtra(HostedEventDetailsActivity.EXTRA_TITLE, event.title);
            hosted.putExtra(HostedEventDetailsActivity.EXTRA_DATE, event.dateTime);
            hosted.putExtra(HostedEventDetailsActivity.EXTRA_LOCATION, event.location);
            hosted.putExtra(HostedEventDetailsActivity.EXTRA_IMAGE_URL, event.imageUrl);
            hosted.putExtra(HostedEventDetailsActivity.EXTRA_IS_HOST, fromHostedList);
            startActivity(hosted);
        } else {
            Intent intent = new Intent(this, EventDetailsActivity.class);
            intent.putExtra("id", event.id);
            intent.putExtra("title", event.title);
            intent.putExtra("dateTime", event.dateTime);
            intent.putExtra("location", event.location);
            intent.putExtra("imageUrl", event.imageUrl);
            intent.putExtra("apiKey", HomeActivity.TM_API_KEY);
            intent.putExtra(EventDetailsActivity.EXTRA_ALREADY_JOINED, true);
            if (event.bookingUrl != null) {
                intent.putExtra("bookingUrl", event.bookingUrl);
            }
            startActivity(intent);
        }
    }
}
