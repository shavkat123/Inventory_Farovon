package com.inventory.farovon.db;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import java.util.List;

@Dao
public interface PendingUploadDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void addToQueue(PendingUploadEntity upload);

    @Query("SELECT * FROM pending_uploads")
    List<PendingUploadEntity> getQueue();

    @Query("DELETE FROM pending_uploads WHERE roomCode IN (:roomCodes)")
    void deleteFromQueue(List<String> roomCodes);
}
