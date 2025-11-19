package com.inventory.farovon.db;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "mol_movement_documents")
public class MolMovementDocument {
    @PrimaryKey(autoGenerate = true)
    public long id;

    public long date;
    public String fromMol;
    public String toMol;
}
