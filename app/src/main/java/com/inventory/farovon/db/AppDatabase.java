package com.inventory.farovon.db;

import android.content.Context;
import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;

@Database(entities = {
        OrganizationEntity.class,
        DepartmentEntity.class,
        InventoryItemEntity.class,
        PendingUploadEntity.class,
        MolMovementDocument.class,
        MolMovementItem.class
}, version = 8, exportSchema = false)
public abstract class AppDatabase extends RoomDatabase {

    public abstract OrganizationDao organizationDao();
    public abstract DepartmentDao departmentDao();
    public abstract InventoryItemDao inventoryItemDao();
    public abstract PendingUploadDao pendingUploadDao();
    public abstract MolMovementDao molMovementDao();

    private static volatile AppDatabase INSTANCE;

    public static AppDatabase getDatabase(final Context context) {
        if (INSTANCE == null) {
            synchronized (AppDatabase.class) {
                if (INSTANCE == null) {
                    INSTANCE = Room.databaseBuilder(context.getApplicationContext(),
                            AppDatabase.class, "inventory_database")
                            .fallbackToDestructiveMigration()
                            .build();
                }
            }
        }
        return INSTANCE;
    }
}
