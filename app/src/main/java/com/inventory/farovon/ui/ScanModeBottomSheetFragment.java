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

        MaterialButtonToggleGroup toggleGroupTop = view.findViewById(R.id.toggle_group_scan_mode_top);
        MaterialButtonToggleGroup toggleGroupBottom = view.findViewById(R.id.toggle_group_scan_mode_bottom);

        // Set the initial checked button based on currentScanMode
        if (currentScanMode != null) {
            switch (currentScanMode) {
                case "RFID":
                    toggleGroupTop.check(R.id.button_rfid);
                    break;
                case "BARCODE":
                    toggleGroupTop.check(R.id.button_barcode);
                    break;
                case "SN":
                    toggleGroupTop.check(R.id.button_sn);
                    break;
                case "CAMERA":
                    toggleGroupTop.check(R.id.button_camera);
                    break;
                case "MANUAL":
                    toggleGroupBottom.check(R.id.button_manual_input);
                    break;
            }
        }


        toggleGroupTop.addOnButtonCheckedListener((group, checkedId, isChecked) -> {
            if (isChecked) {
                toggleGroupBottom.clearChecked();
                String selectedMode = "NONE";
                if (checkedId == R.id.button_rfid) {
                    selectedMode = "RFID";
                } else if (checkedId == R.id.button_barcode) {
                    selectedMode = "BARCODE";
                } else if (checkedId == R.id.button_sn) {
                    selectedMode = "SN";
                } else if (checkedId == R.id.button_camera) {
                    selectedMode = "CAMERA";
                }
                mListener.onScanModeSelected(selectedMode);
                dismiss();
            }
        });

        toggleGroupBottom.addOnButtonCheckedListener((group, checkedId, isChecked) -> {
            if (isChecked) {
                toggleGroupTop.clearChecked();
                String selectedMode = "NONE";
                if (checkedId == R.id.button_manual_input) {
                    selectedMode = "MANUAL";
                }
                mListener.onScanModeSelected(selectedMode);
                dismiss();
            }
        });

        return view;
    }
}