package com.inventory.farovon.ui.issuefromwarehouse;

import android.app.AlertDialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.inventory.farovon.R;
import com.inventory.farovon.model.Asset;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class AssetsFragment extends Fragment {

    private List<Asset> assets = new ArrayList<>();
    private AssetAdapter adapter;

    public static AssetsFragment newInstance(List<Asset> assets) {
        AssetsFragment fragment = new AssetsFragment();
        Bundle args = new Bundle();
        args.putSerializable("ASSETS", (Serializable) assets);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            assets = (List<Asset>) getArguments().getSerializable("ASSETS");
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_assets, container, false);

        RecyclerView recyclerView = view.findViewById(R.id.assets_recycler_view);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        adapter = new AssetAdapter(assets);
        recyclerView.setAdapter(adapter);

        view.findViewById(R.id.button_rfid_scan).setOnClickListener(v ->
                Toast.makeText(getContext(), "RFID Scan clicked", Toast.LENGTH_SHORT).show());

        view.findViewById(R.id.button_select).setOnClickListener(v -> showSelectAssetsDialog());

        return view;
    }

    private void showSelectAssetsDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        LayoutInflater inflater = requireActivity().getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.dialog_select_assets, null);
        builder.setView(dialogView);

        RecyclerView recyclerView = dialogView.findViewById(R.id.assets_recycler_view);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        // Dummy data for available assets
        List<Asset> availableAssets = new ArrayList<>();
        availableAssets.add(new Asset("Принтер HP", "INV004", "SN004", "Склад 1", "Технопарк", "На складе"));
        availableAssets.add(new Asset("Сканер Canon", "INV005", "SN005", "Склад 1", "Технопарк", "На складе"));

        SelectableAssetAdapter selectableAdapter = new SelectableAssetAdapter(availableAssets);
        recyclerView.setAdapter(selectableAdapter);

        AlertDialog dialog = builder.create();
        dialogView.findViewById(R.id.confirm_button).setOnClickListener(v -> {
            assets.addAll(selectableAdapter.getSelectedAssets());
            adapter.notifyDataSetChanged();
            dialog.dismiss();
        });

        dialog.show();
    }
}
