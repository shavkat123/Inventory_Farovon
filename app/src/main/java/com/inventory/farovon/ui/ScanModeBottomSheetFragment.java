package com.inventory.farovon.ui;

import android.os.Bundle;
import android.app.AlertDialog;
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
            btnRfid.setBackgroundColor(v.getId() == R.id.button_rfid ? ContextCompat.getColor(getContext(), R.color.colorPrimaryBright) : Color.TRANSPARENT);
            btnRfid.setTextColor(v.getId() == R.id.button_rfid ? ContextCompat.getColor(getContext(), android.R.color.white) : ContextCompat.getColor(getContext(), android.R.color.darker_gray));

            btnBarcode.setBackgroundColor(v.getId() == R.id.button_barcode ? ContextCompat.getColor(getContext(), R.color.colorPrimaryBright) : Color.TRANSPARENT);
            btnBarcode.setTextColor(v.getId() == R.id.button_barcode ? ContextCompat.getColor(getContext(), android.R.color.white) : ContextCompat.getColor(getContext(), android.R.color.darker_gray));

            btnSn.setBackgroundColor(v.getId() == R.id.button_sn ? ContextCompat.getColor(getContext(), R.color.colorPrimaryBright) : Color.TRANSPARENT);
            btnSn.setTextColor(v.getId() == R.id.button_sn ? ContextCompat.getColor(getContext(), android.R.color.white) : ContextCompat.getColor(getContext(), android.R.color.darker_gray));

            btnCamera.setBackgroundColor(v.getId() == R.id.button_camera ? ContextCompat.getColor(getContext(), R.color.colorPrimaryBright) : Color.TRANSPARENT);
            btnCamera.setTextColor(v.getId() == R.id.button_camera ? ContextCompat.getColor(getContext(), android.R.color.white) : ContextCompat.getColor(getContext(), android.R.color.darker_gray));
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
}
