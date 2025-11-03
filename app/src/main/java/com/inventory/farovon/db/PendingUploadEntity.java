package com.inventory.farovon.db;

import androidx.room.Entity;
import androidx.room.PrimaryKey;
import androidx.annotation.NonNull;

@Entity(tableName = "pending_uploads")
public class PendingUploadEntity {
    @PrimaryKey
    @NonNull
    public String roomCode;

    public PendingUploadEntity(@NonNull String roomCode) {
        this.roomCode = roomCode;
    }
}
