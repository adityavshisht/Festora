package com.metabots.festora;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class MainActivity extends AppCompatActivity {

    private static final long SPLASH_DELAY_MS = 2500L; // 2.5s splash

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main); // root view must have id @+id/main

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        new Handler(Looper.getMainLooper()).postDelayed(() -> {
                    FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
                    Intent next;
                    if (user == null) {
                        next = new Intent(MainActivity.this, OptionActivity.class);
                    } else {
                        String uid = user.getUid();
                        if (TermsPrefs.hasAccepted(MainActivity.this, uid)) {
                            next = new Intent(MainActivity.this, HomeActivity.class);
                        } else {
                            next = new Intent(MainActivity.this, TermsActivity.class)
                                    .putExtra(TermsActivity.EXTRA_REDIRECT, "home")
                                    .putExtra(TermsActivity.EXTRA_USER_ID, uid);
                        }
                    }
                    next.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    startActivity(next);
                    finish();



            // Clear back stack so splash/auth screens aren’t reachable via back
            next.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK
                    | Intent.FLAG_ACTIVITY_CLEAR_TASK
                    | Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(next);
            finish();
        }, SPLASH_DELAY_MS);
    }
}
