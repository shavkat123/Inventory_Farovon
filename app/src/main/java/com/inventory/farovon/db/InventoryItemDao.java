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

    @Query("SELECT * FROM inventory_items WHERE departmentId = :departmentId AND location = :location")
    List<InventoryItemEntity> getByDepartmentIdAndLocation(int departmentId, String location);

    @Query("DELETE FROM inventory_items WHERE departmentId = :departmentId")
    void clearByDepartmentId(int departmentId);

    @Query("DELETE FROM inventory_items WHERE departmentId = :departmentId AND location = :location")
    void clearByDepartmentIdAndLocation(int departmentId, String location);

    @Query("SELECT * FROM inventory_items WHERE rf = :rfid")
    List<InventoryItemEntity> findByRfid(String rfid);

    @Query("SELECT * FROM inventory_items WHERE rf IN (:rfids)")
    List<InventoryItemEntity> findByRfidList(List<String> rfids);

    @Query("SELECT * FROM inventory_items WHERE code = :barcode")
    List<InventoryItemEntity> findByBarcode(String barcode);

    @Query("SELECT * FROM inventory_items WHERE serialNumber = :serialNumber")
    List<InventoryItemEntity> findBySerialNumber(String serialNumber);

    @Query("SELECT * FROM inventory_items WHERE code LIKE '%' || :query || '%' OR name LIKE '%' || :query || '%' OR rf LIKE '%' || :query || '%' OR serialNumber LIKE '%' || :query || '%'")
    List<InventoryItemEntity> findByQuery(String query);
}
