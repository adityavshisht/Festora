package com.metabots.festora;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Patterns;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class SignupActivity extends AppCompatActivity {

    private FirebaseAuth auth;
    private EditText etEmail, etPassword, etConfirm;
    private Button btnSignup, btnGoogle; // may be null if not in layout
    private TextView tvLogin;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_signup);

        auth       = FirebaseAuth.getInstance();

        // Make sure these IDs match your activity_signup.xml
        etEmail    = findViewById(R.id.etEmail);
        etPassword = findViewById(R.id.etPassword);
        etConfirm  = findViewById(R.id.etConfirm);           // <- ensure this id exists
        btnSignup  = findViewById(R.id.btnDoSignup);         // <- ensure this id exists
        tvLogin    = findViewById(R.id.tvGoLogin);           // <- ensure this id exists

        // Optional Google button. If not present in layout, this stays null and is safely ignored.
        try {
            btnGoogle = findViewById(R.id.btnGoogle);        // <- only if you have it in XML
        } catch (Exception ignored) { btnGoogle = null; }

        btnSignup.setOnClickListener(v -> trySignup());

        if (btnGoogle != null) {
            btnGoogle.setOnClickListener(v ->
                    startActivity(new Intent(SignupActivity.this, GoogleSignInActivity.class)));
        }

        tvLogin.setOnClickListener(v ->
                startActivity(new Intent(SignupActivity.this, LoginActivity.class)));
        Button btnBackSignup = findViewById(R.id.btnBackSignup);
        btnBackSignup.setOnClickListener(v -> finish());

    }

    // IMPORTANT: do NOT auto-redirect in onStart() or you’ll bounce away from sign-up screen
    // Handle navigation only after successful account creation.

    private void trySignup() {
        final String email = etEmail.getText().toString().trim();
        final String pass  = etPassword.getText().toString();
        final String conf  = etConfirm.getText().toString();

        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            etEmail.setError("Enter a valid email");
            etEmail.requestFocus();
            return;
        }
        if (TextUtils.isEmpty(pass) || pass.length() < 6) {
            etPassword.setError("Password must be at least 6 characters");
            etPassword.requestFocus();
            return;
        }
        if (!pass.equals(conf)) {
            etConfirm.setError("Passwords do not match");
            etConfirm.requestFocus();
            return;
        }

        btnSignup.setEnabled(false);

        auth.createUserWithEmailAndPassword(email, pass)
                .addOnSuccessListener((AuthResult res) -> {
                    FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
                    String uid = (user != null) ? user.getUid() : null;
                    Toast.makeText(this, "Account created!", Toast.LENGTH_SHORT).show();
                    routeByTerms(uid);
                })
                .addOnFailureListener(e -> {
                    btnSignup.setEnabled(true);
                    String msg = (e.getMessage() == null || e.getMessage().trim().isEmpty())
                            ? "Sign up failed" : e.getMessage();
                    Toast.makeText(this, msg, Toast.LENGTH_LONG).show();
                });
    }

    /** Navigate based on whether THIS user accepted terms. */
    private void routeByTerms(String uid) {
        Intent next;
        if (TermsPrefs.hasAccepted(this, uid)) {
            next = new Intent(this, HomeActivity.class);
        } else {
            next = new Intent(this, TermsActivity.class)
                    .putExtra(TermsActivity.EXTRA_REDIRECT, "home")
                    .putExtra(TermsActivity.EXTRA_USER_ID, uid);
        }
        next.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(next);
        finish();
    }

}
