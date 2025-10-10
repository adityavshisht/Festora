package com.metabots.festora;

import android.content.Context;
import android.content.SharedPreferences;

public final class TermsPrefs {
    private static final String PREFS = "auth_prefs";
    private static final String KEY_PREFIX = "accepted_terms_v1_"; // bump v1→v2 if terms change

    private TermsPrefs(){}

    private static String k(String userKey) {
        // null-safe, normalize; you can hash if you prefer
        String u = (userKey == null) ? "anon" : userKey.trim().toLowerCase();
        return KEY_PREFIX + u;
    }

    /** Has THIS user accepted the current terms? */
    public static boolean hasAccepted(Context ctx, String userKey) {
        SharedPreferences sp = ctx.getSharedPreferences(PREFS, Context.MODE_PRIVATE);
        return sp.getBoolean(k(userKey), false);
    }

    /** Mark accepted for THIS user. */
    public static void setAccepted(Context ctx, String userKey, boolean accepted) {
        SharedPreferences sp = ctx.getSharedPreferences(PREFS, Context.MODE_PRIVATE);
        sp.edit().putBoolean(k(userKey), accepted).apply();
    }

    /**call on logout to be tidy (not required). */
    public static void clearForUser(Context ctx, String userKey) {
        SharedPreferences sp = ctx.getSharedPreferences(PREFS, Context.MODE_PRIVATE);
        sp.edit().remove(k(userKey)).apply();
    }
}
