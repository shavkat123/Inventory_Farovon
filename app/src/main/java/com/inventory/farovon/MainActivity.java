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
        binding.appBarMain.fab.setOnClickListener(view -> Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                .setAction("Action", null)
                .setAnchorView(R.id.fab).show());
        DrawerLayout drawer = binding.drawerLayout;
        NavigationView navigationView = binding.navView;

        updateNavHeader();

        // Passing each menu ID as a set of Ids because each
        // menu should be considered as top level destinations.
        mAppBarConfiguration = new AppBarConfiguration.Builder(
                R.id.nav_home, R.id.nav_gallery, R.id.nav_slideshow, R.id.nav_logout)
                .setOpenableLayout(drawer)
                .build();
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment_content_main);
        NavigationUI.setupActionBarWithNavController(this, navController, mAppBarConfiguration);
        NavigationUI.setupWithNavController(navigationView, navController);

        navigationView.setNavigationItemSelectedListener(item -> {
            int id = item.getItemId();
            if (id == R.id.nav_logout) {
                sessionManager.logoutUser();
                Intent intent = new Intent(MainActivity.this, LoginActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intent);
                return true;
            }
            if (id == R.id.nav_settings) {
                // Placeholder for settings
                Snackbar.make(binding.getRoot(), "Настройки в разработке", Snackbar.LENGTH_SHORT).show();
                return true;
            }
            // Handle other items by navigating
            boolean handled = NavigationUI.onNavDestinationSelected(item, navController);
            if (handled) {
                drawer.closeDrawers();
            }
            return handled;
        });

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
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onSupportNavigateUp() {
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment_content_main);
        return NavigationUI.navigateUp(navController, mAppBarConfiguration)
                || super.onSupportNavigateUp();
    }
}