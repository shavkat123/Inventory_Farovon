package com.inventory.farovon;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.inventory.farovon.db.PrimaryInventoryDocument;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class PrimaryInventoryAdapter extends RecyclerView.Adapter<PrimaryInventoryAdapter.ViewHolder> {

    private List<PrimaryInventoryDocument> documents = new ArrayList<>();
    private final SimpleDateFormat dateFormat = new SimpleDateFormat("dd.MM.yyyy HH:mm", Locale.getDefault());

    public void setDocuments(List<PrimaryInventoryDocument> documents) {
        this.documents = documents;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_primary_inventory_document, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        PrimaryInventoryDocument doc = documents.get(position);
        holder.tvDate.setText(dateFormat.format(new Date(doc.date)));
        holder.tvName.setText(doc.name);
        holder.tvInventoryNumber.setText("Инв. №: " + doc.inventoryNumber);
        holder.tvStatus.setText(doc.status);

        holder.itemView.setOnClickListener(v -> {
            android.content.Context context = v.getContext();
            android.content.Intent intent = new android.content.Intent(context, PrimaryInventoryDetailActivity.class);
            intent.putExtra(PrimaryInventoryDetailActivity.EXTRA_DOCUMENT_ID, doc.id);
            context.startActivity(intent);
        });
    }

    @Override
    public int getItemCount() {
        return documents.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvDate, tvName, tvInventoryNumber, tvStatus;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvDate = itemView.findViewById(R.id.tv_date);
            tvName = itemView.findViewById(R.id.tv_name);
            tvInventoryNumber = itemView.findViewById(R.id.tv_inventory_number);
            tvStatus = itemView.findViewById(R.id.tv_status);
        }
    }
}
