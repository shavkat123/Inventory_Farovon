package com.inventory.farovon.db;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import java.util.List;

@Dao
public interface PendingUploadDao {
    @Insert
    void insert(PendingUploadEntity upload);

    @Delete
    void delete(PendingUploadEntity upload);

    @Query("SELECT * FROM pending_uploads")
    List<PendingUploadEntity> getAll();
}
