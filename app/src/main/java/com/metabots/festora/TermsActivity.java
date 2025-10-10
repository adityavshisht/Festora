package com.metabots.festora;

import android.content.Intent;
import android.os.Bundle;
import android.text.method.ScrollingMovementMethod;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.TextView;

import androidx.activity.OnBackPressedCallback;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

public class TermsActivity extends AppCompatActivity {
    public static final String EXTRA_REDIRECT = "redirect";
    public static final String EXTRA_USER_ID  = "user_id";

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

        final String userId = getIntent().getStringExtra(EXTRA_USER_ID);

        TextView tvTerms = findViewById(R.id.tvTermsBody);
        tvTerms.setMovementMethod(ScrollingMovementMethod.getInstance());
        tvTerms.setVerticalScrollBarEnabled(true);
        tvTerms.setFocusable(true);
        tvTerms.setFocusableInTouchMode(true);

        tvTerms.setOnScrollChangeListener((v, sx, sy, ox, oy) -> {
            scrolledToBottom = isAtBottom(tvTerms);
            updateAcceptEnabled();
        });
        tvTerms.post(() -> {
            scrolledToBottom = isAtBottom(tvTerms);
            updateAcceptEnabled();
        });

        chkAgree.setOnCheckedChangeListener((btn, checked) -> updateAcceptEnabled());
        updateAcceptEnabled();

        btnAccept.setOnClickListener(v -> {
            TermsPrefs.setAccepted(this, userId, true);
            Intent next = new Intent(this, HomeActivity.class);
            next.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(next);
        });


        btnDecline.setOnClickListener(v -> finishAffinity());

        getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override public void handleOnBackPressed() {
                finishAffinity();
            }
        });
    }

    private boolean isAtBottom(TextView tv) {
        if (tv.getLayout() == null) return false;
        int contentHeight = tv.getLayout().getHeight()
                + tv.getTotalPaddingTop()
                + tv.getTotalPaddingBottom();
        int viewHeight = tv.getHeight();
        int maxScroll = Math.max(0, contentHeight - viewHeight);
        int threshold = 8; // px
        return tv.getScrollY() >= (maxScroll - threshold);
    }

    private void updateAcceptEnabled() {
        btnAccept.setEnabled(chkAgree.isChecked() && scrolledToBottom);
    }
}
