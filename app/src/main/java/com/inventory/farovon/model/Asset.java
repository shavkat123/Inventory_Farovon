package com.inventory.farovon.model;

import java.io.Serializable;

public class Asset implements Serializable {
    private String name;
    private String inventoryNumber;
    private String serialNumber;
    private String location;
    private String organization;
    private String status;

    public Asset(String name, String inventoryNumber, String serialNumber, String location, String organization, String status) {
        this.name = name;
        this.inventoryNumber = inventoryNumber;
        this.serialNumber = serialNumber;
        this.location = location;
        this.organization = organization;
        this.status = status;
    }

    // Getters and setters
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getInventoryNumber() { return inventoryNumber; }
    public void setInventoryNumber(String inventoryNumber) { this.inventoryNumber = inventoryNumber; }
    public String getSerialNumber() { return serialNumber; }
    public void setSerialNumber(String serialNumber) { this.serialNumber = serialNumber; }
    public String getLocation() { return location; }
    public void setLocation(String location) { this.location = location; }
    public String getOrganization() { return organization; }
    public void setOrganization(String organization) { this.organization = organization; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
}
