package com.inventory.farovon.ui.gallery;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Rect;
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
import android.widget.ProgressBar;
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
import com.inventory.farovon.NomenclatureActivity;
import com.inventory.farovon.R;
import com.inventory.farovon.db.AppDatabase;
import com.inventory.farovon.db.InventoryItemEntity;
import com.inventory.farovon.ui.login.SessionManager;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserFactory;
import java.io.IOException;
import java.io.StringReader;
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

public class GalleryFragment extends Fragment {

    private PreviewView previewView;
    private TextView tvResult;
    private TextView tvHint;
    private Button btnRequestPermission;
    private ProgressBar progressBar;
    private AppDatabase db;

    private ExecutorService cameraExecutor;
    private ExecutorService databaseExecutor;
    private ProcessCameraProvider cameraProvider;
    private volatile boolean isProcessingBarcode = false;

    private ActivityResultLauncher<String> requestPermissionLauncher;
    private final Handler mainHandler = new Handler(Looper.getMainLooper());

    private Rect overlayRect;
    private SessionManager sessionManager;
    private String roomCodeToVerify;
    private String roomNameToVerify;
    private String departmentCode;
    private int departmentId;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        sessionManager = new SessionManager(requireActivity());
        if (getArguments() != null) {
            roomCodeToVerify = getArguments().getString("room_code_to_verify");
            roomNameToVerify = getArguments().getString("room_name_to_verify");
            departmentCode = getArguments().getString("department_code");
            departmentId = getArguments().getInt("department_id", -1);
        }
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
        databaseExecutor = Executors.newSingleThreadExecutor();
        db = AppDatabase.getDatabase(requireContext());
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_gallery, container, false);
        previewView = root.findViewById(R.id.previewView);
        tvResult = root.findViewById(R.id.tvResult);
        tvHint = root.findViewById(R.id.tvScanHint);
        btnRequestPermission = root.findViewById(R.id.btnRequestPermission);
        progressBar = root.findViewById(R.id.progressBar);
        cameraExecutor = Executors.newSingleThreadExecutor();
        btnRequestPermission.setOnClickListener(v -> {
            Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
                    Uri.fromParts("package", requireContext().getPackageName(), null));
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
        });
        View overlay = root.findViewById(R.id.overlay);
        overlay.post(() -> {
            overlayRect = new Rect();
            overlay.getGlobalVisibleRect(overlayRect);
        });
        checkPermissionAndStart();
        return root;
    }

    private void checkPermissionAndStart() {
        if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
            tvHint.setVisibility(View.VISIBLE);
            btnRequestPermission.setVisibility(View.GONE);
            startCamera();
        } else {
            requestPermissionLauncher.launch(Manifest.permission.CAMERA);
        }
    }

    private void startCamera() {
        ListenableFuture<ProcessCameraProvider> cameraProviderFuture = ProcessCameraProvider.getInstance(requireContext());
        cameraProviderFuture.addListener(() -> {
            try {
                cameraProvider = cameraProviderFuture.get();
                CameraSelector cameraSelector = new CameraSelector.Builder().requireLensFacing(CameraSelector.LENS_FACING_BACK).build();
                ImageAnalysis imageAnalysis = new ImageAnalysis.Builder().setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST).build();
                BarcodeScannerOptions options = new BarcodeScannerOptions.Builder().setBarcodeFormats(Barcode.FORMAT_ALL_FORMATS).build();
                BarcodeScanner scanner = BarcodeScanning.getClient(options);
                imageAnalysis.setAnalyzer(cameraExecutor, image -> {
                    if (image.getImage() == null) {
                        image.close();
                        return;
                    }
                    InputImage inputImage = InputImage.fromMediaImage(image.getImage(), image.getImageInfo().getRotationDegrees());
                    scanner.process(inputImage)
                            .addOnSuccessListener(barcodes -> {
                                if (barcodes != null && !barcodes.isEmpty() && !isProcessingBarcode) {
                                    isProcessingBarcode = true;
                                    processBarcodes(barcodes, image.getWidth(), image.getHeight());
                                }
                            })
                            .addOnFailureListener(e -> Log.e("GalleryFragment", "Ошибка сканера", e))
                            .addOnCompleteListener(task -> image.close());
                });
                cameraProvider.unbindAll();
                Preview preview = new Preview.Builder().build();
                preview.setSurfaceProvider(previewView.getSurfaceProvider());
                cameraProvider.bindToLifecycle(getViewLifecycleOwner(), cameraSelector, preview, imageAnalysis);
            } catch (Exception e) {
                Log.e("GalleryFragment", "Ошибка запуска камеры", e);
            }
        }, ContextCompat.getMainExecutor(requireContext()));
    }

    private void processBarcodes(List<Barcode> barcodes, int imageWidth, int imageHeight) {
        if (overlayRect == null) {
            isProcessingBarcode = false;
            return;
        }
        for (Barcode barcode : barcodes) {
            Rect bounds = barcode.getBoundingBox();
            if (bounds != null) {
                Rect mappedRect = mapToPreviewView(bounds, imageWidth, imageHeight);
                if (overlayRect.contains(mappedRect)) {
                    final String value = barcode.getRawValue();
                    if (value != null && !value.isEmpty()) {
                        mainHandler.post(() -> {
                            if (!isAdded()) {
                                isProcessingBarcode = false;
                                return;
                            }
                            tvResult.setText("Сканировано: " + value);
                            if (roomCodeToVerify != null && roomCodeToVerify.equals(value)) {
                                Toast.makeText(requireContext(), "Код помещения подтвержден!", Toast.LENGTH_SHORT).show();
                                fetchAndSaveInventoryData(value);
                            } else if (roomCodeToVerify != null) {
                                Toast.makeText(requireContext(), "Неверный QR-код. Отсканирован: " + value, Toast.LENGTH_LONG).show();
                                mainHandler.postDelayed(() -> isProcessingBarcode = false, 2000);
                            } else {
                                isProcessingBarcode = false;
                            }
                        });
                    } else {
                        isProcessingBarcode = false;
                    }
                    break;
                }
            }
        }
        if (!isProcessingBarcode) {
            isProcessingBarcode = false;
        }
    }

    private Rect mapToPreviewView(Rect bounds, int imageWidth, int imageHeight) {
        if (previewView.getWidth() == 0 || previewView.getHeight() == 0) return bounds;
        float scaleX = (float) previewView.getWidth() / imageWidth;
        float scaleY = (float) previewView.getHeight() / imageHeight;
        return new Rect((int)(bounds.left * scaleX), (int)(bounds.top * scaleY), (int)(bounds.right * scaleX), (int)(bounds.bottom * scaleY));
    }

    private void fetchAndSaveInventoryData(String roomCode) {
        mainHandler.post(() -> progressBar.setVisibility(View.VISIBLE));
        String serverIP = sessionManager.getIpAddress();
        String username = sessionManager.getUsername();
        String password = sessionManager.getPassword();
        String url = "http://" + serverIP + "/my1c/hs/hw/say";
        OkHttpClient client = new OkHttpClient();
        String json = "{\"otdel\":\"" + roomCode + "\"}";
        RequestBody body = RequestBody.create(json, MediaType.parse("application/json; charset=utf-8"));
        Request request = new Request.Builder()
                .url(url)
                .post(body)
                .header("Authorization", okhttp3.Credentials.basic(username, password))
                .build();
        client.newCall(request).enqueue(new okhttp3.Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                mainHandler.post(() -> {
                    progressBar.setVisibility(View.GONE);
                    Toast.makeText(requireContext(), "Ошибка сети: " + e.getMessage(), Toast.LENGTH_LONG).show();
                    isProcessingBarcode = false;
                });
            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                if (!response.isSuccessful()) {
                    mainHandler.post(() -> {
                        progressBar.setVisibility(View.GONE);
                        Toast.makeText(requireContext(), "Ошибка сервера: " + response.code(), Toast.LENGTH_LONG).show();
                        isProcessingBarcode = false;
                    });
                    return;
                }
                try {
                    String xmlResponse = response.body().string();
                    List<InventoryItemEntity> items = parseInventoryXml(xmlResponse);
                    databaseExecutor.execute(() -> {
                        db.inventoryItemDao().clearByDepartmentIdAndLocation(departmentId, roomCodeToVerify);
                        db.inventoryItemDao().insertAll(items);
                        mainHandler.post(() -> {
                            progressBar.setVisibility(View.GONE);
                            navigateToNomenclature();
                        });
                    });
                } catch (Exception e) {
                    mainHandler.post(() -> {
                        progressBar.setVisibility(View.GONE);
                        Toast.makeText(requireContext(), "Ошибка обработки данных: " + e.getMessage(), Toast.LENGTH_LONG).show();
                        isProcessingBarcode = false;
                    });
                }
            }
        });
    }

    private List<InventoryItemEntity> parseInventoryXml(String xml) throws Exception {
        List<InventoryItemEntity> items = new ArrayList<>();
        XmlPullParserFactory factory = XmlPullParserFactory.newInstance();
        factory.setNamespaceAware(true);
        XmlPullParser xpp = factory.newPullParser();
        xpp.setInput(new StringReader(xml));
        InventoryItemEntity currentItem = null;
        String text = "";
        int eventType = xpp.getEventType();
        while (eventType != XmlPullParser.END_DOCUMENT) {
            String tagName = xpp.getName();
            switch (eventType) {
                case XmlPullParser.START_TAG:
                    if ("Product".equalsIgnoreCase(tagName)) {
                        currentItem = new InventoryItemEntity();
                        currentItem.departmentId = departmentId;
                    }
                    break;
                case XmlPullParser.TEXT:
                    text = xpp.getText();
                    break;
                case XmlPullParser.END_TAG:
                    if (currentItem != null) {
                        if ("code".equalsIgnoreCase(tagName)) {
                            currentItem.code = text;
                        } else if ("name".equalsIgnoreCase(tagName)) {
                            currentItem.name = text;
                        } else if ("rf".equalsIgnoreCase(tagName)) {
                            currentItem.rf = text != null ? text : "";
                        } else if ("mol".equalsIgnoreCase(tagName)) {
                            currentItem.mol = text != null ? text : "";
                        } else if ("location".equalsIgnoreCase(tagName)) {
                            currentItem.location = text != null ? text : "";
                        } else if ("Product".equalsIgnoreCase(tagName)) {
                            items.add(currentItem);
                            currentItem = null;
                        }
                    }
                    break;
            }
            eventType = xpp.next();
        }
        return items;
    }

    private void navigateToNomenclature() {
        if (!isAdded()) return;
        Intent intent = new Intent(requireContext(), NomenclatureActivity.class);
        intent.putExtra("room_code", roomCodeToVerify);
        intent.putExtra("department_code", departmentCode);
        intent.putExtra("department_id", departmentId);
        intent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP | Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(intent);
        mainHandler.postDelayed(() -> isProcessingBarcode = false, 500);
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
    }
}
