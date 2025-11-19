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

    // Allow setting the listener programmatically
    public void setScanModeListener(ScanModeListener listener) {
        mListener = listener;
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        if (mListener == null && context instanceof ScanModeListener) {
            mListener = (ScanModeListener) context;
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.dialog_scan_mode, container, false);

        View.OnClickListener listener = v -> {
            if (mListener != null) {
                if (v.getId() == R.id.button_rfid) {
                    mListener.onScanModeSelected("RFID");
                } else if (v.getId() == R.id.button_barcode) {
                    mListener.onScanModeSelected("BARCODE");
                } else if (v.getId() == R.id.button_sn) {
                    mListener.onScanModeSelected("SN");
                } else if (v.getId() == R.id.button_camera) {
                    mListener.onScanModeSelected("CAMERA");
                } else if (v.getId() == R.id.button_manual_input) {
                    mListener.onScanModeSelected("MANUAL");
                }
            }
            dismiss();
        };

        view.findViewById(R.id.button_rfid).setOnClickListener(listener);
        view.findViewById(R.id.button_barcode).setOnClickListener(listener);
        view.findViewById(R.id.button_sn).setOnClickListener(listener);
        view.findViewById(R.id.button_camera).setOnClickListener(listener);
        view.findViewById(R.id.button_manual_input).setOnClickListener(listener);

        return view;
    }
}