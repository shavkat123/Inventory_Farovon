package com.inventory.farovon.ui.login;

import android.app.AlertDialog;
import android.app.Dialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

import com.inventory.farovon.R;

public class LoginDialogFragment extends DialogFragment {

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        LayoutInflater inflater = requireActivity().getLayoutInflater();
        View view = inflater.inflate(R.layout.dialog_login, null);

        final EditText ipAddress = view.findViewById(R.id.ipAddress);
        final EditText username = view.findViewById(R.id.username);
        final EditText password = view.findViewById(R.id.password);
        final Button loginButton = view.findViewById(R.id.loginButton);

        loginButton.setOnClickListener(v -> {
            String ip = ipAddress.getText().toString();
            String user = username.getText().toString();
            String pass = password.getText().toString();

            // Handle login logic here

            dismiss();
        });

        builder.setView(view);
        return builder.create();
    }
}