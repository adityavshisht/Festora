package com.metabots.festora;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class OptionActivity extends AppCompatActivity {

    private View btnLogin, btnSignup, btnGoogle;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_option);

        View root = findViewById(R.id.option_root);
        if (root != null) {
            ViewCompat.setOnApplyWindowInsetsListener(root, (v, insets) -> {
                Insets bars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
                v.setPadding(bars.left, bars.top, bars.right, bars.bottom);
                return insets;
            });
        }

        btnLogin  = findViewById(R.id.btnLogin);
        btnSignup = findViewById(R.id.btnSignup);
        btnGoogle = findViewById(R.id.btnGoogle);

        if (btnLogin != null) {
            btnLogin.setOnClickListener(v ->
                    startActivity(new Intent(OptionActivity.this, LoginActivity.class)));
        }
        if (btnSignup != null) {
            btnSignup.setOnClickListener(v ->
                    startActivity(new Intent(OptionActivity.this, SignupActivity.class)));
        }
        if (btnGoogle != null) {
            btnGoogle.setOnClickListener(v ->
                    startActivity(new Intent(OptionActivity.this, GoogleSignInActivity.class)));
        }
    }

    @Override
    protected void onStart() {
        super.onStart();

        boolean signedIn = com.google.firebase.auth.FirebaseAuth.getInstance().getCurrentUser() != null;

        if (!signedIn) {
            // Stay on OptionActivity (login / signup choices)
            return;
        }

        boolean accepted = TermsPrefs.hasAccepted(this);

        if (!accepted) {
            // Force Terms first, block back to this screen
            startActivity(new Intent(this, TermsActivity.class)
                    .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK));
            finish();
        } else {
            // Signed in and accepted → Home
            startActivity(new Intent(this, HomeActivity.class)
                    .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK));
            finish();
        }
    }

}
