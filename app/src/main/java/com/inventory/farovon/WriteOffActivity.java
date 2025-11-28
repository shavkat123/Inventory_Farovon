package com.inventory.farovon;

import android.os.Bundle;
import android.view.KeyEvent;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.viewpager2.widget.ViewPager2;
import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;
import com.inventory.farovon.db.AppDatabase;
import com.inventory.farovon.db.InventoryItemDao;
import com.inventory.farovon.db.InventoryItemEntity;
import com.inventory.farovon.db.WriteOffDao;
import com.inventory.farovon.db.WriteOffDocument;
import com.inventory.farovon.ui.login.SessionManager;
import com.inventory.farovon.ui.writeoff.WriteOffAssetsFragment;
import com.inventory.farovon.ui.writeoff.WriteOffParamsFragment;
import com.inventory.farovon.ui.writeoff.WriteOffPagerAdapter;

import org.json.JSONObject;

import java.util.Date;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import okhttp3.Credentials;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class WriteOffActivity extends AppCompatActivity implements WriteOffAssetsFragment.OnItemCountChangeListener {

    private WriteOffPagerAdapter adapter;
    private WriteOffDao writeOffDao;
    private InventoryItemDao inventoryItemDao;
    private ExecutorService databaseExecutor;
    private ViewPager2 viewPager;
    private TabLayout tabLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_write_off);

        androidx.appcompat.widget.Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        viewPager = findViewById(R.id.view_pager);
        tabLayout = findViewById(R.id.tab_layout);

        viewPager.setOffscreenPageLimit(2);
        adapter = new WriteOffPagerAdapter(this);
        viewPager.setAdapter(adapter);

        new TabLayoutMediator(tabLayout, viewPager,
                (tab, position) -> {
                    if (position == 0) {
                        tab.setText("Парам.");
                    } else {
                        tab.setText("ОУ (0)");
                    }
                }
        ).attach();

        AppDatabase db = AppDatabase.getDatabase(getApplicationContext());
        writeOffDao = db.writeOffDao();
        inventoryItemDao = db.inventoryItemDao();
        databaseExecutor = Executors.newSingleThreadExecutor();
    }

    public void saveDocument() {
        WriteOffParamsFragment params = adapter.getParametersFragment();
        String name = params.getName();
        String organization = params.getOrganization();
        String department = params.getDepartment();

        List<String> scannedRfids = adapter.getAssetsFragment().getScannedBarcodes();

        if (name.isEmpty() || organization.isEmpty() || department.isEmpty()) {
            Toast.makeText(this, "Пожалуйста, заполните все поля (Название, Организация, Подразделение)", Toast.LENGTH_SHORT).show();
            return;
        }

        WriteOffDocument document = new WriteOffDocument();
        document.date = new Date().getTime();
        document.name = name;
        document.organization = organization;
        document.department = department;
        document.status = "На согласовании";

        databaseExecutor.execute(() -> {
            writeOffDao.insertFullDocument(document, scannedRfids);
            sendWriteOffTo1C(document, scannedRfids);
            runOnUiThread(() -> {
                Toast.makeText(this, "Документ списания сохранен", Toast.LENGTH_SHORT).show();
                finish();
            });
        });
    }

    private void sendWriteOffTo1C(WriteOffDocument document, List<String> rfids) {
        SessionManager sessionManager = new SessionManager(getApplicationContext());
        String ip = sessionManager.getIpAddress();
        String username = sessionManager.getUsername();
        String password = sessionManager.getPassword();
        String url = "http://" + ip + "/my1c/hs/writeff/os";

        OkHttpClient client = new OkHttpClient();

        for (String rfid : rfids) {
            try {
                String fixedAssetCode = rfid;
                List<InventoryItemEntity> items = inventoryItemDao.findByRfid(rfid);
                if (items != null && !items.isEmpty()) {
                    String code = items.get(0).code;
                    if (code != null && !code.isEmpty()) {
                        fixedAssetCode = code;
                    }
                }

                JSONObject json = new JSONObject();
                json.put("Organization", document.organization != null ? document.organization : "");
                json.put("DivisionOrganization", document.department != null ? document.department : "");
                json.put("FixedAsset", fixedAssetCode);
                json.put("token", UUID.randomUUID().toString());

                RequestBody body = RequestBody.create(json.toString(), okhttp3.MediaType.parse("application/json; charset=utf-8"));
                Request request = new Request.Builder()
                        .url(url)
                        .post(body)
                        .header("Authorization", Credentials.basic(username, password))
                        .build();

                Response response = client.newCall(request).execute();
                if (!response.isSuccessful()) {
                    System.err.println("Failed to send write-off for: " + fixedAssetCode + " Code: " + response.code());
                } else {
                    System.out.println("Successfully sent write-off for: " + fixedAssetCode);
                }
                response.close();

            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (viewPager.getCurrentItem() == 1) { // Assets Fragment
            if (adapter.getAssetsFragment() != null) {
                boolean handled = adapter.getAssetsFragment().onKeyDown(keyCode, event);
                if (handled) return true;
            }
        }
        return super.onKeyDown(keyCode, event);
    }

    @Override
    public boolean onKeyUp(int keyCode, KeyEvent event) {
        if (viewPager.getCurrentItem() == 1) { // Assets Fragment
            if (adapter.getAssetsFragment() != null) {
                boolean handled = adapter.getAssetsFragment().onKeyUp(keyCode, event);
                if (handled) return true;
            }
        }
        return super.onKeyUp(keyCode, event);
    }

    @Override
    public void onItemCountChanged(int count) {
        if (tabLayout != null && tabLayout.getTabCount() > 1) {
            TabLayout.Tab tab = tabLayout.getTabAt(1);
            if (tab != null) {
                tab.setText("ОУ (" + count + ")");
            }
        }
    }
}
