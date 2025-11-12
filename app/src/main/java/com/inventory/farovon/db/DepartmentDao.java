package com.inventory.farovon.db;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import java.util.List;

@Dao
public interface DepartmentDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertAll(List<DepartmentEntity> departments);

    @Query("SELECT * FROM departments WHERE organizationId = :organizationId")
    List<DepartmentEntity> getByOrganizationId(int organizationId);

    @Query("DELETE FROM departments")
    void clearAll();

    @Query("UPDATE departments SET is_completed = :isCompleted WHERE id = :departmentId")
    void updateCompletionStatus(int departmentId, boolean isCompleted);

    @Query("SELECT * FROM departments WHERE is_completed = 1")
    List<DepartmentEntity> getCompletedDepartments();

    @Query("SELECT id FROM departments WHERE code = :code")
    int getIdByCode(String code);
}
