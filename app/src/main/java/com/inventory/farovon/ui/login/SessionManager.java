package com.inventory.farovon.ui.login;

import android.content.Context;
import android.content.SharedPreferences;

public class SessionManager {

    private static final String PREF_NAME = "1C_Session";
    private static final String KEY_IP_ADDRESS = "ip_address";
    private static final String KEY_USERNAME = "username";
    private static final String KEY_PASSWORD = "password";

    private final SharedPreferences pref;
    private final SharedPreferences.Editor editor;

    public SessionManager(Context context) {
        pref = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        editor = pref.edit();
    }

    public void createLoginSession(String ipAddress, String username, String password) {
        editor.putString(KEY_IP_ADDRESS, ipAddress);
        editor.putString(KEY_USERNAME, username);
        editor.putString(KEY_PASSWORD, password);
        editor.commit();
    }

    public String getIpAddress() {
        return pref.getString(KEY_IP_ADDRESS, null);
    }

    public String getUsername() {
        return pref.getString(KEY_USERNAME, null);
    }

    public String getPassword() {
        return pref.getString(KEY_PASSWORD, null);
    }
}