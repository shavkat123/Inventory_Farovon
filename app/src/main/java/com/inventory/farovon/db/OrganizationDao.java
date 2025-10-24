package com.inventory.farovon.db;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import java.util.List;

@Dao
public interface OrganizationDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    long insert(OrganizationEntity organization);

    @Query("SELECT * FROM organizations")
    List<OrganizationEntity> getAll();

    @Query("DELETE FROM organizations")
    void clearAll();
}
