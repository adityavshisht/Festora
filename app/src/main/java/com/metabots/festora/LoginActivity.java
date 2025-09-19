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

        // Bind views (MUST match activity_login.xml exactly)
        etEmail    = findViewById(R.id.etEmail);
        etPassword = findViewById(R.id.etPassword);
        btnLogin   = findViewById(R.id.btnLogin);
        btnGoogle  = findViewById(R.id.btnGoogle);
        tvSignup   = findViewById(R.id.tvSignup);

        auth = FirebaseAuth.getInstance();

        // Email/password login
        btnLogin.setOnClickListener(v -> tryLogin());

        // Google login placeholder
        btnGoogle.setOnClickListener(v ->
                Toast.makeText(this, "Google sign-in coming soon", Toast.LENGTH_SHORT).show());

        // “Not a user? Sign up”
        tvSignup.setOnClickListener(v ->
                startActivity(new Intent(LoginActivity.this, SignupActivity.class)));
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

        // If Firebase isn’t configured yet, UNCOMMENT the 3 lines below to verify
        // the navigation path works without crashing, then configure Firebase.
        // startActivity(new Intent(LoginActivity.this, HomeActivity.class));
        // finish();
        // return;

        btnLogin.setEnabled(false);

        auth.signInWithEmailAndPassword(email, pass)
                .addOnSuccessListener(result -> {
                    Toast.makeText(this, "Welcome!", Toast.LENGTH_SHORT).show();
                    startActivity(new Intent(LoginActivity.this, HomeActivity.class));
                    finish();
                })
                .addOnFailureListener(e -> {
                    btnLogin.setEnabled(true);
                    String msg = e.getMessage();
                    if (msg == null || msg.trim().isEmpty()) msg = "Login failed";
                    Toast.makeText(this, msg, Toast.LENGTH_LONG).show();
                });
    }

    @Override
    protected void onStart() {
        super.onStart();
        // Auto-skip login if already signed in
        if (FirebaseAuth.getInstance().getCurrentUser() != null) {
            startActivity(new Intent(this, HomeActivity.class));
            finish();
        }
    }
}
