package com.inventory.farovon.db;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "issue_items")
public class IssueItem {
    @PrimaryKey(autoGenerate = true)
    public long id;

    public long documentId;
    public String rfid;
}
