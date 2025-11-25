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
import com.inventory.farovon.db.WriteOffDao;
import com.inventory.farovon.db.WriteOffDocument;
import com.inventory.farovon.ui.writeoff.WriteOffAdapter;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class WriteOffListActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private WriteOffAdapter adapter;
    private WriteOffDao writeOffDao;
    private ExecutorService databaseExecutor;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_write_off_list);

        androidx.appcompat.widget.Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        recyclerView = findViewById(R.id.documents_recycler_view);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        AppDatabase db = AppDatabase.getDatabase(getApplicationContext());
        writeOffDao = db.writeOffDao();
        databaseExecutor = Executors.newSingleThreadExecutor();

        findViewById(R.id.fab_add_document).setOnClickListener(v -> {
            Intent intent = new Intent(this, WriteOffActivity.class);
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
            List<WriteOffDocument> documents = writeOffDao.getAllDocuments();
            runOnUiThread(() -> {
                adapter = new WriteOffAdapter(documents, documentId -> {
                    Intent intent = new Intent(this, WriteOffDetailActivity.class);
                    intent.putExtra(WriteOffDetailActivity.EXTRA_DOCUMENT_ID, documentId);
                    startActivity(intent);
                });
                recyclerView.setAdapter(adapter);
            });
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.mol_movement_list_menu, menu); // Reuse menu with search

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
            List<WriteOffDocument> documents;
            if (query == null || query.trim().isEmpty()) {
                documents = writeOffDao.getAllDocuments();
            } else {
                documents = writeOffDao.searchDocuments(query);
            }

            runOnUiThread(() -> {
                adapter = new WriteOffAdapter(documents, documentId -> {
                    Intent intent = new Intent(this, WriteOffDetailActivity.class);
                    intent.putExtra(WriteOffDetailActivity.EXTRA_DOCUMENT_ID, documentId);
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
