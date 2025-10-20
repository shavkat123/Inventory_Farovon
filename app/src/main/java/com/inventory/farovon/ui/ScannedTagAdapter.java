package com.inventory.farovon.ui;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.inventory.farovon.R;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class ScannedTagAdapter extends RecyclerView.Adapter<ScannedTagAdapter.TagViewHolder> {

    // Use LinkedHashMap to maintain insertion order and easily access tags by ID
    private final Map<String, Integer> scannedTags = new LinkedHashMap<>();
    private final List<String> tagIdList = new ArrayList<>();

    @NonNull
    @Override
    public TagViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.list_item_scanned_tag, parent, false);
        return new TagViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull TagViewHolder holder, int position) {
        String tagId = tagIdList.get(position);
        Integer count = scannedTags.get(tagId);
        holder.bind(tagId, count != null ? count : 0);
    }

    @Override
    public int getItemCount() {
        return tagIdList.size();
    }

    /**
     * Adds a new tag to the list or increments its count if it already exists.
     * @param tagId The ID of the scanned tag.
     */
    public void addTag(String tagId) {
        if (scannedTags.containsKey(tagId)) {
            // Tag exists, increment count
            int count = scannedTags.get(tagId);
            scannedTags.put(tagId, count + 1);
            int position = tagIdList.indexOf(tagId);
            if (position != -1) {
                notifyItemChanged(position);
            }
        } else {
            // New tag, add to list
            scannedTags.put(tagId, 1);
            tagIdList.add(tagId);
            notifyItemInserted(tagIdList.size() - 1);
        }
    }

    /**
     * Clears all tags from the list.
     */
    public void clearTags() {
        scannedTags.clear();
        tagIdList.clear();
        notifyDataSetChanged();
    }

    public List<String> getTagList() {
        return new ArrayList<>(tagIdList);
    }


    static class TagViewHolder extends RecyclerView.ViewHolder {
        private final TextView tvTagId;
        private final TextView tvScanCount;

        public TagViewHolder(@NonNull View itemView) {
            super(itemView);
            tvTagId = itemView.findViewById(R.id.tv_tag_id);
            tvScanCount = itemView.findViewById(R.id.tv_scan_count);
        }

        public void bind(String tagId, int count) {
            tvTagId.setText(tagId);
            tvScanCount.setText(String.valueOf(count));
        }
    }
}