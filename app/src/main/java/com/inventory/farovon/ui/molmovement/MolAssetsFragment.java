package com.inventory.farovon.ui.molmovement;

import android.content.Intent;
import android.media.AudioManager;
import android.media.ToneGenerator;
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
import com.inventory.farovon.db.AppDatabase;
import com.inventory.farovon.db.InventoryItemDao;
import com.inventory.farovon.db.InventoryItemEntity;
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

    private List<InventoryItemEntity> scannedItems = new ArrayList<>();
    private AssetDetailAdapter adapter;
    private RecyclerView recyclerView;
    private View emptyStateView;

    private RFIDWithUHFUART mReader;
    private Handler handler = new Handler(Looper.getMainLooper());
    private ExecutorService rfidExecutor;
    private ExecutorService databaseExecutor;
    private InventoryItemDao inventoryItemDao;
    private Set<String> foundEpcSet = new HashSet<>();
    private boolean isRfidScanning = false;
    private ToneGenerator toneGenerator;
    private OnItemCountChangeListener countChangeListener;

    public interface OnItemCountChangeListener {
        void onItemCountChanged(int count);
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (getActivity() instanceof OnItemCountChangeListener) {
            countChangeListener = (OnItemCountChangeListener) getActivity();
        }

        AppDatabase db = AppDatabase.getDatabase(requireContext().getApplicationContext());
        inventoryItemDao = db.inventoryItemDao();
        databaseExecutor = Executors.newSingleThreadExecutor();
        toneGenerator = new ToneGenerator(AudioManager.STREAM_MUSIC, 100);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_mol_assets, container, false);

        recyclerView = view.findViewById(R.id.assets_recycler_view);
        emptyStateView = view.findViewById(R.id.empty_state_group);

        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        adapter = new AssetDetailAdapter(scannedItems);
        recyclerView.setAdapter(adapter);

        view.findViewById(R.id.button_scan).setOnClickListener(v -> {
            ScanModeBottomSheetFragment bottomSheet = new ScanModeBottomSheetFragment();
            bottomSheet.setScanModeListener(this);
            bottomSheet.show(getParentFragmentManager(), bottomSheet.getTag());
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

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (toneGenerator != null) {
            toneGenerator.release();
            toneGenerator = null;
        }
    }

    private void initRfidReader() {
        try {
            mReader = RFIDWithUHFUART.getInstance();
            mReader.init(requireContext().getApplicationContext());
        } catch (Exception e) {
            Log.e(TAG, "Failed to initialize RFID Reader", e);
        }
    }

    private void startRfidScanning() {
        if (mReader == null) {
            Log.e(TAG, "Reader not initialized");
            return;
        }
        isRfidScanning = true;
        Log.i(TAG, "Starting RFID scanning");

        mReader.startInventoryTag();
        rfidExecutor = Executors.newSingleThreadExecutor();
        rfidExecutor.execute(() -> {
            while (isRfidScanning) {
                UHFTAGInfo tag = mReader.readTagFromBuffer();
                if (tag != null) {
                    String epc = tag.getEPC();
                    Log.d(TAG, "RFID Tag Found: " + epc);
                    if (foundEpcSet.add(epc)) {
                        if (toneGenerator != null) {
                            toneGenerator.startTone(ToneGenerator.TONE_PROP_BEEP);
                        }
                        searchAndAddItem(epc);
                    }
                }
            }
        });
    }

    private void searchAndAddItem(String rfid) {
        databaseExecutor.execute(() -> {
            List<InventoryItemEntity> foundItems = inventoryItemDao.findByRfid(rfid);
            handler.post(() -> {
                if (foundItems != null && !foundItems.isEmpty()) {
                    scannedItems.addAll(foundItems);
                } else {
                    InventoryItemEntity unknownItem = new InventoryItemEntity();
                    unknownItem.rf = rfid;
                    unknownItem.name = "Неизвестный объект";
                    scannedItems.add(unknownItem);
                }
                adapter.notifyDataSetChanged();
                updateUI();
            });
        });
    }

    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_F9 || keyCode == KeyEvent.KEYCODE_F10 || keyCode == 280 || keyCode == 293) {
            if (!isRfidScanning) {
                startRfidScanning();
            }
            return true;
        }
        return false;
    }

    public boolean onKeyUp(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_F9 || keyCode == KeyEvent.KEYCODE_F10 || keyCode == 280 || keyCode == 293) {
            stopRfidScanning();
            return true;
        }
        return false;
    }

    private void stopRfidScanning() {
        if (isRfidScanning) {
            isRfidScanning = false;
            if (mReader != null) mReader.stopInventory();
            if (rfidExecutor != null && !rfidExecutor.isShutdown()) rfidExecutor.shutdown();
        }
    }

    private void updateUI() {
        boolean isEmpty = scannedItems.isEmpty();
        recyclerView.setVisibility(isEmpty ? View.GONE : View.VISIBLE);
        emptyStateView.setVisibility(isEmpty ? View.VISIBLE : View.GONE);

        if (countChangeListener != null) {
            countChangeListener.onItemCountChanged(scannedItems.size());
        }
    }

    public List<String> getScannedBarcodes() {
        List<String> rfids = new ArrayList<>();
        for (InventoryItemEntity item : scannedItems) {
            if (item.rf != null && !item.rf.isEmpty()) {
                rfids.add(item.rf);
            }
        }
        return rfids;
    }

    @Override
    public void onScanModeSelected(String mode) {
        if ("RFID".equals(mode)) {
            Toast.makeText(getContext(), "Режим RFID. Нажмите курок.", Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(getContext(), mode + " - в разработке", Toast.LENGTH_SHORT).show();
        }
    }
}
