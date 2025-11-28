package com.inventory.farovon.db;

import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.PrimaryKey;

@Entity(tableName = "write_off_items",
        foreignKeys = @ForeignKey(entity = WriteOffDocument.class,
                                  parentColumns = "id",
                                  childColumns = "documentId",
                                  onDelete = ForeignKey.CASCADE))
public class WriteOffItem {
    @PrimaryKey(autoGenerate = true)
    public long id;

    public long documentId;
    public String rfid;
}
