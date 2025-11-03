package com.inventory.farovon;

import java.io.Serializable;

public class Nomenclature implements Serializable {
    private String code;
    private String name;
    private String rf;
    private String mol;
    private String location;
    public int scanCount;

    public Nomenclature(String code, String name, String rf, String mol, String location) {
        this.code = code;
        this.name = name;
        this.rf = rf;
        this.mol = mol;
        this.location = location;
        this.scanCount = 0;
    }

    public String getCode() { return code; }
    public String getName() { return name; }
    public String getRfid() { return rf; }
    public String getMol() { return mol; }
    public String getLocation() { return location; }
}
