package com.inventory.farovon;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;
import com.google.android.material.navigation.NavigationView;
import com.google.android.material.snackbar.Snackbar;
import com.inventory.farovon.databinding.ActivityMainBinding;
import com.inventory.farovon.ui.login.LoginActivity;
import com.inventory.farovon.ui.login.SessionManager;

public class MainActivity extends AppCompatActivity {

    private AppBarConfiguration mAppBarConfiguration;
    private ActivityMainBinding binding;
    private SessionManager sessionManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        sessionManager = new SessionManager(this);

        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        setSupportActionBar(binding.appBarMain.toolbar);
        binding.appBarMain.fab.setOnClickListener(view -> Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                .setAction("Action", null)
                .setAnchorView(R.id.fab).show());
        DrawerLayout drawer = binding.drawerLayout;
        NavigationView navigationView = binding.navView;

        updateNavHeader();

        // Passing each menu ID as a set of Ids because each
        // menu should be considered as top level destinations.
        mAppBarConfiguration = new AppBarConfiguration.Builder(
                R.id.nav_home)
                .setOpenableLayout(drawer)
                .build();
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment_content_main);
        NavigationUI.setupActionBarWithNavController(this, navController, mAppBarConfiguration);
        NavigationUI.setupWithNavController(navigationView, navController);

        setupDrawerButtons(navigationView);
    }

    private void updateNavHeader() {
        NavigationView navigationView = binding.navView;
        View headerView = navigationView.getHeaderView(0);

        TextView navUsername = headerView.findViewById(R.id.nav_header_username);
        String username = sessionManager.getUsername();

        if (username != null) {
            navUsername.setText(username);
        }

        TextView navServerIP = headerView.findViewById(R.id.nav_header_server_ip);
        String serverIP = sessionManager.getIpAddress();
        if (serverIP != null) {
            navServerIP.setText(serverIP);
        }
    }

    @Override
    public boolean onSupportNavigateUp() {
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment_content_main);
        return NavigationUI.navigateUp(navController, mAppBarConfiguration)
                || super.onSupportNavigateUp();
    }

    private void setupDrawerButtons(NavigationView navigationView) {
        TextView settingsButton = navigationView.findViewById(R.id.nav_settings_button);
        TextView logoutButton = navigationView.findViewById(R.id.nav_logout_button);

        settingsButton.setOnClickListener(v -> {
            Snackbar.make(binding.getRoot(), "Настройки в разработке", Snackbar.LENGTH_SHORT).show();
            binding.drawerLayout.closeDrawers();
        });

        logoutButton.setOnClickListener(v -> {
            sessionManager.logoutUser();
            Intent intent = new Intent(MainActivity.this, LoginActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
        });
    }
}