package com.inventory.farovon.ui.gallery;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Rect;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.provider.Settings;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.camera.core.CameraSelector;
import androidx.camera.core.ImageAnalysis;
import androidx.camera.core.ImageProxy;
import androidx.camera.core.Preview;
import androidx.camera.lifecycle.ProcessCameraProvider;
import androidx.camera.view.PreviewView;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import com.google.common.util.concurrent.ListenableFuture;
import com.google.mlkit.vision.barcode.BarcodeScanner;
import com.google.mlkit.vision.barcode.BarcodeScannerOptions;
import com.google.mlkit.vision.barcode.BarcodeScanning;
import com.google.mlkit.vision.barcode.common.Barcode;
import com.google.mlkit.vision.common.InputImage;

import com.inventory.farovon.MainActivity;
import com.inventory.farovon.NomenclatureActivity;
import com.inventory.farovon.R;
import com.inventory.farovon.Nomenclature;
import com.inventory.farovon.db.AppDatabase;
import com.inventory.farovon.db.InventoryItemEntity;
import com.inventory.farovon.ui.login.SessionManager;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import okhttp3.Call;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

public class GalleryFragment extends Fragment {

    private PreviewView previewView;
    private TextView tvResult;
    private TextView tvHint;
    private Button btnRequestPermission;

    private ExecutorService cameraExecutor;
    private ExecutorService databaseExecutor;
    private ProcessCameraProvider cameraProvider;
    private volatile boolean isProcessingBarcode = false;

    private ActivityResultLauncher<String> requestPermissionLauncher;
    private final Handler mainHandler = new Handler(Looper.getMainLooper());

    // 🔹 Поле для рамки overlay
    private Rect overlayRect;

    private SessionManager sessionManager;
    private AppDatabase db;

    private String roomCodeToVerify;
    private String roomNameToVerify;
    private String departmentCode;
    private int departmentId;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        sessionManager = new SessionManager(getActivity());
        db = AppDatabase.getDatabase(requireContext());
        databaseExecutor = Executors.newSingleThreadExecutor();

        if (getArguments() != null) {
            roomCodeToVerify = getArguments().getString("room_code_to_verify");
            roomNameToVerify = getArguments().getString("room_name_to_verify");
            departmentCode = getArguments().getString("department_code");
            departmentId = getArguments().getInt("department_id", -1);
        }

