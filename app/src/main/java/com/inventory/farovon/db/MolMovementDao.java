package com.inventory.farovon.db;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Transaction;

import java.util.List;

@Dao
public interface MolMovementDao {

    @Insert
    long insertDocument(MolMovementDocument document);

    @Insert
    void insertItems(List<MolMovementItem> items);

    @Transaction
    @Query("SELECT * FROM mol_movement_documents ORDER BY date DESC")
    List<MolMovementDocument> getAllDocuments();

    @Transaction
    default void insertFullMovement(MolMovementDocument document, List<String> rfids) {
        long documentId = insertDocument(document);
        if (rfids != null && !rfids.isEmpty()) {
            List<MolMovementItem> items = new ArrayList<>();
            for (String rfid : rfids) {
                MolMovementItem item = new MolMovementItem();
                item.documentId = documentId;
                item.rfid = rfid;
                items.add(item);
            }
            insertItems(items);
        }
    }
}
