package com.inventory.farovon.db;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "asset_movement_documents")
public class AssetMovementDocument {
    @PrimaryKey(autoGenerate = true)
    public long id;

    public long date;

    public String fromIssuer;
    public String fromIssuerDepartment;
    public String fromOrganization;
    public String fromLocation;

    public String toRecipient;
    public String toRecipientDepartment;
    public String toOrganization;
    public String toLocation;
}
