package com.inventory.farovon.db;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Transaction;

import java.util.ArrayList;
import java.util.List;

@Dao
public interface ReturnDao {

    @Insert
    long insertDocument(ReturnDocument document);

    @Insert
    void insertItems(List<ReturnItem> items);

    @Transaction
    @Query("SELECT * FROM return_documents ORDER BY date DESC")
    List<ReturnDocument> getAllDocuments();

    @Transaction
    @Query("SELECT * FROM return_documents WHERE " +
            "fromIssuer LIKE '%' || :query || '%' OR " +
            "toRecipient LIKE '%' || :query || '%' OR " +
            "fromIssuerDepartment LIKE '%' || :query || '%' OR " +
            "fromOrganization LIKE '%' || :query || '%' OR " +
            "id LIKE '%' || :query || '%' " +
            "ORDER BY date DESC")
    List<ReturnDocument> searchDocuments(String query);

    @Query("SELECT * FROM return_items WHERE documentId = :documentId")
    List<ReturnItem> getItemsForDocument(long documentId);

    @Query("SELECT * FROM return_documents WHERE id = :documentId")
    ReturnDocument getDocumentById(long documentId);

    @Transaction
    default void insertFullMovement(ReturnDocument document, List<String> rfids) {
        long documentId = insertDocument(document);
        if (rfids != null && !rfids.isEmpty()) {
            List<ReturnItem> items = new ArrayList<>();
            for (String rfid : rfids) {
                ReturnItem item = new ReturnItem();
                item.documentId = documentId;
                item.rfid = rfid;
                items.add(item);
            }
            insertItems(items);
        }
    }
}
