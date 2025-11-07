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
import com.inventory.farovon.db.InventoryItemEntity;
import com.inventory.farovon.db.OrganizationEntity;
import com.inventory.farovon.db.PendingUploadEntity;
import com.inventory.farovon.model.OrganizationItem;
import com.inventory.farovon.ui.login.SessionManager;
import org.xmlpull.v1.XmlPullParserException;
import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Credentials;
import okhttp3.MediaType;
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
    private final ExecutorService networkExecutor = Executors.newFixedThreadPool(4); // Increased pool size
    private final ExecutorService databaseExecutor = Executors.newSingleThreadExecutor();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_organization_inventory);
        // ... (standard setup)
    }

    // ... (menu setup and other methods remain the same)

    private void syncData() {
        mainHandler.post(() -> {
            progressBar.setVisibility(View.VISIBLE);
            Toast.makeText(this, "Начинается полная синхронизация...", Toast.LENGTH_SHORT).show();
        });

        // First, sync the organization structure
        syncOrganizationStructure(this::syncAllInventory);
    }

    private void syncOrganizationStructure(Runnable onComplete) {
        String ip = sessionManager.getIpAddress();
        String url = "http://" + ip + "/my1c/hs/checking/schema";
        // ... (client and request setup)

        // As before, but on success, call the onComplete runnable
        // ...
        // In onResponse:
        databaseExecutor.execute(() -> {
            // ... (clear tables and save new structure)
            mainHandler.post(onComplete);
        });
    }

    private void syncAllInventory() {
        mainHandler.post(() -> Toast.makeText(this, "Загрузка инвентаря для каждого помещения...", Toast.LENGTH_SHORT).show());

        databaseExecutor.execute(() -> {
            db.inventoryItemDao().clearAll();
            List<DepartmentEntity> allDepartments = db.departmentDao().getAll(); // Assuming you add getAll() to DepartmentDao
            List<DepartmentEntity> locations = filterLocations(allDepartments);

            if (locations.isEmpty()) {
                mainHandler.post(() -> {
                    Toast.makeText(this, "Помещения не найдены. Синхронизация инвентаря пропущена.", Toast.LENGTH_LONG).show();
                    loadDataFromDb();
                });
                return;
            }

            AtomicInteger completedCount = new AtomicInteger(0);
            int total = locations.size();

            for (DepartmentEntity location : locations) {
                fetchInventoryForLocation(location, () -> {
                    int current = completedCount.incrementAndGet();
                    mainHandler.post(() -> {
                        Toast.makeText(this, "Синхронизация: " + current + " / " + total, Toast.LENGTH_SHORT).show();
                        if (current == total) {
                            Toast.makeText(this, "Полная синхронизация завершена!", Toast.LENGTH_LONG).show();
                            loadDataFromDb();
                        }
                    });
                });
            }
        });
    }

    // You'll need to add a getAll() method to your DepartmentDao
    // @Query("SELECT * FROM departments")
    // List<DepartmentEntity> getAll();

    private List<DepartmentEntity> filterLocations(List<DepartmentEntity> allDepts) {
        List<DepartmentEntity> locations = new ArrayList<>();
        // Simple heuristic: if a department code is numeric, it's a location.
        // This logic may need to be adjusted based on your actual data.
        for(DepartmentEntity dept : allDepts) {
            if(dept.code != null && dept.code.matches("\\d+")) {
                locations.add(dept);
            }
        }
        return locations;
    }


    private void fetchInventoryForLocation(DepartmentEntity location, Runnable onComplete) {
        String ip = sessionManager.getIpAddress();
        String url = "http://" + ip + "/my1c/hs/hw/say";
        String json = "{\"otdel\":\"" + location.code + "\"}";
        RequestBody body = RequestBody.create(json, MediaType.get("application/json; charset=utf-f"));
        // ... (client and request setup)

        networkExecutor.execute(() -> {
            try {
                Response response = client.newCall(request).execute();
                if (response.isSuccessful() && response.body() != null) {
                    String xmlString = response.body().string();
                    List<InventoryItemEntity> items = parseInventoryXml(xmlString, location.id, location.code);
                    if (!items.isEmpty()) {
                        db.inventoryItemDao().insertAll(items);
                    }
                }
            } catch (IOException | XmlPullParserException e) {
                Log.e(TAG, "Failed to fetch inventory for location: " + location.code, e);
            } finally {
                onComplete.run();
            }
        });
    }

    private List<InventoryItemEntity> parseInventoryXml(String xml, int resolvedDepartmentId, String roomCode) throws Exception {
        List<InventoryItemEntity> items = new ArrayList<>();
        XmlPullParserFactory factory = XmlPullParserFactory.newInstance();
        XmlPullParser parser = factory.newPullParser();
        parser.setInput(new StringReader(xml));

        InventoryItemEntity currentItem = null;
        String text = null;
        int eventType = parser.getEventType();

        while (eventType != XmlPullParser.END_DOCUMENT) {
            String tagName = parser.getName();
            switch (eventType) {
                case XmlPullParser.START_TAG:
                    if ("Product".equalsIgnoreCase(tagName)) {
                        currentItem = new InventoryItemEntity();
                        currentItem.departmentId = resolvedDepartmentId;
                        currentItem.code = "";
                        currentItem.name = "";
                        currentItem.rf = "";
                        currentItem.mol = "";
                        currentItem.location = "";
                    }
                    break;
                case XmlPullParser.TEXT:
                    text = parser.getText();
                    break;
                case XmlPullParser.END_TAG:
                    if (currentItem != null) {
                        if ("Code".equalsIgnoreCase(tagName)) {
                            currentItem.code = (text != null) ? text : "";
                        } else if ("Name".equalsIgnoreCase(tagName)) {
                            currentItem.name = (text != null) ? text : "";
                        } else if ("rf".equalsIgnoreCase(tagName)) {
                            currentItem.rf = (text != null) ? text : "";
                        } else if ("mol".equalsIgnoreCase(tagName)) {
                            currentItem.mol = (text != null) ? text : "";
                        } else if ("location".equalsIgnoreCase(tagName)) {
                            currentItem.location = (text != null) ? text : "";
                        } else if ("Product".equalsIgnoreCase(tagName)) {
                            currentItem.location = roomCode;
                            if (currentItem.code != null && !currentItem.code.isEmpty() &&
                                currentItem.name != null && !currentItem.name.isEmpty()) {
                                items.add(currentItem);
                            }
                            currentItem = null;
                        }
                    }
                    break;
            }
            eventType = parser.next();
        }
        return items;
    }

    // ... (rest of the class)
}
