package com.inventory.farovon;

import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.inventory.farovon.model.OrganizationItem;

import java.util.ArrayList;
import java.util.List;

public class OrganizationInventoryActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_organization_inventory);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        RecyclerView recyclerView = findViewById(R.id.organization_recycler_view);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.addItemDecoration(new TreeItemDecoration(
                ContextCompat.getColor(this, R.color.colorPrimary),
                4f,
                64
        ));

        List<OrganizationItem> items = createTestData();
        OrganizationAdapter adapter = new OrganizationAdapter(items);
        recyclerView.setAdapter(adapter);
    }

    private List<OrganizationItem> createTestData() {
        List<OrganizationItem> items = new ArrayList<>();

        OrganizationItem org1 = new OrganizationItem("Главный офис", 0);
        OrganizationItem department1_1 = new OrganizationItem("Отдел продаж", 1);
        OrganizationItem office1_1_1 = new OrganizationItem("Кабинет 101", 2);
        OrganizationItem office1_1_2 = new OrganizationItem("Кабинет 102", 2);
        department1_1.addChild(office1_1_1);
        department1_1.addChild(office1_1_2);
        org1.addChild(department1_1);

        OrganizationItem department1_2 = new OrganizationItem("Отдел маркетинга", 1);
        OrganizationItem office1_2_1 = new OrganizationItem("Кабинет 201", 2);
        department1_2.addChild(office1_2_1);
        org1.addChild(department1_2);

        items.add(org1);

        OrganizationItem org2 = new OrganizationItem("Филиал", 0);
        OrganizationItem department2_1 = new OrganizationItem("Склад", 1);
        OrganizationItem office2_1_1 = new OrganizationItem("Помещение A", 2);
        department2_1.addChild(office2_1_1);
        org2.addChild(department2_1);
        items.add(org2);

        return items;
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }
}