package com.inventory.farovon.ui.inventory;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.content.Intent;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.inventory.farovon.R;

public class InventoryFragment extends Fragment {

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_inventory, container, false);

        LinearLayout orgInventoryButton = root.findViewById(R.id.org_inventory_button);
        LinearLayout quickInventoryButton = root.findViewById(R.id.quick_inventory_button);

        orgInventoryButton.setOnClickListener(v -> {
            Intent intent = new Intent(getActivity(), com.inventory.farovon.OrganizationInventoryActivity.class);
            startActivity(intent);
        });

        quickInventoryButton.setOnClickListener(v -> {
            Intent intent = new Intent(getActivity(), com.inventory.farovon.IdentificationActivity.class);
            startActivity(intent);
        });

        return root;
    }
}