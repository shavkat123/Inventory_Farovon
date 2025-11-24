package com.inventory.farovon.db;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "asset_movement_items")
public class AssetMovementItem {
    @PrimaryKey(autoGenerate = true)
    public long id;

    public long documentId;
    public String rfid;
}
