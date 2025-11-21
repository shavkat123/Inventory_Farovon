package com.inventory.farovon;

import android.os.Bundle;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.viewpager2.widget.ViewPager2;
import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;
import com.inventory.farovon.db.AppDatabase;
import android.view.KeyEvent;
import com.inventory.farovon.db.MolMovementDao;
import com.inventory.farovon.db.MolMovementDocument;
import com.inventory.farovon.ui.molmovement.MolMovementPagerAdapter;

import java.util.Date;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class MolMovementActivity extends AppCompatActivity {

    private MolMovementPagerAdapter adapter;
    private MolMovementDao molMovementDao;
    private ExecutorService databaseExecutor;
    private ViewPager2 viewPager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_mol_movement);

        androidx.appcompat.widget.Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        viewPager = findViewById(R.id.view_pager);
        TabLayout tabLayout = findViewById(R.id.tab_layout);

        viewPager.setOffscreenPageLimit(2); // Keep both fragments in memory
        adapter = new MolMovementPagerAdapter(this);
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
        molMovementDao = db.molMovementDao();
        databaseExecutor = Executors.newSingleThreadExecutor();
    }

    public void saveDocument() {
        String fromMol = adapter.getParametersFragment().getFromMol();
        String toMol = adapter.getParametersFragment().getToMol();
        List<String> scannedRfids = adapter.getAssetsFragment().getScannedBarcodes();

        if (fromMol.isEmpty() || toMol.isEmpty()) {
            Toast.makeText(this, "Пожалуйста, заполните поля 'Откуда' и 'Куда'", Toast.LENGTH_SHORT).show();
            return;
        }

        MolMovementDocument document = new MolMovementDocument();
        document.date = new Date().getTime();
        document.fromMol = fromMol;
        document.toMol = toMol;

        databaseExecutor.execute(() -> {
            molMovementDao.insertFullMovement(document, scannedRfids);
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
}