package com.inventory.farovon;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
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
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_nomenclature, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Nomenclature item = items.get(position);
        holder.bind(item, foundRfids.contains(item.getRfid()));
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        private final TextView tvName;
        private final TextView tvCode;
        private final TextView tvRf;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvName = itemView.findViewById(R.id.tvName);
            tvCode = itemView.findViewById(R.id.tvCode);
            tvRf = itemView.findViewById(R.id.rfid);
        }

        public void bind(Nomenclature item, boolean isFound) {
            tvName.setText(item.getName());
            tvCode.setText(item.getCode());
            tvRf.setText(item.getRfid());

            if (isFound) {
                itemView.setBackgroundColor(ContextCompat.getColor(itemView.getContext(), R.color.item_found_background));
            } else {
                itemView.setBackgroundColor(ContextCompat.getColor(itemView.getContext(), android.R.color.transparent));
            }
        }
    }
}
