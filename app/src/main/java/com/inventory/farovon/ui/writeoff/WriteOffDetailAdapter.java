package com.inventory.farovon.ui.writeoff;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.inventory.farovon.R;
import com.inventory.farovon.db.InventoryItemEntity;
import java.util.List;

public class WriteOffDetailAdapter extends RecyclerView.Adapter<WriteOffDetailAdapter.ItemViewHolder> {

    private final List<InventoryItemEntity> items;

    public WriteOffDetailAdapter(List<InventoryItemEntity> items) {
        this.items = items;
    }

    @NonNull
    @Override
    public ItemViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // Reuse existing item layout for inventory items
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_inventory, parent, false);
        return new ItemViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ItemViewHolder holder, int position) {
        InventoryItemEntity item = items.get(position);
        holder.bind(item);
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    class ItemViewHolder extends RecyclerView.ViewHolder {
        TextView name;
        TextView code;
        View menuButton;

        public ItemViewHolder(@NonNull View itemView) {
            super(itemView);
            name = itemView.findViewById(R.id.item_name);
            code = itemView.findViewById(R.id.item_code);
            menuButton = itemView.findViewById(R.id.options_menu);

            // Hide menu button for read-only detail view
            if (menuButton != null) {
                menuButton.setVisibility(View.GONE);
            }
        }

        public void bind(InventoryItemEntity item) {
            name.setText(item.name != null ? item.name : "Неизвестный");
            code.setText(item.code != null ? item.code : (item.rf != null ? item.rf : ""));
        }
    }
}
