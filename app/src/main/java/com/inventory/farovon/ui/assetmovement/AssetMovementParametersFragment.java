package com.inventory.farovon.ui.assetmovement;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.EditText;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import com.inventory.farovon.R;
import com.inventory.farovon.db.AppDatabase;
import com.inventory.farovon.db.AssetMovementDao;
import com.inventory.farovon.db.AssetMovementDocument;
import com.inventory.farovon.db.DepartmentDao;
import com.inventory.farovon.db.DepartmentEntity;
import com.inventory.farovon.AssetMovementActivity;
import com.google.android.material.textfield.MaterialAutoCompleteTextView;
import com.google.android.material.textfield.TextInputEditText;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class AssetMovementParametersFragment extends Fragment {

    private TextInputEditText fromIssuer, fromIssuerDepartment, fromOrganization;
    private MaterialAutoCompleteTextView fromLocation;
    private TextInputEditText toRecipient, toRecipientDepartment, toOrganization, toLocation;
    private View createButton;
    private ExecutorService databaseExecutor;
    private AssetMovementDao assetMovementDao;
    private DepartmentDao departmentDao;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        databaseExecutor = Executors.newSingleThreadExecutor();
        AppDatabase db = AppDatabase.getDatabase(requireContext().getApplicationContext());
        assetMovementDao = db.assetMovementDao();
        departmentDao = db.departmentDao();
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_asset_movement_parameters, container, false);

        fromIssuer = view.findViewById(R.id.from_issuer);
        fromIssuerDepartment = view.findViewById(R.id.from_issuer_department);
        fromOrganization = view.findViewById(R.id.from_organization);
        fromLocation = view.findViewById(R.id.from_location);

        toRecipient = view.findViewById(R.id.to_recipient);
        toRecipientDepartment = view.findViewById(R.id.to_recipient_department);
        toOrganization = view.findViewById(R.id.to_organization);
        toLocation = view.findViewById(R.id.to_location);
        createButton = view.findViewById(R.id.button_create_document);

        createButton.setOnClickListener(v -> {
            if (getActivity() instanceof AssetMovementActivity) {
                ((AssetMovementActivity) requireActivity()).saveDocument();
            }
        });

        View fromHeader = view.findViewById(R.id.from_header);
        View fromBody = view.findViewById(R.id.from_body);
        View fromChevron = view.findViewById(R.id.from_chevron);

        View toHeader = view.findViewById(R.id.to_header);
        View toBody = view.findViewById(R.id.to_body);
        View toChevron = view.findViewById(R.id.to_chevron);

        fromHeader.setOnClickListener(v -> toggleSection(fromBody, fromChevron));
        toHeader.setOnClickListener(v -> toggleSection(toBody, toChevron));

        if (getArguments() != null && getArguments().containsKey("DOCUMENT_ID")) {
            long documentId = getArguments().getLong("DOCUMENT_ID");
            loadDocument(documentId);
        }

        loadDepartments();

        return view;
    }

    private void loadDepartments() {
        databaseExecutor.execute(() -> {
            List<DepartmentEntity> departments = departmentDao.getAll();
            List<String> departmentNames = new ArrayList<>();
            for (DepartmentEntity dep : departments) {
                if (dep.name != null) {
                    departmentNames.add(dep.name);
                }
            }

            new Handler(Looper.getMainLooper()).post(() -> {
                if (getContext() != null) {
                    ArrayAdapter<String> adapter = new ArrayAdapter<>(requireContext(), android.R.layout.simple_dropdown_item_1line, departmentNames);
                    fromLocation.setAdapter(adapter);
                }
            });
        });
    }

    private void loadDocument(long documentId) {
        databaseExecutor.execute(() -> {
            AssetMovementDocument document = assetMovementDao.getDocumentById(documentId);
            new Handler(Looper.getMainLooper()).post(() -> {
                if (document != null) {
                    displayDocumentData(document);
                    setViewMode();
                }
            });
        });
    }

    private void toggleSection(View body, View chevron) {
        boolean isVisible = body.getVisibility() == View.VISIBLE;
        body.setVisibility(isVisible ? View.GONE : View.VISIBLE);
        chevron.animate().rotation(isVisible ? 0f : 180f).setDuration(200).start();
    }

    public String getFromIssuer() { return fromIssuer.getText().toString().trim(); }
    public String getFromIssuerDepartment() { return fromIssuerDepartment.getText().toString().trim(); }
    public String getFromOrganization() { return fromOrganization.getText().toString().trim(); }
    public String getFromLocation() { return fromLocation.getText().toString().trim(); }

    public String getToRecipient() { return toRecipient.getText().toString().trim(); }
    public String getToRecipientDepartment() { return toRecipientDepartment.getText().toString().trim(); }
    public String getToOrganization() { return toOrganization.getText().toString().trim(); }
    public String getToLocation() { return toLocation.getText().toString().trim(); }

    public void setViewMode() {
        fromIssuer.setEnabled(false);
        fromIssuerDepartment.setEnabled(false);
        fromOrganization.setEnabled(false);
        fromLocation.setEnabled(false);
        toRecipient.setEnabled(false);
        toRecipientDepartment.setEnabled(false);
        toOrganization.setEnabled(false);
        toLocation.setEnabled(false);
        // Hide button if I add it
        if (createButton != null) createButton.setVisibility(View.GONE);
    }

    public void displayDocumentData(AssetMovementDocument document) {
        if (document != null) {
            fromIssuer.setText(document.fromIssuer);
            fromIssuerDepartment.setText(document.fromIssuerDepartment);
            fromOrganization.setText(document.fromOrganization);
            fromLocation.setText(document.fromLocation);
            toRecipient.setText(document.toRecipient);
            toRecipientDepartment.setText(document.toRecipientDepartment);
            toOrganization.setText(document.toOrganization);
            toLocation.setText(document.toLocation);
        }
    }
}
