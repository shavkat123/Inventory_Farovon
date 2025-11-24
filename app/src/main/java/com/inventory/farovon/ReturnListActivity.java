package com.inventory.farovon;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.inventory.farovon.db.AppDatabase;
import com.inventory.farovon.db.ReturnDao;
import com.inventory.farovon.db.ReturnDocument;
import com.inventory.farovon.ui.returntowarehouse.ReturnAdapter;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ReturnListActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private ReturnAdapter adapter;
    private ReturnDao returnDao;
    private ExecutorService databaseExecutor;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_mol_movement_list); // Reusing layout

        androidx.appcompat.widget.Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("Возврат на склад");
        }

        recyclerView = findViewById(R.id.documents_recycler_view);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        AppDatabase db = AppDatabase.getDatabase(getApplicationContext());
        returnDao = db.returnDao();
        databaseExecutor = Executors.newSingleThreadExecutor();

        findViewById(R.id.fab_add_document).setOnClickListener(v -> {
            Intent intent = new Intent(this, ReturnToWarehouseActivity.class);
            startActivity(intent);
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadDocuments();
    }

    private void loadDocuments() {
        databaseExecutor.execute(() -> {
            List<ReturnDocument> documents = returnDao.getAllDocuments();
            runOnUiThread(() -> {
                adapter = new ReturnAdapter(documents, documentId -> {
                    Intent intent = new Intent(this, ReturnDetailActivity.class);
                    intent.putExtra(ReturnDetailActivity.EXTRA_DOCUMENT_ID, documentId);
                    startActivity(intent);
                });
                recyclerView.setAdapter(adapter);
            });
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.mol_movement_list_menu, menu);

        MenuItem searchItem = menu.findItem(R.id.action_search);
        SearchView searchView = (SearchView) searchItem.getActionView();

        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                performSearch(query);
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                performSearch(newText);
                return false;
            }
        });

        return true;
    }

    private void performSearch(String query) {
        databaseExecutor.execute(() -> {
            List<ReturnDocument> documents;
            if (query == null || query.trim().isEmpty()) {
                documents = returnDao.getAllDocuments();
            } else {
                documents = returnDao.searchDocuments(query);
            }

            runOnUiThread(() -> {
                adapter = new ReturnAdapter(documents, documentId -> {
                    Intent intent = new Intent(this, ReturnDetailActivity.class);
                    intent.putExtra(ReturnDetailActivity.EXTRA_DOCUMENT_ID, documentId);
                    startActivity(intent);
                });
                recyclerView.setAdapter(adapter);
            });
        });
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }
}
