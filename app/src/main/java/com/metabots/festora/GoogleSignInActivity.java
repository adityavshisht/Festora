package com.metabots.festora;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.GoogleAuthProvider;

public class GoogleSignInActivity extends AppCompatActivity {

    private GoogleSignInClient googleClient;
    private FirebaseAuth auth;

    private final ActivityResultLauncher<Intent> signInLauncher =
            registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
                if (result.getResultCode() != RESULT_OK || result.getData() == null) {
                    Toast.makeText(this, "Google sign-in canceled", Toast.LENGTH_SHORT).show();
                    finish();
                    return;
                }
                try {
                    GoogleSignInAccount account = GoogleSignIn.getSignedInAccountFromIntent(result.getData())
                            .getResult(ApiException.class);

                    if (account == null) {
                        Toast.makeText(this, "No Google account selected.", Toast.LENGTH_SHORT).show();
                        finish();
                        return;
                    }

                    String idToken = account.getIdToken();
                    AuthCredential credential = GoogleAuthProvider.getCredential(idToken, null);

                    auth.signInWithCredential(credential).addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            boolean accepted = TermsPrefs.hasAccepted(this);
                            Intent next = new Intent(this, accepted ? HomeActivity.class : TermsActivity.class);
                            next.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                            startActivity(next);
                            finish();
                        } else {
                            String msg = (task.getException() != null)
                                    ? task.getException().getMessage()
                                    : "Firebase auth failed";
                            Toast.makeText(this, msg, Toast.LENGTH_LONG).show();
                            finish();
                        }
                    });

                } catch (ApiException e) {
                    Toast.makeText(this, "Google sign-in failed: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    finish();
                }
            });

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_google_sign_in);

        auth = FirebaseAuth.getInstance();

        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id)) // from google-services.json config
                .requestEmail()
                .build();

        googleClient = GoogleSignIn.getClient(this, gso);
    }

    @Override
    protected void onStart() {
        super.onStart();

        if (FirebaseAuth.getInstance().getCurrentUser() != null) {
            if (!TermsPrefs.hasAccepted(this)) {
                startActivity(new Intent(this, TermsActivity.class));
            } else {
                startActivity(new Intent(this, HomeActivity.class));
            }
            finish();
            return;
        }

        // Always show account chooser
        googleClient.signOut().addOnCompleteListener(t ->
                signInLauncher.launch(googleClient.getSignInIntent()));
    }
}
