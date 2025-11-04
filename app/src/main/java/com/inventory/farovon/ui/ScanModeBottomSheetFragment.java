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

        final Button btnRfid = view.findViewById(R.id.button_rfid);
        final Button btnBarcode = view.findViewById(R.id.button_barcode);
        final Button btnSn = view.findViewById(R.id.button_sn);
        final Button btnCamera = view.findViewById(R.id.button_camera);

        View.OnClickListener tabClickListener = v -> {
            updateTabSelection(v.getId());
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

        view.findViewById(R.id.button_scanner_settings).setOnClickListener(v -> showScannerPowerDialog());
    }

    private void showScannerPowerDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        LayoutInflater inflater = requireActivity().getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.dialog_scanner_power, null);
        builder.setView(dialogView);
        builder.create().show();
    }

    private void updateTabSelection(int selectedId) {
        View view = getView();
        if (view == null) return;

        final Button btnRfid = view.findViewById(R.id.button_rfid);
        final Button btnBarcode = view.findViewById(R.id.button_barcode);
        final Button btnSn = view.findViewById(R.id.button_sn);
        final Button btnCamera = view.findViewById(R.id.button_camera);

        btnRfid.setBackgroundResource(selectedId == R.id.button_rfid ? R.drawable.tab_background_active : android.R.color.transparent);
        btnRfid.setTextColor(selectedId == R.id.button_rfid ? ContextCompat.getColor(getContext(), android.R.color.white) : ContextCompat.getColor(getContext(), android.R.color.darker_gray));

        btnBarcode.setBackgroundResource(selectedId == R.id.button_barcode ? R.drawable.tab_background_active : android.R.color.transparent);
        btnBarcode.setTextColor(selectedId == R.id.button_barcode ? ContextCompat.getColor(getContext(), android.R.color.white) : ContextCompat.getColor(getContext(), android.R.color.darker_gray));

        btnSn.setBackgroundResource(selectedId == R.id.button_sn ? R.drawable.tab_background_active : android.R.color.transparent);
        btnSn.setTextColor(selectedId == R.id.button_sn ? ContextCompat.getColor(getContext(), android.R.color.white) : ContextCompat.getColor(getContext(), android.R.color.darker_gray));

        btnCamera.setBackgroundResource(selectedId == R.id.button_camera ? R.drawable.tab_background_active : android.R.color.transparent);
        btnCamera.setTextColor(selectedId == R.id.button_camera ? ContextCompat.getColor(getContext(), android.R.color.white) : ContextCompat.getColor(getContext(), android.R.color.darker_gray));
    }
}
