package com.metabots.festora;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.MenuItem;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.google.android.material.button.MaterialButton;

public class HelpSupportActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_help_support);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle("Help & Support");
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        MaterialButton btnEmail = findViewById(R.id.btnEmailSupport);
        btnEmail.setOnClickListener(v -> {
            Intent email = new Intent(Intent.ACTION_SENDTO);
            email.setData(Uri.parse("mailto:"));
            email.putExtra(Intent.EXTRA_EMAIL, new String[]{"support@festora.com"});
            email.putExtra(Intent.EXTRA_SUBJECT, "Festora Support");
            email.putExtra(Intent.EXTRA_TEXT, "Describe your issue here…");
            startActivity(Intent.createChooser(email, "Contact Support"));
        });
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
