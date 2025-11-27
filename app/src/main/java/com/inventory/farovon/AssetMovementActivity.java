package com.inventory.farovon;

import android.os.Bundle;
import android.view.KeyEvent;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.viewpager2.widget.ViewPager2;
import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;
import com.inventory.farovon.db.AppDatabase;
import com.inventory.farovon.db.AssetMovementDao;
import com.inventory.farovon.db.AssetMovementDocument;
import com.inventory.farovon.db.InventoryItemDao;
import com.inventory.farovon.db.InventoryItemEntity;
import com.inventory.farovon.ui.assetmovement.AssetMovementAssetsFragment;
import com.inventory.farovon.ui.assetmovement.AssetMovementPagerAdapter;
import com.inventory.farovon.ui.login.SessionManager;

import org.json.JSONObject;

import java.io.IOException;
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

public class AssetMovementActivity extends AppCompatActivity implements AssetMovementAssetsFragment.OnItemCountChangeListener {

    private AssetMovementPagerAdapter adapter;
    private AssetMovementDao assetMovementDao;
    private InventoryItemDao inventoryItemDao;
    private ExecutorService databaseExecutor;
    private ViewPager2 viewPager;
    private TabLayout tabLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_mol_movement); // Reuse MolMovement layout which has tab_layout and view_pager

        androidx.appcompat.widget.Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("Перемещение МП");
        }

        viewPager = findViewById(R.id.view_pager);
        tabLayout = findViewById(R.id.tab_layout);

        viewPager.setOffscreenPageLimit(2);
        adapter = new AssetMovementPagerAdapter(this);
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
        assetMovementDao = db.assetMovementDao();
        inventoryItemDao = db.inventoryItemDao();
        databaseExecutor = Executors.newSingleThreadExecutor();
    }

    public void saveDocument() {
        String fromIssuer = adapter.getParametersFragment().getFromIssuer();
        String fromDepartment = adapter.getParametersFragment().getFromIssuerDepartment();
        String fromOrganization = adapter.getParametersFragment().getFromOrganization();
        String fromLocation = adapter.getParametersFragment().getFromLocation();

        String toRecipient = adapter.getParametersFragment().getToRecipient();
        String toDepartment = adapter.getParametersFragment().getToRecipientDepartment();
        String toOrganization = adapter.getParametersFragment().getToOrganization();
        String toLocation = adapter.getParametersFragment().getToLocation();

        List<String> scannedRfids = adapter.getAssetsFragment().getScannedBarcodes();

        if (fromIssuer.isEmpty() || toRecipient.isEmpty()) {
            Toast.makeText(this, "Пожалуйста, заполните обязательные поля", Toast.LENGTH_SHORT).show();
            return;
        }

        AssetMovementDocument document = new AssetMovementDocument();
        document.date = new Date().getTime();
        document.fromIssuer = fromIssuer;
        document.fromIssuerDepartment = fromDepartment;
        document.fromOrganization = fromOrganization;
        document.fromLocation = fromLocation;
        document.toRecipient = toRecipient;
        document.toRecipientDepartment = toDepartment;
        document.toOrganization = toOrganization;
        document.toLocation = toLocation;

        databaseExecutor.execute(() -> {
            assetMovementDao.insertFullMovement(document, scannedRfids);
            sendAssetMovement(document, scannedRfids);
            runOnUiThread(() -> {
                Toast.makeText(this, "Документ сохранен", Toast.LENGTH_SHORT).show();
                finish();
            });
        });
    }

    private void sendAssetMovement(AssetMovementDocument document, List<String> rfids) {
        SessionManager sessionManager = new SessionManager(getApplicationContext());
        String ip = sessionManager.getIpAddress();
        String username = sessionManager.getUsername();
        String password = sessionManager.getPassword();
        String url = "http://" + ip + "/my1c/hs/transfer/os";

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
                json.put("Organization", document.fromOrganization != null ? document.fromOrganization : "");
                json.put("Division", document.fromIssuerDepartment != null ? document.fromIssuerDepartment : "");
                json.put("DivisionOrganization", document.toRecipientDepartment != null ? document.toRecipientDepartment : "");
                json.put("MOL", document.fromIssuer != null ? document.fromIssuer : "");
                json.put("MOLOrganization", document.toRecipient != null ? document.toRecipient : "");
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
                    // Log error or handle failure
                    System.err.println("Failed to send asset movement for: " + fixedAssetCode + " Code: " + response.code());
                } else {
                    System.out.println("Successfully sent asset movement for: " + fixedAssetCode);
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