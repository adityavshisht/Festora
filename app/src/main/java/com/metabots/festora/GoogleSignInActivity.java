package com.metabots.festora;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;

public class GoogleSignInActivity extends AppCompatActivity {

    private static final int RC_SIGN_IN = 9001;

    private GoogleSignInClient googleClient;
    private FirebaseAuth auth;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_google_sign_in); // create a simple layout or reuse your login layout

        auth = FirebaseAuth.getInstance();

        // Configure Google Sign-In (replace default_web_client_id with yours from google-services.json)
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();
        googleClient = GoogleSignIn.getClient(this, gso);

        // Launch Google sign-in
        startActivityForResult(googleClient.getSignInIntent(), RC_SIGN_IN);
    }

    @Override
    @Deprecated
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == RC_SIGN_IN) {
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
            try {
                GoogleSignInAccount account = task.getResult(ApiException.class);
                if (account != null) {
                    firebaseAuthWithGoogle(account.getIdToken());
                } else {
                    Toast.makeText(this, "Google Sign-In failed", Toast.LENGTH_LONG).show();
                    finish();
                }
            } catch (ApiException e) {
                Toast.makeText(this, "Google Sign-In error: " + e.getMessage(), Toast.LENGTH_LONG).show();
                finish();
            }
        }
    }

    private void firebaseAuthWithGoogle(String idToken) {
        AuthCredential credential = GoogleAuthProvider.getCredential(idToken, null);
        auth.signInWithCredential(credential)
                .addOnSuccessListener(result -> routeAfterSignIn())
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Auth failed: " + e.getMessage(), Toast.LENGTH_LONG).show();
                    finish();
                });
    }

    /** Route based on per-user Terms acceptance. */
    private void routeAfterSignIn() {
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
    }
}
