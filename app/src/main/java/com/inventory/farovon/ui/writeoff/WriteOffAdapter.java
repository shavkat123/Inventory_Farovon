package com.inventory.farovon.ui.writeoff;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.inventory.farovon.R;
import com.inventory.farovon.db.WriteOffDocument;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class WriteOffAdapter extends RecyclerView.Adapter<WriteOffAdapter.DocumentViewHolder> {

    private final List<WriteOffDocument> documentList;
    private final SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm", Locale.getDefault());
    private final SimpleDateFormat dateFormat = new SimpleDateFormat("dd.MM.yyyy", Locale.getDefault());
    private OnItemClickListener listener;

    public interface OnItemClickListener {
        void onItemClick(long documentId);
    }

    public WriteOffAdapter(List<WriteOffDocument> documentList, OnItemClickListener listener) {
        this.documentList = documentList;
        this.listener = listener;
    }

    @NonNull
    @Override
    public DocumentViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_write_off_document, parent, false);
        return new DocumentViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull DocumentViewHolder holder, int position) {
        WriteOffDocument document = documentList.get(position);
        holder.bind(document, listener);
    }

    @Override
    public int getItemCount() {
        return documentList.size();
    }

    class DocumentViewHolder extends RecyclerView.ViewHolder {
        TextView documentNumber;
        TextView documentStatus;
        TextView documentDate;
        TextView documentTime;
        TextView documentName;

        public DocumentViewHolder(@NonNull View itemView) {
            super(itemView);
            documentNumber = itemView.findViewById(R.id.document_number);
            documentStatus = itemView.findViewById(R.id.document_status);
            documentDate = itemView.findViewById(R.id.document_date);
            documentTime = itemView.findViewById(R.id.document_time);
            documentName = itemView.findViewById(R.id.document_name);
        }

        public void bind(final WriteOffDocument document, final OnItemClickListener listener) {
            documentNumber.setText(String.format(Locale.getDefault(), "%05d", document.id));

            // Status logic
            if (document.status != null) {
                documentStatus.setText(document.status);
                if (document.status.equalsIgnoreCase("Списано")) {
                   // Optional: Change color for completed status if needed
                   // documentStatus.setBackgroundResource(R.drawable.status_background_completed);
                }
            } else {
                 documentStatus.setText("На согласовании");
            }

            documentDate.setText(dateFormat.format(new Date(document.date)));
            documentTime.setText(timeFormat.format(new Date(document.date)));
            documentName.setText(document.name != null ? document.name : "Без названия");

            itemView.setOnClickListener(v -> {
                if(listener != null) {
                    listener.onItemClick(document.id);
                }
            });
        }
    }
}
