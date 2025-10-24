package com.inventory.farovon;

import com.inventory.farovon.model.OrganizationItem;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

public class OrganizationXmlParser {

    public List<OrganizationItem> parse(InputStream is) throws Exception {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        DocumentBuilder builder = factory.newDocumentBuilder();
        Document doc = builder.parse(is);
        doc.getDocumentElement().normalize();

        List<OrganizationItem> organizationItems = new ArrayList<>();
        NodeList organizationNodes = doc.getElementsByTagName("Organization");

        for (int i = 0; i < organizationNodes.getLength(); i++) {
            Node orgNode = organizationNodes.item(i);
            if (orgNode.getNodeType() == Node.ELEMENT_NODE) {
                Element orgElement = (Element) orgNode;
                String orgName = orgElement.getAttribute("ref");
                OrganizationItem orgItem = new OrganizationItem(orgName, 0);

                Map<String, OrganizationItem> departmentMap = new HashMap<>();
                List<Element> departmentElements = new ArrayList<>();

                NodeList departmentNodes = orgElement.getElementsByTagName("Department");
                for (int j = 0; j < departmentNodes.getLength(); j++) {
                    departmentElements.add((Element) departmentNodes.item(j));
                }

                for (Element deptElement : departmentElements) {
                    String deptName = deptElement.getAttribute("name");
                    OrganizationItem deptItem = new OrganizationItem(deptName, 1);
                    departmentMap.put(deptName, deptItem);
                }

                for (Element deptElement : departmentElements) {
                    String deptName = deptElement.getAttribute("name");
                    String parentRef = deptElement.getAttribute("parentRef");
                    OrganizationItem deptItem = departmentMap.get(deptName);

                    if (parentRef != null && !parentRef.isEmpty() && departmentMap.containsKey(parentRef)) {
                        OrganizationItem parentItem = departmentMap.get(parentRef);
                        if (parentItem != null) {
                            parentItem.addChild(deptItem);
                            deptItem.setLevel(parentItem.getLevel() + 1);
                        }
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