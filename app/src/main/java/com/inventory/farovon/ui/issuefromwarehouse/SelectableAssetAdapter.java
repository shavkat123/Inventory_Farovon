package com.inventory.farovon.ui.issuefromwarehouse;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.inventory.farovon.R;
import com.inventory.farovon.model.Asset;
import java.util.ArrayList;
import java.util.List;

public class SelectableAssetAdapter extends RecyclerView.Adapter<SelectableAssetAdapter.ViewHolder> {

    private List<Asset> assets;
    private List<Asset> selectedAssets = new ArrayList<>();

    public SelectableAssetAdapter(List<Asset> assets) {
        this.assets = assets;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_selectable_asset, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Asset asset = assets.get(position);
        holder.assetName.setText(asset.getName());
        holder.assetInventoryNumber.setText("Инв. номер: " + asset.getInventoryNumber());
        holder.checkBox.setOnCheckedChangeListener(null);
        holder.checkBox.setChecked(selectedAssets.contains(asset));
        holder.checkBox.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                selectedAssets.add(asset);
            } else {
                selectedAssets.remove(asset);
            }
        });
    }

    @Override
    public int getItemCount() {
        return assets.size();
    }

    public List<Asset> getSelectedAssets() {
        return selectedAssets;
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView assetName, assetInventoryNumber;
        CheckBox checkBox;

        ViewHolder(View view) {
            super(view);
            assetName = view.findViewById(R.id.asset_name);
            assetInventoryNumber = view.findViewById(R.id.asset_inventory_number);
            checkBox = view.findViewById(R.id.checkbox);
        }
    }
}
