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
    private TextInputEditText etOrganization;
    private TextInputEditText etDepartment;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_write_off_params, container, false);

        etName = view.findViewById(R.id.et_name);
        etOrganization = view.findViewById(R.id.et_organization);
        etDepartment = view.findViewById(R.id.et_department);

        view.findViewById(R.id.button_save).setOnClickListener(v -> {
            if (getActivity() instanceof WriteOffActivity) {
                ((WriteOffActivity) getActivity()).saveDocument();
            }
        });

        return view;
    }

    public String getName() {
        return etName.getText() != null ? etName.getText().toString().trim() : "";
    }

    public String getOrganization() {
        return etOrganization.getText() != null ? etOrganization.getText().toString().trim() : "";
    }

    public String getDepartment() {
        return etDepartment.getText() != null ? etDepartment.getText().toString().trim() : "";
    }
}
