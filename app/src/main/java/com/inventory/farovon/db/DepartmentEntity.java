package com.inventory.farovon.db;

import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.PrimaryKey;

@Entity(tableName = "departments",
        foreignKeys = @ForeignKey(entity = OrganizationEntity.class,
                                  parentColumns = "id",
                                  childColumns = "organizationId",
                                  onDelete = ForeignKey.CASCADE))
public class DepartmentEntity {
    @PrimaryKey(autoGenerate = true)
    public int id;
    public int organizationId;
    public String code;
    public String name;
    public String parentRef;
}
