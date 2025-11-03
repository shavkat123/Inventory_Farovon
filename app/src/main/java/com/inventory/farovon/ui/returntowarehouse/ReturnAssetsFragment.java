package com.inventory.farovon.ui.returntowarehouse;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.media.AudioManager;
import android.media.ToneGenerator;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;
import com.google.android.material.button.MaterialButton;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.constraintlayout.widget.Group;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.inventory.farovon.Nomenclature;
import com.inventory.farovon.R;
import com.rscja.deviceapi.RFIDWithUHFUART;

import java.util.HashSet;
import java.util.Set;

public class ReturnAssetsFragment extends Fragment {

    private RFIDWithUHFUART mReader;
    private boolean isScanning = false;
    private final Handler handler = new Handler(Looper.getMainLooper());
    private MaterialButton btnScan;
    private ToneGenerator toneGenerator;
    private RecyclerView recyclerView;
    private ReturnAssetsAdapter adapter;
    private Group emptyStateGroup;
    private final Set<String> scannedEpcs = new HashSet<>();

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_return_assets, container, false);

        btnScan = view.findViewById(R.id.button_rfid_scan);
        recyclerView = view.findViewById(R.id.assets_recycler_view);
        emptyStateGroup = view.findViewById(R.id.empty_state_group);

        setupRecyclerView();
        updateUiVisibility();

        try {
            mReader = RFIDWithUHFUART.getInstance();
        } catch (Exception e) {
            Toast.makeText(getContext(), "SDK init error: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }

        btnScan.setOnClickListener(v -> {
            if (!isScanning) {
                startScanning();
            } else {
                stopScanning();
            }
        });

        view.findViewById(R.id.button_select).setOnClickListener(v ->
                Toast.makeText(getContext(), "Select clicked", Toast.LENGTH_SHORT).show());

        toneGenerator = new ToneGenerator(AudioManager.STREAM_MUSIC, 100);

        return view;
    }

    private void setupRecyclerView() {
        adapter = new ReturnAssetsAdapter();
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerView.setAdapter(adapter);
    }

    private void updateUiVisibility() {
        if (adapter.getItemCount() > 0) {
            recyclerView.setVisibility(View.VISIBLE);
            emptyStateGroup.setVisibility(View.GONE);
        } else {
            recyclerView.setVisibility(View.GONE);
            emptyStateGroup.setVisibility(View.VISIBLE);
        }
    }

    private void startScanning() {
        scannedEpcs.clear();
        if (mReader == null) {
            Toast.makeText(getContext(), "Ридер не инициализирован", Toast.LENGTH_SHORT).show();
            return;
        }
        if (mReader.init(getContext())) {
            mReader.setPower(30);
            boolean ok = mReader.startInventoryTag();
            if (!ok) {
                Toast.makeText(getContext(), "Не удалось запустить инвентарь", Toast.LENGTH_SHORT).show();
                return;
            }
            isScanning = true;
            // btnScan.setText("Стоп"); // Иконка изменится сама
            handler.post(pollRunnable);
        } else {
            Toast.makeText(getContext(), "Ошибка инициализации ридера", Toast.LENGTH_SHORT).show();
        }
    }

    private void stopScanning() {
        if (isScanning && mReader != null) {
            try { mReader.stopInventory(); } catch (Exception ignored) {}
        }
        isScanning = false;
        // btnScan.setText("Сканировать"); // Иконка изменится сама
        handler.removeCallbacks(pollRunnable);
        if (mReader != null) mReader.free();
    }

    private final Runnable pollRunnable = new Runnable() {
        @Override public void run() {
            if (!isScanning || mReader == null) return;

            com.rscja.deviceapi.entity.UHFTAGInfo info;
            int burst = 0;
            while ((info = mReader.readTagFromBuffer()) != null) {
                String epc = info.getEPC();
                if (epc != null && !scannedEpcs.contains(epc)) {
                    scannedEpcs.add(epc);
                    // TODO: Fetch Nomenclature details from DB or server by EPC
                    // For now, creating a dummy item
                    Nomenclature nomenclature = new Nomenclature("Unknown", epc, epc, "", "");
                    getActivity().runOnUiThread(() -> {
                        adapter.addItem(nomenclature);
                        updateUiVisibility();
                        ((ReturnToWarehouseActivity) getActivity()).updateTabTitle(adapter.getItemCount());
                        toneGenerator.startTone(ToneGenerator.TONE_PROP_ACK, 150);
                    });
                }
                if (++burst > 200) break;
            }
            handler.postDelayed(this, 60);
        }
    };

    @Override
    public void onPause() {
        super.onPause();
        stopScanning();
    }

    @Override
    public void onDestroy() {
        stopScanning();
        if (toneGenerator != null) {
            toneGenerator.release();
            toneGenerator = null;
        }
        super.onDestroy();
    }
}