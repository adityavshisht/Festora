package com.metabots.festora;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.widget.CompoundButton;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.NotificationManagerCompat;
import androidx.core.content.ContextCompat;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.materialswitch.MaterialSwitch;

public class NotificationsActivity extends AppCompatActivity {

    private MaterialSwitch swAllow;
    private MaterialButton btnOpenSystem;

    // Request POST_NOTIFICATIONS on Android 13+
    private final ActivityResultLauncher<String> requestNotifPermission =
            registerForActivityResult(new ActivityResultContracts.RequestPermission(), granted -> {
                if (granted) {
                    Toast.makeText(this, "Notifications permitted.", Toast.LENGTH_SHORT).show();
                    refreshSwitchFromSystem();
                } else {
                    Toast.makeText(this, "Permission denied. Open system settings to enable.", Toast.LENGTH_LONG).show();
                    swAllow.setChecked(false);
                }
            });

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_notifications);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle("Notifications");
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        swAllow = findViewById(R.id.swAllowNotifications);
        btnOpenSystem = findViewById(R.id.btnOpenSystemSettings);

        // Initial state mirrors whether system allows notifications for this app
        refreshSwitchFromSystem();

        swAllow.setOnCheckedChangeListener((CompoundButton buttonView, boolean isChecked) -> {
            // We can't actually toggle system notifications programmatically.
            // If user turns ON:
            if (isChecked) {
                if (Build.VERSION.SDK_INT >= 33) {
                    // Check runtime permission on Android 13+
                    if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS)
                            != PackageManager.PERMISSION_GRANTED) {
                        requestNotifPermission.launch(Manifest.permission.POST_NOTIFICATIONS);
                        return;
                    }
                }
                // If permission granted (or below 33), but system notifications are still disabled
                if (!NotificationManagerCompat.from(this).areNotificationsEnabled()) {
                    Toast.makeText(this, "Notifications are disabled in system settings.", Toast.LENGTH_LONG).show();
                    openAppNotificationSettings();
                    // Keep switch reflecting actual system state
                    swAllow.setChecked(false);
                } else {
                    Toast.makeText(this, "Notifications are ON.", Toast.LENGTH_SHORT).show();
                }
            } else {
                // User turned OFF: guide them to system settings to actually disable
                Toast.makeText(this, "To turn off notifications, use system settings.", Toast.LENGTH_LONG).show();
                openAppNotificationSettings();
                // Reflect real system state after returning
                swAllow.postDelayed(this::refreshSwitchFromSystem, 500);
            }
        });

        btnOpenSystem.setOnClickListener(v -> openAppNotificationSettings());
    }

    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return true;
    }

    private void refreshSwitchFromSystem() {
        boolean enabled = NotificationManagerCompat.from(this).areNotificationsEnabled();
        swAllow.setChecked(enabled);
    }

    private void openAppNotificationSettings() {
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                Intent intent = new Intent(Settings.ACTION_APP_NOTIFICATION_SETTINGS)
                        .putExtra(Settings.EXTRA_APP_PACKAGE, getPackageName());
                startActivity(intent);
            } else {
                Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
                        .setData(Uri.parse("package:" + getPackageName()));
                startActivity(intent);
            }
        } catch (Exception e) {
            // Fallback
            Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
                    .setData(Uri.parse("package:" + getPackageName()));
            startActivity(intent);
        }
    }
}
