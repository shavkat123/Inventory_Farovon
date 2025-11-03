package com.inventory.farovon;

import com.inventory.farovon.model.OrganizationItem;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class OrganizationXmlParser {

    // This parser is specifically designed to be extremely lenient.
    // It does not parse XML as a structure, but rather hunts for specific patterns,
    // ignoring most syntax rules. This makes it robust against malformed XML from the server.

    public List<OrganizationItem> parse(String xmlString) {
        List<OrganizationItem> organizationItems = new ArrayList<>();
        if (xmlString == null || xmlString.isEmpty()) {
            return organizationItems;
        }

        // Split the entire XML string into <Organization> blocks.
        // This is more robust than a single regex for the whole file.
        String[] orgBlocks = xmlString.split("</Organization>");

        for (String orgBlock : orgBlocks) {
            if (!orgBlock.contains("<Organization")) {
                continue;
            }

            // Regex to find the 'ref' of an Organization. It looks for ref='...' or ref="..."
            Pattern orgPattern = Pattern.compile("<Organization\\s+ref=['\"]([^'\"]*)['\"]");
            Matcher orgMatcher = orgPattern.matcher(orgBlock);

            if (orgMatcher.find()) {
                String orgName = orgMatcher.group(1);
                OrganizationItem orgItem = new OrganizationItem(orgName, 0);

                // Regex to find all attributes of a Department tag. Very lenient.
                Pattern deptPattern = Pattern.compile("<Department\\s+([^>]+)/?>");
                Matcher deptMatcher = deptPattern.matcher(orgBlock);

                Map<String, OrganizationItem> departmentMap = new HashMap<>();
                List<Map<String, String>> departmentsData = new ArrayList<>();

                while (deptMatcher.find()) {
                    String attributes = deptMatcher.group(1);
                    Map<String, String> attrMap = new HashMap<>();

                    // Regex to extract key-value pairs from the attribute string.
                    Pattern attrPattern = Pattern.compile("(\\w+)=['\"]([^'\"]*)['\"]");
                    Matcher attrMatcher = attrPattern.matcher(attributes);
                    while(attrMatcher.find()){
                        attrMap.put(attrMatcher.group(1), attrMatcher.group(2));
                    }

                    if(attrMap.containsKey("name")){
                        departmentsData.add(attrMap);
                        OrganizationItem deptItem = new OrganizationItem(attrMap.get("name"), 1);
                        deptItem.setCode(attrMap.get("code"));
                        departmentMap.put(attrMap.get("name"), deptItem);
                    }
                }

                for (Map<String, String> attrs : departmentsData) {
                    String parentRef = attrs.get("parentRef");
                    OrganizationItem deptItem = departmentMap.get(attrs.get("name"));

                    if (parentRef != null && !parentRef.isEmpty() && departmentMap.containsKey(parentRef)) {
                        departmentMap.get(parentRef).addChild(deptItem);
                        deptItem.setLevel(departmentMap.get(parentRef).getLevel() + 1);
                    } else {
                        orgItem.addChild(deptItem);
                    }
                }
                organizationItems.add(orgItem);
            }
        }
        return organizationItems;
    }
}
