package com.inventory.farovon;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.app.AlertDialog;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class InventoryAdapter extends RecyclerView.Adapter<InventoryAdapter.ViewHolder> {

    private List<Nomenclature> items = new ArrayList<>();
    private final Set<String> foundRfids = new HashSet<>();

    public void ignoreItem(int position) {
        if (position >= 0 && position < items.size()) {
            items.remove(position);
            notifyItemRemoved(position);
        }
    }

    public void setItems(List<Nomenclature> items) {
        this.items = items;
        foundRfids.clear();
        notifyDataSetChanged();
    }

    public void addFoundRfid(String rfid) {
        if (foundRfids.add(rfid)) {
            notifyDataSetChanged();
        }
    }

    public boolean areAllItemsFound() {
        if (items.isEmpty()) {
            return false;
        }
        return foundRfids.size() == items.size();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_inventory, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Nomenclature item = items.get(position);
        holder.bind(item, foundRfids.contains(item.getRfid()), this);
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        private final TextView tvName;
        private final TextView tvCode;
        private final TextView tvRf;
        private final ImageView ivMoreOptions;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvName = itemView.findViewById(R.id.tvName);
            tvCode = itemView.findViewById(R.id.tvCode);
            tvRf = itemView.findViewById(R.id.rfid);
            ivMoreOptions = itemView.findViewById(R.id.iv_more_options);
        }

        public void bind(Nomenclature item, boolean isFound, InventoryAdapter adapter) {
            tvName.setText(item.getName());
            tvCode.setText(item.getCode());
            tvRf.setText(item.getRfid());

            if (isFound) {
                itemView.setBackgroundColor(ContextCompat.getColor(itemView.getContext(), R.color.item_found_background));
            } else {
                itemView.setBackgroundColor(ContextCompat.getColor(itemView.getContext(), android.R.color.transparent));
            }

            ivMoreOptions.setOnClickListener(v -> showPopupMenu(v.getContext(), v, adapter, getAdapterPosition()));

            itemView.setOnClickListener(v -> {
                // Строим детальное сообщение
                StringBuilder details = new StringBuilder();
                details.append("Наименование: ").append(item.getName()).append("\n\n");
                details.append("Инв. номер: ").append(item.getCode()).append("\n\n");
                details.append("RFID: ").append(item.getRfid()).append("\n\n");
                details.append("МОЛ: ").append(item.getMol() != null ? item.getMol() : "—").append("\n\n");
                details.append("Местоположение: ").append(item.getLocation() != null ? item.getLocation() : "—");

                // Показываем стандартный диалог
                new AlertDialog.Builder(v.getContext())
                        .setTitle("Детализация")
                        .setMessage(details.toString())
                        .setPositiveButton("OK", null)
                        .show();
            });
        }

        private void showPopupMenu(Context context, View view, InventoryAdapter adapter, int position) {
            PopupMenu popup = new PopupMenu(context, view);
            popup.getMenuInflater().inflate(R.menu.inventory_item_options, popup.getMenu());
            popup.setOnMenuItemClickListener(menuItem -> {
                int itemId = menuItem.getItemId();
                if (itemId == R.id.action_move) {
                    Toast.makeText(context, "Перемещение", Toast.LENGTH_SHORT).show();
                    adapter.ignoreItem(position);
                    return true;
                } else if (itemId == R.id.action_write_off) {
                    Toast.makeText(context, "Списание", Toast.LENGTH_SHORT).show();
                    adapter.ignoreItem(position);
                    return true;
                } else if (itemId == R.id.action_ignore) {
                    adapter.ignoreItem(position);
                    return true;
                }
                return false;
            });
            popup.show();
        }
    }
}
