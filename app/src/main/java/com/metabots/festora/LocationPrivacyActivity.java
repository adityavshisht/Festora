package com.metabots.festora;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.materialswitch.MaterialSwitch;

public class LocationPrivacyActivity extends AppCompatActivity {

    private MaterialSwitch swAllowLocation;
    private MaterialButton btnOpenAppSettings;
    private MaterialButton btnOpenDeviceLocation;

    // Request COARSE + FINE (precise may be downgraded by user on Android 12+)
    private final ActivityResultLauncher<String[]> requestLocationPerms =
            registerForActivityResult(new ActivityResultContracts.RequestMultiplePermissions(), result -> {
                boolean coarse = Boolean.TRUE.equals(result.getOrDefault(Manifest.permission.ACCESS_COARSE_LOCATION, false));
                boolean fine   = Boolean.TRUE.equals(result.getOrDefault(Manifest.permission.ACCESS_FINE_LOCATION, false));
                boolean granted = coarse || fine;
                swAllowLocation.setChecked(granted);
                if (granted) {
                    Toast.makeText(this, "Location permission granted.", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(this, "Permission denied. You can enable it in Settings.", Toast.LENGTH_LONG).show();
                }
            });

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_location_privacy);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle("Privacy · Location");
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        swAllowLocation = findViewById(R.id.swAllowLocation);
        btnOpenAppSettings = findViewById(R.id.btnOpenAppSettings);
        btnOpenDeviceLocation = findViewById(R.id.btnOpenDeviceLocation);

        refreshSwitch();

        swAllowLocation.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                // Ask for permission if not already granted
                if (!hasLocationPermission()) {
                    requestLocationPerms.launch(new String[] {
                            Manifest.permission.ACCESS_COARSE_LOCATION,
                            Manifest.permission.ACCESS_FINE_LOCATION
                    });
                } else {
                    Toast.makeText(this, "Location already enabled.", Toast.LENGTH_SHORT).show();
                }
            } else {
                // Can’t revoke programmatically; guide user to Settings
                Toast.makeText(this, "Turn off location in App Settings.", Toast.LENGTH_LONG).show();
                openAppPermissionSettings();
                // Reflect actual state after returning
                swAllowLocation.postDelayed(this::refreshSwitch, 500);
            }
        });

        btnOpenAppSettings.setOnClickListener(v -> openAppPermissionSettings());
        btnOpenDeviceLocation.setOnClickListener(v -> openDeviceLocationSettings());
    }

    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return true;
    }

    private boolean hasLocationPermission() {
        int coarse = ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION);
        int fine   = ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION);
        return coarse == PackageManager.PERMISSION_GRANTED || fine == PackageManager.PERMISSION_GRANTED;
    }

    private void refreshSwitch() {
        swAllowLocation.setChecked(hasLocationPermission());
    }

    private void openAppPermissionSettings() {
        // Open this app’s settings screen
        Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
                .setData(Uri.parse("package:" + getPackageName()));
        startActivity(intent);
    }

    private void openDeviceLocationSettings() {
        // Open device-wide Location settings (to toggle GPS, etc.)
        Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
        startActivity(intent);
    }
}
