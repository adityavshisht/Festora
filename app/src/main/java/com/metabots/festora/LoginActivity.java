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
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;

public class LoginActivity extends AppCompatActivity {

    private FirebaseAuth auth;
    private EditText etEmail, etPassword;
    private Button btnLogin, btnGoogle;
    private TextView tvSignup;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_login);

        etEmail    = findViewById(R.id.etEmail);
        etPassword = findViewById(R.id.etPassword);
        btnLogin   = findViewById(R.id.btnLogin);
        btnGoogle  = findViewById(R.id.btnGoogle);
        tvSignup   = findViewById(R.id.tvSignup);

        auth = FirebaseAuth.getInstance();

        btnLogin.setOnClickListener(v -> tryLogin());

        btnGoogle.setOnClickListener(v ->
                startActivity(new Intent(LoginActivity.this, GoogleSignInActivity.class)));

        tvSignup.setOnClickListener(v ->
                startActivity(new Intent(LoginActivity.this, SignupActivity.class)));
    }

    @Override
    protected void onStart() {
        super.onStart();
        if (FirebaseAuth.getInstance().getCurrentUser() != null) {
            if (!TermsPrefs.hasAccepted(this)) {
                startActivity(new Intent(this, TermsActivity.class)
                        .putExtra(TermsActivity.EXTRA_REDIRECT, "home"));
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
                    Toast.makeText(this, "Welcome!", Toast.LENGTH_SHORT).show();

                    boolean accepted = TermsPrefs.hasAccepted(this);
                    Intent next = new Intent(this, accepted ? HomeActivity.class : TermsActivity.class);
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
