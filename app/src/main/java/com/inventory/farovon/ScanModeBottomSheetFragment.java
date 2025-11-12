package com.inventory.farovon;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;

import com.google.android.material.button.MaterialButtonToggleGroup;
import android.content.Context;
import android.app.AlertDialog;
import android.widget.ImageButton;

    public interface ScanModeListener {
        void onScanModeSelected(int modeId);
    }

    private ScanModeListener mListener;

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        try {
            mListener = (ScanModeListener) context;
        } catch (ClassCastException e) {
            throw new ClassCastException(context.toString() + " must implement ScanModeListener");
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.dialog_scan_mode, container, false);

        MaterialButtonToggleGroup toggleGroup = view.findViewById(R.id.toggle_group_scan_mode);

        toggleGroup.check(R.id.btn_barcode);

        toggleGroup.addOnButtonCheckedListener((group, checkedId, isChecked) -> {
            if (isChecked) {
                mListener.onScanModeSelected(checkedId);
                dismiss();
            }
        });

        ImageButton settingsButton = view.findViewById(R.id.btn_scanner_settings);
        settingsButton.setOnClickListener(v -> showPowerDialog());

        return view;
    }

    private void showPowerDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
        LayoutInflater inflater = requireActivity().getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.dialog_scanner_power, null);
        builder.setView(dialogView);

        final android.widget.SeekBar seekBar = dialogView.findViewById(R.id.seekbar_power);
        final android.widget.TextView powerValue = dialogView.findViewById(R.id.tv_power_value);

        android.content.SharedPreferences prefs = android.preference.PreferenceManager.getDefaultSharedPreferences(requireContext());
        int currentPower = prefs.getInt("scanner_power", 15);

        seekBar.setProgress(currentPower - 1);
        powerValue.setText("Мощность: " + currentPower);

        seekBar.setOnSeekBarChangeListener(new android.widget.SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(android.widget.SeekBar seekBar, int progress, boolean fromUser) {
                powerValue.setText("Мощность: " + (progress + 1));
            }

            @Override
            public void onStartTrackingTouch(android.widget.SeekBar seekBar) {}

            @Override
            public void onStopTrackingTouch(android.widget.SeekBar seekBar) {}
        });

        builder.setPositiveButton("OK", (dialog, which) -> {
            int newPower = seekBar.getProgress() + 1;
            android.content.SharedPreferences.Editor editor = prefs.edit();
            editor.putInt("scanner_power", newPower);
            editor.apply();
            android.widget.Toast.makeText(requireContext(), "Мощность установлена: " + newPower, android.widget.Toast.LENGTH_SHORT).show();
        });
        builder.setNegativeButton("Отмена", (dialog, which) -> dialog.cancel());

        builder.create().show();
    }
}
