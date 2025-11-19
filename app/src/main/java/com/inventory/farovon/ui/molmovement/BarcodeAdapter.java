package com.inventory.farovon.ui.molmovement;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.inventory.farovon.R;
import java.util.List;

public class BarcodeAdapter extends RecyclerView.Adapter<BarcodeAdapter.BarcodeViewHolder> {

    private List<String> barcodeList;

    public BarcodeAdapter(List<String> barcodeList) {
        this.barcodeList = barcodeList;
    }

    @NonNull
    @Override
    public BarcodeViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_barcode, parent, false);
        return new BarcodeViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull BarcodeViewHolder holder, int position) {
        holder.barcodeTextView.setText(barcodeList.get(position));
    }

    @Override
    public int getItemCount() {
        return barcodeList.size();
    }

    static class BarcodeViewHolder extends RecyclerView.ViewHolder {
        TextView barcodeTextView;

        public BarcodeViewHolder(@NonNull View itemView) {
            super(itemView);
            barcodeTextView = itemView.findViewById(R.id.barcode_text);
        }
    }
}