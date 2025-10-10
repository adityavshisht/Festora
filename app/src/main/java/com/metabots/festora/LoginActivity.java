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
import androidx.activity.OnBackPressedCallback;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class LoginActivity extends AppCompatActivity {

    private FirebaseAuth auth;
    private EditText etEmail, etPassword;
    private Button btnLogin, btnGoogle, btnBackLogin; // <-- include back
    private TextView tvSignup;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_login);

        etEmail      = findViewById(R.id.etEmail);
        etPassword   = findViewById(R.id.etPassword);
        btnLogin     = findViewById(R.id.btnLogin);
        btnGoogle    = findViewById(R.id.btnGoogle);
        btnBackLogin = findViewById(R.id.btnBackLogin); // <-- wire in onCreate
        tvSignup     = findViewById(R.id.tvSignup);

        auth = FirebaseAuth.getInstance();

        btnLogin.setOnClickListener(v -> tryLogin());
        btnGoogle.setOnClickListener(v ->
                startActivity(new Intent(LoginActivity.this, GoogleSignInActivity.class)));
        tvSignup.setOnClickListener(v ->
                startActivity(new Intent(LoginActivity.this, SignupActivity.class)));

        // Back button with fallback if this activity is task root
        btnBackLogin.setOnClickListener(v -> {
            if (isTaskRoot()) {
                startActivity(new Intent(LoginActivity.this, OptionActivity.class));
            }
            finish();
        });

        // Optional: make system back behave the same way
        getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override public void handleOnBackPressed() {
                if (isTaskRoot()) {
                    startActivity(new Intent(LoginActivity.this, OptionActivity.class));
                }
                finish();
            }
        });
    }

    @Override
    protected void onStart() {
        super.onStart();
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null) {
            String uid = user.getUid();
            if (!TermsPrefs.hasAccepted(this, uid)) {
                startActivity(new Intent(this, TermsActivity.class)
                        .putExtra(TermsActivity.EXTRA_REDIRECT, "home")
                        .putExtra(TermsActivity.EXTRA_USER_ID, uid));
            } else {
                startActivity(new Intent(this, HomeActivity.class));
            }
            finish();
        }
    }

    private void tryLogin() {
        final String email = etEmail.getText().toString().trim();
        final String pass  = etPassword.getText().toString();

        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            etEmail.setError("Enter a valid email");
            etEmail.requestFocus();
            return;
        }
        if (TextUtils.isEmpty(pass)) {
            etPassword.setError("Enter password");
            etPassword.requestFocus();
            return;
        }

        btnLogin.setEnabled(false);

        auth.signInWithEmailAndPassword(email, pass)
                .addOnSuccessListener(result -> {
                    FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
                    String uid = (user != null) ? user.getUid() : null;

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
                })
                .addOnFailureListener(e -> {
                    btnLogin.setEnabled(true);
                    String msg = e.getMessage();
                    if (msg == null || msg.trim().isEmpty()) msg = "Login failed";
                    Toast.makeText(this, msg, Toast.LENGTH_LONG).show();
                });
    }
}
