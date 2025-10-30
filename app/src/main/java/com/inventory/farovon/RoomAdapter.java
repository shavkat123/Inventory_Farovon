package com.inventory.farovon;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.util.ArrayList;
import java.util.List;

public class RoomAdapter extends RecyclerView.Adapter<RoomAdapter.RoomViewHolder> {

    private List<Nomenclature> items = new ArrayList<>();
    private OnScanClickListener listener;

    public interface OnScanClickListener {
        void onScanClick(Nomenclature item);
    }

    public void setOnScanClickListener(OnScanClickListener listener) {
        this.listener = listener;
    }

    public void setItems(List<Nomenclature> items) {
        this.items = items;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public RoomViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_room, parent, false);
        return new RoomViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull RoomViewHolder holder, int position) {
        Nomenclature item = items.get(position);
        holder.bind(item, listener);
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    static class RoomViewHolder extends RecyclerView.ViewHolder {
        private final TextView roomName;
        private final ImageView scanIcon;

        public RoomViewHolder(@NonNull View itemView) {
            super(itemView);
            roomName = itemView.findViewById(R.id.tv_room_name);
            scanIcon = itemView.findViewById(R.id.iv_scan);
        }

        public void bind(final Nomenclature item, final OnScanClickListener listener) {
            roomName.setText(item.getName());
            scanIcon.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onScanClick(item);
                }
            });
        }
    }
}
