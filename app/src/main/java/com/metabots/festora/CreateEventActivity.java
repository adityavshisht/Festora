package com.metabots.festora;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.Toast;
import android.util.Log;

import androidx.activity.EdgeToEdge;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import com.google.android.material.datepicker.MaterialDatePicker;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.timepicker.MaterialTimePicker;
import com.google.android.material.timepicker.TimeFormat;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.SetOptions;
import com.google.firebase.firestore.WriteBatch;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import static com.metabots.festora.data.FirestoreContract.Collections.EVENTS;
import static com.metabots.festora.data.FirestoreContract.EventFields.*;

public class CreateEventActivity extends AppCompatActivity {

    private static final String TAG = "CreateEvent";

    private TextInputEditText etTitle, etLocation, etImageUrl, etDescription;
    private MaterialButton btnPickDate, btnPickTime, btnCreate;
    private ChipGroup chipGroup;
    private final Calendar when = Calendar.getInstance();

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_create_event);

        // Toolbar + back
        MaterialToolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        toolbar.setNavigationOnClickListener(v -> getOnBackPressedDispatcher().onBackPressed());

        // Views
        etTitle       = findViewById(R.id.etTitle);
        etLocation    = findViewById(R.id.etLocation);
        etImageUrl    = findViewById(R.id.etImageUrl);
        etDescription = findViewById(R.id.etDescription);
        chipGroup     = findViewById(R.id.chipGroupCategory);
        btnPickDate   = findViewById(R.id.btnPickDate);
        btnPickTime   = findViewById(R.id.btnPickTime);
        btnCreate     = findViewById(R.id.btnCreateEvent);

        updateDateLabel();
        updateTimeLabel();

        btnPickDate.setOnClickListener(v -> showDatePicker());
        btnPickTime.setOnClickListener(v -> showTimePicker());
        btnCreate.setOnClickListener(v -> tryCreate());
    }

    private void showDatePicker() {
        MaterialDatePicker<Long> dp = MaterialDatePicker.Builder
                .datePicker()
                .setTitleText("Select date")
                .setSelection(when.getTimeInMillis())
                .build();
        dp.addOnPositiveButtonClickListener(selection -> {
            when.setTimeInMillis(selection);
            updateDateLabel();
        });
        dp.show(getSupportFragmentManager(), "date");
    }

    private void showTimePicker() {
        MaterialTimePicker tp = new MaterialTimePicker.Builder()
                .setTimeFormat(TimeFormat.CLOCK_12H)
                .setHour(when.get(Calendar.HOUR_OF_DAY))
                .setMinute(when.get(Calendar.MINUTE))
                .setTitleText("Select time")
                .build();
        tp.addOnPositiveButtonClickListener(view -> {
            when.set(Calendar.HOUR_OF_DAY, tp.getHour());
            when.set(Calendar.MINUTE, tp.getMinute());
            when.set(Calendar.SECOND, 0);
            when.set(Calendar.MILLISECOND, 0);
            updateTimeLabel();
        });
        tp.show(getSupportFragmentManager(), "time");
    }

    private void updateDateLabel() {
        btnPickDate.setText(new SimpleDateFormat("EEE, MMM d", Locale.getDefault()).format(when.getTime()));
    }

    private void updateTimeLabel() {
        btnPickTime.setText(new SimpleDateFormat("h:mm a", Locale.getDefault()).format(when.getTime()));
    }

    private @Nullable String selectedCategory() {
        int id = chipGroup.getCheckedChipId();
        if (id == -1) return null;
        Chip c = findViewById(id);
        return c != null ? c.getText().toString() : null;
    }

    private void tryCreate() {
        String title       = safeText(etTitle);
        String location    = safeText(etLocation);
        String imageUrl    = safeText(etImageUrl);
        String description = safeText(etDescription);
        String category    = selectedCategory();

        if (TextUtils.isEmpty(title)) { etTitle.setError("Title is required"); etTitle.requestFocus(); return; }
        if (TextUtils.isEmpty(location)) { etLocation.setError("Location is required"); etLocation.requestFocus(); return; }

        Toast.makeText(this, "Creating…", Toast.LENGTH_SHORT).show();
        btnCreate.setEnabled(false);

        String dateText = new SimpleDateFormat("EEE, MMM d • h:mm a", Locale.getDefault())
                .format(when.getTime());

        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        String hostUid = user != null ? user.getUid() : null;
        String hostEmail = user != null ? user.getEmail() : null;

        String eventId = String.valueOf(System.currentTimeMillis());

        Map<String, Object> doc = new HashMap<>();
        doc.put(TITLE, title);
        doc.put(DATE_TEXT, dateText);
        doc.put(LOCATION, location);
        doc.put(IMAGE_URL, TextUtils.isEmpty(imageUrl) ? null : imageUrl);
        doc.put(CATEGORY, category);
        doc.put(DESCRIPTION, description);
        doc.put(HOST_UID, hostUid);
        doc.put(HOST_EMAIL, hostEmail);
        doc.put(CREATED_AT, FieldValue.serverTimestamp());

        Log.d(TAG, "Saving event " + eventId + " …");
        FirebaseFirestore.getInstance()
                .collection(EVENTS)
                .document(eventId)
                .set(doc)
                .addOnSuccessListener(unused -> {
                    Log.d(TAG, "Firestore: success");
                    Toast.makeText(CreateEventActivity.this, "Event created", Toast.LENGTH_SHORT).show();

                    enrollHostAsAttendee(eventId, doc, hostUid, hostEmail);

                    // Explicitly return to Home no matter where we came from
                    Intent home = new Intent(CreateEventActivity.this, HomeActivity.class);
                    home.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
                    startActivity(home);
                    finish();
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Firestore: failure", e);
                    btnCreate.setEnabled(true);
                    String msg = e != null && e.getMessage() != null ? e.getMessage() : "Failed to save";
                    Toast.makeText(CreateEventActivity.this, msg, Toast.LENGTH_LONG).show();
                })
                .addOnCompleteListener(task -> {
                    Log.d(TAG, "Firestore: complete (success=" + task.isSuccessful() + ")");
                });
    }

    private static String safeText(TextInputEditText t) {
        return t.getText() == null ? "" : t.getText().toString().trim();
    }

    private void enrollHostAsAttendee(String eventId, Map<String, Object> eventDoc, @Nullable String hostUid, @Nullable String hostEmail) {
        if (TextUtils.isEmpty(eventId) || hostUid == null) return;

        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user == null || !hostUid.equals(user.getUid())) return;

        FirebaseFirestore db = FirebaseFirestore.getInstance();
        WriteBatch batch = db.batch();

        Map<String, Object> userMeta = new HashMap<>();
        userMeta.put("lastJoinedAt", FieldValue.serverTimestamp());

        Map<String, Object> ticket = new HashMap<>();
        ticket.put("eventId", eventId);
        ticket.put("title", (String) eventDoc.get(TITLE));
        ticket.put("dateText", (String) eventDoc.get(DATE_TEXT));
        ticket.put("location", (String) eventDoc.get(LOCATION));
        ticket.put("imageUrl", eventDoc.get(IMAGE_URL));
        ticket.put("bookingUrl", null);
        ticket.put("attendeeName", user.getDisplayName());
        ticket.put("attendeeEmail", hostEmail);
        ticket.put("joinedAt", FieldValue.serverTimestamp());
        ticket.put("source", "firestore");
        ticket.put("ticketType", "host");
        ticket.put("hostUid", hostUid);
        ticket.put("hostEmail", hostEmail);
        if (eventDoc.get(CATEGORY) != null) ticket.put("category", eventDoc.get(CATEGORY));
        if (eventDoc.get(DESCRIPTION) != null) ticket.put("description", eventDoc.get(DESCRIPTION));

        batch.set(
                db.collection("users").document(hostUid),
                userMeta,
                SetOptions.merge()
        );
        batch.set(
                db.collection("users").document(hostUid)
                        .collection("tickets")
                        .document(eventId),
                ticket,
                SetOptions.merge()
        );

        batch.commit()
                .addOnFailureListener(e ->
                        Log.w(TAG, "Failed to enroll host as attendee: " + e.getMessage(), e)
                );
    }
}
