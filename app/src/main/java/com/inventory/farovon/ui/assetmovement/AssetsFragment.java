package com.inventory.farovon.ui.assetmovement;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import com.inventory.farovon.R;

public class AssetsFragment extends Fragment {

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_assets, container, false);

        view.findViewById(R.id.button_rfid_scan).setOnClickListener(v ->
                Toast.makeText(getContext(), "RFID Scan clicked", Toast.LENGTH_SHORT).show());

        view.findViewById(R.id.button_select).setOnClickListener(v ->
                Toast.makeText(getContext(), "Select clicked", Toast.LENGTH_SHORT).show());

        return view;
    }
}