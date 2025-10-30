package com.inventory.farovon;

public class Room {
    private final String code;
    private final String name;

    public Room(String code, String name) {
        this.code = code;
        this.name = name;
    }

    public String getCode() {
        return code;
    }

    public String getName() {
        return name;
    }
}
