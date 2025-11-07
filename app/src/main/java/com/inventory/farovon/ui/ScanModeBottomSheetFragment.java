package com.inventory.farovon.ui;

import android.os.Bundle;
import android.app.AlertDialog;
import android.content.Context;
import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.inventory.farovon.R;

public class ScanModeBottomSheetFragment extends BottomSheetDialogFragment {

    public interface ScanModeListener {
        void onRfidSelected();
        void onBarcodeSelected();
    }

    private ScanModeListener mListener;
    private Button btnRfid, btnBarcode, btnSn, btnCamera;

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        if (context instanceof ScanModeListener) {
            mListener = (ScanModeListener) context;
        } else {
            throw new RuntimeException(context.toString() + " must implement ScanModeListener");
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.dialog_scan_mode, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        btnRfid = view.findViewById(R.id.button_rfid);
        btnBarcode = view.findViewById(R.id.button_barcode);
        btnSn = view.findViewById(R.id.button_sn);
        btnCamera = view.findViewById(R.id.button_camera);

        View.OnClickListener tabClickListener = v -> {
            selectTab((Button) v);
            if (v.getId() == R.id.button_rfid) {
                mListener.onRfidSelected();
            } else if (v.getId() == R.id.button_barcode) {
                mListener.onBarcodeSelected();
            }
        };

        btnRfid.setOnClickListener(tabClickListener);
        btnBarcode.setOnClickListener(tabClickListener);
        btnSn.setOnClickListener(tabClickListener);
        btnCamera.setOnClickListener(tabClickListener);

        // Set default selection
        selectTab(btnRfid);

        view.findViewById(R.id.button_scanner_settings).setOnClickListener(v -> showScannerPowerDialog());
    }

    private void selectTab(Button selectedButton) {
        btnRfid.setSelected(false);
        btnBarcode.setSelected(false);
        btnSn.setSelected(false);
        btnCamera.setSelected(false);

        selectedButton.setSelected(true);
    }

    private void showScannerPowerDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        LayoutInflater inflater = requireActivity().getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.dialog_scanner_power, null);
        builder.setView(dialogView);
        builder.create().show();
    }
}
