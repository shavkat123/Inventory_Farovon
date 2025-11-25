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
import com.inventory.farovon.ui.assetmovement.AssetMovementAssetsFragment;
import com.inventory.farovon.ui.assetmovement.AssetMovementPagerAdapter;

import java.util.Date;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class AssetMovementActivity extends AppCompatActivity implements AssetMovementAssetsFragment.OnItemCountChangeListener {

    private AssetMovementPagerAdapter adapter;
    private AssetMovementDao assetMovementDao;
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
            runOnUiThread(() -> {
                Toast.makeText(this, "Документ сохранен", Toast.LENGTH_SHORT).show();
                finish();
            });
        });
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