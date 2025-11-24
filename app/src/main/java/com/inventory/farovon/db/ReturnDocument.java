package com.inventory.farovon.db;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "return_documents")
public class ReturnDocument {
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
