package com.inventory.farovon.ui;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.DialogFragment;
import com.inventory.farovon.R;

public class ScanOrManualInputDialog extends DialogFragment {

    public interface ScanListener {
        void onScanCompleted(String scannedData);
    }

    private ScanListener listener;

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        try {
            listener = (ScanListener) context;
        } catch (ClassCastException e) {
            throw new ClassCastException(context.toString() + " must implement ScanListener");
        }
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity(), R.style.TransparentDialog);
        LayoutInflater inflater = requireActivity().getLayoutInflater();
        View view = inflater.inflate(R.layout.dialog_hardware_scan, null);
        builder.setView(view);

        final EditText hiddenEditText = view.findViewById(R.id.hidden_edit_text);

        // Request focus to ensure the hardware scanner input goes here
        hiddenEditText.requestFocus();

        hiddenEditText.setOnEditorActionListener((v, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                String scannedData = v.getText().toString().trim();
                if (!scannedData.isEmpty() && listener != null) {
                    listener.onScanCompleted(scannedData);
                    dismiss();
                    return true;
                }
            }
            return false;
        });

        // Create a transparent dialog
        AlertDialog dialog = builder.create();
        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        }

        return dialog;
    }
}
