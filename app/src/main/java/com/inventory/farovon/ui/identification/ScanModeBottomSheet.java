package com.inventory.farovon.ui.identification;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.inventory.farovon.R;

public class ScanModeBottomSheet extends BottomSheetDialogFragment {

    public static final String TAG = "ScanModeBottomSheet";

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.dialog_scan_mode, container, false);

        // Placeholder listeners for dialog buttons
        view.findViewById(R.id.chip_rfid).setOnClickListener(v -> Toast.makeText(getContext(), "RFID Selected", Toast.LENGTH_SHORT).show());
        view.findViewById(R.id.chip_barcode).setOnClickListener(v -> Toast.makeText(getContext(), "Barcode Selected", Toast.LENGTH_SHORT).show());
        view.findViewById(R.id.chip_sn).setOnClickListener(v -> Toast.makeText(getContext(), "SN Selected", Toast.LENGTH_SHORT).show());
        view.findViewById(R.id.chip_camera).setOnClickListener(v -> Toast.makeText(getContext(), "Camera Selected", Toast.LENGTH_SHORT).show());
        view.findViewById(R.id.button_manual_input).setOnClickListener(v -> {
            Toast.makeText(getContext(), "Manual Input Clicked", Toast.LENGTH_SHORT).show();
            dismiss();
        });

        return view;
    }
}