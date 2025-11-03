package com.inventory.farovon.db;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "pending_uploads")
public class PendingUploadEntity {
    @PrimaryKey(autoGenerate = true)
    public int id;

    public String jsonData;

    public PendingUploadEntity(String jsonData) {
        this.jsonData = jsonData;
    }
}
