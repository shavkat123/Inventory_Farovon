package com.inventory.farovon;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;

public class InventoryAdapter extends RecyclerView.Adapter<InventoryAdapter.ViewHolder> {

    private final List<InventoryItem> inventoryItems;

    public InventoryAdapter(List<InventoryItem> inventoryItems) {
        this.inventoryItems = inventoryItems;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_inventory, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        InventoryItem item = inventoryItems.get(position);
        holder.name.setText(item.getName());
        holder.invCode.setText("Инв. код: " + item.getInvCode());
        holder.status.setText("Статус: " + item.getStatus());
    }

    @Override
    public int getItemCount() {
        return inventoryItems.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        public final TextView name;
        public final TextView invCode;
        public final TextView status;

        public ViewHolder(View view) {
            super(view);
            name = view.findViewById(R.id.item_name);
            invCode = view.findViewById(R.id.item_inv_code);
            status = view.findViewById(R.id.item_status);
        }
    }
}
