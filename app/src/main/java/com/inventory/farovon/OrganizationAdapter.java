package com.inventory.farovon;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.inventory.farovon.model.OrganizationItem;

import java.util.ArrayList;
import java.util.List;

public class OrganizationAdapter extends RecyclerView.Adapter<OrganizationAdapter.OrganizationViewHolder> {

    private List<OrganizationItem> items;
    private List<OrganizationItem> visibleItems;

    public OrganizationAdapter(List<OrganizationItem> items) {
        this.items = items;
        this.visibleItems = new ArrayList<>();
        updateVisibleItems();
    }

    @NonNull
    @Override
    public OrganizationViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_organization, parent, false);
        return new OrganizationViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull OrganizationViewHolder holder, int position) {
        OrganizationItem item = visibleItems.get(position);
        holder.bind(item);
    }

    @Override
    public int getItemCount() {
        return visibleItems.size();
    }

    private void updateVisibleItems() {
        visibleItems.clear();
        for (OrganizationItem item : items) {
            addVisibleItem(item);
        }
    }

    private void addVisibleItem(OrganizationItem item) {
        visibleItems.add(item);
        if (item.isExpanded()) {
            for (OrganizationItem child : item.getChildren()) {
                addVisibleItem(child);
            }
        }
    }

    class OrganizationViewHolder extends RecyclerView.ViewHolder {

        private TextView name;
        private ImageView scanButton;

        public OrganizationViewHolder(@NonNull View itemView) {
            super(itemView);
            name = itemView.findViewById(R.id.organization_name);
            scanButton = itemView.findViewById(R.id.scan_button);
        }

        public void bind(OrganizationItem item) {
            name.setText(item.getName());
            itemView.setPadding(item.getLevel() * 32, itemView.getPaddingTop(), itemView.getPaddingRight(), itemView.getPaddingBottom());

            itemView.setOnClickListener(v -> {
                if (getAdapterPosition() != RecyclerView.NO_POSITION) {
                    OrganizationItem clickedItem = visibleItems.get(getAdapterPosition());
                    if (!clickedItem.getChildren().isEmpty()) {
                        clickedItem.setExpanded(!clickedItem.isExpanded());
                        updateVisibleItems();
                        notifyDataSetChanged();
                    }
                }
            });

            scanButton.setOnClickListener(v -> {
                Toast.makeText(itemView.getContext(), "Scan " + item.getName(), Toast.LENGTH_SHORT).show();
            });
        }
    }
}