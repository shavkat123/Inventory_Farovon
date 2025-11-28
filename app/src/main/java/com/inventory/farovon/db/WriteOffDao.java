package com.inventory.farovon.db;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Transaction;

import java.util.ArrayList;
import java.util.List;

@Dao
public abstract class WriteOffDao {

    @Insert
    public abstract long insertDocument(WriteOffDocument document);

    @Insert
    public abstract void insertItems(List<WriteOffItem> items);

    @Transaction
    @Query("SELECT * FROM write_off_documents ORDER BY date DESC")
    public abstract List<WriteOffDocument> getAllDocuments();

    @Transaction
    @Query("SELECT * FROM write_off_documents WHERE " +
            "name LIKE '%' || :query || '%' OR " +
            "organization LIKE '%' || :query || '%' OR " +
            "department LIKE '%' || :query || '%' OR " +
            "status LIKE '%' || :query || '%' OR " +
            "id LIKE '%' || :query || '%' " +
            "ORDER BY date DESC")
    public abstract List<WriteOffDocument> searchDocuments(String query);

    @Query("SELECT * FROM write_off_items WHERE documentId = :documentId")
    public abstract List<WriteOffItem> getItemsForDocument(long documentId);

    @Query("SELECT * FROM write_off_documents WHERE id = :documentId")
    public abstract WriteOffDocument getDocumentById(long documentId);

    @Transaction
    public void insertFullDocument(WriteOffDocument document, List<String> rfids) {
        long documentId = insertDocument(document);
        if (rfids != null && !rfids.isEmpty()) {
            List<WriteOffItem> items = new ArrayList<>();
            for (String rfid : rfids) {
                WriteOffItem item = new WriteOffItem();
                item.documentId = documentId;
                item.rfid = rfid;
                items.add(item);
            }
            insertItems(items);
        }
    }
}
