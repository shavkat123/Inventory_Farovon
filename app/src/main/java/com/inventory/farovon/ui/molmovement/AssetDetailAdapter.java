package com.inventory.farovon.ui.molmovement;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.inventory.farovon.R;
import com.inventory.farovon.db.InventoryItemEntity;
import java.util.List;

public class AssetDetailAdapter extends RecyclerView.Adapter<AssetDetailAdapter.AssetViewHolder> {

    private final List<InventoryItemEntity> assetList;

    public AssetDetailAdapter(List<InventoryItemEntity> assetList) {
        this.assetList = assetList;
    }

    @NonNull
    @Override
    public AssetViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_asset_detail, parent, false);
        return new AssetViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull AssetViewHolder holder, int position) {
        holder.bind(assetList.get(position));
    }

    @Override
    public int getItemCount() {
        return assetList.size();
    }

    static class AssetViewHolder extends RecyclerView.ViewHolder {
        TextView name;
        TextView inventoryNumber;
        TextView serialNumber;
        TextView location;
        TextView mol;
        TextView organization;

        public AssetViewHolder(@NonNull View itemView) {
            super(itemView);
            name = itemView.findViewById(R.id.asset_name);
            inventoryNumber = itemView.findViewById(R.id.inventory_number);
            serialNumber = itemView.findViewById(R.id.serial_number);
            location = itemView.findViewById(R.id.location);
            mol = itemView.findViewById(R.id.mol);
            organization = itemView.findViewById(R.id.organization);
        }

        public void bind(InventoryItemEntity item) {
            name.setText(item.name);
            inventoryNumber.setText("Инв. №: " + (item.code != null ? item.code : "Не назначено"));
            serialNumber.setText("Серийный №: " + (item.serialNumber != null ? item.serialNumber : "Не назначено"));
            location.setText("Местоположение: " + (item.location != null ? item.location : "Не назначено"));
            mol.setText("Эксплуатирующий: " + (item.mol != null ? item.mol : "Не назначено"));
        }
    }
}
