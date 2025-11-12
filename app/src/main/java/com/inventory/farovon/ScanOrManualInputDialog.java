package com.inventory.farovon;

import android.app.Dialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.DialogFragment;

public class ScanOrManualInputDialog extends DialogFragment {

    public interface ScanOrManualInputListener {
        void onCodeEntered(String code);
    }

    private static final String ARG_TITLE = "title";
    private ScanOrManualInputListener listener;

    public static ScanOrManualInputDialog newInstance(String title) {
        ScanOrManualInputDialog fragment = new ScanOrManualInputDialog();
        Bundle args = new Bundle();
        args.putString(ARG_TITLE, title);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        try {
            listener = (ScanOrManualInputListener) getActivity();
        } catch (ClassCastException e) {
            throw new ClassCastException("Calling activity must implement ScanOrManualInputListener");
        }
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        LayoutInflater inflater = requireActivity().getLayoutInflater();
        View view = inflater.inflate(R.layout.dialog_scan_or_manual_input, null);

        TextView titleView = view.findViewById(R.id.dialog_title);
        EditText input = view.findViewById(R.id.edit_text_input);

        String title = getArguments().getString(ARG_TITLE);
        titleView.setText(title);

        builder.setView(view)
                .setPositiveButton("OK", (dialog, id) -> {
                    String code = input.getText().toString();
                    listener.onCodeEntered(code);
                })
                .setNegativeButton("Отмена", (dialog, id) -> {
                    ScanOrManualInputDialog.this.getDialog().cancel();
                });
        return builder.create();
    }
}
