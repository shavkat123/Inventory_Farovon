package com.inventory.farovon.ui.molmovement;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.inventory.farovon.R;
import com.inventory.farovon.db.MolMovementDocument;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class MolMovementAdapter extends RecyclerView.Adapter<MolMovementAdapter.DocumentViewHolder> {

    private final List<MolMovementDocument> documentList;
    private final SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm", Locale.getDefault());
    private final SimpleDateFormat dateFormat = new SimpleDateFormat("dd.MM.yyyy", Locale.getDefault());
    private OnItemClickListener listener;

    public interface OnItemClickListener {
        void onItemClick(long documentId);
    }

    public MolMovementAdapter(List<MolMovementDocument> documentList, OnItemClickListener listener) {
        this.documentList = documentList;
        this.listener = listener;
    }

    @NonNull
    @Override
    public DocumentViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_mol_movement_document, parent, false);
        return new DocumentViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull DocumentViewHolder holder, int position) {
        MolMovementDocument document = documentList.get(position);
        holder.bind(document, listener);
    }

    @Override
    public int getItemCount() {
        return documentList.size();
    }

    class DocumentViewHolder extends RecyclerView.ViewHolder {
        TextView documentNumber;
        TextView documentDate;
        TextView documentTime;
        TextView documentRoute;

        public DocumentViewHolder(@NonNull View itemView) {
            super(itemView);
            documentNumber = itemView.findViewById(R.id.document_number);
            documentDate = itemView.findViewById(R.id.document_date);
            documentTime = itemView.findViewById(R.id.document_time);
            documentRoute = itemView.findViewById(R.id.document_route);
        }

        public void bind(final MolMovementDocument document, final OnItemClickListener listener) {
            documentNumber.setText(String.format(Locale.getDefault(), "%05d", document.id));
            documentDate.setText(dateFormat.format(new Date(document.date)));
            documentTime.setText(timeFormat.format(new Date(document.date)));
            String route = "Откуда: " + document.fromMol + " | Куда: " + document.toMol;
            documentRoute.setText(route);

            itemView.setOnClickListener(v -> {
                if(listener != null) {
                    listener.onItemClick(document.id);
                }
            });
        }
    }
}
