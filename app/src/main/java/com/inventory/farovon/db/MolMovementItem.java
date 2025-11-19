package com.inventory.farovon.db;

import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.PrimaryKey;

@Entity(tableName = "mol_movement_items",
        foreignKeys = @ForeignKey(entity = MolMovementDocument.class,
                                  parentColumns = "id",
                                  childColumns = "documentId",
                                  onDelete = ForeignKey.CASCADE))
public class MolMovementItem {
    @PrimaryKey(autoGenerate = true)
    public long id;

    public long documentId;
    public String rfid;
}
