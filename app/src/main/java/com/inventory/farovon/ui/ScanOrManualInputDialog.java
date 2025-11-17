package com.inventory.farovon.ui;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.DialogFragment;
import com.inventory.farovon.R;

public class ScanOrManualInputDialog extends DialogFragment {

    public interface ScanListener {
        void onScanCompleted(String scannedData);
    }

    private ScanListener listener;
    private Handler handler = new Handler(Looper.getMainLooper());
    private Runnable workRunnable;

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
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        LayoutInflater inflater = requireActivity().getLayoutInflater();
        View view = inflater.inflate(R.layout.dialog_hardware_scan, null);
        builder.setView(view);

        EditText hiddenEditText = view.findViewById(R.id.hidden_edit_text);
        hiddenEditText.requestFocus();

        hiddenEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}

            @Override
            public void afterTextChanged(Editable s) {
                handler.removeCallbacks(workRunnable);
                workRunnable = () -> {
                    if (s.length() > 0) {
                        listener.onScanCompleted(s.toString());
                        dismiss();
                    }
                };
                handler.postDelayed(workRunnable, 500); // Wait 500ms for scanner input to complete
            }
        });

        return builder.create();
    }
}