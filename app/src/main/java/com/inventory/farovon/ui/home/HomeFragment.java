package com.inventory.farovon.ui.home;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.inventory.farovon.AssetMovementActivity;
import com.inventory.farovon.R;

import java.util.ArrayList;
import java.util.List;

public class HomeFragment extends Fragment {

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_home, container, false);

        RecyclerView recyclerView = root.findViewById(R.id.mainMenuRecyclerView);
        recyclerView.setLayoutManager(new GridLayoutManager(getContext(), 3));

        List<MenuItem> menuItems = new ArrayList<>();
        // Existing functionality
        menuItems.add(new MenuItem("Инвентаризация", R.drawable.ic_menu_camera));
        // Placeholders
        menuItems.add(new MenuItem("Быстрая идентификация", R.drawable.ic_menu_search));
        menuItems.add(new MenuItem("Выдача со склада", R.drawable.ic_menu_upload));
        menuItems.add(new MenuItem("Возврат на склад", R.drawable.ic_menu_download));
        menuItems.add(new MenuItem("Списание", R.drawable.ic_menu_delete));
        menuItems.add(new MenuItem("Перемещение МП", R.drawable.ic_menu_send));
        menuItems.add(new MenuItem("Помещенные МОЛ", R.drawable.ic_menu_myplaces));
        menuItems.add(new MenuItem("Первичная инвентаризация", R.drawable.ic_menu_add));


        MainMenuAdapter adapter = new MainMenuAdapter(menuItems, item -> {
            if (item.getTitle().equals("Инвентаризация")) {
                NavController navController = Navigation.findNavController(requireActivity(), R.id.nav_host_fragment_content_main);
                navController.navigate(R.id.nav_gallery);
            } else {
                Toast.makeText(getContext(), item.getTitle() + " - в разработке", Toast.LENGTH_SHORT).show();
            }
        });

        recyclerView.setAdapter(adapter);

        return root;
    }
}