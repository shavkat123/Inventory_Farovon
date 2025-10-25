package com.inventory.farovon.model;

import java.util.ArrayList;
import java.util.List;

public class OrganizationItem {
    private int id;
    private String name;
    private String code;
    private int level;
    private boolean isExpanded;
    private List<OrganizationItem> children;

    public OrganizationItem(String name, int level) {
        this.name = name;
        this.level = level;
        this.isExpanded = false;
        this.children = new ArrayList<>();
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public int getLevel() {
        return level;
    }

    public void setLevel(int level) {
        this.level = level;
    }

    public boolean isExpanded() {
        return isExpanded;
    }

    public void setExpanded(boolean expanded) {
        isExpanded = expanded;
    }

    public List<OrganizationItem> getChildren() {
        return children;
    }

    public void addChild(OrganizationItem child) {
        children.add(child);
    }
}
