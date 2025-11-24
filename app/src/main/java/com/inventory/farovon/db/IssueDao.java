package com.inventory.farovon.db;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Transaction;

import java.util.ArrayList;
import java.util.List;

@Dao
public interface IssueDao {

    @Insert
    long insertDocument(IssueDocument document);

    @Insert
    void insertItems(List<IssueItem> items);

    @Transaction
    @Query("SELECT * FROM issue_documents ORDER BY date DESC")
    List<IssueDocument> getAllDocuments();

    @Transaction
    @Query("SELECT * FROM issue_documents WHERE " +
            "fromIssuer LIKE '%' || :query || '%' OR " +
            "toRecipient LIKE '%' || :query || '%' OR " +
            "fromIssuerDepartment LIKE '%' || :query || '%' OR " +
            "fromOrganization LIKE '%' || :query || '%' OR " +
            "id LIKE '%' || :query || '%' " +
            "ORDER BY date DESC")
    List<IssueDocument> searchDocuments(String query);

    @Query("SELECT * FROM issue_items WHERE documentId = :documentId")
    List<IssueItem> getItemsForDocument(long documentId);

    @Query("SELECT * FROM issue_documents WHERE id = :documentId")
    IssueDocument getDocumentById(long documentId);

    @Transaction
    default void insertFullMovement(IssueDocument document, List<String> rfids) {
        long documentId = insertDocument(document);
        if (rfids != null && !rfids.isEmpty()) {
            List<IssueItem> items = new ArrayList<>();
            for (String rfid : rfids) {
                IssueItem item = new IssueItem();
                item.documentId = documentId;
                item.rfid = rfid;
                items.add(item);
            }
            insertItems(items);
        }
    }
}
