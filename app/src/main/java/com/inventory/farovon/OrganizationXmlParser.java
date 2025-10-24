package com.inventory.farovon;

import com.inventory.farovon.model.OrganizationItem;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserFactory;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class OrganizationXmlParser {

    public List<OrganizationItem> parse(InputStream is) throws Exception {
        XmlPullParserFactory factory = XmlPullParserFactory.newInstance();
        factory.setNamespaceAware(true);
        XmlPullParser parser = factory.newPullParser();
        parser.setInput(is, null);

        List<OrganizationItem> organizationItems = new ArrayList<>();
        OrganizationItem currentOrganization = null;
        Map<String, OrganizationItem> departmentMap = new HashMap<>();
        int eventType = parser.getEventType();

        while (eventType != XmlPullParser.END_DOCUMENT) {
            String tagName = parser.getName();
            switch (eventType) {
                case XmlPullParser.START_TAG:
                    if ("Organization".equalsIgnoreCase(tagName)) {
                        currentOrganization = new OrganizationItem(parser.getAttributeValue(null, "ref"), 0);
                        organizationItems.add(currentOrganization);
                        departmentMap.clear();
                    } else if ("Department".equalsIgnoreCase(tagName) && currentOrganization != null) {
                        String name = parser.getAttributeValue(null, "name");
                        String code = parser.getAttributeValue(null, "code");
                        String parentRef = parser.getAttributeValue(null, "parentRef");

                        OrganizationItem deptItem = new OrganizationItem(name, 1);
                        deptItem.setCode(code);

                        departmentMap.put(name, deptItem);

                        if (parentRef != null && !parentRef.isEmpty() && departmentMap.containsKey(parentRef)) {
                            OrganizationItem parentItem = departmentMap.get(parentRef);
                            if (parentItem != null) {
                                parentItem.addChild(deptItem);
                                deptItem.setLevel(parentItem.getLevel() + 1);
                            }
                        } else {
                            currentOrganization.addChild(deptItem);
                        }
                    }
                    break;
            }
            eventType = parser.next();
        }
        return organizationItems;
    }
}
