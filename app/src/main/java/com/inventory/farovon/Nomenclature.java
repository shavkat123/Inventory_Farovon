package com.inventory.farovon;

import java.io.Serializable;

public class Nomenclature implements Serializable { // добавляем Serializable
    private String code;
    private String name;
    private String rf;
    public int scanCount; // для scanCount
    public Nomenclature(String code, String name, String rf) {
        this.code = code;
        this.name = name;
        this.rf = rf;
        this.scanCount = 0;
    }

    public String getCode() { return code; }
    public String getName() { return name; }
    public String getRfid() { return rf; }
}
