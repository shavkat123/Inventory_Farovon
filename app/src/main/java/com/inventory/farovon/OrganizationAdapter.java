package com.inventory.farovon;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.Space;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.inventory.farovon.model.OrganizationItem;
import java.util.ArrayList;
import java.util.List;

public class OrganizationAdapter extends RecyclerView.Adapter<OrganizationAdapter.ViewHolder> {

    private final List<OrganizationItem> items;
    private final List<OrganizationItem> visibleItems;

    public OrganizationAdapter(List<OrganizationItem> items) {
        this.items = items;
        this.visibleItems = new ArrayList<>();
        updateVisibleItems();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_organization, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        holder.bind(visibleItems.get(position));
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
        if (item.isExpanded() && !item.getChildren().isEmpty()) {
            for (OrganizationItem child : item.getChildren()) {
                addVisibleItem(child);
            }
        }
    }

    class ViewHolder extends RecyclerView.ViewHolder {
        TextView name;
        ImageView itemIcon;
        ImageView expandIndicator;
        Space indentationSpace;

        ViewHolder(View itemView) {
            super(itemView);
            name = itemView.findViewById(R.id.organization_name);
            itemIcon = itemView.findViewById(R.id.item_icon);
            expandIndicator = itemView.findViewById(R.id.expand_indicator);
            indentationSpace = itemView.findViewById(R.id.indentation_space);
        }

        void bind(OrganizationItem item) {
            name.setText(item.getName());

            ViewGroup.LayoutParams params = indentationSpace.getLayoutParams();
            params.width = item.getLevel() * 64;
            indentationSpace.setLayoutParams(params);

            boolean hasChildren = !item.getChildren().isEmpty();

            if (hasChildren) {
                itemIcon.setImageResource(R.drawable.ic_folder);
                expandIndicator.setVisibility(View.VISIBLE);
                expandIndicator.setRotation(item.isExpanded() ? 90f : 0f);
            } else {
                itemIcon.setImageResource(R.drawable.ic_document);
                expandIndicator.setVisibility(View.INVISIBLE);
            }

            itemView.setOnClickListener(v -> {
                if (hasChildren) {
                    item.setExpanded(!item.isExpanded());
                    updateVisibleItems();
                    notifyDataSetChanged();
                } else if (item.getCode() != null && !item.getCode().isEmpty()) {
                    Context context = itemView.getContext();
                    Intent intent = new Intent(context, InventoryListActivity.class);
                    intent.putExtra(InventoryListActivity.EXTRA_DEPARTMENT_ID, item.getId());
                    intent.putExtra(InventoryListActivity.EXTRA_DEPARTMENT_CODE, item.getCode());
                    context.startActivity(intent);
                }
            });
        }
    }
}
