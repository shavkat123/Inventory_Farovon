package com.inventory.farovon.ui.returntowarehouse;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.inventory.farovon.Nomenclature;
import com.inventory.farovon.R;

import java.util.ArrayList;
import java.util.List;

public class ReturnAssetsAdapter extends RecyclerView.Adapter<ReturnAssetsAdapter.ViewHolder> {

    private List<Nomenclature> items = new ArrayList<>();

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_return_asset, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Nomenclature item = items.get(position);
        holder.assetName.setText(item.getName());
        holder.assetCode.setText("Инв. номер: " + item.getCode());
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    public void setItems(List<Nomenclature> items) {
        this.items = items;
        notifyDataSetChanged();
    }

    public void addItem(Nomenclature item) {
        items.add(item);
        notifyItemInserted(items.size() - 1);
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView assetName;
        TextView assetCode;

        ViewHolder(View view) {
            super(view);
            assetName = view.findViewById(R.id.asset_name);
            assetCode = view.findViewById(R.id.asset_code);
        }
    }
}