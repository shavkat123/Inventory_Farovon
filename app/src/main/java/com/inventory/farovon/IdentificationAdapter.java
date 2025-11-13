package com.inventory.farovon;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.inventory.farovon.db.InventoryItemEntity;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class IdentificationAdapter extends RecyclerView.Adapter<IdentificationAdapter.ViewHolder> {

    private final List<InventoryItemEntity> items = new ArrayList<>();
    private final Set<String> displayedRfids = new HashSet<>();

    public void addItem(InventoryItemEntity item) {
        if (item != null && item.rf != null && !item.rf.isEmpty() && !displayedRfids.contains(item.rf)) {
            items.add(item);
            displayedRfids.add(item.rf);
            notifyItemInserted(items.size() - 1);
        }
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_inventory, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        InventoryItemEntity item = items.get(position);
        holder.tvName.setText(item.name);
        holder.tvCode.setText(item.code);
        holder.tvRfid.setText(item.rf);

        holder.ivMoreOptions.setOnClickListener(v -> {
            PopupMenu popup = new PopupMenu(v.getContext(), v);
            popup.getMenuInflater().inflate(R.menu.inventory_item_menu, popup.getMenu());
            popup.setOnMenuItemClickListener(menuItem -> {
                int itemId = menuItem.getItemId();
                if (itemId == R.id.action_move) {
                    Toast.makeText(v.getContext(), "Перемещение: " + item.name, Toast.LENGTH_SHORT).show();
                    return true;
                } else if (itemId == R.id.action_write_off) {
                    Toast.makeText(v.getContext(), "Списание: " + item.name, Toast.LENGTH_SHORT).show();
                    return true;
                }
                return false;
            });
            popup.show();
        });
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvName, tvCode, tvRfid;
        ImageView ivMoreOptions;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvName = itemView.findViewById(R.id.tvName);
            tvCode = itemView.findViewById(R.id.tvCode);
            tvRfid = itemView.findViewById(R.id.rfid);
            ivMoreOptions = itemView.findViewById(R.id.iv_more_options);
        }
    }
}
