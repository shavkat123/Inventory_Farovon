package com.inventory.farovon;

public class Room {
    private final String code;
    private final String name;
    private boolean isCompleted;

    public Room(String code, String name) {
        this.code = code;
        this.name = name;
        this.isCompleted = false;
    }

    public String getCode() {
        return code;
    }

    public String getName() {
        return name;
    }

    public boolean isCompleted() {
        return isCompleted;
    }

    public void setCompleted(boolean completed) {
        isCompleted = completed;
    }
}
