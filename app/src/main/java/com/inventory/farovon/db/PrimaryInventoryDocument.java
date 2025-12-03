package com.inventory.farovon.db;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "primary_inventory_documents")
public class PrimaryInventoryDocument {
    @PrimaryKey(autoGenerate = true)
    public long id;

    public long date;
    public String type;
    public String name;
    public String color;
    public String characteristics;
    public String inventoryNumber;
    public String status;
}
