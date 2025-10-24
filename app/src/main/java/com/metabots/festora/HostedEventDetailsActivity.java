package com.metabots.festora;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Base64;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.bumptech.glide.Glide;
import com.google.android.material.button.MaterialButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.SetOptions;
import com.google.firebase.firestore.WriteBatch;

import java.util.HashMap;
import java.util.Map;

import static com.metabots.festora.data.FirestoreContract.Collections.EVENTS;
import static com.metabots.festora.data.FirestoreContract.EventFields.CATEGORY;
import static com.metabots.festora.data.FirestoreContract.EventFields.DATE_TEXT;
import static com.metabots.festora.data.FirestoreContract.EventFields.DESCRIPTION;
import static com.metabots.festora.data.FirestoreContract.EventFields.HOST_EMAIL;
import static com.metabots.festora.data.FirestoreContract.EventFields.HOST_UID;
import static com.metabots.festora.data.FirestoreContract.EventFields.IMAGE_URL;
import static com.metabots.festora.data.FirestoreContract.EventFields.LOCATION;
import static com.metabots.festora.data.FirestoreContract.EventFields.TITLE;

public class HostedEventDetailsActivity extends AppCompatActivity {

    public static final String EXTRA_EVENT_ID = "extra_event_id";
    public static final String EXTRA_TITLE = "extra_title";
    public static final String EXTRA_DATE = "extra_date";
    public static final String EXTRA_LOCATION = "extra_location";
    public static final String EXTRA_IMAGE_URL = "extra_image_url";
    public static final String EXTRA_IS_HOST = "extra_is_host";

    private ProgressBar progress;
    private TextView txtTitle;
    private TextView txtDate;
    private TextView txtLocation;
    private TextView txtCategory;
    private TextView txtDescription;
    private ImageView imgBanner;
    private MaterialButton btnDelete;
    private MaterialButton btnJoin;

    private String eventId;
    private boolean isHost;

    private String title;
    private String date;
    private String location;
    private String category;
    private String description;
    private String imageUrl;
    private String hostUid;
    private String hostEmail;

