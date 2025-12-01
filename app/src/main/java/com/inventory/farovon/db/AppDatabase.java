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
        MolMovementItem.class,
        AssetMovementDocument.class,
        AssetMovementItem.class,
        IssueDocument.class,
        IssueItem.class,
        ReturnDocument.class,
        ReturnItem.class,
        WriteOffDocument.class,
        WriteOffItem.class,
        RoomEntity.class
}, version = 15, exportSchema = false)
public abstract class AppDatabase extends RoomDatabase {

    public abstract OrganizationDao organizationDao();
    public abstract DepartmentDao departmentDao();
    public abstract RoomDao roomDao();
    public abstract InventoryItemDao inventoryItemDao();
    public abstract PendingUploadDao pendingUploadDao();
    public abstract MolMovementDao molMovementDao();
    public abstract AssetMovementDao assetMovementDao();
    public abstract IssueDao issueDao();
    public abstract ReturnDao returnDao();
    public abstract WriteOffDao writeOffDao();

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
