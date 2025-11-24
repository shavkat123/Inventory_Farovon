package com.inventory.farovon;

import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.viewpager2.widget.ViewPager2;
import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;
import com.inventory.farovon.ui.returntowarehouse.ReturnDetailPagerAdapter;

public class ReturnDetailActivity extends AppCompatActivity {

    public static final String EXTRA_DOCUMENT_ID = "extra_document_id";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_mol_movement_detail); // Reusing layout

        androidx.appcompat.widget.Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("Просмотр возврата");
        }

        long documentId = getIntent().getLongExtra(EXTRA_DOCUMENT_ID, -1);
        if (documentId == -1) {
            finish();
            return;
        }

        ViewPager2 viewPager = findViewById(R.id.view_pager);
        TabLayout tabLayout = findViewById(R.id.tab_layout);

        ReturnDetailPagerAdapter adapter = new ReturnDetailPagerAdapter(this, documentId);
        viewPager.setAdapter(adapter);

        new TabLayoutMediator(tabLayout, viewPager,
                (tab, position) -> {
                    if (position == 0) {
                        tab.setText("Парам.");
                    } else {
                        tab.setText("ОУ");
                    }
                }
        ).attach();
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }
}
