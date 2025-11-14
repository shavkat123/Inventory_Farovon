package com.inventory.farovon.ui;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.inventory.farovon.R;

public class ScanModeBottomSheetFragment extends BottomSheetDialogFragment {

    private ScanModeListener mListener;

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

        view.findViewById(R.id.button_rfid).setOnClickListener(v -> {
            mListener.onScanModeSelected("RFID");
            dismiss();
        });
        view.findViewById(R.id.button_barcode).setOnClickListener(v -> {
            mListener.onScanModeSelected("BARCODE");
            dismiss();
        });
        view.findViewById(R.id.button_sn).setOnClickListener(v -> {
            mListener.onScanModeSelected("SN");
            dismiss();
        });
        view.findViewById(R.id.button_camera).setOnClickListener(v -> {
            mListener.onScanModeSelected("CAMERA");
            dismiss();
        });
        view.findViewById(R.id.button_manual_input).setOnClickListener(v -> {
            mListener.onScanModeSelected("MANUAL");
            dismiss();
        });

        return view;
    }
}