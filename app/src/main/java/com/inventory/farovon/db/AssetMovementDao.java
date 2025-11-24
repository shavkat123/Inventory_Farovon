package com.inventory.farovon.db;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Transaction;

import java.util.ArrayList;
import java.util.List;

@Dao
public interface AssetMovementDao {

    @Insert
    long insertDocument(AssetMovementDocument document);

    @Insert
    void insertItems(List<AssetMovementItem> items);

    @Transaction
    @Query("SELECT * FROM asset_movement_documents ORDER BY date DESC")
    List<AssetMovementDocument> getAllDocuments();

    @Transaction
    @Query("SELECT * FROM asset_movement_documents WHERE " +
            "fromIssuer LIKE '%' || :query || '%' OR " +
            "toRecipient LIKE '%' || :query || '%' OR " +
            "fromIssuerDepartment LIKE '%' || :query || '%' OR " +
            "fromOrganization LIKE '%' || :query || '%' OR " +
            "id LIKE '%' || :query || '%' " +
            "ORDER BY date DESC")
    List<AssetMovementDocument> searchDocuments(String query);

    @Query("SELECT * FROM asset_movement_items WHERE documentId = :documentId")
    List<AssetMovementItem> getItemsForDocument(long documentId);

    @Query("SELECT * FROM asset_movement_documents WHERE id = :documentId")
    AssetMovementDocument getDocumentById(long documentId);

    @Transaction
    default void insertFullMovement(AssetMovementDocument document, List<String> rfids) {
        long documentId = insertDocument(document);
        if (rfids != null && !rfids.isEmpty()) {
            List<AssetMovementItem> items = new ArrayList<>();
            for (String rfid : rfids) {
                AssetMovementItem item = new AssetMovementItem();
                item.documentId = documentId;
                item.rfid = rfid;
                items.add(item);
            }
            insertItems(items);
        }
    }
}
