package com.inventory.farovon.ui.inventory;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.navigation.fragment.NavHostFragment;
import com.inventory.farovon.OrganizationInventoryActivity;
import com.inventory.farovon.R;

public class InventoryFragment extends Fragment {

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_inventory, container, false);

        root.findViewById(R.id.btn_organization_inventory).setOnClickListener(v -> {
            Intent intent = new Intent(getActivity(), OrganizationInventoryActivity.class);
            startActivity(intent);
        });

        root.findViewById(R.id.btn_room_inventory).setOnClickListener(v -> {
            NavHostFragment.findNavController(InventoryFragment.this)
                    .navigate(R.id.action_inventory_to_gallery);
        });

        return root;
    }
}
