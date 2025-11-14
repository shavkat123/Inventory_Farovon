package com.inventory.farovon;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.inventory.farovon.db.InventoryItemEntity;
import java.util.List;

public class IdentificationAdapter extends RecyclerView.Adapter<IdentificationAdapter.ViewHolder> {

    private final List<InventoryItemEntity> items;

    public IdentificationAdapter(List<InventoryItemEntity> items) {
        this.items = items;
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
        holder.name.setText(item.name);
        holder.code.setText("Код: " + item.code);
        holder.rfid.setText("RFID: " + item.rf);
        holder.mol.setText("МОЛ: " + item.mol);
        holder.location.setText("Локация: " + item.location);
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView name, code, rfid, mol, location;

        ViewHolder(View view) {
            super(view);
            name = view.findViewById(R.id.item_name);
            code = view.findViewById(R.id.item_code);
            rfid = view.findViewById(R.id.item_rfid);
            mol = view.findViewById(R.id.item_mol);
            location = view.findViewById(R.id.item_location);
            // Hide the options menu as it's not needed here
            view.findViewById(R.id.options_menu).setVisibility(View.GONE);
        }
    }
}