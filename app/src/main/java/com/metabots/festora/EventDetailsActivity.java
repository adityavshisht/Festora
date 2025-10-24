package com.metabots.festora;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.SetOptions;
import com.google.firebase.firestore.WriteBatch;
import com.metabots.festora.databinding.ActivityEventDetailsBinding;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class EventDetailsActivity extends AppCompatActivity {
    public static final String EXTRA_ALREADY_JOINED = "extra_already_joined";

    private ActivityEventDetailsBinding binding;
    private final OkHttpClient client = new OkHttpClient();

    private String eventId;
    private String title;
    private String dateTime;
    private String location;
    private String imageUrl;
    private String bookingUrl;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityEventDetailsBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // Toolbar with back arrow
        setSupportActionBar(binding.toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
        binding.toolbar.setNavigationOnClickListener(v ->
                getOnBackPressedDispatcher().onBackPressed()
        );

        // Read extras from HomeActivity
        Intent intent = getIntent();
        eventId  = intent.getStringExtra("id");
        title    = intent.getStringExtra("title");
        dateTime = intent.getStringExtra("dateTime");
        location = intent.getStringExtra("location");
        imageUrl = intent.getStringExtra("imageUrl");
        String apiKey   = intent.getStringExtra("apiKey");
        boolean alreadyJoined = intent.getBooleanExtra(EXTRA_ALREADY_JOINED, false);
        String overrideBookingUrl = intent.getStringExtra("bookingUrl");
        if (!TextUtils.isEmpty(overrideBookingUrl)) {
            bookingUrl = overrideBookingUrl;
        }

        // Guard: required extras
        if (eventId == null || eventId.trim().isEmpty()) {
            Toast.makeText(this, "Missing event id", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }
        // Set initial values
        binding.txtTitle.setText(title != null ? title : "—");
        binding.txtDate.setText(dateTime != null ? dateTime : "Date TBA");
        binding.txtVenue.setText(location != null ? location : "Location TBA");
        binding.txtLocation.setText("");
        binding.txtAddress.setText("");
        if (imageUrl != null && !imageUrl.isEmpty()) {
            Glide.with(this).load(imageUrl).into(binding.heroImage);
        } else {
            binding.heroImage.setImageResource(R.drawable.logo1);
        }

        // Initially disable booking until data arrives
        binding.btnBook.setEnabled(false);
        if (alreadyJoined) {
            binding.btnJoin.setText(R.string.joined);
            binding.btnJoin.setEnabled(false);
            binding.btnJoin.setVisibility(View.VISIBLE);
        } else {
            binding.btnJoin.setText(R.string.join_and_pay);
            binding.btnJoin.setEnabled(true);
            binding.btnJoin.setVisibility(View.VISIBLE);
        }

        if (!TextUtils.isEmpty(bookingUrl)) {
            binding.btnBook.setEnabled(true);
            binding.btnBook.setOnClickListener(v -> {
                FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
                if (user != null) {
                    persistTicketForUser(user);
                }
                Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(bookingUrl));
                startActivity(browserIntent);
            });
        }

        // Share button (shares title and location)
        binding.btnShare.setOnClickListener(v -> {
            StringBuilder shareText = new StringBuilder();
            if (title != null && !title.isEmpty()) shareText.append(title);
            if (location != null && !location.isEmpty()) {
                if (shareText.length() > 0) shareText.append(" — ");
                shareText.append(location);
            }
            Intent shareIntent = new Intent(Intent.ACTION_SEND);
            shareIntent.setType("text/plain");
            shareIntent.putExtra(Intent.EXTRA_TEXT, shareText.toString());
            startActivity(Intent.createChooser(shareIntent, "Share event"));
        });

        // Add to calendar
        binding.btnCalendar.setOnClickListener(v -> {
            Intent calIntent = new Intent(Intent.ACTION_INSERT);
            calIntent.setData(android.provider.CalendarContract.Events.CONTENT_URI);
            calIntent.putExtra(android.provider.CalendarContract.Events.TITLE, title != null ? title : "");
            calIntent.putExtra(android.provider.CalendarContract.Events.EVENT_LOCATION, location != null ? location : "");
            startActivity(calIntent);
        });

        binding.btnJoin.setOnClickListener(v -> {
            FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
            if (user == null) {
                Toast.makeText(this, "Please sign in to join events", Toast.LENGTH_SHORT).show();
                startActivity(new Intent(this, LoginActivity.class));
                return;
            }

            Intent join = new Intent(this, JoinEventActivity.class);
            join.putExtra(JoinEventActivity.EXTRA_EVENT_ID, eventId);
            join.putExtra(JoinEventActivity.EXTRA_TITLE, title);
            join.putExtra(JoinEventActivity.EXTRA_DATE, dateTime);
            join.putExtra(JoinEventActivity.EXTRA_LOCATION, location);
            join.putExtra(JoinEventActivity.EXTRA_IMAGE_URL, imageUrl);
            join.putExtra(JoinEventActivity.EXTRA_BOOKING_URL, bookingUrl);
            startActivity(join);
        });

        // Fetch full details
        if (!TextUtils.isEmpty(apiKey)) {
            fetchEventDetails(eventId, apiKey);
        } else {
            binding.progress.setVisibility(View.GONE);
        }
    }

    private void persistTicketForUser(@NonNull FirebaseUser user) {
        if (TextUtils.isEmpty(eventId)) return;

        FirebaseFirestore db = FirebaseFirestore.getInstance();
        WriteBatch batch = db.batch();

        Map<String, Object> userMeta = new HashMap<>();
        userMeta.put("lastJoinedAt", FieldValue.serverTimestamp());

        Map<String, Object> ticket = new HashMap<>();
        ticket.put("eventId", eventId);
        ticket.put("title", title);
        ticket.put("dateText", dateTime);
        ticket.put("location", location);
        ticket.put("imageUrl", imageUrl);
        ticket.put("bookingUrl", bookingUrl);
        ticket.put("attendeeName", user.getDisplayName());
        ticket.put("attendeeEmail", user.getEmail());
        ticket.put("joinedAt", FieldValue.serverTimestamp());
        ticket.put("source", "ticketmaster");
        ticket.put("ticketType", "attendee");

        Date parsed = parseDate(dateTime);
        if (parsed != null) {
            ticket.put("eventDate", new com.google.firebase.Timestamp(parsed));
        }

        batch.set(db.collection("users").document(user.getUid()), userMeta, SetOptions.merge());
        batch.set(
                db.collection("users")
                        .document(user.getUid())
                        .collection("tickets")
                        .document(eventId),
                ticket,
                SetOptions.merge()
        );

        batch.commit();
    }

    private Date parseDate(@Nullable String value) {
        if (TextUtils.isEmpty(value)) return null;
        String[] patterns = {
                "EEE, MMM d • h:mm a",
                "EEEE, MMMM d • h:mm a",
                "yyyy-MM-dd'T'HH:mm:ss"
        };
        for (String pattern : patterns) {
            try {
                SimpleDateFormat sdf = new SimpleDateFormat(pattern, Locale.getDefault());
                sdf.setLenient(false);
                return sdf.parse(value);
            } catch (ParseException ignored) {
            }
        }
        return null;
    }

    private void fetchEventDetails(String id, String apiKey) {
        binding.progress.setVisibility(View.VISIBLE);
        String url = "https://app.ticketmaster.com/discovery/v2/events/" + id + ".json?apikey=" + apiKey;

        Request request = new Request.Builder().url(url).build();
        client.newCall(request).enqueue(new Callback() {
            @Override public void onFailure(Call call, IOException e) {
                runOnUiThread(() -> {
                    binding.progress.setVisibility(View.GONE);
                    Toast.makeText(EventDetailsActivity.this, "Failed to load details", Toast.LENGTH_SHORT).show();
                });
            }

            @Override public void onResponse(Call call, Response response) throws IOException {
                if (!response.isSuccessful() || response.body() == null) {
                    runOnUiThread(() -> {
                        binding.progress.setVisibility(View.GONE);
                        Toast.makeText(EventDetailsActivity.this, "Failed to load details", Toast.LENGTH_SHORT).show();
                    });
                    return;
                }

                String body = response.body().string();
                try {
                    JSONObject root = new JSONObject(body);

                    // Parse venue, city, country, address
                    JSONObject embedded = root.optJSONObject("_embedded");
                    String venueName = null;
                    String city = null;
                    String country = null;
                    String address = null;
                    if (embedded != null) {
                        JSONArray venues = embedded.optJSONArray("venues");
                        if (venues != null && venues.length() > 0) {
                            JSONObject v = venues.optJSONObject(0);
                            if (v != null) {
                                venueName = v.optString("name", null);
                                JSONObject cityObj = v.optJSONObject("city");
                                city = cityObj != null ? cityObj.optString("name", null) : null;
                                JSONObject countryObj = v.optJSONObject("country");
                                country = countryObj != null ? countryObj.optString("name", null) : null;
                                JSONObject addr = v.optJSONObject("address");
                                address = addr != null ? addr.optString("line1", null) : null;
                            }
                        }
                    }

                    // Price range
                    String priceRange = null;
                    JSONArray pr = root.optJSONArray("priceRanges");
                    if (pr != null && pr.length() > 0) {
                        JSONObject p0 = pr.optJSONObject(0);
                        if (p0 != null) {
                            boolean hasMin = p0.has("min");
                            boolean hasMax = p0.has("max");
                            Double minPrice = hasMin ? p0.optDouble("min") : null;
                            Double maxPrice = hasMax ? p0.optDouble("max") : null;
                            String currency = p0.optString("currency", null);
                            if (hasMin || hasMax) {
                                priceRange = (minPrice != null ? String.valueOf(minPrice) : "?") + " - " +
                                        (maxPrice != null ? String.valueOf(maxPrice) : "?") +
                                        (currency != null ? " " + currency : "");
                            }
                        }
                    }

                    // Notes/description
                    String note = root.optString("pleaseNote", null);

                    // Booking URL
                    String bookingUrlValue = root.optString("url", null);

                    // Hero image (choose first valid url)
                    String heroImage = null;
                    JSONArray imagesArr = root.optJSONArray("images");
                    if (imagesArr != null && imagesArr.length() > 0) {
                        for (int i = 0; i < imagesArr.length(); i++) {
                            JSONObject imgObj = imagesArr.optJSONObject(i);
                            if (imgObj != null) {
                                String u = imgObj.optString("url", null);
                                if (u != null && !u.isEmpty()) {
                                    heroImage = u;
                                    break;
                                }
                            }
                        }
                    }

                    // Seatmap image
                    String seatmapUrl = null;
                    JSONObject seatmap = root.optJSONObject("seatmap");
                    if (seatmap != null) {
                        seatmapUrl = seatmap.optString("staticUrl", null);
                    }

                    // Categories (segment + genre)
                    String genre = null;
                    String subGenre = null;
                    JSONArray classifications = root.optJSONArray("classifications");
                    if (classifications != null && classifications.length() > 0) {
                        JSONObject c0 = classifications.optJSONObject(0);
                        if (c0 != null) {
                            JSONObject seg = c0.optJSONObject("segment");
                            JSONObject gen = c0.optJSONObject("genre");
                            genre = seg != null ? seg.optString("name", null) : null;
                            subGenre = gen != null ? gen.optString("name", null) : null;
                        }
                    }

                    final String fVenueName = venueName;
                    final String fCity = city;
                    final String fCountry = country;
                    final String fAddress = address;
                    final String fPriceRange = priceRange;
                    final String fNote = note;
                    final String fBookingUrl = bookingUrlValue;
                    final String fHeroImage = heroImage;
                    final String fSeatmapUrl = seatmapUrl;
                    final String fGenre = genre;
                    final String fSubGenre = subGenre;

                    runOnUiThread(() -> {
                        binding.progress.setVisibility(View.GONE);

                        if (fVenueName != null) binding.txtVenue.setText(fVenueName);

                        if (fCity != null || fCountry != null) {
                            String loc = (fCity != null ? fCity : "") +
                                    ((fCity != null && fCountry != null) ? ", " : "") +
                                    (fCountry != null ? fCountry : "");
                            binding.txtLocation.setText(loc);
                        }

                        if (fAddress != null) binding.txtAddress.setText(fAddress);

                        if (fPriceRange != null) {
                            binding.txtPrice.setText(fPriceRange);
                        } else {
                            binding.txtPrice.setText("See provider");
                        }

                        if (fGenre != null || fSubGenre != null) {
                            String cats = (fGenre != null ? fGenre : "") +
                                    ((fGenre != null && fSubGenre != null) ? ", " : "") +
                                    (fSubGenre != null ? fSubGenre : "");
                            binding.txtCategories.setText(cats);
                        }

                        if (fNote != null && !fNote.isEmpty()) {
                            binding.txtDescription.setText(fNote);
                        }

                        if (fHeroImage != null && !fHeroImage.isEmpty()) {
                            Glide.with(EventDetailsActivity.this).load(fHeroImage).into(binding.heroImage);
                        }

                        if (fSeatmapUrl != null && !fSeatmapUrl.isEmpty()) {
                            binding.seatmap.setVisibility(View.VISIBLE);
                            Glide.with(EventDetailsActivity.this).load(fSeatmapUrl).into(binding.seatmap);
                        } else {
                            binding.seatmap.setVisibility(View.GONE);
                        }

                        EventDetailsActivity.this.bookingUrl = fBookingUrl;

                        if (fBookingUrl != null && !fBookingUrl.isEmpty()) {
                            binding.btnBook.setEnabled(true);
                            binding.btnBook.setOnClickListener(v -> {
                                Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(fBookingUrl));
                                startActivity(browserIntent);
                            });
                        } else {
                            binding.btnBook.setEnabled(false);
                        }
                    });

                } catch (Exception e) {
                    runOnUiThread(() -> {
                        binding.progress.setVisibility(View.GONE);
                        Toast.makeText(EventDetailsActivity.this, "Failed to parse details", Toast.LENGTH_SHORT).show();
                    });
                }
            }
        });
    }
}
