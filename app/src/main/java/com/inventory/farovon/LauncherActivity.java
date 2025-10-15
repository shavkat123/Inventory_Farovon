package com.inventory.farovon;

import android.content.Intent;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;

import com.inventory.farovon.ui.login.LoginActivity;
import com.inventory.farovon.ui.login.SessionManager;

public class LauncherActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        SessionManager sessionManager = new SessionManager(this);
        Intent intent;

        // Check if user is logged in
        if (sessionManager.getIpAddress() != null && sessionManager.getUsername() != null && sessionManager.getPassword() != null) {
            // User is logged in, start MainActivity
            intent = new Intent(this, MainActivity.class);
        } else {
            // User is not logged in, start LoginActivity
            intent = new Intent(this, LoginActivity.class);
        }

        startActivity(intent);
        finish(); // Finish LauncherActivity so user can't go back to it
    }
}