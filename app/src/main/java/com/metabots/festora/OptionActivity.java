package com.metabots.festora;

import android.content.Intent;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import android.view.View;

import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;


public class OptionActivity extends AppCompatActivity {
    private View btnLogin, btnSignup, btnGoogle;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_option);

        View root = findViewById(R.id.option_root);
        if (root != null) {
            ViewCompat.setOnApplyWindowInsetsListener(root, (v, insets) -> {
                Insets bars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
                v.setPadding(bars.left, bars.top, bars.right, bars.bottom);
                return insets;
            });
        }
        btnLogin  = findViewById(R.id.btnLogin);
        btnSignup = findViewById(R.id.btnSignup);
        btnGoogle = findViewById(R.id.btnGoogle);

        btnLogin.setOnClickListener(v ->
                startActivity(new Intent(OptionActivity.this, LoginActivity.class)));

        btnSignup.setOnClickListener(v ->
                startActivity(new Intent(OptionActivity.this, SignupActivity.class)));

        // If you've already implemented GoogleSignInActivity (from earlier step), this will launch it.


    }
}