package com.inventory.farovon;

public class InventoryItem {
    private String name;
    private String code;
    private String invCode;
    private String status;

    public InventoryItem(String name, String code, String invCode, String status) {
        this.name = name;
        this.code = code;
        this.invCode = invCode;
        this.status = status;
    }

    public String getName() {
        return name;
    }

    public String getCode() {
        return code;
    }

    public String getInvCode() {
        return invCode;
    }

    public String getStatus() {
        return status;
    }
}
