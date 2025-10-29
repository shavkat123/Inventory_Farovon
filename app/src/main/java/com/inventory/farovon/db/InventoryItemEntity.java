package com.inventory.farovon.db;

import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.PrimaryKey;

@Entity(tableName = "inventory_items",
        foreignKeys = @ForeignKey(entity = DepartmentEntity.class,
                                  parentColumns = "id",
                                  childColumns = "departmentId",
                                  onDelete = ForeignKey.CASCADE))
public class InventoryItemEntity {
    @PrimaryKey(autoGenerate = true)
    public int id;
    public int departmentId;
    public String code;
    public String name;
    public String rf;
    public String mol;
    public String location;
}
