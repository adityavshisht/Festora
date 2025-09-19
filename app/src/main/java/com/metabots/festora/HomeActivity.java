package com.metabots.festora;

import android.content.Intent;
import android.os.Bundle;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.material.button.MaterialButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.util.ArrayList;
import java.util.List;

public class HomeActivity extends AppCompatActivity {

    @Override
    public boolean onCreateOptionsMenu(android.view.Menu menu) {
        getMenuInflater().inflate(R.menu.home_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(android.view.MenuItem item) {
        if (item.getItemId() == R.id.action_profile) {
            // Open ProfileActivity
            startActivity(new Intent(this, ProfileActivity.class));
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_home);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.home_root), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Welcome text + logout


        /*btnLogout.setOnClickListener(v -> {
            FirebaseAuth.getInstance().signOut();
            GoogleSignInClient client = GoogleSignIn.getClient(
                    this, new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN).build());
            client.signOut();
            Intent i = new Intent(this, OptionActivity.class);
            i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(i);
            finish();
        });*/

        // 👉 RecyclerView with sample events
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

        // 👉 Toolbar
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        // 👉 Floating Action Button
        findViewById(R.id.fabCreate).setOnClickListener(v ->
                Toast.makeText(this, "Create new event", Toast.LENGTH_SHORT).show()
        );
    }
}
