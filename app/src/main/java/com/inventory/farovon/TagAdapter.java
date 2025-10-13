package com.inventory.farovon;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class TagAdapter extends RecyclerView.Adapter<TagAdapter.TagViewHolder> {

    // Сохраняем порядок добавления (для стабильного списка)
    private final Map<String, Integer> tagCountMap = new LinkedHashMap<>();
    private final List<String> epcList = new ArrayList<>();
    private final Map<String, Integer> indexMap = new HashMap<>(); // EPC -> позиция

    public void addTag(String epc) {
        if (epc == null || epc.isEmpty()) return;

        Integer pos = indexMap.get(epc);
        if (pos == null) {
            epcList.add(epc);
            int newPos = epcList.size() - 1;
            indexMap.put(epc, newPos);
            tagCountMap.put(epc, 1);
            notifyItemInserted(newPos);
        } else {
            int newCount = tagCountMap.get(epc) + 1;
            tagCountMap.put(epc, newCount);
            notifyItemChanged(pos);
        }
    }

    public void clearTags() {
        int size = epcList.size();
        if (size == 0) return;
        epcList.clear();
        tagCountMap.clear();
        indexMap.clear();
        // Важно: именно удалить диапазон, а не просто notifyDataSetChanged()
        notifyItemRangeRemoved(0, size);
    }

    @NonNull
    @Override
    public TagViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_tag, parent, false);
        return new TagViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull TagViewHolder holder, int position) {
        String epc = epcList.get(position);
        Integer count = tagCountMap.get(epc);
        holder.tvEpc.setText(epc);
        holder.tvCount.setText(String.valueOf(count != null ? count : 0));
    }

    @Override
    public int getItemCount() {
        return epcList.size();
    }

    static class TagViewHolder extends RecyclerView.ViewHolder {
        final TextView tvEpc, tvCount;
        TagViewHolder(View itemView) {
            super(itemView);
            tvEpc = itemView.findViewById(R.id.tvEpc);
            tvCount = itemView.findViewById(R.id.tvCount);
        }
    }
}
