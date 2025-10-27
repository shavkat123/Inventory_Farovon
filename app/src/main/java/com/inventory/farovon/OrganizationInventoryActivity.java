package com.inventory.farovon;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.inventory.farovon.db.AppDatabase;
import com.inventory.farovon.db.DepartmentEntity;
import com.inventory.farovon.db.OrganizationEntity;
import com.inventory.farovon.model.OrganizationItem;
import com.inventory.farovon.ui.login.SessionManager;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Credentials;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class OrganizationInventoryActivity extends AppCompatActivity {

    private static final String TAG = "OrgInventoryActivity";

    private RecyclerView recyclerView;
    private ProgressBar progressBar;
    private OrganizationAdapter adapter;
    private SessionManager sessionManager;
    private AppDatabase db;
    private final Handler mainHandler = new Handler(Looper.getMainLooper());
    private final ExecutorService databaseExecutor = Executors.newSingleThreadExecutor();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_organization_inventory);

        sessionManager = new SessionManager(this);
        db = AppDatabase.getDatabase(this);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        recyclerView = findViewById(R.id.organization_recycler_view);
        progressBar = findViewById(R.id.progress_bar);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        loadDataFromDb();
        syncData();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.organization_inventory_menu, menu);
        return true;
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        MenuItem statusItem = menu.findItem(R.id.action_status);
        if (isNetworkAvailable()) {
            statusItem.setIcon(R.drawable.ic_status_online);
        } else {
            statusItem.setIcon(R.drawable.ic_status_offline);
        }
        return super.onPrepareOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == R.id.action_sync) {
            syncData();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private boolean isNetworkAvailable() {
        ConnectivityManager connectivityManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
    }

    // ... (the rest of the class is the same)
    private void loadDataFromDb() {
        progressBar.setVisibility(View.VISIBLE);
        recyclerView.setVisibility(View.GONE);
        databaseExecutor.execute(() -> {
            List<OrganizationEntity> orgEntities = db.organizationDao().getAll();
            List<OrganizationItem> orgItems = new ArrayList<>();

            for (OrganizationEntity orgEntity : orgEntities) {
                OrganizationItem orgItem = new OrganizationItem(orgEntity.name, 0);
                orgItem.setId(orgEntity.id);
                List<DepartmentEntity> deptEntities = db.departmentDao().getByOrganizationId(orgEntity.id);

                Map<String, OrganizationItem> departmentMap = new HashMap<>();
                for (DepartmentEntity deptEntity : deptEntities) {
                    OrganizationItem deptItem = new OrganizationItem(deptEntity.name, 1);
                    deptItem.setId(deptEntity.id);
                    deptItem.setCode(deptEntity.code);
                    departmentMap.put(deptEntity.name, deptItem);
                }

                for (DepartmentEntity deptEntity : deptEntities) {
                     OrganizationItem deptItem = departmentMap.get(deptEntity.name);
                     if (deptEntity.parentRef != null && !deptEntity.parentRef.isEmpty() && departmentMap.containsKey(deptEntity.parentRef)) {
                         OrganizationItem parentItem = departmentMap.get(deptEntity.parentRef);
                         if (parentItem != null) {
                            parentItem.addChild(deptItem);
                            deptItem.setLevel(parentItem.getLevel() + 1);
                         }
                     } else {
                         orgItem.addChild(deptItem);
                     }
                }
                orgItems.add(orgItem);
            }

            mainHandler.post(() -> {
                progressBar.setVisibility(View.GONE);
                recyclerView.setVisibility(View.VISIBLE);
                adapter = new OrganizationAdapter(orgItems);
                recyclerView.setAdapter(adapter);
            });
        });
    }

    private void syncData() {
        invalidateOptionsMenu();
        String ip = sessionManager.getIpAddress();
        String username = sessionManager.getUsername();
        String password = sessionManager.getPassword();
        String url = "http://" + ip + "/my1c/hs/checking/schema";

        OkHttpClient client = new OkHttpClient();
        RequestBody body = RequestBody.create("", null);
        Request request = new Request.Builder()
                .url(url)
                .post(body)
                .header("Authorization", Credentials.basic(username, password))
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                mainHandler.post(() -> {
                    Toast.makeText(OrganizationInventoryActivity.this, "Ошибка синхронизации", Toast.LENGTH_SHORT).show();
                    invalidateOptionsMenu();
                });
            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) {
                mainHandler.post(() -> invalidateOptionsMenu());
                if (response.isSuccessful() && response.body() != null) {
                    try {
                        String xmlString = response.body().string();
                        OrganizationXmlParser parser = new OrganizationXmlParser();
                        List<OrganizationItem> orgItems = parser.parse(xmlString);

                        databaseExecutor.execute(() -> {
                            db.organizationDao().clearAll();
                            for (OrganizationItem orgItem : orgItems) {
                                OrganizationEntity orgEntity = new OrganizationEntity();
                                orgEntity.name = orgItem.getName();
                                long orgId = db.organizationDao().insert(orgEntity);
                                saveDepartmentsRecursive(orgItem.getChildren(), (int) orgId, "");
                            }
                            mainHandler.post(() -> loadDataFromDb());
                        });

                    } catch (Exception e) {
                        Log.e(TAG, "Parsing or DB error", e);
                    }
                }
            }
        });
    }

    private void saveDepartmentsRecursive(List<OrganizationItem> deptItems, int orgId, String parentRef) {
        if (deptItems == null || deptItems.isEmpty()) {
            return;
        }

        List<DepartmentEntity> deptEntities = new ArrayList<>();
        for (OrganizationItem deptItem : deptItems) {
            DepartmentEntity deptEntity = new DepartmentEntity();
            deptEntity.organizationId = orgId;
            deptEntity.code = deptItem.getCode();
            deptEntity.name = deptItem.getName();
            deptEntity.parentRef = parentRef;
            deptEntities.add(deptEntity);
        }
        db.departmentDao().insertAll(deptEntities);

        for (OrganizationItem deptItem : deptItems) {
            saveDepartmentsRecursive(deptItem.getChildren(), orgId, deptItem.getName());
        }
    }

    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return true;
    }
}
