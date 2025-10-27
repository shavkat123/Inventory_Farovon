package com.inventory.farovon;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserFactory;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.StringReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class InventoryListActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private InventoryAdapter adapter;
    private final List<InventoryItem> inventoryItems = new ArrayList<>();
    private TextView titleText;
    private final ExecutorService executor = Executors.newSingleThreadExecutor();
    private final Handler handler = new Handler(Looper.getMainLooper());

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_inventory_list);

        titleText = findViewById(R.id.title_text);
        recyclerView = findViewById(R.id.inventory_recycler_view);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new InventoryAdapter(inventoryItems);
        recyclerView.setAdapter(adapter);

        String itemCode = getIntent().getStringExtra("ITEM_CODE");
        String itemName = getIntent().getStringExtra("ITEM_NAME");

        if (itemName != null) {
            titleText.setText(itemName);
        }

        if (itemCode != null) {
            fetchInventory(itemCode);
        }
    }

    private void fetchInventory(String code) {
        executor.execute(() -> {
            String xmlResult = fetchXmlFromServer(code);
            List<InventoryItem> parsedItems = parseXmlWithPullParser(xmlResult);

            handler.post(() -> {
                if (parsedItems != null && !parsedItems.isEmpty()) {
                    inventoryItems.clear();
                    inventoryItems.addAll(parsedItems);
                    adapter.notifyDataSetChanged();
                } else {
                     Log.e("InventoryListActivity", "Parsing resulted in an empty list.");
                }
            });
        });
    }

    private String fetchXmlFromServer(String code) {
        try {
            SessionManager sessionManager = new SessionManager(getApplicationContext());
            URL url = new URL("http://" + sessionManager.getIP() + "/my1c/hs/hw/say");
            HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
            try {
                urlConnection.setRequestMethod("POST");
                urlConnection.setDoOutput(true);
                urlConnection.setRequestProperty("Content-Type", "text/plain; charset=utf-8");

                try (OutputStream os = urlConnection.getOutputStream()) {
                    byte[] input = code.getBytes(StandardCharsets.UTF_8);
                    os.write(input, 0, input.length);
                }

                InputStream in = urlConnection.getInputStream();
                java.util.Scanner s = new java.util.Scanner(in).useDelimiter("\\A");
                return s.hasNext() ? s.next() : "";
            } finally {
                urlConnection.disconnect();
            }
        } catch (Exception e) {
            Log.e("InventoryListActivity", "Error fetching inventory data", e);
            return null;
        }
    }

    private List<InventoryItem> parseXmlWithPullParser(String xml) {
        if (xml == null || xml.isEmpty()) {
            return new ArrayList<>();
        }
        List<InventoryItem> items = new ArrayList<>();
        try {
            XmlPullParserFactory factory = XmlPullParserFactory.newInstance();
            XmlPullParser parser = factory.newPullParser();
            parser.setInput(new StringReader(xml));

            int eventType = parser.getEventType();
            while (eventType != XmlPullParser.END_DOCUMENT) {
                if (eventType == XmlPullParser.START_TAG && "СтрокаН".equals(parser.getName())) {
                    String name = parser.getAttributeValue(null, "Наименование");
                    String code = parser.getAttributeValue(null, "Код");
                    String invCode = parser.getAttributeValue(null, "ИнвКод");
                    String status = parser.getAttributeValue(null, "Статус");
                    items.add(new InventoryItem(name, code, invCode, status));
                }
                eventType = parser.next();
            }
        } catch (Exception e) {
            Log.e("InventoryListActivity", "XML parsing error", e);
            return new ArrayList<>();
        }
        return items;
    }
}
