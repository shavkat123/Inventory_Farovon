package com.inventory.farovon.ui.inventory;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.navigation.fragment.NavHostFragment;
import androidx.recyclerview.widget.RecyclerView;

import com.inventory.farovon.IdentificationActivity;
import com.inventory.farovon.OrganizationInventoryActivity;
import com.inventory.farovon.R;
import com.inventory.farovon.ui.home.MainMenuAdapter;
import com.inventory.farovon.ui.home.MenuItem;
import java.util.ArrayList;
import java.util.List;

public class InventoryFragment extends Fragment implements MainMenuAdapter.OnMenuItemClickListener {

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_inventory, container, false);

        RecyclerView recyclerView = root.findViewById(R.id.inventory_recycler_view);

        List<MenuItem> menuItems = new ArrayList<>();
        menuItems.add(new MenuItem(getString(R.string.menu_organization_inventory), R.drawable.icons8____2_96));
        menuItems.add(new MenuItem(getString(R.string.menu_room_inventory), R.drawable.ic_menu_camera));

        MainMenuAdapter adapter = new MainMenuAdapter(menuItems, this);
        recyclerView.setAdapter(adapter);

        return root;
    }

    @Override
    public void onMenuItemClick(MenuItem item) {
        if (item.getTitle().equals(getString(R.string.menu_organization_inventory))) {
            Intent intent = new Intent(getActivity(), OrganizationInventoryActivity.class);
            startActivity(intent);
        } else if (item.getTitle().equals(getString(R.string.menu_room_inventory))) {
            NavHostFragment.findNavController(this).navigate(R.id.action_inventory_to_gallery);
        }
    }
}
