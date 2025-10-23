package com.inventory.farovon.model;

import java.io.Serializable;
import java.util.Date;
import java.util.List;

public class IssueDocument implements Serializable {
    private String number;
    private Date date;
    private String fromIssuer;
    private String fromIssuerDepartment;
    private String fromOrganization;
    private String fromLocation;
    private String toRecipient;
    private String toRecipientDepartment;
    private String toOrganization;
    private String toLocation;
    private String status;
    private List<Asset> assets;

    public IssueDocument(String number, Date date, String fromIssuer, String fromIssuerDepartment, String fromOrganization, String fromLocation, String toRecipient, String toRecipientDepartment, String toOrganization, String toLocation, String status, List<Asset> assets) {
        this.number = number;
        this.date = date;
        this.fromIssuer = fromIssuer;
        this.fromIssuerDepartment = fromIssuerDepartment;
        this.fromOrganization = fromOrganization;
        this.fromLocation = fromLocation;
        this.toRecipient = toRecipient;
        this.toRecipientDepartment = toRecipientDepartment;
        this.toOrganization = toOrganization;
        this.toLocation = toLocation;
        this.status = status;
        this.assets = assets;
    }

    // Getters
    public String getNumber() { return number; }
    public Date getDate() { return date; }
    public String getFromIssuer() { return fromIssuer; }
    public String getFromIssuerDepartment() { return fromIssuerDepartment; }
    public String getFromOrganization() { return fromOrganization; }
    public String getFromLocation() { return fromLocation; }
    public String getToRecipient() { return toRecipient; }
    public String getToRecipientDepartment() { return toRecipientDepartment; }
    public String getToOrganization() { return toOrganization; }
    public String getToLocation() { return toLocation; }
    public String getStatus() { return status; }
    public List<Asset> getAssets() { return assets; }
}
