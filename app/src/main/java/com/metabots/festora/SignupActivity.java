package com.metabots.festora;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Patterns;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.material.button.MaterialButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.UserProfileChangeRequest;

public class SignupActivity extends AppCompatActivity {

    private FirebaseAuth auth;

    private EditText etName, etEmail, etPassword, etConfirm;
    private MaterialButton btnSignup, btnBack;
    private TextView tvGoLogin;
    private ProgressBar progress;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_signup);

        // Handle safe insets
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets bars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(bars.left, bars.top, bars.right, bars.bottom);
            return insets;
        });

        auth = FirebaseAuth.getInstance();

        // Bind views
        etName     = findViewById(R.id.etName);
        etEmail    = findViewById(R.id.etEmail);
        etPassword = findViewById(R.id.etPassword);
        etConfirm  = findViewById(R.id.etConfirm);
        btnSignup  = findViewById(R.id.btnDoSignup);
        btnBack    = findViewById(R.id.btnBackSignup);
        tvGoLogin  = findViewById(R.id.tvGoLogin);
        progress   = findViewById(R.id.progress);

        btnBack.setOnClickListener(v -> finish());
        tvGoLogin.setOnClickListener(v ->
                startActivity(new Intent(SignupActivity.this, LoginActivity.class)));

        btnSignup.setOnClickListener(v -> trySignup());
    }

    private void trySignup() {
        String name  = etName.getText().toString().trim();
        String email = etEmail.getText().toString().trim();
        String pass  = etPassword.getText().toString();
        String conf  = etConfirm.getText().toString();

        // Validation
        if (TextUtils.isEmpty(name)) { etName.setError("Enter name"); etName.requestFocus(); return; }
        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) { etEmail.setError("Enter a valid email"); etEmail.requestFocus(); return; }
        if (pass.length() < 6) { etPassword.setError("At least 6 characters"); etPassword.requestFocus(); return; }
        if (!pass.equals(conf)) { etConfirm.setError("Passwords do not match"); etConfirm.requestFocus(); return; }

        setLoading(true);

        auth.createUserWithEmailAndPassword(email, pass).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                if (auth.getCurrentUser() != null) {
                    // Save display name
                    auth.getCurrentUser().updateProfile(
                            new UserProfileChangeRequest.Builder()
                                    .setDisplayName(name)
                                    .build()
                    );
                    // Optional: send verification email
                    // auth.getCurrentUser().sendEmailVerification();
                }
                startActivity(new Intent(SignupActivity.this, HomeActivity.class));
                finish();
            } else {
                setLoading(false);
                String msg = (task.getException() != null)
                        ? task.getException().getMessage()
                        : "Signup failed";
                Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void setLoading(boolean loading) {
        btnSignup.setEnabled(!loading);
        etName.setEnabled(!loading);
        etEmail.setEnabled(!loading);
        etPassword.setEnabled(!loading);
        etConfirm.setEnabled(!loading);
        progress.setVisibility(loading ? android.view.View.VISIBLE : android.view.View.GONE);
    }
}
