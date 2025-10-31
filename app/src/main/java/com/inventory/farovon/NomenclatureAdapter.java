package com.inventory.farovon;

import android.graphics.Typeface;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.recyclerview.widget.RecyclerView;

import java.util.*;

/**
 * Адаптер для списка предполагаемых товаров:
 * Показывает Name / Code / RFID / ScanCount.
 * Счётчики попаданий (scanCount) хранятся внутри адаптера (countsByEpc),
 * а не в модели Nomenclature.
 */
public class NomenclatureAdapter extends RecyclerView.Adapter<NomenclatureAdapter.VH> {

    private final List<Nomenclature> items = new ArrayList<>();

    // Индекс EPC -> позиция элемента в списке
    private final Map<String, Integer> rfIndex = new HashMap<>();
    // Счётчики по EPC (нормализованному)
    private final Map<String, Integer> countsByEpc = new HashMap<>();

    public NomenclatureAdapter() {}

    public NomenclatureAdapter(List<Nomenclature> list) {
        setItems(list);
    }

    /** Полная замена списка элементов (сброс счётчиков). */
    public void setItems(List<Nomenclature> list) {
        items.clear();
        if (list != null) items.addAll(list);
        rebuildIndex();
        countsByEpc.clear();
        notifyDataSetChanged();
    }

    /** Обнулить видимые счётчики. */
    public void clearCounts() {
        if (countsByEpc.isEmpty()) return;
        countsByEpc.clear();
        notifyDataSetChanged();
    }

    /**
     * Инкремент счётчика для строки по пришедшему EPC.
     * @return true — если EPC найден в списке (по полю rfid).
     */
    public boolean incrementByEpc(String epcRaw) {
        String key = normalizeEpc(epcRaw);
        if (key == null) return false;

        Integer pos = rfIndex.get(key);
        if (pos == null) return false;

        int newCount = countsByEpc.getOrDefault(key, 0) + 1;
        countsByEpc.put(key, newCount);
        notifyItemChanged(pos);
        return true;
    }

    public void removeItem(int position) {
        if (position >= 0 && position < items.size()) {
            items.remove(position);
            rebuildIndex();
            notifyItemRemoved(position);
            notifyItemRangeChanged(position, items.size());
        }
    }

    private void rebuildIndex() {
        rfIndex.clear();
        for (int i = 0; i < items.size(); i++) {
            String k = normalizeEpc(getRfid(items.get(i)));
            if (k != null && !k.isEmpty()) {
                rfIndex.put(k, i);
            }
        }
    }

    /** Приведение EPC к единому виду, чтобы совпадения ловились надёжно. */
    private static String normalizeEpc(String epc) {
        if (epc == null) return null;
        return epc.replace(" ", "")
                .replace("-", "")
                .replace(":", "")
                .toUpperCase(Locale.ROOT);
    }

    // ==== Геттеры для модели ====
    // Если у твоего Nomenclature публичные поля (name, code, rfid) —
    // замени вызовы на прямой доступ: it.name / it.code / it.rfid.
    private String getName(Nomenclature it) { return it.getName(); }
    private String getCode(Nomenclature it) { return it.getCode(); }
    private String getRfid(Nomenclature it) { return it.getRfid(); }

    @NonNull
    @Override
    public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                // это твой layout элемента со строками tvName, tvCode, rfid, scanCount
                .inflate(R.layout.item_inventory, parent, false);
        return new VH(v);
    }

    @Override
    public void onBindViewHolder(@NonNull VH h, int position) {
        Nomenclature it = items.get(position);

        String name = getName(it);
        String code = getCode(it);
        String rfid = getRfid(it);
        String key  = normalizeEpc(rfid);

        int count = key == null ? 0 : countsByEpc.getOrDefault(key, 0);

        h.tvName.setText(name != null ? name : "");
        h.tvCode.setText(code != null ? code : "");
        h.tvRfid.setText(rfid != null ? rfid : "");

        // Жирный шрифт для найденных
        h.tvName.setTypeface(null, count > 0 ? Typeface.BOLD : Typeface.NORMAL);

        // Фон: зелёный если нашли, иначе — обычный
        h.root.setBackgroundResource(count > 0
                ? R.drawable.bg_item_found     // см. drawable из предыдущего сообщения
                : R.drawable.bg_item_normal);

        h.root.setOnClickListener(v -> {
            // Строим детальное сообщение
            StringBuilder details = new StringBuilder();
            details.append("Наименование: ").append(getName(it)).append("\n\n");
            details.append("Инв. номер: ").append(getCode(it)).append("\n\n");
            details.append("RFID: ").append(getRfid(it)).append("\n\n");
            details.append("МОЛ: ").append(it.getMol() != null ? it.getMol() : "—").append("\n\n");
            details.append("Местоположение: ").append(it.getLocation() != null ? it.getLocation() : "—");

            // Показываем стандартный диалог
            new AlertDialog.Builder(v.getContext())
                    .setTitle("Детализация")
                    .setMessage(details.toString())
                    .setPositiveButton("OK", null)
                    .show();
        });

        h.ivMoreOptions.setOnClickListener(v -> {
            PopupMenu popup = new PopupMenu(v.getContext(), v);
            popup.getMenuInflater().inflate(R.menu.inventory_item_menu, popup.getMenu());
            popup.setOnMenuItemClickListener(menuItem -> {
                int itemId = menuItem.getItemId();
                int adapterPosition = h.getAdapterPosition();
                if (adapterPosition == RecyclerView.NO_POSITION) {
                    return false;
                }

                if (itemId == R.id.action_move) {
                    Toast.makeText(v.getContext(), "Перемещение: " + name, Toast.LENGTH_SHORT).show();
                    removeItem(adapterPosition);
                    return true;
                } else if (itemId == R.id.action_write_off) {
                    Toast.makeText(v.getContext(), "Списание: " + name, Toast.LENGTH_SHORT).show();
                    removeItem(adapterPosition);
                    return true;
                } else if (itemId == R.id.action_ignore) {
                    removeItem(adapterPosition);
                    return true;
                }
                return false;
            });
            popup.show();
        });
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    /** Хранилище ссылок на вью элемента. */
    static class VH extends RecyclerView.ViewHolder {
        View root;
        TextView tvName, tvCode, tvRfid;
        ImageView ivMoreOptions;

        VH(@NonNull View itemView) {
            super(itemView);
            // rootItem — id корневого RelativeLayout в твоём item_nomenclature.xml
            root      = itemView.findViewById(R.id.rootItem);
            if (root == null) root = itemView; // страховка, если id не задан

            tvName    = itemView.findViewById(R.id.tvName);
            tvCode    = itemView.findViewById(R.id.tvCode);
            tvRfid    = itemView.findViewById(R.id.rfid);
            ivMoreOptions = itemView.findViewById(R.id.iv_more_options);
        }
    }
}