    private final FirebaseFirestore db = FirebaseFirestore.getInstance();

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_hosted_event_details);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        toolbar.setNavigationOnClickListener(v -> finish());

        progress = findViewById(R.id.progress);
        txtTitle = findViewById(R.id.txtTitle);
        txtDate = findViewById(R.id.txtDate);
        txtLocation = findViewById(R.id.txtLocation);
        txtCategory = findViewById(R.id.txtCategory);
        txtDescription = findViewById(R.id.txtDescription);
        imgBanner = findViewById(R.id.imgBanner);
        btnDelete = findViewById(R.id.btnDelete);
        btnJoin = findViewById(R.id.btnJoin);

        eventId = getIntent().getStringExtra(EXTRA_EVENT_ID);
        title = getIntent().getStringExtra(EXTRA_TITLE);
        date = getIntent().getStringExtra(EXTRA_DATE);
        location = getIntent().getStringExtra(EXTRA_LOCATION);
        imageUrl = getIntent().getStringExtra(EXTRA_IMAGE_URL);
        isHost = getIntent().getBooleanExtra(EXTRA_IS_HOST, false);

        bindImage(imageUrl, imgBanner);
        if (!TextUtils.isEmpty(title)) txtTitle.setText(title);
        if (!TextUtils.isEmpty(date)) txtDate.setText(date);
        if (!TextUtils.isEmpty(location)) txtLocation.setText(location);

        if (TextUtils.isEmpty(eventId)) {
            Toast.makeText(this, "Missing event id", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        loadEvent();
    }

    private void loadEvent() {
        progress.setVisibility(View.VISIBLE);
        db.collection(EVENTS)
                .document(eventId)
                .get()
                .addOnSuccessListener(this::bindEvent)
                .addOnFailureListener(e -> {
                    progress.setVisibility(View.GONE);
                    Toast.makeText(this, "Failed to load event: " + e.getMessage(), Toast.LENGTH_LONG).show();
                });
    }

    private void bindEvent(DocumentSnapshot snapshot) {
        progress.setVisibility(View.GONE);
        if (snapshot == null || !snapshot.exists()) {
            Toast.makeText(this, "Event not found", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        title = snapshot.getString(TITLE);
        date = snapshot.getString(DATE_TEXT);
        location = snapshot.getString(LOCATION);
        category = snapshot.getString(CATEGORY);
        description = snapshot.getString(DESCRIPTION);
        imageUrl = snapshot.getString(IMAGE_URL);
        hostUid = snapshot.getString(HOST_UID);
        hostEmail = snapshot.getString(HOST_EMAIL);

        if (!TextUtils.isEmpty(title)) txtTitle.setText(title);
        if (!TextUtils.isEmpty(date)) txtDate.setText(date);
        if (!TextUtils.isEmpty(location)) txtLocation.setText(location);
        txtCategory.setText(!TextUtils.isEmpty(category) ? category : "");
        txtDescription.setText(!TextUtils.isEmpty(description) ? description : "");
        bindImage(imageUrl, imgBanner);

        configureActions();
    }

    private void configureActions() {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        boolean isHostNow = user != null && !TextUtils.isEmpty(hostUid) && hostUid.equals(user.getUid());

        if (isHost || isHostNow) {
            btnDelete.setVisibility(View.VISIBLE);
            btnDelete.setOnClickListener(v -> confirmDelete());
            btnJoin.setVisibility(View.GONE);
            return;
        }

        btnDelete.setVisibility(View.GONE);

        if (user == null) {
            btnJoin.setVisibility(View.VISIBLE);
            btnJoin.setEnabled(true);
            btnJoin.setText(R.string.join_event);
            btnJoin.setOnClickListener(v -> startActivity(new Intent(this, LoginActivity.class)));
            return;
        }

        btnJoin.setVisibility(View.INVISIBLE);
        db.collection("users")
                .document(user.getUid())
                .collection("tickets")
                .document(eventId)
                .get()
                .addOnSuccessListener(ticket -> {
                    boolean alreadyJoined = ticket != null && ticket.exists();
                    btnJoin.setVisibility(View.VISIBLE);
                    if (alreadyJoined) {
                        btnJoin.setText(R.string.joined);
                        btnJoin.setEnabled(false);
                    } else {
                        btnJoin.setText(R.string.join_event);
                        btnJoin.setEnabled(true);
                        btnJoin.setOnClickListener(v -> joinHostedEvent(user));
                    }
                })
                .addOnFailureListener(e -> {
                    btnJoin.setVisibility(View.VISIBLE);
                    btnJoin.setText(R.string.join_event);
                    btnJoin.setEnabled(true);
                    btnJoin.setOnClickListener(v -> joinHostedEvent(user));
                });
    }

    private void joinHostedEvent(FirebaseUser user) {
        if (user == null) return;
        progress.setVisibility(View.VISIBLE);

        WriteBatch batch = db.batch();

        Map<String, Object> userMeta = new HashMap<>();
        userMeta.put("lastJoinedAt", FieldValue.serverTimestamp());

        Map<String, Object> ticket = new HashMap<>();
        ticket.put("eventId", eventId);
        ticket.put("title", title);
        ticket.put("dateText", date);
        ticket.put("location", location);
        ticket.put("imageUrl", imageUrl);
        ticket.put("bookingUrl", null);
        ticket.put("attendeeName", user.getDisplayName());
        ticket.put("attendeeEmail", user.getEmail());
        ticket.put("joinedAt", FieldValue.serverTimestamp());
        ticket.put("source", "firestore");
        ticket.put("ticketType", "attendee");
        ticket.put("hostUid", hostUid);
        ticket.put("hostEmail", hostEmail);
        if (!TextUtils.isEmpty(category)) ticket.put("category", category);
        if (!TextUtils.isEmpty(description)) ticket.put("description", description);

        batch.set(db.collection("users").document(user.getUid()), userMeta, SetOptions.merge());
        batch.set(
                db.collection("users")
                        .document(user.getUid())
                        .collection("tickets")
                        .document(eventId),
                ticket,
                SetOptions.merge()
        );

        batch.commit()
                .addOnSuccessListener(unused -> {
                    progress.setVisibility(View.GONE);
                    Toast.makeText(this, R.string.joined, Toast.LENGTH_SHORT).show();
                    configureActions();
                })
                .addOnFailureListener(e -> {
                    progress.setVisibility(View.GONE);
                    Toast.makeText(this, "Failed to join: " + e.getMessage(), Toast.LENGTH_LONG).show();
                });
    }

    private void confirmDelete() {
        new AlertDialog.Builder(this)
                .setTitle(R.string.delete_event)
                .setMessage(R.string.delete_event_confirm)
                .setNegativeButton(android.R.string.cancel, null)
                .setPositiveButton(R.string.delete_event, this::deleteEvent)
                .show();
    }

    private void deleteEvent(DialogInterface dialogInterface, int which) {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user == null) {
            Toast.makeText(this, "Please sign in again", Toast.LENGTH_SHORT).show();
            return;
        }

        progress.setVisibility(View.VISIBLE);
        WriteBatch batch = db.batch();
        DocumentReference eventRef = db.collection(EVENTS).document(eventId);
        batch.delete(eventRef);
        batch.delete(
                db.collection("users")
                        .document(user.getUid())
                        .collection("tickets")
                        .document(eventId)
        );

        batch.commit()
                .addOnSuccessListener(unused -> {
                    progress.setVisibility(View.GONE);
                    Toast.makeText(this, R.string.event_deleted, Toast.LENGTH_SHORT).show();
                    finish();
                })
                .addOnFailureListener(e -> {
                    progress.setVisibility(View.GONE);
                    Toast.makeText(this, "Failed to delete: " + e.getMessage(), Toast.LENGTH_LONG).show();
                });
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void bindImage(@Nullable String value, @NonNull ImageView view) {
        if (TextUtils.isEmpty(value)) {
            view.setVisibility(View.GONE);
            return;
        }
        view.setVisibility(View.VISIBLE);
        if (value.startsWith("data:image")) {
            int comma = value.indexOf(',');
            if (comma != -1 && comma + 1 < value.length()) {
                try {
                    byte[] bytes = Base64.decode(value.substring(comma + 1).replaceAll("\\s", ""), Base64.DEFAULT);
                    Glide.with(this).load(bytes).into(view);
                    return;
                } catch (IllegalArgumentException ignored) {
                    view.setVisibility(View.GONE);
                    return;
                }
            }
        }
        Glide.with(this).load(value).into(view);
    }
}
