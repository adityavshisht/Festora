package com.metabots.festora;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.MenuItem;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.EmailAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.FirebaseAuthRecentLoginRequiredException;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserInfo;
import com.google.firebase.auth.UserProfileChangeRequest;

public class PersonalInfoActivity extends AppCompatActivity {

    private FirebaseAuth auth;
    private FirebaseUser user;

    private TextInputEditText etName, etEmail;
    private TextInputEditText etCurrentPassword, etNewPassword, etConfirmPassword;
    private MaterialButton btnSaveProfile, btnChangePassword;
    private ProgressBar progress;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_personal_info);

        auth = FirebaseAuth.getInstance();
        user = auth.getCurrentUser();

        // Toolbar
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle("Personal Information");
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        // Views
        etName = findViewById(R.id.etName);
        etEmail = findViewById(R.id.etEmail);
        etCurrentPassword = findViewById(R.id.etCurrentPassword);
        etNewPassword = findViewById(R.id.etNewPassword);
        etConfirmPassword = findViewById(R.id.etConfirmPassword);
        btnSaveProfile = findViewById(R.id.btnSaveProfile);
        btnChangePassword = findViewById(R.id.btnChangePassword);
        progress = findViewById(R.id.progress);

        // Prefill user info
        if (user != null) {
            if (!TextUtils.isEmpty(user.getDisplayName())) {
                etName.setText(user.getDisplayName());
            }
            if (!TextUtils.isEmpty(user.getEmail())) {
                etEmail.setText(user.getEmail());
            }
        }

        // Show password section only if provider is "password"
        boolean isPasswordProvider = false;
        if (user != null) {
            for (UserInfo info : user.getProviderData()) {
                if ("password".equals(info.getProviderId())) {
                    isPasswordProvider = true;
                    break;
                }
            }
        }
        findViewById(R.id.groupPasswordSection).setVisibility(isPasswordProvider ? View.VISIBLE : View.GONE);

        // Listeners
        btnSaveProfile.setOnClickListener(v -> saveProfile());
        btnChangePassword.setOnClickListener(v -> changePassword());
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void setBusy(boolean busy) {
        progress.setVisibility(busy ? View.VISIBLE : View.GONE);
        btnSaveProfile.setEnabled(!busy);
        btnChangePassword.setEnabled(!busy);
    }

    private void saveProfile() {
        if (user == null) {
            Toast.makeText(this, "Not signed in.", Toast.LENGTH_SHORT).show();
            return;
        }

        final String name = String.valueOf(etName.getText()).trim();
        final String newEmail = String.valueOf(etEmail.getText()).trim();
        final String currentEmail = user.getEmail();

        if (TextUtils.isEmpty(name) && TextUtils.isEmpty(newEmail)) {
            Toast.makeText(this, "Nothing to update.", Toast.LENGTH_SHORT).show();
            return;
        }

        setBusy(true);

        Runnable onNameUpdated = () -> {
            if (!TextUtils.isEmpty(newEmail) && !TextUtils.equals(newEmail, currentEmail)) {
                String currentPwd = String.valueOf(etCurrentPassword.getText());
                if (TextUtils.isEmpty(currentPwd)) {
                    setBusy(false);
                    etCurrentPassword.setError("Enter current password to change email");
                    etCurrentPassword.requestFocus();
                    return;
                }
                user.reauthenticate(EmailAuthProvider.getCredential(currentEmail, currentPwd))
                        .addOnCompleteListener(reauthTask -> {
                            if (!reauthTask.isSuccessful()) {
                                setBusy(false);
                                etCurrentPassword.setError("Current password is incorrect");
                                etCurrentPassword.requestFocus();
                                return;
                            }
                            user.updateEmail(newEmail).addOnCompleteListener(updateTask -> {
                                setBusy(false);
                                if (updateTask.isSuccessful()) {
                                    Toast.makeText(this, "Email updated.", Toast.LENGTH_SHORT).show();
                                } else {
                                    Toast.makeText(this, "Failed to update email.", Toast.LENGTH_LONG).show();
                                }
                            });
                        });
            } else {
                setBusy(false);
                Toast.makeText(this, "Profile updated.", Toast.LENGTH_SHORT).show();
            }
        };

        if (!TextUtils.isEmpty(name) && !TextUtils.equals(name, user.getDisplayName())) {
            UserProfileChangeRequest req = new UserProfileChangeRequest.Builder()
                    .setDisplayName(name)
                    .build();
            user.updateProfile(req).addOnCompleteListener(task -> {
                if (!task.isSuccessful()) {
                    setBusy(false);
                    Toast.makeText(this, "Failed to update name.", Toast.LENGTH_LONG).show();
                } else {
                    onNameUpdated.run();
                }
            });
        } else {
            onNameUpdated.run();
        }
    }

    private void changePassword() {
        if (user == null) {
            Toast.makeText(this, "Not signed in.", Toast.LENGTH_SHORT).show();
            return;
        }

        String currentPwd = String.valueOf(etCurrentPassword.getText());
        String newPwd = String.valueOf(etNewPassword.getText());
        String confirmPwd = String.valueOf(etConfirmPassword.getText());

        if (TextUtils.isEmpty(currentPwd)) {
            etCurrentPassword.setError("Enter current password");
            etCurrentPassword.requestFocus();
            return;
        }
        if (TextUtils.isEmpty(newPwd)) {
            etNewPassword.setError("Enter new password");
            etNewPassword.requestFocus();
            return;
        }
        if (newPwd.length() < 6) {
            etNewPassword.setError("Password must be at least 6 characters");
            etNewPassword.requestFocus();
            return;
        }
        if (newPwd.equals(currentPwd)) {
            etNewPassword.setError("New password must be different from current");
            etNewPassword.requestFocus();
            return;
        }
        if (!newPwd.equals(confirmPwd)) {
            etConfirmPassword.setError("Passwords do not match");
            etConfirmPassword.requestFocus();
            return;
        }

        setBusy(true);
        String email = user.getEmail();
        if (TextUtils.isEmpty(email)) {
            setBusy(false);
            Toast.makeText(this, "Email not available for re-auth.", Toast.LENGTH_LONG).show();
            return;
        }

        user.reauthenticate(EmailAuthProvider.getCredential(email, currentPwd))
                .addOnCompleteListener(reauthTask -> {
                    if (!reauthTask.isSuccessful()) {
                        setBusy(false);
                        etCurrentPassword.setError("Current password is incorrect");
                        etCurrentPassword.requestFocus();
                        return;
                    }
                    user.updatePassword(newPwd).addOnCompleteListener(updateTask -> {
                        setBusy(false);
                        if (updateTask.isSuccessful()) {
                            Toast.makeText(this, "Password changed.", Toast.LENGTH_SHORT).show();
                            etNewPassword.setText("");
                            etConfirmPassword.setText("");
                        } else {
                            Toast.makeText(this, "Failed to change password.", Toast.LENGTH_LONG).show();
                        }
                    });
                });
    }
}
