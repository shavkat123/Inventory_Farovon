package com.inventory.farovon.ui.inventory;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import com.inventory.farovon.IdentificationActivity;
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

        root.findViewById(R.id.btn_quick_inventory).setOnClickListener(v -> {
            Intent intent = new Intent(getActivity(), IdentificationActivity.class);
            startActivity(intent);
        });

        return root;
    }
}
