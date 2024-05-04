package com.tardigames.rhcompletion;

import android.content.Context;
import android.content.SharedPreferences;

import androidx.annotation.NonNull;

public class Options {

    private static SharedPreferences sharedPref;
    private static final String opt_nickname = "nickname";
    private static final String opt_autostart = "autostart";
    private static final String opt_width = "width";

    // Init
    public static void init(Context p_context) {
        sharedPref = p_context.getSharedPreferences("RHCompletionPreferences", Context.MODE_PRIVATE);
    }

    // Read saved nickname
    public static @NonNull String getNickname() {
        return sharedPref.getString(opt_nickname, "Nickname");
    }

    // Save new nickname
    public static void setNickname(@NonNull String p_nickname) {
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putString(opt_nickname, p_nickname);
        editor.apply();
    }

    // Read saved autostart
    public static Boolean getAutostart() {
        return sharedPref.getBoolean(opt_autostart, false);
    }

    // Save new autostart
    public static void setAutostart(Boolean p_autostart) {
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putBoolean(opt_autostart, p_autostart);
        editor.apply();
    }

    // Read saved width
    public static int getWidth() {
        return sharedPref.getInt(opt_width, 50);
    }

    public static void setWidth(int p_width) {
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putInt(opt_width, p_width);
        editor.apply();
    }

}
