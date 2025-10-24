package com.metabots.festora;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Patterns;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.bumptech.glide.Glide;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.SetOptions;
import com.google.firebase.firestore.WriteBatch;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class JoinEventActivity extends AppCompatActivity {

    public static final String EXTRA_EVENT_ID = "extra_event_id";
    public static final String EXTRA_TITLE = "extra_title";
    public static final String EXTRA_DATE = "extra_date";
    public static final String EXTRA_LOCATION = "extra_location";
    public static final String EXTRA_IMAGE_URL = "extra_image_url";
    public static final String EXTRA_BOOKING_URL = "extra_booking_url";

    private TextInputEditText etName;
    private TextInputEditText etEmail;
    private TextInputEditText etCardNumber;
    private TextInputEditText etExpiry;
    private TextInputEditText etCvv;
    private MaterialButton btnJoin;
    private ProgressBar progress;

    private String eventId;
    private String title;
    private String dateText;
    private String location;
    private String imageUrl;
    private String bookingUrl;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_join_event);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        toolbar.setNavigationOnClickListener(v -> finish());

        Intent intent = getIntent();
        eventId = intent.getStringExtra(EXTRA_EVENT_ID);
        title = intent.getStringExtra(EXTRA_TITLE);
        dateText = intent.getStringExtra(EXTRA_DATE);
        location = intent.getStringExtra(EXTRA_LOCATION);
        imageUrl = intent.getStringExtra(EXTRA_IMAGE_URL);
        bookingUrl = intent.getStringExtra(EXTRA_BOOKING_URL);

        if (TextUtils.isEmpty(eventId)) {
            Toast.makeText(this, "Missing event information", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        TextView tvTitle = findViewById(R.id.txtSummaryTitle);
        TextView tvDate = findViewById(R.id.txtSummaryDate);
        TextView tvLocation = findViewById(R.id.txtSummaryLocation);
        ImageView imgPreview = findViewById(R.id.imgPreview);

        tvTitle.setText(!TextUtils.isEmpty(title) ? title : "—");
        tvDate.setText(!TextUtils.isEmpty(dateText) ? dateText : "Date TBA");
        tvLocation.setText(!TextUtils.isEmpty(location) ? location : "Location TBA");

        if (!TextUtils.isEmpty(imageUrl)) {
            imgPreview.setVisibility(View.VISIBLE);
            Glide.with(this).load(imageUrl).into(imgPreview);
        } else {
            imgPreview.setVisibility(View.GONE);
        }

        etName = findViewById(R.id.etName);
        etEmail = findViewById(R.id.etEmail);
        etCardNumber = findViewById(R.id.etCardNumber);
        etExpiry = findViewById(R.id.etExpiry);
        etCvv = findViewById(R.id.etCvv);
        btnJoin = findViewById(R.id.btnJoinEvent);
        progress = findViewById(R.id.progress);

        btnJoin.setOnClickListener(v -> attemptJoin());
    }

    private void attemptJoin() {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user == null) {
            Toast.makeText(this, "Please sign in again", Toast.LENGTH_SHORT).show();
            startActivity(new Intent(this, LoginActivity.class));
            finish();
            return;
        }

        String name = safeText(etName);
        String email = safeText(etEmail);
        String card = safeText(etCardNumber).replace(" ", "");
        String expiry = safeText(etExpiry);
        String cvv = safeText(etCvv);

        if (TextUtils.isEmpty(name)) {
            etName.setError("Enter your full name");
            etName.requestFocus();
            return;
        }
        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            etEmail.setError("Enter a valid email");
            etEmail.requestFocus();
            return;
        }
        if (card.length() < 12 || card.length() > 19) {
            etCardNumber.setError("Enter a valid card number");
            etCardNumber.requestFocus();
            return;
        }
        if (!expiry.matches("^(0[1-9]|1[0-2])/\\d{2}$")) {
            etExpiry.setError("Use MM/YY");
            etExpiry.requestFocus();
            return;
        }
        if (cvv.length() < 3 || cvv.length() > 4) {
            etCvv.setError("CVV must be 3-4 digits");
            etCvv.requestFocus();
            return;
        }

        setBusy(true);

        Map<String, Object> ticket = new HashMap<>();
        ticket.put("eventId", eventId);
        ticket.put("title", title);
        ticket.put("dateText", dateText);
        ticket.put("location", location);
        ticket.put("imageUrl", imageUrl);
        ticket.put("bookingUrl", bookingUrl);
        ticket.put("attendeeName", name);
        ticket.put("attendeeEmail", email);
        ticket.put("paymentLast4", card.substring(card.length() - 4));
        ticket.put("joinedAt", FieldValue.serverTimestamp());
        ticket.put("source", "ticketmaster");
        ticket.put("ticketType", "attendee");

        Timestamp ts = parseEventTimestamp(dateText);
        if (ts != null) {
            ticket.put("eventDate", ts);
        }

        FirebaseFirestore db = FirebaseFirestore.getInstance();
        String uid = user.getUid();

        WriteBatch batch = db.batch();
        Map<String, Object> userMeta = new HashMap<>();
        userMeta.put("lastJoinedAt", FieldValue.serverTimestamp());
        // ensure a placeholder user doc exists so rules evaluate predictably
        batch.set(
                db.collection("users").document(uid),
                userMeta,
                SetOptions.merge()
        );
        batch.set(
                db.collection("users").document(uid)
                        .collection("tickets")
                        .document(eventId),
                ticket
        );

        batch.commit()
                .addOnSuccessListener(unused -> {
                    Toast.makeText(this, "You joined this event!", Toast.LENGTH_SHORT).show();
                    finish();
                })
                .addOnFailureListener(e -> {
                    setBusy(false);
                    Toast.makeText(this, "Failed to join: " + e.getMessage(), Toast.LENGTH_LONG).show();
                })
                .addOnCompleteListener(task -> setBusy(false));
    }

    private void setBusy(boolean busy) {
        progress.setVisibility(busy ? View.VISIBLE : View.GONE);
        btnJoin.setEnabled(!busy);
    }

    private static String safeText(TextInputEditText editText) {
        return editText.getText() == null ? "" : editText.getText().toString().trim();
    }

    @Nullable
    private Timestamp parseEventTimestamp(@Nullable String date) {
        if (TextUtils.isEmpty(date)) return null;
        String[] patterns = {
                "EEE, MMM d • h:mm a",
                "EEEE, MMMM d • h:mm a",
                "yyyy-MM-dd'T'HH:mm:ss"
        };
        for (String pattern : patterns) {
            try {
                SimpleDateFormat sdf = new SimpleDateFormat(pattern, Locale.getDefault());
                sdf.setLenient(false);
                Date parsed = sdf.parse(date);
                if (parsed != null) {
                    return new Timestamp(parsed);
                }
            } catch (ParseException ignored) {
            }
        }
        return null;
    }
}
