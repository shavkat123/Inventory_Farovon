package com.inventory.farovon;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

public class NomenclatureListFragment extends Fragment {

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_nomenclature_list, container, false);

        RecyclerView recyclerView = view.findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));

        // Получаем данные из аргументов
        List<Nomenclature> items = new ArrayList<>();
        if (getArguments() != null) {
            items = (List<Nomenclature>) getArguments().getSerializable("items");
        }

        NomenclatureAdapter adapter = new NomenclatureAdapter();
        adapter.setItems(items != null ? items : new ArrayList<>());
        recyclerView.setAdapter(adapter);

        return view;
    }
}
