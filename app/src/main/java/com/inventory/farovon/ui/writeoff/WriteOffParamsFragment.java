package com.inventory.farovon.ui.writeoff;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;
import androidx.fragment.app.Fragment;

import com.google.android.material.textfield.TextInputEditText;
import com.inventory.farovon.R;
import com.inventory.farovon.WriteOffActivity;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import static android.app.Activity.RESULT_OK;

public class WriteOffParamsFragment extends Fragment {

    private TextInputEditText etName;
    private TextInputEditText etReason;
    private TextInputEditText etDepartment;
    private TextInputEditText etCondition;
    private ImageView ivPhoto;
    private LinearLayout layoutTakePhoto;
    private String currentPhotoPath;

    private final ActivityResultLauncher<Intent> cameraLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == RESULT_OK) {
                    displayPhoto();
                }
            }
    );

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_write_off_params, container, false);

        etName = view.findViewById(R.id.et_name);
        etReason = view.findViewById(R.id.et_reason);
        etDepartment = view.findViewById(R.id.et_department);
        etCondition = view.findViewById(R.id.et_condition);
        ivPhoto = view.findViewById(R.id.iv_photo);
        layoutTakePhoto = view.findViewById(R.id.layout_take_photo);

        layoutTakePhoto.setOnClickListener(v -> checkCameraPermissionAndOpen());
        ivPhoto.setOnClickListener(v -> checkCameraPermissionAndOpen());

        view.findViewById(R.id.button_save).setOnClickListener(v -> {
            if (getActivity() instanceof WriteOffActivity) {
                ((WriteOffActivity) getActivity()).saveDocument();
            }
        });

        return view;
    }

    private void checkCameraPermissionAndOpen() {
        if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(new String[]{Manifest.permission.CAMERA}, 100);
        } else {
            dispatchTakePictureIntent();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == 100) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                dispatchTakePictureIntent();
            } else {
                Toast.makeText(getContext(), "Для фото требуется разрешение на камеру", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void dispatchTakePictureIntent() {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (takePictureIntent.resolveActivity(requireActivity().getPackageManager()) != null) {
            File photoFile = null;
            try {
                photoFile = createImageFile();
            } catch (IOException ex) {
                Toast.makeText(getContext(), "Ошибка при создании файла фото", Toast.LENGTH_SHORT).show();
            }
            if (photoFile != null) {
                Uri photoURI = FileProvider.getUriForFile(requireContext(),
                        "com.inventory.farovon.fileprovider",
                        photoFile);
                takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI);
                cameraLauncher.launch(takePictureIntent);
            }
        } else {
             Toast.makeText(getContext(), "Камера не найдена", Toast.LENGTH_SHORT).show();
        }
    }

    private File createImageFile() throws IOException {
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(new Date());
        String imageFileName = "JPEG_" + timeStamp + "_";
        File storageDir = requireContext().getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        File image = File.createTempFile(
                imageFileName,  /* prefix */
                ".jpg",         /* suffix */
                storageDir      /* directory */
        );
        currentPhotoPath = image.getAbsolutePath();
        return image;
    }

    private void displayPhoto() {
        if (currentPhotoPath != null) {
            ivPhoto.setVisibility(View.VISIBLE);
            layoutTakePhoto.setVisibility(View.GONE);
            ivPhoto.setImageURI(Uri.fromFile(new File(currentPhotoPath)));
        }
    }

    public String getName() {
        return etName.getText() != null ? etName.getText().toString().trim() : "";
    }

    public String getReason() {
        return etReason.getText() != null ? etReason.getText().toString().trim() : "";
    }

    public String getDepartment() {
        return etDepartment.getText() != null ? etDepartment.getText().toString().trim() : "";
    }

    public String getCondition() {
        return etCondition.getText() != null ? etCondition.getText().toString().trim() : "";
    }

    public String getPhotoPath() {
        return currentPhotoPath;
    }
}
