package com.metabots.festora;

import android.content.Context;
import android.content.SharedPreferences;

public final class TermsPrefs {
    private static final String PREFS = "auth_prefs";
    private static final String KEY_ACCEPTED_TERMS = "accepted_terms_v1"; // bump version if your terms change

    private TermsPrefs(){}

    public static boolean hasAccepted(Context ctx) {
        SharedPreferences sp = ctx.getSharedPreferences(PREFS, Context.MODE_PRIVATE);
        return sp.getBoolean(KEY_ACCEPTED_TERMS, false);
    }

    public static void setAccepted(Context ctx, boolean accepted) {
        SharedPreferences sp = ctx.getSharedPreferences(PREFS, Context.MODE_PRIVATE);
        sp.edit().putBoolean(KEY_ACCEPTED_TERMS, accepted).apply();
    }
}
