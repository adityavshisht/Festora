package com.metabots.festora;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.MenuItem;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.material.card.MaterialCardView;
import com.google.android.material.button.MaterialButton;
import com.google.firebase.auth.FirebaseAuth;

public class ProfileActivity extends AppCompatActivity {

    private GoogleSignInClient googleClient;
    private static final String AUTH_PREFS = "auth_prefs";
    private static final String KEY_LOGGED_IN = "logged_in";

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

        MaterialCardView rowPersonal = findViewById(R.id.rowPersonal);
        rowPersonal.setOnClickListener(v ->
                startActivity(new Intent(ProfileActivity.this, PersonalInfoActivity.class)));

        MaterialCardView rowEvents = findViewById(R.id.rowEvents);
        rowEvents.setOnClickListener(v ->
                startActivity(new Intent(ProfileActivity.this, MyEventsProfileActivity.class)));

        MaterialCardView rowNotifications = findViewById(R.id.rowNotifications);
        rowNotifications.setOnClickListener(v ->
                startActivity(new Intent(ProfileActivity.this, NotificationsActivity.class)));

        MaterialCardView rowPrivacy = findViewById(R.id.rowPrivacy);
        rowPrivacy.setOnClickListener(v ->
                startActivity(new Intent(ProfileActivity.this, LocationPrivacyActivity.class)));

        MaterialCardView rowHelpSupport = findViewById(R.id.rowHelpSupport);
        rowHelpSupport.setOnClickListener(v ->
                startActivity(new Intent(ProfileActivity.this, HelpSupportActivity.class)));

        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN).build();
        googleClient = GoogleSignIn.getClient(this, gso);

        MaterialButton btnLogout = findViewById(R.id.btnLogout);
        btnLogout.setOnClickListener(v -> performLogout());
    }

    private void performLogout() {
        FirebaseAuth.getInstance().signOut();

        if (googleClient != null) {
            googleClient.signOut();
        }

        SharedPreferences prefs = getSharedPreferences(AUTH_PREFS, MODE_PRIVATE);
        prefs.edit().putBoolean(KEY_LOGGED_IN, false).apply();

        Intent i = new Intent(ProfileActivity.this, OptionActivity.class);
        i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(i);
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
