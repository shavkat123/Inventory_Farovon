package com.inventory.farovon.ui.molmovement;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.inventory.farovon.CameraScanActivity;
import com.inventory.farovon.R;

import java.util.ArrayList;
import java.util.List;

import static android.app.Activity.RESULT_OK;

public class MolAssetsFragment extends Fragment {

    private ActivityResultLauncher<Intent> cameraLauncher;
    private List<String> scannedBarcodes = new ArrayList<>();
    private BarcodeAdapter adapter;
    private RecyclerView recyclerView;
    private View emptyStateView;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        cameraLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                        String scannedCode = result.getData().getStringExtra("scanned_code");
                        if (scannedCode != null) {
                            scannedBarcodes.add(scannedCode);
                            adapter.notifyItemInserted(scannedBarcodes.size() - 1);
                            updateUI();
                        }
                    }
                });
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_mol_assets, container, false);

        recyclerView = view.findViewById(R.id.assets_recycler_view);
        emptyStateView = view.findViewById(R.id.empty_state_group);

        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        adapter = new BarcodeAdapter(scannedBarcodes);
        recyclerView.setAdapter(adapter);

        view.findViewById(R.id.button_barcode_scan).setOnClickListener(v -> {
            Intent intent = new Intent(getActivity(), CameraScanActivity.class);
            cameraLauncher.launch(intent);
        });

        updateUI();

        return view;
    }

    private void updateUI() {
        if (scannedBarcodes.isEmpty()) {
            recyclerView.setVisibility(View.GONE);
            emptyStateView.setVisibility(View.VISIBLE);
        } else {
            recyclerView.setVisibility(View.VISIBLE);
            emptyStateView.setVisibility(View.GONE);
        }
    }
}