package com.inventory.farovon;

import android.content.Intent;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.inventory.farovon.data.DocumentRepository;
import com.inventory.farovon.model.IssueDocument;
import com.inventory.farovon.ui.issuefromwarehouse.IssueDocumentAdapter;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class IssueDocumentListActivity extends AppCompatActivity {

    private IssueDocumentAdapter adapter;
    private RecyclerView recyclerView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_issue_document_list);

        setSupportActionBar(findViewById(R.id.toolbar));
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle("Выдача со склада");
        }

        findViewById(R.id.fab).setOnClickListener(v -> {
            startActivity(new Intent(this, IssueFromWarehouseActivity.class));
        });

        recyclerView = findViewById(R.id.recycler_view);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
    }

    @Override
    protected void onResume() {
        super.onResume();
        updateList();
    }

    private void updateList() {
        List<IssueDocument> documents = DocumentRepository.getInstance().getDocuments();
        Map<String, List<IssueDocument>> documentsByDate = new LinkedHashMap<>();
        SimpleDateFormat sdf = new SimpleDateFormat("dd MMMM yyyy", new Locale("ru"));

        for (IssueDocument doc : documents) {
            String dateString = sdf.format(doc.getDate());
            if (!documentsByDate.containsKey(dateString)) {
                documentsByDate.put(dateString, new ArrayList<>());
            }
            documentsByDate.get(dateString).add(doc);
        }

        adapter = new IssueDocumentAdapter(documentsByDate);
        recyclerView.setAdapter(adapter);
    }
}
