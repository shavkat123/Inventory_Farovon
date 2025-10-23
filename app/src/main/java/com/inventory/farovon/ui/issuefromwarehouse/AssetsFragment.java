package com.inventory.farovon.ui.issuefromwarehouse;

import android.app.AlertDialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.Group;
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
    private RecyclerView recyclerView;
    private Group emptyStateGroup;

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
        if (assets == null) {
            assets = new ArrayList<>();
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_assets, container, false);

        recyclerView = view.findViewById(R.id.assets_recycler_view);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        emptyStateGroup = view.findViewById(R.id.empty_state_group);

        adapter = new AssetAdapter(assets);
        recyclerView.setAdapter(adapter);

        updateVisibility();

        view.findViewById(R.id.button_rfid_scan).setOnClickListener(v -> showScannerPowerDialog());

        view.findViewById(R.id.button_select).setOnClickListener(v -> showSelectAssetsDialog());
        return view;
    }

    private void showScannerPowerDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        LayoutInflater inflater = requireActivity().getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.dialog_scanner_power, null);
        builder.setView(dialogView);

        android.widget.SeekBar powerSeekBar = dialogView.findViewById(R.id.power_seekbar);
        android.widget.TextView powerValueText = dialogView.findViewById(R.id.power_value_text);

        powerSeekBar.setOnSeekBarChangeListener(new android.widget.SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(android.widget.SeekBar seekBar, int progress, boolean fromUser) {
                powerValueText.setText(String.valueOf(progress + 1));
            }

            @Override
            public void onStartTrackingTouch(android.widget.SeekBar seekBar) {
            }

            @Override
            public void onStopTrackingTouch(android.widget.SeekBar seekBar) {
            }
        });

        AlertDialog dialog = builder.create();

        dialogView.findViewById(R.id.confirm_button).setOnClickListener(v -> {
            int power = powerSeekBar.getProgress() + 1;
            Toast.makeText(getContext(), "Установлена мощность сканера: " + power, Toast.LENGTH_SHORT).show();
            dialog.dismiss();
        });

        dialog.show();
    }

    private void updateVisibility() {
        if (assets.isEmpty()) {
            recyclerView.setVisibility(View.GONE);
            emptyStateGroup.setVisibility(View.VISIBLE);
        } else {
            recyclerView.setVisibility(View.VISIBLE);
            emptyStateGroup.setVisibility(View.GONE);
        }
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
            addAssets(selectableAdapter.getSelectedAssets());
            dialog.dismiss();
        });

        dialog.show();
    }

    public void addAssets(List<Asset> newAssets) {
        assets.addAll(newAssets);
        adapter.notifyDataSetChanged();
        updateVisibility();
    }
}
