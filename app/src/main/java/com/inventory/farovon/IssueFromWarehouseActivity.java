package com.inventory.farovon;

import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;
import com.inventory.farovon.data.DocumentRepository;
import com.inventory.farovon.model.Asset;
import com.inventory.farovon.model.IssueDocument;
import com.inventory.farovon.ui.issuefromwarehouse.IssueFromWarehousePagerAdapter;
import com.inventory.farovon.ui.issuefromwarehouse.ParametersFragment;

import java.util.ArrayList;
import java.util.List;

import androidx.viewpager2.widget.ViewPager2;

public class IssueFromWarehouseActivity extends AppCompatActivity {

    private boolean isEditMode = false;
    private IssueDocument currentDocument;
    private List<Asset> assets = new ArrayList<>();
    private IssueFromWarehousePagerAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_issue_from_warehouse);

        setSupportActionBar(findViewById(R.id.toolbar));
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        String documentId = getIntent().getStringExtra("DOCUMENT_ID");
        if (documentId != null) {
            isEditMode = true;
            currentDocument = DocumentRepository.getInstance().getDocumentById(documentId);
            if (currentDocument != null) {
                assets = currentDocument.getAssets();
            }
        }

        setupUI();
    }

    private void setupUI() {
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle(isEditMode ? "Просмотр выдачи" : "Создание выдачи");
        }

        ViewPager2 viewPager = findViewById(R.id.view_pager);
        TabLayout tabLayout = findViewById(R.id.tab_layout);

        adapter = new IssueFromWarehousePagerAdapter(this, currentDocument, isEditMode);
        viewPager.setAdapter(adapter);

        new TabLayoutMediator(tabLayout, viewPager,
                (tab, position) -> {
                    if (position == 0) {
                        tab.setText("Парам.");
                    } else {
                        tab.setText("ОУ (" + assets.size() + ")");
                    }
                }
        ).attach();

        if (isEditMode) {
            findViewById(R.id.button_create).setVisibility(View.GONE);
        } else {
            findViewById(R.id.button_create).setOnClickListener(v -> createDocument());
        }
    }

    private void createDocument() {
        ParametersFragment fragment = (ParametersFragment) getSupportFragmentManager().findFragmentByTag("f0");
        if (fragment != null) {
            if (fragment.validateFields()) {
                IssueDocument newDocument = fragment.getDocumentData(assets);
                DocumentRepository.getInstance().addDocument(newDocument);
                Toast.makeText(this, "Документ создан", Toast.LENGTH_SHORT).show();
                finish();
            } else {
                Toast.makeText(this, "Пожалуйста, заполните все поля", Toast.LENGTH_SHORT).show();
            }
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.issue_from_warehouse_menu, menu);
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

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }
}
