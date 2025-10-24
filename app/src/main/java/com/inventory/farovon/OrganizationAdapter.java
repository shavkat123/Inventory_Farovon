package com.inventory.farovon;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.card.MaterialCardView;
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

    public OrganizationItem getVisibleItem(int position) {
        return visibleItems.get(position);
    }

    public OrganizationItem getParentOf(OrganizationItem item) {
        for (OrganizationItem parentCandidate : items) {
            OrganizationItem result = findParent(parentCandidate, item);
            if (result != null) {
                return result;
            }
        }
        return null;
    }

    private OrganizationItem findParent(OrganizationItem current, OrganizationItem target) {
        for (OrganizationItem child : current.getChildren()) {
            if (child == target) {
                return current;
            }
            OrganizationItem result = findParent(child, target);
            if (result != null) {
                return result;
            }
        }
        return null;
    }

    class OrganizationViewHolder extends RecyclerView.ViewHolder {

        private TextView name;
        private ImageView scanButton;
        private MaterialCardView cardView;

        public OrganizationViewHolder(@NonNull View itemView) {
            super(itemView);
            name = itemView.findViewById(R.id.organization_name);
            scanButton = itemView.findViewById(R.id.scan_button);
            cardView = itemView.findViewById(R.id.card_view);
        }

        public void bind(OrganizationItem item) {
            name.setText(item.getName());
            itemView.setPadding(item.getLevel() * 64, itemView.getPaddingTop(), itemView.getPaddingRight(), itemView.getPaddingBottom());

            Context context = itemView.getContext();
            int[] colorResources = {
                    R.color.tree_level_0,
                    R.color.tree_level_1,
                    R.color.tree_level_2
            };
            int colorRes = colorResources[Math.min(item.getLevel(), colorResources.length - 1)];
            cardView.setCardBackgroundColor(ContextCompat.getColor(context, colorRes));

            itemView.setOnClickListener(v -> {
                if (getAdapterPosition() != RecyclerView.NO_POSITION) {
                    OrganizationItem clickedItem = visibleItems.get(getAdapterPosition());

                    // If the item has a code, it's a department/room. Open the inventory list.
                    String code = clickedItem.getCode();
                    if (code != null && !code.isEmpty()) {
                        android.content.Intent intent = new android.content.Intent(itemView.getContext(), InventoryListActivity.class);
                        intent.putExtra(InventoryListActivity.EXTRA_DEPARTMENT_ID, clickedItem.getId());
                        intent.putExtra(InventoryListActivity.EXTRA_DEPARTMENT_CODE, code);
                        itemView.getContext().startActivity(intent);
                    }

                    // If the item has children, toggle its expanded state.
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