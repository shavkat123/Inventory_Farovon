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
import com.inventory.farovon.db.IssueDao;
import com.inventory.farovon.db.IssueDocument;
import com.inventory.farovon.ui.issuefromwarehouse.IssueAdapter;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class IssueListActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private IssueAdapter adapter;
    private IssueDao issueDao;
    private ExecutorService databaseExecutor;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_mol_movement_list); // Reusing layout

        androidx.appcompat.widget.Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("Выдача со склада");
        }

        recyclerView = findViewById(R.id.documents_recycler_view);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        AppDatabase db = AppDatabase.getDatabase(getApplicationContext());
        issueDao = db.issueDao();
        databaseExecutor = Executors.newSingleThreadExecutor();

        findViewById(R.id.fab_add_document).setOnClickListener(v -> {
            Intent intent = new Intent(this, IssueFromWarehouseActivity.class);
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
            List<IssueDocument> documents = issueDao.getAllDocuments();
            runOnUiThread(() -> {
                adapter = new IssueAdapter(documents, documentId -> {
                    Intent intent = new Intent(this, IssueDetailActivity.class);
                    intent.putExtra(IssueDetailActivity.EXTRA_DOCUMENT_ID, documentId);
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
            List<IssueDocument> documents;
            if (query == null || query.trim().isEmpty()) {
                documents = issueDao.getAllDocuments();
            } else {
                documents = issueDao.searchDocuments(query);
            }

            runOnUiThread(() -> {
                adapter = new IssueAdapter(documents, documentId -> {
                    Intent intent = new Intent(this, IssueDetailActivity.class);
                    intent.putExtra(IssueDetailActivity.EXTRA_DOCUMENT_ID, documentId);
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