        // Регистрируем launcher разрешения
        requestPermissionLauncher = registerForActivityResult(
                new ActivityResultContracts.RequestPermission(),
                isGranted -> {
                    if (isGranted) {
                        startCamera();
                        tvHint.setVisibility(View.VISIBLE);
                        btnRequestPermission.setVisibility(View.GONE);
                    } else {
                        tvHint.setVisibility(View.GONE);
                        btnRequestPermission.setVisibility(View.VISIBLE);
                        Toast.makeText(requireContext(), "Нужно разрешение на камеру", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {

        View root = inflater.inflate(R.layout.fragment_gallery, container, false);

        previewView = root.findViewById(R.id.previewView);
        tvResult = root.findViewById(R.id.tvResult);
        tvHint = root.findViewById(R.id.tvScanHint);
        btnRequestPermission = root.findViewById(R.id.btnRequestPermission);

        cameraExecutor = Executors.newSingleThreadExecutor();

        btnRequestPermission.setOnClickListener(v -> {
            Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
                    Uri.fromParts("package", requireContext().getPackageName(), null));
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
        });

        // 🔹 Получаем координаты overlay после отрисовки
        View overlay = root.findViewById(R.id.overlay);
        overlay.post(() -> {
            overlayRect = new Rect();
            overlay.getGlobalVisibleRect(overlayRect);
        });

        checkPermissionAndStart();

        return root;
    }

    private List<Nomenclature> parseXml(String xmlResponse) {
        List<Nomenclature> list = new ArrayList<>();
        try {
            InputStream stream = new ByteArrayInputStream(xmlResponse.getBytes(StandardCharsets.UTF_8));

            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            DocumentBuilder builder = factory.newDocumentBuilder();
            Document doc = builder.parse(stream);
            doc.getDocumentElement().normalize();

            NodeList nList = doc.getElementsByTagName("Product");

            for (int i = 0; i < nList.getLength(); i++) {
                Node node = nList.item(i);
                if (node.getNodeType() == Node.ELEMENT_NODE) {
                    Element element = (Element) node;

                    String code = element.getElementsByTagName("Code").item(0).getTextContent();
                    String name = element.getElementsByTagName("Name").item(0).getTextContent();
                    String rf = element.getElementsByTagName("rf").item(0).getTextContent();

                    list.add(new Nomenclature(code, name, rf, null, null));
                }
            }

            Log.d("GalleryFragment", "Parsed items: " + list.size());
        } catch (Exception e) {
            e.printStackTrace();
            Log.e("GalleryFragment", "XML parse error", e);
        }
        return list;
    }


    private void checkPermissionAndStart() {
        if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.CAMERA)
                == PackageManager.PERMISSION_GRANTED) {
            tvHint.setVisibility(View.VISIBLE);
            btnRequestPermission.setVisibility(View.GONE);
            startCamera();
        } else {
            requestPermissionLauncher.launch(Manifest.permission.CAMERA);
        }
    }

    private void startCamera() {
        ListenableFuture<ProcessCameraProvider> cameraProviderFuture =
                ProcessCameraProvider.getInstance(requireContext());

        cameraProviderFuture.addListener(() -> {
            try {
                cameraProvider = cameraProviderFuture.get();

                CameraSelector cameraSelector = new CameraSelector.Builder()
                        .requireLensFacing(CameraSelector.LENS_FACING_BACK)
                        .build();

                ImageAnalysis imageAnalysis = new ImageAnalysis.Builder()
                        .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                        .build();

                BarcodeScannerOptions options =
                        new BarcodeScannerOptions.Builder()
                                .setBarcodeFormats(Barcode.FORMAT_ALL_FORMATS)
                                .build();

                BarcodeScanner scanner = BarcodeScanning.getClient(options);

                imageAnalysis.setAnalyzer(cameraExecutor, new ImageAnalysis.Analyzer() {
                    @Override
                    public void analyze(@NonNull ImageProxy image) {
                        if (image.getImage() == null) {
                            image.close();
                            return;
                        }

                        InputImage inputImage = InputImage.fromMediaImage(
                                image.getImage(),
                                image.getImageInfo().getRotationDegrees()
                        );

                        scanner.process(inputImage)
                                .addOnSuccessListener(barcodes -> {
                                    if (barcodes != null && !barcodes.isEmpty() && !isProcessingBarcode) {
                                        isProcessingBarcode = true;
                                        processBarcodes(barcodes, image.getWidth(), image.getHeight());
                                    }
                                })
                                .addOnFailureListener(e -> Log.e("GalleryFragment", "Ошибка сканера", e))
                                .addOnCompleteListener(task -> image.close());
                    }
                });

                cameraProvider.unbindAll();

                Preview preview = new Preview.Builder().build();
                preview.setSurfaceProvider(previewView.getSurfaceProvider());

                cameraProvider.bindToLifecycle(
                        getViewLifecycleOwner(),
                        cameraSelector,
                        preview,
                        imageAnalysis
                );

            } catch (Exception e) {
                Log.e("GalleryFragment", "Ошибка запуска камеры", e);
            }
        }, ContextCompat.getMainExecutor(requireContext()));
    }

    // 🔹 Обновленный метод обработки штрихкодов
    private void processBarcodes(List<Barcode> barcodes, int imageWidth, int imageHeight) {
        if (overlayRect == null) {
            isProcessingBarcode = false;
            return;
        }

        boolean found = false;
        for (Barcode barcode : barcodes) {
            Rect bounds = barcode.getBoundingBox();
            if (bounds != null) {
                Rect mappedRect = mapToPreviewView(bounds, imageWidth, imageHeight);

                if (overlayRect.contains(mappedRect)) {
                    final String value = barcode.getRawValue();
                    if (value != null && !value.isEmpty()) {
                        found = true;
                        mainHandler.post(() -> {
                            if (!isAdded()) {
                                return;
                            }
                            tvResult.setText("Сканировано: " + value);
                            if (roomCodeToVerify != null && roomCodeToVerify.equals(value)) {
                                Toast.makeText(requireContext(), "Код помещения подтвержден!", Toast.LENGTH_SHORT).show();
                                loadRoomItems(value);
                            } else if (roomCodeToVerify != null) {
                                Toast.makeText(requireContext(), "Неверный QR-код помещения. Отсканирован: " + value, Toast.LENGTH_LONG).show();
                                mainHandler.postDelayed(() -> isProcessingBarcode = false, 2000);
                            } else {
                                loadRoomItems(value);
                                mainHandler.postDelayed(() -> isProcessingBarcode = false, 5000);
                            }
                        });
                    }
                    break;
                }
            }
        }
        // Если ни один штрихкод не попал в рамку — сбрасываем флаг
        if (!found) {
            isProcessingBarcode = false;
        }
    }

    // 🔹 Масштабируем координаты из кадра камеры в PreviewView
    private Rect mapToPreviewView(Rect bounds, int imageWidth, int imageHeight) {
        if (previewView.getWidth() == 0 || previewView.getHeight() == 0) return bounds;

        float scaleX = (float) previewView.getWidth() / imageWidth;
        float scaleY = (float) previewView.getHeight() / imageHeight;

        return new Rect(
                (int)(bounds.left * scaleX),
                (int)(bounds.top * scaleY),
                (int)(bounds.right * scaleX),
                (int)(bounds.bottom * scaleY)
        );
    }

    private void loadRoomItems(String roomCode) {
        databaseExecutor.execute(() -> {
            List<InventoryItemEntity> entities = db.inventoryItemDao().getByDepartmentIdAndLocation(departmentId, roomCode);
            if (!entities.isEmpty()) {
                List<Nomenclature> items = new ArrayList<>();
                for (InventoryItemEntity e : entities) {
                    items.add(new Nomenclature(e.code, e.name, e.rf, e.mol, e.location));
                }
                mainHandler.post(() -> openNomenclatureActivity(items, roomCode));
            } else {
                if (isNetworkAvailable()) {
                    fetchFromNetwork(roomCode);
                } else {
                    mainHandler.post(() -> {
                        Toast.makeText(requireContext(), "Данные не найдены и нет сети", Toast.LENGTH_SHORT).show();
                        isProcessingBarcode = false; // Allow rescanning
                    });
                }
            }
        });
    }

    private void fetchFromNetwork(String roomCode) {
        String serverIP = sessionManager.getIpAddress();
        String url = "http://" + serverIP +"/my1c/hs/hw/say";
        Log.i("GalleryFragment", url);
        OkHttpClient client = new OkHttpClient();

        String json = "{\"odel\":\"" + roomCode + "\"}";
        RequestBody body = RequestBody.create(
                json,
                MediaType.parse("application/json; charset=utf-8")
        );

        String credentials = okhttp3.Credentials.basic(sessionManager.getUsername(), sessionManager.getPassword());

        Request request = new Request.Builder()
                .url(url)
                .post(body)
                .header("Authorization", credentials)
                .header("Content-Type", "application/json")
                .build();

        client.newCall(request).enqueue(new okhttp3.Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                Log.e("GalleryFragment", "Ошибка сети", e);
                mainHandler.post(() -> {
                    String errorMsg = "Ошибка сети: " + e.getMessage();
                    Toast.makeText(requireContext(), errorMsg, Toast.LENGTH_SHORT).show();
                    isProcessingBarcode = false; // Allow rescanning
                });
            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                if (response.isSuccessful() && response.body() != null) {
                    final String xmlResponse = response.body().string();
                    final List<Nomenclature> items = parseXml(xmlResponse);

                    if (items.isEmpty()) {
                         mainHandler.post(() -> {
                             Toast.makeText(requireContext(), "Инвентарь пуст", Toast.LENGTH_SHORT).show();
                             isProcessingBarcode = false;
                         });
                         return;
                    }

                    // Save to DB
                    databaseExecutor.execute(() -> {
                        db.inventoryItemDao().clearByDepartmentIdAndLocation(departmentId, roomCode);
                        List<InventoryItemEntity> entities = new ArrayList<>();
                        for (Nomenclature item : items) {
                            InventoryItemEntity e = new InventoryItemEntity();
                            e.departmentId = departmentId;
                            e.code = item.getCode();
                            e.name = item.getName();
                            e.rf = item.getRfid();
                            e.location = roomCode; // Ensure correct location
                            e.mol = ""; // Or null, XML parser puts null in Nomenclature
                            e.serialNumber = ""; // XML parser doesn't read it
                            entities.add(e);
                        }
                        db.inventoryItemDao().insertAll(entities);

                        mainHandler.post(() -> openNomenclatureActivity(items, roomCode));
                    });
                } else {
                     mainHandler.post(() -> {
                        Toast.makeText(requireContext(), "Ошибка сервера: " + response.code(), Toast.LENGTH_SHORT).show();
                        isProcessingBarcode = false;
                     });
                }
            }
        });
    }

    private void openNomenclatureActivity(List<Nomenclature> items, String roomCode) {
        Intent intent = new Intent(requireContext(), NomenclatureActivity.class);
        intent.putExtra("items", new ArrayList<>(items));
        intent.putExtra("room_code", roomCode);
        intent.putExtra("department_code", departmentCode);
        intent.putExtra("department_id", departmentId);
        intent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP | Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(intent);
        // Do not reset isProcessingBarcode here immediately if you want to prevent double scans during transition.
        // It will be reset next time user comes back or via the timeout in processBarcodes if we rely on that.
        // But better is to just leave it true until onResume? No, existing code relied on timeouts.
        // I will reset it here or let the activity start.
        // The original code didn't reset it in success path of sendBarcodeToServer.
    }

    private boolean isNetworkAvailable() {
        ConnectivityManager connectivityManager = (ConnectivityManager) requireContext().getSystemService(Context.CONNECTIVITY_SERVICE);
        if (connectivityManager != null) {
            NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
            return activeNetworkInfo != null && activeNetworkInfo.isConnected();
        }
        return false;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (cameraProvider != null) {
            cameraProvider.unbindAll();
        }
        if (cameraExecutor != null) {
            cameraExecutor.shutdown();
        }
        if (databaseExecutor != null) {
             databaseExecutor.shutdown();
        }
    }
}
