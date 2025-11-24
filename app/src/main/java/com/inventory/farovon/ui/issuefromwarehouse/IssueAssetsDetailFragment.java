package com.inventory.farovon.ui.issuefromwarehouse;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.inventory.farovon.R;
import com.inventory.farovon.db.AppDatabase;
import com.inventory.farovon.db.InventoryItemDao;
import com.inventory.farovon.db.InventoryItemEntity;
import com.inventory.farovon.db.IssueDao;
import com.inventory.farovon.db.IssueItem;
import com.inventory.farovon.ui.molmovement.AssetDetailAdapter;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;

public class IssueAssetsDetailFragment extends Fragment {

    private static final String ARG_DOCUMENT_ID = "document_id";

    private List<InventoryItemEntity> assetList = new ArrayList<>();
    private AssetDetailAdapter adapter;
    private RecyclerView recyclerView;
    private ExecutorService databaseExecutor;
    private InventoryItemDao inventoryItemDao;
    private IssueDao issueDao;
    private Handler handler = new Handler(Looper.getMainLooper());

    public static IssueAssetsDetailFragment newInstance(long documentId) {
        IssueAssetsDetailFragment fragment = new IssueAssetsDetailFragment();
        Bundle args = new Bundle();
        args.putLong(ARG_DOCUMENT_ID, documentId);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        AppDatabase db = AppDatabase.getDatabase(requireContext().getApplicationContext());
        inventoryItemDao = db.inventoryItemDao();
        issueDao = db.issueDao();
        databaseExecutor = Executors.newSingleThreadExecutor();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_assets_detail, container, false);
        recyclerView = view.findViewById(R.id.assets_recycler_view);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        adapter = new AssetDetailAdapter(assetList);
        recyclerView.setAdapter(adapter);

        if (getArguments() != null) {
            long documentId = getArguments().getLong(ARG_DOCUMENT_ID);
            loadAssetsForDocument(documentId);
        }

        return view;
    }

    private void loadAssetsForDocument(long documentId) {
        databaseExecutor.execute(() -> {
            List<IssueItem> items = issueDao.getItemsForDocument(documentId);
            if (items.isEmpty()) {
                return;
            }
            List<String> rfids = items.stream().map(item -> item.rfid).collect(Collectors.toList());
            List<InventoryItemEntity> loadedAssets = inventoryItemDao.findByRfidList(rfids);

            // Handle unknown items
            Set<String> foundRfids = loadedAssets.stream().map(asset -> asset.rf).collect(Collectors.toSet());
            for (String rfid : rfids) {
                if (!foundRfids.contains(rfid)) {
                    InventoryItemEntity unknown = new InventoryItemEntity();
                    unknown.rf = rfid;
                    unknown.name = "Неизвестный объект";
                    loadedAssets.add(unknown);
                }
            }

            handler.post(() -> {
                assetList.clear();
                assetList.addAll(loadedAssets);
                adapter.notifyDataSetChanged();
            });
        });
    }
}
