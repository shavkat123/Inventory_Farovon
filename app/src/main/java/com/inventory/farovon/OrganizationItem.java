package com.inventory.farovon;

public class OrganizationItem {
    private String ref;
    private String code;
    private String name;
    private String type;
    private int level;

    public OrganizationItem(String ref, String code, String name, String type, int level) {
        this.ref = ref;
        this.code = code;
        this.name = name;
        this.type = type;
        this.level = level;
    }

    public String getRef() {
        return ref;
    }

    public String getCode() {
        return code;
    }

    public String getName() {
        return name;
    }

    public String getType() {
        return type;
    }

    public int getLevel() {
        return level;
    }
}
