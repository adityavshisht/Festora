package com.metabots.festora;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

public class TermsActivity extends AppCompatActivity {
    public static final String EXTRA_REDIRECT = "redirect";

    private Button btnAccept, btnDecline;
    private CheckBox chkAgree;
    private boolean scrolledToBottom = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_terms);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle("Terms & Conditions");
        }

        btnAccept  = findViewById(R.id.btnAcceptTerms);
        btnDecline = findViewById(R.id.btnDeclineTerms);
        chkAgree   = findViewById(R.id.chkAgree);

        TextView tvTerms = findViewById(R.id.tvTermsBody);
        tvTerms.setOnScrollChangeListener((v, sx, sy, ox, oy) -> {
            int scrollY = tvTerms.getScrollY();
            int max = tvTerms.getLayout() == null ? 0 :
                    tvTerms.getLayout().getHeight() + tvTerms.getTotalPaddingTop()
                            + tvTerms.getTotalPaddingBottom() - tvTerms.getHeight();
            scrolledToBottom = scrollY >= Math.max(0, max - 8);
            updateAcceptEnabled();
        });

        chkAgree.setOnCheckedChangeListener((buttonView, isChecked) -> updateAcceptEnabled());
        updateAcceptEnabled();

        btnAccept.setOnClickListener(v -> {
            TermsPrefs.setAccepted(this, true);
            String redirect = getIntent().getStringExtra(EXTRA_REDIRECT);
            Intent next = (redirect != null && redirect.equals("home"))
                    ? new Intent(this, HomeActivity.class)
                    : new Intent(this, HomeActivity.class); // default -> Home
            next.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(next);
        });

        btnDecline.setOnClickListener(v -> finishAffinity());
    }

    private void updateAcceptEnabled() {
        btnAccept.setEnabled(chkAgree.isChecked() && scrolledToBottom);
    }

    @Override
    public void onBackPressed() {
        // User must accept to proceed
        finishAffinity();
    }
}
