package com.inventory.farovon.db;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;
import java.util.List;

@Dao
public interface PrimaryInventoryDao {
    @Insert
    long insert(PrimaryInventoryDocument document);

    @Query("SELECT * FROM primary_inventory_documents ORDER BY date DESC")
    List<PrimaryInventoryDocument> getAll();

    @Query("SELECT * FROM primary_inventory_documents WHERE id = :id")
    PrimaryInventoryDocument getById(long id);

    @Query("SELECT * FROM primary_inventory_documents WHERE name LIKE '%' || :query || '%' OR inventoryNumber LIKE '%' || :query || '%' ORDER BY date DESC")
    List<PrimaryInventoryDocument> findByQuery(String query);
}
