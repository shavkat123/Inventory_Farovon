package com.inventory.farovon.db;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import java.util.List;

@Dao
public interface InventoryItemDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertAll(List<InventoryItemEntity> items);

    @Query("SELECT * FROM inventory_items WHERE departmentId = :departmentId")
    List<InventoryItemEntity> getByDepartmentId(int departmentId);

    @Query("DELETE FROM inventory_items WHERE departmentId = :departmentId")
    void clearByDepartmentId(int departmentId);
}
