package com.inventory.farovon.db;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Transaction;

import java.util.ArrayList;
import java.util.List;

@Dao
public abstract class MolMovementDao {

    @Insert
    public abstract long insertDocument(MolMovementDocument document);

    @Insert
    public abstract void insertItems(List<MolMovementItem> items);

    @Transaction
    @Query("SELECT * FROM mol_movement_documents ORDER BY date DESC")
    public abstract List<MolMovementDocument> getAllDocuments();

    @Transaction
    @Query("SELECT * FROM mol_movement_documents WHERE " +
            "fromMol LIKE '%' || :query || '%' OR " +
            "toMol LIKE '%' || :query || '%' OR " +
            "fromDepartment LIKE '%' || :query || '%' OR " +
            "fromOrganization LIKE '%' || :query || '%' OR " +
            "id LIKE '%' || :query || '%' " +
            "ORDER BY date DESC")
    public abstract List<MolMovementDocument> searchDocuments(String query);

    @Query("SELECT * FROM mol_movement_items WHERE documentId = :documentId")
    public abstract List<MolMovementItem> getItemsForDocument(long documentId);

    @Query("SELECT * FROM mol_movement_documents WHERE id = :documentId")
    public abstract MolMovementDocument getDocumentById(long documentId);

    @Transaction
    public void insertFullMovement(MolMovementDocument document, List<String> rfids) {
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
