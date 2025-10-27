package com.inventory.farovon;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserFactory;
import java.io.InputStream;
import java.io.StringReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class OrganizationInventoryActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private OrganizationAdapter adapter;
    private final List<OrganizationItem> organizationItems = new ArrayList<>();
    private final ExecutorService executor = Executors.newSingleThreadExecutor();
    private final Handler handler = new Handler(Looper.getMainLooper());

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_organization_inventory);

        recyclerView = findViewById(R.id.recycler_view);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new OrganizationAdapter(organizationItems);
        recyclerView.setAdapter(adapter);

        fetchData();
    }

    private void fetchData() {
        executor.execute(() -> {
            String xmlResult = fetchXmlFromServer();
            List<OrganizationItem> parsedItems = parseXmlWithPullParser(xmlResult);

            handler.post(() -> {
                if (parsedItems != null && !parsedItems.isEmpty()) {
                    organizationItems.clear();
                    organizationItems.addAll(parsedItems);
                    adapter.notifyDataSetChanged();
                } else {
                    Log.e("OrgInventoryActivity", "Parsing resulted in an empty list.");
                }
            });
        });
    }

    private String fetchXmlFromServer() {
        try {
            SessionManager sessionManager = new SessionManager(getApplicationContext());
            URL url = new URL("http://" + sessionManager.getIP() + "/my1c/hs/checking/schema");
            HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
            try {
                InputStream in = urlConnection.getInputStream();
                java.util.Scanner s = new java.util.Scanner(in).useDelimiter("\\A");
                return s.hasNext() ? s.next() : "";
            } finally {
                urlConnection.disconnect();
            }
        } catch (Exception e) {
            Log.e("OrgInventoryActivity", "Error fetching data", e);
            return null;
        }
    }

    private List<OrganizationItem> parseXmlWithPullParser(String xml) {
        if (xml == null || xml.isEmpty()) {
            return new ArrayList<>();
        }

        List<OrganizationItem> items = new ArrayList<>();
        try {
            XmlPullParserFactory factory = XmlPullParserFactory.newInstance();
            XmlPullParser parser = factory.newPullParser();
            parser.setInput(new StringReader(xml));

            int eventType = parser.getEventType();
            int currentLevel = -1;

            while (eventType != XmlPullParser.END_DOCUMENT) {
                String tagName = parser.getName();
                switch (eventType) {
                    case XmlPullParser.START_TAG:
                        currentLevel++;
                        if ("Организация".equals(tagName) || "Подразделение".equals(tagName) || "Кабинет".equals(tagName)) {
                            String ref = parser.getAttributeValue(null, "ref");
                            String code = parser.getAttributeValue(null, "code");
                            String name = parser.getAttributeValue(null, "name");
                            if (name != null && code != null && ref != null) {
                                items.add(new OrganizationItem(ref, code, name, tagName, currentLevel));
                            }
                        }
                        break;
                    case XmlPullParser.END_TAG:
                        currentLevel--;
                        break;
                }
                eventType = parser.next();
            }
        } catch (Exception e) {
            Log.e("OrgInventoryActivity", "XML parsing error", e);
            return new ArrayList<>();
        }
        return items;
    }
}
