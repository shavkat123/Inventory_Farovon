package com.inventory.farovon.db;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "write_off_documents")
public class WriteOffDocument {
    @PrimaryKey(autoGenerate = true)
    public long id;

    public long date;
    public String name; // Название объекта
    public String organization; // Организация
    public String department; // Подразделение
    public String status; // Статус: "На согласовании", "Списано"
}
