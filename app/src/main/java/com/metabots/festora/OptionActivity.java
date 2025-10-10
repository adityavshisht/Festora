package com.metabots.festora;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;

import androidx.activity.EdgeToEdge;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class OptionActivity extends AppCompatActivity {

    private Button btnLogin, btnSignup, btnGoogle;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_option);

        btnLogin  = findViewById(R.id.btnLogin);
        btnSignup = findViewById(R.id.btnSignup);
        btnGoogle = findViewById(R.id.btnGoogle);

        btnLogin.setOnClickListener(v ->
                startActivity(new Intent(this, LoginActivity.class)));

        btnSignup.setOnClickListener(v ->
                startActivity(new Intent(this, SignupActivity.class)));

        btnGoogle.setOnClickListener(v ->
                startActivity(new Intent(this, GoogleSignInActivity.class)));
    }

    @Override
    protected void onStart() {
        super.onStart();
        // If a user is already signed in, route based on per-user Terms acceptance.
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null) {
            String uid = user.getUid();

            Intent next;
            if (TermsPrefs.hasAccepted(this, uid)) {
                next = new Intent(this, HomeActivity.class);
            } else {
                next = new Intent(this, TermsActivity.class)
                        .putExtra(TermsActivity.EXTRA_REDIRECT, "home")
                        .putExtra(TermsActivity.EXTRA_USER_ID, uid);
            }

            next.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK
                    | Intent.FLAG_ACTIVITY_CLEAR_TASK
                    | Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(next);
            finish();
        }
        // else: no user -> stay on this screen and let them pick login/signup
    }
}
