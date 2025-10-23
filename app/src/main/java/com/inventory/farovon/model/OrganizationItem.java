package com.inventory.farovon.model;

import java.util.ArrayList;
import java.util.List;

public class OrganizationItem {
    private String name;
    private int level;
    private boolean isExpanded;
    private List<OrganizationItem> children;

    public OrganizationItem(String name, int level) {
        this.name = name;
        this.level = level;
        this.isExpanded = false;
        this.children = new ArrayList<>();
    }

    public String getName() {
        return name;
    }

    public int getLevel() {
        return level;
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