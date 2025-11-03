package com.inventory.farovon.ui.returntowarehouse;

import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;
import androidx.viewpager2.widget.ViewPager2;
import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;
import com.inventory.farovon.R;

public class ReturnToWarehouseActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_return_to_warehouse);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("Возврат на склад");
        }

        ViewPager2 viewPager = findViewById(R.id.view_pager);
        TabLayout tabLayout = findViewById(R.id.tab_layout);

        ReturnToWarehousePagerAdapter adapter = new ReturnToWarehousePagerAdapter(this);
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

        viewPager.setCurrentItem(1, false);

        findViewById(R.id.button_create).setOnClickListener(v ->
                Toast.makeText(this, "Документ 'Возврат на склад' создан", Toast.LENGTH_SHORT).show());
    }

    public void updateTabTitle(int count) {
        TabLayout tabLayout = findViewById(R.id.tab_layout);
        TabLayout.Tab tab = tabLayout.getTabAt(1);
        if (tab != null) {
            tab.setText("ОУ (" + count + ")");
        }
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.asset_movement_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.action_reset) {
            Toast.makeText(this, "Reset Clicked", Toast.LENGTH_SHORT).show();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}