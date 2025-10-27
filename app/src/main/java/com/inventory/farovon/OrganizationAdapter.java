package com.inventory.farovon;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class OrganizationAdapter extends RecyclerView.Adapter<OrganizationAdapter.ViewHolder> {

    private final List<OrganizationItem> organizationItems;

    public OrganizationAdapter(List<OrganizationItem> organizationItems) {
        this.organizationItems = organizationItems;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_organization, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        OrganizationItem item = organizationItems.get(position);
        holder.name.setText(item.getName());

        // Set indentation based on level
        int paddingStart = item.getLevel() * 40; // 40 pixels per level
        holder.itemView.setPadding(paddingStart, holder.itemView.getPaddingTop(), holder.itemView.getPaddingRight(), holder.itemView.getPaddingBottom());

        holder.scanButton.setOnClickListener(v -> {
            Context context = v.getContext();
            Intent intent = new Intent(context, InventoryListActivity.class);
            intent.putExtra("ITEM_CODE", item.getCode());
            intent.putExtra("ITEM_NAME", item.getName());
            context.startActivity(intent);
        });
    }

    @Override
    public int getItemCount() {
        return organizationItems.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        public final TextView name;
        public final ImageButton scanButton;

        public ViewHolder(View view) {
            super(view);
            name = view.findViewById(R.id.item_name);
            scanButton = view.findViewById(R.id.scan_button);
        }
    }
}
