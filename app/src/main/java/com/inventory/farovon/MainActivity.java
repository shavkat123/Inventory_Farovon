package com.inventory.farovon;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
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

import okhttp3.OkHttpClient;
import okhttp3.Request;

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
        DrawerLayout drawer = binding.drawerLayout;
        NavigationView navigationView = binding.navView;

        updateNavHeader();

        // Passing each menu ID as a set of Ids because each
        // menu should be considered as top level destinations.
        mAppBarConfiguration = new AppBarConfiguration.Builder(
                R.id.nav_home, R.id.nav_gallery)
                .setOpenableLayout(drawer)
                .build();
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment_content_main);
        NavigationUI.setupActionBarWithNavController(this, navController, mAppBarConfiguration);
        NavigationUI.setupWithNavController(navigationView, navController);

        setupDrawerButtons(navigationView);

        drawer.addDrawerListener(new DrawerLayout.SimpleDrawerListener() {
            @Override
            public void onDrawerOpened(View drawerView) {
                super.onDrawerOpened(drawerView);
                checkConnectionStatus();
            }
        });
    }

    private void updateNavHeader() {
        NavigationView navigationView = binding.navView;
        View headerView = navigationView.getHeaderView(0);
        TextView navUsername = headerView.findViewById(R.id.nav_header_username);
        TextView navServerIp = headerView.findViewById(R.id.nav_header_server_ip);

        String username = sessionManager.getUsername();
        String ipAddress = sessionManager.getIpAddress();

        if (username != null) {
            navUsername.setText(username);
        }
        if (ipAddress != null) {
            navServerIp.setText(ipAddress);
        }
    }

    private void checkConnectionStatus() {
        String ip = sessionManager.getIpAddress();
        String user = sessionManager.getUsername();
        String pass = sessionManager.getPassword();

        if (ip == null || user == null || pass == null) {
            return; // No credentials, no check
        }

        String url = "http://" + ip + "/my1c/hs/checking/check";
        OkHttpClient client = new OkHttpClient.Builder()
                .connectTimeout(5, java.util.concurrent.TimeUnit.SECONDS)
                .build();

        Request request = new Request.Builder()
                .url(url)
                .header("Authorization", okhttp3.Credentials.basic(user, pass))
                .build();

        client.newCall(request).enqueue(new okhttp3.Callback() {
            @Override
            public void onFailure(@NonNull okhttp3.Call call, @NonNull java.io.IOException e) {
                runOnUiThread(() -> {
                    View headerView = binding.navView.getHeaderView(0);
                    TextView statusView = headerView.findViewById(R.id.nav_header_status);
                    statusView.setText("Оффлайн");
                    statusView.setTextColor(getResources().getColor(android.R.color.holo_red_light));
                });
            }

            @Override
            public void onResponse(@NonNull okhttp3.Call call, @NonNull okhttp3.Response response) {
                runOnUiThread(() -> {
                    View headerView = binding.navView.getHeaderView(0);
                    TextView statusView = headerView.findViewById(R.id.nav_header_status);
                    if (response.isSuccessful()) {
                        statusView.setText("Онлайн");
                        statusView.setTextColor(getResources().getColor(android.R.color.holo_green_light));
                    } else {
                        statusView.setText("Оффлайн");
                        statusView.setTextColor(getResources().getColor(android.R.color.holo_red_light));
                    }
                });
            }
        });
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