package com.inventory.farovon.db;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;
import java.util.List;

@Dao
public interface RoomDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertAll(List<RoomEntity> rooms);

    @Query("SELECT * FROM rooms WHERE departmentId = :departmentId")
    List<RoomEntity> getByDepartmentId(int departmentId);

    @Query("DELETE FROM rooms WHERE departmentId = :departmentId")
    void deleteByDepartmentId(int departmentId);

    @Query("UPDATE rooms SET isCompleted = :isCompleted WHERE code = :roomCode")
    void updateCompletionStatus(String roomCode, boolean isCompleted);

    @Update
    void update(RoomEntity room);
}
