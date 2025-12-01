package com.inventory.farovon.db;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "rooms")
public class RoomEntity {
    @PrimaryKey(autoGenerate = true)
    public int id;

    public String code;
    public String name;
    public int departmentId;
    public boolean isCompleted;

    public RoomEntity(String code, String name, int departmentId) {
        this.code = code;
        this.name = name;
        this.departmentId = departmentId;
        this.isCompleted = false;
    }
}
