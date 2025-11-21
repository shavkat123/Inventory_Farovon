package com.inventory.farovon;

import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.viewpager2.widget.ViewPager2;
import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;
import com.inventory.farovon.ui.molmovement.MolAssetsDetailFragment;
import com.inventory.farovon.ui.molmovement.MolMovementDetailPagerAdapter;

public class MolMovementDetailActivity extends AppCompatActivity implements MolAssetsDetailFragment.OnItemCountChangeListener {

    public static final String EXTRA_DOCUMENT_ID = "extra_document_id";
    private TabLayout tabLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_mol_movement_detail);

        androidx.appcompat.widget.Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        long documentId = getIntent().getLongExtra(EXTRA_DOCUMENT_ID, -1);
        if (documentId == -1) {
            finish();
            return;
        }

        ViewPager2 viewPager = findViewById(R.id.view_pager);
        tabLayout = findViewById(R.id.tab_layout);

        MolMovementDetailPagerAdapter adapter = new MolMovementDetailPagerAdapter(this, documentId);
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
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
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
