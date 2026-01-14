package com.inventory.farovon.ui;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.google.android.material.button.MaterialButtonToggleGroup;
import com.inventory.farovon.R;

public class ScanModeBottomSheetFragment extends BottomSheetDialogFragment {

    private ScanModeListener mListener;
    private String currentScanMode;

    public static ScanModeBottomSheetFragment newInstance(String currentScanMode) {
        ScanModeBottomSheetFragment fragment = new ScanModeBottomSheetFragment();
        Bundle args = new Bundle();
        args.putString("currentScanMode", currentScanMode);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            currentScanMode = getArguments().getString("currentScanMode", "NONE");
        }
    }

    public interface ScanModeListener {
        void onScanModeSelected(String mode);
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        if (context instanceof ScanModeListener) {
            mListener = (ScanModeListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement ScanModeListener");
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.dialog_scan_mode, container, false);

        MaterialButtonToggleGroup toggleGroup = view.findViewById(R.id.toggle_group_scan_mode);

        // Set the initial checked button based on currentScanMode
        if (currentScanMode != null) {
            switch (currentScanMode) {
                case "RFID":
                    toggleGroup.check(R.id.button_rfid);
                    break;
                case "BARCODE":
                    toggleGroup.check(R.id.button_barcode);
                    break;
                case "SN":
                    toggleGroup.check(R.id.button_sn);
                    break;
                case "CAMERA":
                    toggleGroup.check(R.id.button_camera);
                    break;
                case "MANUAL":
                    toggleGroup.check(R.id.button_manual_input);
                    break;
            }
        }


        toggleGroup.addOnButtonCheckedListener((group, checkedId, isChecked) -> {
            if (isChecked) {
                String selectedMode = "NONE";
                if (checkedId == R.id.button_rfid) {
                    selectedMode = "RFID";
                } else if (checkedId == R.id.button_barcode) {
                    selectedMode = "BARCODE";
                } else if (checkedId == R.id.button_sn) {
                    selectedMode = "SN";
                } else if (checkedId == R.id.button_camera) {
                    selectedMode = "CAMERA";
                } else if (checkedId == R.id.button_manual_input) {
                    selectedMode = "MANUAL";
                }
                mListener.onScanModeSelected(selectedMode);
                dismiss();
            }
        });

        return view;
    }
}
