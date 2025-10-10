package com.metabots.festora;

import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

/** Entry point. Routes to Terms if not accepted for THIS user, else Home.
 *  If no user is signed in, sends to your auth/options screen. */
public class LaunchGateActivity extends AppCompatActivity {

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        Intent next;

        if (user == null) {
            // Not signed in yet → go to your login/option screen
            next = new Intent(this, OptionActivity.class);
        } else {
            String uid = user.getUid();
            if (TermsPrefs.hasAccepted(this, uid)) {
                next = new Intent(this, HomeActivity.class);
            } else {
                next = new Intent(this, TermsActivity.class)
                        .putExtra(TermsActivity.EXTRA_REDIRECT, "home")
                        .putExtra(TermsActivity.EXTRA_USER_ID, uid);
            }
        }

        next.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK
                | Intent.FLAG_ACTIVITY_CLEAR_TASK
                | Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(next);
        finish();
    }
}
