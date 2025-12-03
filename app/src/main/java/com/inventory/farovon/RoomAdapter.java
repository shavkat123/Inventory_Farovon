package com.inventory.farovon;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.inventory.farovon.db.RoomEntity;
import java.util.ArrayList;
import java.util.List;

public class RoomAdapter extends RecyclerView.Adapter<RoomAdapter.RoomViewHolder> {

    private List<RoomEntity> items = new ArrayList<>();
    private OnScanClickListener scanListener;
    private OnItemClickListener itemClickListener;

    public interface OnScanClickListener {
        void onScanClick(RoomEntity item);
    }

    public interface OnItemClickListener {
        void onItemClick(RoomEntity item);
    }

    public void setOnScanClickListener(OnScanClickListener listener) {
        this.scanListener = listener;
    }

    public void setOnItemClickListener(OnItemClickListener listener) {
        this.itemClickListener = listener;
    }

    public void setItems(List<RoomEntity> items) {
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
        RoomEntity item = items.get(position);
        holder.bind(item, scanListener, itemClickListener);
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    static class RoomViewHolder extends RecyclerView.ViewHolder {
        private final TextView roomName;
        private final ImageView scanIcon;
        private final TextView statusCompleted;

        public RoomViewHolder(@NonNull View itemView) {
            super(itemView);
            roomName = itemView.findViewById(R.id.tv_room_name);
            scanIcon = itemView.findViewById(R.id.iv_scan);
            statusCompleted = itemView.findViewById(R.id.tv_status_completed);
        }

        public void bind(final RoomEntity item, final OnScanClickListener scanListener, final OnItemClickListener itemClickListener) {
            roomName.setText(item.name);

            if (item.isCompleted) {
                statusCompleted.setVisibility(View.VISIBLE);
            } else {
                statusCompleted.setVisibility(View.GONE);
            }

            scanIcon.setOnClickListener(v -> {
                if (scanListener != null) {
                    scanListener.onScanClick(item);
                }
            });

            itemView.setOnClickListener(v -> {
                if (itemClickListener != null) {
                    itemClickListener.onItemClick(item);
                }
            });
        }
    }
}
