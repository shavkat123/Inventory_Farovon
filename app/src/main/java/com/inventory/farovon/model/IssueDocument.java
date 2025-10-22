package com.inventory.farovon.model;

import java.io.Serializable;
import java.util.Date;
import java.util.List;

public class IssueDocument implements Serializable {
    private String number;
    private Date date;
    private String fromResponsible;
    private String fromDepartment;
    private String fromOrganization;
    private String toLocation;
    private String status;
    private List<Asset> assets;

    public IssueDocument(String number, Date date, String fromResponsible, String fromDepartment, String fromOrganization, String toLocation, String status, List<Asset> assets) {
        this.number = number;
        this.date = date;
        this.fromResponsible = fromResponsible;
        this.fromDepartment = fromDepartment;
        this.fromOrganization = fromOrganization;
        this.toLocation = toLocation;
        this.status = status;
        this.assets = assets;
    }

    // Getters and setters
    public String getNumber() { return number; }
    public void setNumber(String number) { this.number = number; }
    public Date getDate() { return date; }
    public void setDate(Date date) { this.date = date; }
    public String getFromResponsible() { return fromResponsible; }
    public void setFromResponsible(String fromResponsible) { this.fromResponsible = fromResponsible; }
    public String getFromDepartment() { return fromDepartment; }
    public void setFromDepartment(String fromDepartment) { this.fromDepartment = fromDepartment; }
    public String getFromOrganization() { return fromOrganization; }
    public void setFromOrganization(String fromOrganization) { this.fromOrganization = fromOrganization; }
    public String getToLocation() { return toLocation; }
    public void setToLocation(String toLocation) { this.toLocation = toLocation; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public List<Asset> getAssets() { return assets; }
    public void setAssets(List<Asset> assets) { this.assets = assets; }
}
