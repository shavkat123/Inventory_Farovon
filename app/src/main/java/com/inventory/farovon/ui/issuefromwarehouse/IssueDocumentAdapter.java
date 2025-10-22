package com.inventory.farovon.ui.issuefromwarehouse;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.inventory.farovon.IssueFromWarehouseActivity;
import com.inventory.farovon.R;
import com.inventory.farovon.model.IssueDocument;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class IssueDocumentAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private static final int TYPE_HEADER = 0;
    private static final int TYPE_ITEM = 1;

    private List<Object> items;

    public IssueDocumentAdapter(Map<String, List<IssueDocument>> documentsByDate) {
        items = new ArrayList<>();
        for (Map.Entry<String, List<IssueDocument>> entry : documentsByDate.entrySet()) {
            items.add(entry.getKey());
            items.addAll(entry.getValue());
        }
    }

    @Override
    public int getItemViewType(int position) {
        if (items.get(position) instanceof String) {
            return TYPE_HEADER;
        }
        return TYPE_ITEM;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if (viewType == TYPE_HEADER) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_date_header, parent, false);
            return new HeaderViewHolder(view);
        }
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_issue_document, parent, false);
        return new ItemViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        if (holder instanceof HeaderViewHolder) {
            ((HeaderViewHolder) holder).header.setText((String) items.get(position));
        } else if (holder instanceof ItemViewHolder) {
            IssueDocument document = (IssueDocument) items.get(position);
            ItemViewHolder itemViewHolder = (ItemViewHolder) holder;
            itemViewHolder.documentNumber.setText("Выдача №" + document.getNumber());
            itemViewHolder.documentStatus.setText(document.getStatus());
            itemViewHolder.documentDate.setText(new SimpleDateFormat("HH:mm", Locale.getDefault()).format(document.getDate()));
            itemViewHolder.documentTo.setText("Куда: " + document.getToLocation());

            itemViewHolder.itemView.setOnClickListener(v -> {
                Context context = v.getContext();
                Intent intent = new Intent(context, IssueFromWarehouseActivity.class);
                intent.putExtra("DOCUMENT_ID", document.getNumber());
                context.startActivity(intent);
            });
        }
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    static class HeaderViewHolder extends RecyclerView.ViewHolder {
        TextView header;
        HeaderViewHolder(View view) {
            super(view);
            header = view.findViewById(R.id.date_header);
        }
    }

    static class ItemViewHolder extends RecyclerView.ViewHolder {
        TextView documentNumber, documentStatus, documentDate, documentTo;
        ItemViewHolder(View view) {
            super(view);
            documentNumber = view.findViewById(R.id.document_number);
            documentStatus = view.findViewById(R.id.document_status);
            documentDate = view.findViewById(R.id.document_date);
            documentTo = view.findViewById(R.id.document_to);
        }
    }
}
