package com.inventory.farovon;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;

public class ScanModeBottomSheetFragment extends BottomSheetDialogFragment {

    private Button btnRfid, btnBarcode, btnSn, btnCamera;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.dialog_scan_mode, container, false);

        btnRfid = view.findViewById(R.id.btn_rfid);
        btnBarcode = view.findViewById(R.id.btn_barcode);
        btnSn = view.findViewById(R.id.btn_sn);
        btnCamera = view.findViewById(R.id.btn_camera);

        View.OnClickListener listener = v -> {
            updateButtonSelection(v.getId());
        };

        btnRfid.setOnClickListener(listener);
        btnBarcode.setOnClickListener(listener);
        btnSn.setOnClickListener(listener);
        btnCamera.setOnClickListener(listener);

        // Set initial selection
        updateButtonSelection(R.id.btn_barcode);

        return view;
    }

    private void updateButtonSelection(int selectedId) {
        btnRfid.setSelected(selectedId == R.id.btn_rfid);
        btnBarcode.setSelected(selectedId == R.id.btn_barcode);
        btnSn.setSelected(selectedId == R.id.btn_sn);
        btnCamera.setSelected(selectedId == R.id.btn_camera);
    }
}
