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

    private List<MolMovementDocument> documentList;
    private final SimpleDateFormat dateFormat = new SimpleDateFormat("dd.MM.yyyy HH:mm", Locale.getDefault());

    public MolMovementAdapter(List<MolMovementDocument> documentList) {
        this.documentList = documentList;
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
        holder.dateTextView.setText("Дата: " + dateFormat.format(new Date(document.date)));
        holder.fromMolTextView.setText("Откуда: " + document.fromMol);
        holder.toMolTextView.setText("Куда: " + document.toMol);
    }

    @Override
    public int getItemCount() {
        return documentList.size();
    }

    static class DocumentViewHolder extends RecyclerView.ViewHolder {
        TextView dateTextView;
        TextView fromMolTextView;
        TextView toMolTextView;

        public DocumentViewHolder(@NonNull View itemView) {
            super(itemView);
            dateTextView = itemView.findViewById(R.id.document_date);
            fromMolTextView = itemView.findViewById(R.id.from_mol_text);
            toMolTextView = itemView.findViewById(R.id.to_mol_text);
        }
    }
}
