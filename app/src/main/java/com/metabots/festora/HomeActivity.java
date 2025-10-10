package com.metabots.festora;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.KeyEvent;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import okhttp3.HttpUrl;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class HomeActivity extends AppCompatActivity {

    // Ticketmaster
    private static final String TM_BASE = "https://app.ticketmaster.com/discovery/v2/events.json";
    private static final String TM_API_KEY = "R3e5fsxMy5A86XgSoKHGBHGZPDu9ahNl";

    // Segments
    private static final String SEGMENT_MUSIC  = "KZFzniwnSyZfZ7v7nJ";
    private static final String SEGMENT_SPORTS = "KZFzniwnSyZfZ7v7nE";

    private static final String TAG = "Festora";

    private RecyclerView rv;
    private EventAdapter adapter;
    private final List<Event> data = new ArrayList<>();
    private final OkHttpClient http = new OkHttpClient();
    private final ExecutorService exec = Executors.newSingleThreadExecutor();

    private TextInputEditText etSearch;
    private TextInputLayout tilSearch;
    private TextView tvSection;

    // null = All; else: "Music" | "Sports" | "Tech" | "Networking"
    private String selectedTag = null;

    private void showBanner(String msg) {
        runOnUiThread(() ->
                Snackbar.make(findViewById(R.id.home_root), msg, Snackbar.LENGTH_SHORT).show()
        );
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_home);

        // ---- Gate: user must have accepted Terms (NO EXTRA_USER_ID reference) ----
        FirebaseUser u = FirebaseAuth.getInstance().getCurrentUser();
        String uid = (u != null) ? u.getUid() : null;
        if (!TermsPrefs.hasAccepted(this, uid)) {
            Intent terms = new Intent(this, TermsActivity.class)
                    .putExtra(TermsActivity.EXTRA_REDIRECT, "home");
            terms.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(terms);
            finish();
            return;
        }
        // -------------------------------------------------------------------------

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.home_root), (v, insets) -> {
            Insets bars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(bars.left, bars.top, bars.right, bars.bottom);
            return insets;
        });

        rv = findViewById(R.id.rvEvents);
        rv.setLayoutManager(new LinearLayoutManager(this));
        adapter = new EventAdapter(this, data, e -> showBanner("Open: " + e.title));
        rv.setAdapter(adapter);

        tvSection = findViewById(R.id.tvSection);

        findViewById(R.id.fabCreate).setOnClickListener(v -> showBanner("Create new event"));

        tilSearch = findViewById(R.id.tilSearch);
        etSearch = findViewById(R.id.etSearch);

        // --- Search submit wiring (IME search + Enter key + end icon) ---
        if (etSearch != null) {
            etSearch.setOnEditorActionListener((tv, actionId, keyEvent) -> {
                boolean isImeSearch = actionId == EditorInfo.IME_ACTION_SEARCH;
                boolean isImeNone   = actionId == EditorInfo.IME_NULL; // some keyboards use this
                if (isImeSearch || isImeNone) {
                    submitSearch();
                    return true;
                }
                return false;
            });

            etSearch.setOnKeyListener((v, keyCode, event) -> {
                if (keyCode == KeyEvent.KEYCODE_ENTER
                        && event != null
                        && event.getAction() == KeyEvent.ACTION_UP) {
                    submitSearch();
                    return true;
                }
                return false;
            });
        }
        if (tilSearch != null) {
            tilSearch.setEndIconOnClickListener(v -> submitSearch());
        }
        // -----------------------------------------------------------------

        // Chips (safe listeners only)
        ChipGroup chipGroup = findViewById(R.id.chipGroupCategories);
        Chip chipAll = findViewById(R.id.chipAll);
        Chip chipMusic = findViewById(R.id.chipMusic);
        Chip chipSports = findViewById(R.id.chipSports);
        Chip chipTech = findViewById(R.id.chipTech);
        Chip chipNetworking = findViewById(R.id.chipNetworking);

        chipGroup.setOnCheckedChangeListener((group, checkedId) -> {
            handleChipSelection(group.getCheckedChipId(), chipAll, chipMusic, chipSports, chipTech, chipNetworking);
        });

        chipAll.setOnClickListener(v -> { chipAll.setChecked(true);  handleChipSelection(chipAll.getId(), chipAll, chipMusic, chipSports, chipTech, chipNetworking); });
        chipMusic.setOnClickListener(v -> { chipMusic.setChecked(true); handleChipSelection(chipMusic.getId(), chipAll, chipMusic, chipSports, chipTech, chipNetworking); });
        chipSports.setOnClickListener(v -> { chipSports.setChecked(true); handleChipSelection(chipSports.getId(), chipAll, chipMusic, chipSports, chipTech, chipNetworking); });
        chipTech.setOnClickListener(v -> { chipTech.setChecked(true);   handleChipSelection(chipTech.getId(), chipAll, chipMusic, chipSports, chipTech, chipNetworking); });
        chipNetworking.setOnClickListener(v -> { chipNetworking.setChecked(true); handleChipSelection(chipNetworking.getId(), chipAll, chipMusic, chipSports, chipTech, chipNetworking); });

        // Initial load
        fetchEvents(null, selectedTag);
        showBanner("Home ready");
    }

    private void submitSearch() {
        String q = (etSearch != null && etSearch.getText() != null)
                ? etSearch.getText().toString().trim() : "";

        // Hide keyboard + clear focus
        try {
            InputMethodManager imm = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
            if (imm != null && etSearch != null) {
                imm.hideSoftInputFromWindow(etSearch.getWindowToken(), 0);
            }
        } catch (Exception ignored) {}
        if (etSearch != null) etSearch.clearFocus();

        Log.d(TAG, "Search submit: \"" + q + "\" tag=" + selectedTag);
        fetchEvents(q, selectedTag);
    }

    private void handleChipSelection(int checkedId,
                                     Chip chipAll, Chip chipMusic, Chip chipSports, Chip chipTech, Chip chipNetworking) {
        String prev = selectedTag;
        if (checkedId == chipAll.getId()) {
            selectedTag = null;
        } else if (checkedId == chipMusic.getId()) {
            selectedTag = "Music";
        } else if (checkedId == chipSports.getId()) {
            selectedTag = "Sports";
        } else if (checkedId == chipTech.getId()) {
            selectedTag = "Tech";
        } else if (checkedId == chipNetworking.getId()) {
            selectedTag = "Networking";
        } else {
            selectedTag = null;
        }

        showBanner("Filter: " + (selectedTag == null ? "All" : selectedTag));

        String q = (etSearch != null && etSearch.getText() != null)
                ? etSearch.getText().toString().trim() : "";
        if (!TextUtils.equals(prev, selectedTag)) {
            fetchEvents(q, selectedTag);
        }
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

    private void setSectionTitle(String keyword, String tag, boolean usedCity) {
        String where = usedCity ? "near Montreal" : "in Canada";
        String filter = (tag == null ? "All" : tag);
        if (!TextUtils.isEmpty(keyword)) {
            tvSection.setText("Results for \"" + keyword + "\" (" + filter + ", " + where + ")");
        } else {
            tvSection.setText((tag == null ? "Featured " : (filter + " ")) + where);
        }
    }

    private void fetchEvents(String keywordOrNull, String tag) {
        boolean useCity = (tag == null) && TextUtils.isEmpty(keywordOrNull);
        setSectionTitle(keywordOrNull, tag, useCity);

        exec.execute(() -> {
            try {
                List<Event> events = requestEvents(keywordOrNull, tag, useCity);
                if (events.isEmpty() && useCity) {
                    List<Event> widened = requestEvents(keywordOrNull, tag, false);
                    updateList(widened, tag);
                } else {
                    updateList(events, tag);
                }
            } catch (Exception e) {
                showBanner("Error: " + e.getMessage());
            }
        });
    }

    private List<Event> requestEvents(String keywordOrNull, String tag, boolean useCity) throws IOException, JSONException {
        HttpUrl.Builder url = HttpUrl.parse(TM_BASE).newBuilder()
                .addQueryParameter("size", "25")
                .addQueryParameter("countryCode", "CA")
                .addQueryParameter("sort", "date,asc")
                .addQueryParameter("apikey", TM_API_KEY);

        String kw = keywordOrNull == null ? "" : keywordOrNull.trim();

        if (tag == null) {
            if (!TextUtils.isEmpty(kw)) {
                url.addQueryParameter("keyword", kw);
            } else if (useCity) {
                url.addQueryParameter("city", "Montreal");
            }
        } else {
            switch (tag) {
                case "Music":
                    url.addQueryParameter("segmentId", SEGMENT_MUSIC);
                    break;
                case "Sports":
                    url.addQueryParameter("segmentId", SEGMENT_SPORTS);
                    break;
                case "Tech":
                    url.addQueryParameter("keyword",
                            (TextUtils.isEmpty(kw) ? "" : kw + " ")
                                    + "technology tech conference meetup expo summit");
                    break;
                case "Networking":
                    url.addQueryParameter("keyword",
                            (TextUtils.isEmpty(kw) ? "" : kw + " ")
                                    + "networking mixer meetup professional");
                    break;
            }
        }

        HttpUrl built = url.build();
        Log.d(TAG, "Request URL: " + built);

        Request req = new Request.Builder().url(built).get().build();
        try (Response res = http.newCall(req).execute()) {
            if (!res.isSuccessful() || res.body() == null) return new ArrayList<>();
            String json = res.body().string();
            return parseTicketmaster(json);
        }
    }

    private void updateList(List<Event> events, String tag) {
        runOnUiThread(() -> {
            data.clear();
            data.addAll(events);
            adapter.notifyDataSetChanged();
            String label = (tag == null ? "All" : tag);
            showBanner(label + ": " + events.size() + " events");
            if (events.isEmpty()) {
                showBanner("No events found.");
            }
        });
    }

    private List<Event> parseTicketmaster(String json) throws JSONException {
        List<Event> out = new ArrayList<>();
        JSONObject root = new JSONObject(json);
        if (!root.has("_embedded")) return out;

        JSONArray events = root.getJSONObject("_embedded").optJSONArray("events");
        if (events == null) return out;

        for (int i = 0; i < events.length(); i++) {
            JSONObject ev = events.getJSONObject(i);

            String id = ev.optString("id");
            String title = ev.optString("name");

            String dateText = "";
            JSONObject dates = ev.optJSONObject("dates");
            if (dates != null) {
                JSONObject start = dates.optJSONObject("start");
                if (start != null) {
                    String ldt = start.optString("localDateTime", "");
                    String ld = start.optString("localDate", "");
                    if (!TextUtils.isEmpty(ldt)) {
                        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
                            try {
                                LocalDateTime dt = LocalDateTime.parse(ldt);
                                dateText = dt.format(DateTimeFormatter.ofPattern("EEE, MMM d • h:mm a"));
                            } catch (Exception e) {
                                dateText = ldt;
                            }
                        } else {
                            dateText = ldt.replace("T", " ");
                        }
                    } else {
                        dateText = ld;
                    }
                }
            }

            String location = "";
            JSONObject embedded = ev.optJSONObject("_embedded");
            if (embedded != null) {
                JSONArray venues = embedded.optJSONArray("venues");
                if (venues != null && venues.length() > 0) {
                    JSONObject v0 = venues.optJSONObject(0);
                    if (v0 != null) {
                        String city = v0.optJSONObject("city") != null
                                ? v0.optJSONObject("city").optString("name", "")
                                : "";
                        String state = v0.optJSONObject("state") != null
                                ? v0.optJSONObject("state").optString("name", "")
                                : "";
                        String country = v0.optJSONObject("country") != null
                                ? v0.optJSONObject("country").optString("name", "")
                                : "";
                        StringBuilder sb = new StringBuilder();
                        if (!TextUtils.isEmpty(city)) sb.append(city);
                        if (!TextUtils.isEmpty(state)) {
                            if (sb.length() > 0) sb.append(", ");
                            sb.append(state);
                        }
                        if (!TextUtils.isEmpty(country)) {
                            if (sb.length() > 0) sb.append(", ");
                            sb.append(country);
                        }
                        location = sb.toString();
                    }
                }
            }

            String imageUrl = null;
            JSONArray images = ev.optJSONArray("images");
            if (images != null && images.length() > 0) {
                imageUrl = images.optJSONObject(0).optString("url", null);
            }

            out.add(new Event(id, title, dateText, location, imageUrl));
        }
        return out;
    }
}
