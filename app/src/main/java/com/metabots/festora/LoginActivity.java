package com.metabots.festora;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Patterns;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.material.button.MaterialButton;
import com.google.firebase.auth.FirebaseAuth;

public class LoginActivity extends AppCompatActivity {
    private FirebaseAuth auth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_login);

        // Safe insets
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets bars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(bars.left, bars.top, bars.right, bars.bottom);
            return insets;
        });

        auth = FirebaseAuth.getInstance();

        // Views
        EditText etEmail    = findViewById(R.id.etEmail);
        EditText etPassword = findViewById(R.id.etPassword);
        MaterialButton btnLogin = findViewById(R.id.btnDoLogin);
        MaterialButton btnBack  = findViewById(R.id.btnBackLogin);
        TextView tvForgotPassword = findViewById(R.id.tvForgotPassword);
        TextView tvGoSignup       = findViewById(R.id.tvGoSignup);

        // Back → OptionActivity
        btnBack.setOnClickListener(v -> finish());

        // Go to Signup
        tvGoSignup.setOnClickListener(v ->
                startActivity(new Intent(this, SignupActivity.class)));

        // Login with Firebase
        btnLogin.setOnClickListener(v -> {
            String email = etEmail.getText().toString().trim();
            String pass  = etPassword.getText().toString();

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
            etEmail.setEnabled(false);
            etPassword.setEnabled(false);

            auth.signInWithEmailAndPassword(email, pass).addOnCompleteListener(task -> {
                btnLogin.setEnabled(true);
                etEmail.setEnabled(true);
                etPassword.setEnabled(true);

                if (task.isSuccessful()) {
                    startActivity(new Intent(this, HomeActivity.class));
                    finish();
                } else {
                    String msg = (task.getException() != null)
                            ? task.getException().getMessage() : "Login failed";
                    Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
                }
            });
        });

        // Forgot Password
        tvForgotPassword.setOnClickListener(v -> {
            String email = etEmail.getText().toString().trim();
            if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                etEmail.setError("Enter your email first");
                etEmail.requestFocus();
                return;
            }
            auth.sendPasswordResetEmail(email).addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    Toast.makeText(this, "Reset link sent. Check your email.", Toast.LENGTH_SHORT).show();
                } else {
                    String msg = (task.getException() != null)
                            ? task.getException().getMessage() : "Failed to send reset email";
                    Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
                }
            });
        });
    }

    @Override
    protected void onStart() {
        super.onStart();
        if (FirebaseAuth.getInstance().getCurrentUser() != null) {
            startActivity(new Intent(this, HomeActivity.class));
            finish();
        }
    }
}
