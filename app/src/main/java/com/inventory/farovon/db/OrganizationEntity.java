package com.inventory.farovon.db;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "organizations")
public class OrganizationEntity {
    @PrimaryKey(autoGenerate = true)
    public int id;
    public String name;
}
