package com.inventory.farovon;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.inventory.farovon.db.InventoryItemEntity;

import java.util.ArrayList;
import java.util.List;

public class IdentificationAdapter extends RecyclerView.Adapter<IdentificationAdapter.ViewHolder> implements Filterable {

    private List<InventoryItemEntity> items;
    private List<InventoryItemEntity> itemsFiltered;

    public IdentificationAdapter(List<InventoryItemEntity> items) {
        this.items = items;
        this.itemsFiltered = items;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_inventory, parent, false);
        return new ViewHolder(view, this);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        InventoryItemEntity item = itemsFiltered.get(position);
        holder.name.setText(item.name);
        holder.code.setText("Код: " + item.code);
        holder.rfid.setText("RFID: " + item.rf);
        holder.mol.setText("МОЛ: " + item.mol);
        holder.location.setText("Локация: " + item.location);
    }

    @Override
    public int getItemCount() {
        return itemsFiltered.size();
    }

    @Override
    public Filter getFilter() {
        return new Filter() {
            @Override
            protected FilterResults performFiltering(CharSequence constraint) {
                String charString = constraint.toString();
                if (charString.isEmpty()) {
                    itemsFiltered = items;
                } else {
                    List<InventoryItemEntity> filteredList = new ArrayList<>();
                    for (InventoryItemEntity row : items) {
                        if (row.name.toLowerCase().contains(charString.toLowerCase()) ||
                            row.code.toLowerCase().contains(charString.toLowerCase()) ||
                            row.rf.toLowerCase().contains(charString.toLowerCase())) {
                            filteredList.add(row);
                        }
                    }
                    itemsFiltered = filteredList;
                }

                FilterResults filterResults = new FilterResults();
                filterResults.values = itemsFiltered;
                return filterResults;
            }

            @Override
            protected void publishResults(CharSequence constraint, FilterResults results) {
                itemsFiltered = (ArrayList<InventoryItemEntity>) results.values;
                notifyDataSetChanged();
            }
        };
    }

    public void removeItem(int position) {
        if (position >= 0 && position < itemsFiltered.size()) {
            InventoryItemEntity item = itemsFiltered.get(position);
            itemsFiltered.remove(position);
            items.remove(item);
            notifyItemRemoved(position);
            notifyItemRangeChanged(position, itemsFiltered.size());
        }
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView name, code, rfid, mol, location;
        ImageView optionsMenu;

        ViewHolder(View view, IdentificationAdapter adapter) {
            super(view);
            name = view.findViewById(R.id.item_name);
            code = view.findViewById(R.id.item_code);
            rfid = view.findViewById(R.id.item_rfid);
            mol = view.findViewById(R.id.item_mol);
            location = view.findViewById(R.id.item_location);
            optionsMenu = view.findViewById(R.id.options_menu);

            optionsMenu.setOnClickListener(v -> {
                PopupMenu popup = new PopupMenu(v.getContext(), v);
                popup.getMenuInflater().inflate(R.menu.identification_item_menu, popup.getMenu());
                popup.setOnMenuItemClickListener(item -> {
                    int position = getAdapterPosition();
                    if (position == RecyclerView.NO_POSITION) {
                        return false;
                    }
                    int itemId = item.getItemId();
                    if (itemId == R.id.action_move) {
                        Toast.makeText(v.getContext(), "Перемещение (в разработке)", Toast.LENGTH_SHORT).show();
                        return true;
                    } else if (itemId == R.id.action_write_off) {
                        Toast.makeText(v.getContext(), "Списание (в разработке)", Toast.LENGTH_SHORT).show();
                        return true;
                    } else if (itemId == R.id.action_ignore) {
                        adapter.removeItem(position);
                        return true;
                    }
                    return false;
                });
                popup.show();
            });
        }
    }
}