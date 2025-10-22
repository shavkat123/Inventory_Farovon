package com.inventory.farovon.ui.issuefromwarehouse;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.inventory.farovon.R;
import com.inventory.farovon.model.Asset;
import java.util.List;

public class AssetAdapter extends RecyclerView.Adapter<AssetAdapter.ViewHolder> {

    private List<Asset> assets;

    public AssetAdapter(List<Asset> assets) {
        this.assets = assets == null ? new java.util.ArrayList<>() : assets;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_asset, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Asset asset = assets.get(position);
        holder.assetName.setText(asset.getName());
        holder.assetStatus.setText(asset.getStatus());
        holder.assetInventoryNumber.setText("Инв. номер: " + asset.getInventoryNumber());
        holder.assetSerialNumber.setText("Серийный номер: " + asset.getSerialNumber());
        holder.assetLocation.setText("Местоположение: " + asset.getLocation());
        holder.assetOrganization.setText("Организация: " + asset.getOrganization());
    }

    @Override
    public int getItemCount() {
        return assets.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView assetName, assetStatus, assetInventoryNumber, assetSerialNumber, assetLocation, assetOrganization;

        ViewHolder(View view) {
            super(view);
            assetName = view.findViewById(R.id.asset_name);
            assetStatus = view.findViewById(R.id.asset_status);
            assetInventoryNumber = view.findViewById(R.id.asset_inventory_number);
            assetSerialNumber = view.findViewById(R.id.asset_serial_number);
            assetLocation = view.findViewById(R.id.asset_location);
            assetOrganization = view.findViewById(R.id.asset_organization);
        }
    }
}
