package com.inventory.farovon;

import android.os.Bundle;
import android.view.KeyEvent;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.viewpager2.widget.ViewPager2;
import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;
import com.inventory.farovon.db.AppDatabase;
import com.inventory.farovon.db.WriteOffDao;
import com.inventory.farovon.db.WriteOffDocument;
import com.inventory.farovon.ui.writeoff.WriteOffAssetsFragment;
import com.inventory.farovon.ui.writeoff.WriteOffParamsFragment;
import com.inventory.farovon.ui.writeoff.WriteOffPagerAdapter;

import java.util.Date;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class WriteOffActivity extends AppCompatActivity implements WriteOffAssetsFragment.OnItemCountChangeListener {

    private WriteOffPagerAdapter adapter;
    private WriteOffDao writeOffDao;
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
        databaseExecutor = Executors.newSingleThreadExecutor();
    }

    public void saveDocument() {
        WriteOffParamsFragment params = adapter.getParametersFragment();
        String name = params.getName();
        String reason = params.getReason();
        String department = params.getDepartment();
        String condition = params.getCondition();
        String photoPath = params.getPhotoPath();

        List<String> scannedRfids = adapter.getAssetsFragment().getScannedBarcodes();

        if (name.isEmpty() || reason.isEmpty() || department.isEmpty()) {
             // Basic validation, photo and condition might be optional depending on strictness
            Toast.makeText(this, "Пожалуйста, заполните основные поля (Название, Причина, Подразделение)", Toast.LENGTH_SHORT).show();
            return;
        }

        WriteOffDocument document = new WriteOffDocument();
        document.date = new Date().getTime();
        document.name = name;
        document.reason = reason;
        document.department = department;
        document.condition = condition;
        document.photoPath = photoPath;
        document.status = "На согласовании";

        databaseExecutor.execute(() -> {
            writeOffDao.insertFullDocument(document, scannedRfids);
            runOnUiThread(() -> {
                Toast.makeText(this, "Документ списания сохранен", Toast.LENGTH_SHORT).show();
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
