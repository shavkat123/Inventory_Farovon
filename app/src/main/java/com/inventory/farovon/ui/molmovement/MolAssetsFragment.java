package com.inventory.farovon.ui.molmovement;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.KeyEvent;
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
import com.inventory.farovon.ui.ScanModeBottomSheetFragment;
import com.rscja.deviceapi.RFIDWithUHFUART;
import com.rscja.deviceapi.entity.UHFTAGInfo;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static android.app.Activity.RESULT_OK;

public class MolAssetsFragment extends Fragment implements ScanModeBottomSheetFragment.ScanModeListener {

    private static final String TAG = "MolAssetsFragment";

    private ActivityResultLauncher<Intent> cameraLauncher;
    private List<String> scannedItems = new ArrayList<>();
    private BarcodeAdapter adapter;
    private RecyclerView recyclerView;
    private View emptyStateView;

    private RFIDWithUHFUART mReader;
    private Handler handler = new Handler(Looper.getMainLooper());
    private ExecutorService rfidExecutor;
    private Set<String> foundEpcSet = new HashSet<>();
    private boolean isRfidScanning = false;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        cameraLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                        String scannedCode = result.getData().getStringExtra("scanned_code");
                        if (scannedCode != null) {
                            scannedItems.add(scannedCode);
                            adapter.notifyItemInserted(scannedItems.size() - 1);
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
        adapter = new BarcodeAdapter(scannedItems);
        recyclerView.setAdapter(adapter);

        view.findViewById(R.id.button_scan).setOnClickListener(v -> {
            ScanModeBottomSheetFragment bottomSheet = new ScanModeBottomSheetFragment();
            bottomSheet.setScanModeListener(this);
            bottomSheet.show(getParentFragmentManager(), bottomSheet.getTag());
        });

        // Listen for hardware trigger key events
        view.setFocusableInTouchMode(true);
        view.requestFocus();
        view.setOnKeyListener((v, keyCode, event) -> {
            if (event.getAction() == KeyEvent.ACTION_DOWN && (keyCode == KeyEvent.KEYCODE_F9 || keyCode == KeyEvent.KEYCODE_F10 || keyCode == 280 || keyCode == 293)) {
                if (!isRfidScanning) {
                    startRfidScanning();
                }
                return true;
            }
            if (event.getAction() == KeyEvent.ACTION_UP && (keyCode == KeyEvent.KEYCODE_F9 || keyCode == KeyEvent.KEYCODE_F10 || keyCode == 280 || keyCode == 293)) {
                stopRfidScanning();
                return true;
            }
            return false;
        });

        updateUI();

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        initRfidReader();
    }

    @Override
    public void onPause() {
        super.onPause();
        stopRfidScanning();
        if (mReader != null) {
            mReader.free();
        }
    }

    private void initRfidReader() {
        try {
            mReader = RFIDWithUHFUART.getInstance();
            mReader.init(requireContext().getApplicationContext());
            Log.i(TAG, "RFID Reader initialized successfully.");
        } catch (Exception e) {
            Log.e(TAG, "Failed to initialize RFID Reader", e);
            Toast.makeText(getContext(), "Ошибка инициализации RFID", Toast.LENGTH_SHORT).show();
        }
    }

    private void startRfidScanning() {
        if (mReader == null) {
            Toast.makeText(getContext(), "RFID ридер не инициализирован", Toast.LENGTH_SHORT).show();
            return;
        }
        isRfidScanning = true;
        foundEpcSet.clear();
        scannedItems.clear();
        adapter.notifyDataSetChanged();

        mReader.startInventoryTag();
        rfidExecutor = Executors.newSingleThreadExecutor();
        rfidExecutor.execute(() -> {
            while (isRfidScanning) {
                UHFTAGInfo tag = mReader.readTagFromBuffer();
                if (tag != null) {
                    String epc = tag.getEPC();
                    boolean isNew = foundEpcSet.add(epc);
                    if (isNew) {
                        handler.post(() -> {
                            scannedItems.add(epc);
                            adapter.notifyItemInserted(scannedItems.size() - 1);
                            updateUI();
                        });
                    }
                }
            }
        });
        Log.i(TAG, "RFID scanning started.");
    }

    private void stopRfidScanning() {
        if (isRfidScanning) {
            isRfidScanning = false;
            if (mReader != null) {
                mReader.stopInventory();
            }
            if (rfidExecutor != null && !rfidExecutor.isShutdown()) {
                rfidExecutor.shutdown();
            }
            Log.i(TAG, "RFID scanning stopped.");
        }
    }


    private void updateUI() {
        if (scannedItems.isEmpty()) {
            recyclerView.setVisibility(View.GONE);
            emptyStateView.setVisibility(View.VISIBLE);
        } else {
            recyclerView.setVisibility(View.VISIBLE);
            emptyStateView.setVisibility(View.GONE);
        }
    }

    public List<String> getScannedBarcodes() {
        return scannedItems;
    }

    @Override
    public void onScanModeSelected(String mode) {
        switch (mode) {
            case "RFID":
                Toast.makeText(getContext(), "Режим RFID. Нажмите курок.", Toast.LENGTH_SHORT).show();
                break;
            case "CAMERA":
                Intent intent = new Intent(getActivity(), CameraScanActivity.class);
                cameraLauncher.launch(intent);
                break;
            default:
                Toast.makeText(getContext(), mode + " - в разработке", Toast.LENGTH_SHORT).show();
                break;
        }
    }
}
