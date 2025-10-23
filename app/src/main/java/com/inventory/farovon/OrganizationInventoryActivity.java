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

        List<OrganizationItem> items = createTestData();
        OrganizationAdapter adapter = new OrganizationAdapter(items);
        recyclerView.setAdapter(adapter);
    }

    private List<OrganizationItem> createTestData() {
        List<OrganizationItem> items = new ArrayList<>();

        OrganizationItem org1 = new OrganizationItem("Организация 1", 0);
        OrganizationItem room1_1 = new OrganizationItem("Помещение 1.1", 1);
        OrganizationItem office1_1_1 = new OrganizationItem("Офис 1.1.1", 2);
        OrganizationItem office1_1_2 = new OrganizationItem("Офис 1.1.2", 2);
        room1_1.addChild(office1_1_1);
        room1_1.addChild(office1_1_2);
        org1.addChild(room1_1);

        OrganizationItem room1_2 = new OrganizationItem("Помещение 1.2", 1);
        OrganizationItem office1_2_1 = new OrganizationItem("Офис 1.2.1", 2);
        room1_2.addChild(office1_2_1);
        org1.addChild(room1_2);

        items.add(org1);

        OrganizationItem org2 = new OrganizationItem("Организация 2", 0);
        OrganizationItem room2_1 = new OrganizationItem("Помещение 2.1", 1);
        org2.addChild(room2_1);
        items.add(org2);

        return items;
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }
}