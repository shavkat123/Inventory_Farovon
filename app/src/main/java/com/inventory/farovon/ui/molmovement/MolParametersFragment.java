package com.inventory.farovon.ui.molmovement;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import com.inventory.farovon.R;
import com.inventory.farovon.db.AppDatabase;
import com.inventory.farovon.db.MolMovementDao;
import com.google.android.material.textfield.TextInputEditText;
import com.inventory.farovon.db.MolMovementDocument;
import com.inventory.farovon.MolMovementActivity;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class MolParametersFragment extends Fragment {

    private TextInputEditText fromMolEditText;
    private TextInputEditText fromDepartmentEditText;
    private TextInputEditText fromOrganizationEditText;
    private TextInputEditText toMolEditText;
    private View createButton;
    private ExecutorService databaseExecutor;
    private MolMovementDao molMovementDao;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        databaseExecutor = Executors.newSingleThreadExecutor();
        molMovementDao = AppDatabase.getDatabase(requireContext().getApplicationContext()).molMovementDao();
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_mol_parameters, container, false);

        fromMolEditText = view.findViewById(R.id.from_mol);
        fromDepartmentEditText = view.findViewById(R.id.from_department);
        fromOrganizationEditText = view.findViewById(R.id.from_organization);
        toMolEditText = view.findViewById(R.id.to_mol);
        createButton = view.findViewById(R.id.button_create_document);

        View fromHeader = view.findViewById(R.id.from_header);
        View fromBody = view.findViewById(R.id.from_body);
        View fromChevron = view.findViewById(R.id.from_chevron);

        View toHeader = view.findViewById(R.id.to_header);
        View toBody = view.findViewById(R.id.to_body);
        View toChevron = view.findViewById(R.id.to_chevron);

        fromHeader.setOnClickListener(v -> toggleSection(fromBody, fromChevron));
        toHeader.setOnClickListener(v -> toggleSection(toBody, toChevron));

        createButton.setOnClickListener(v -> {
            if (getActivity() instanceof MolMovementActivity) {
                ((MolMovementActivity) requireActivity()).saveDocument();
            }
        });

        if (getArguments() != null && getArguments().containsKey("DOCUMENT_ID")) {
            long documentId = getArguments().getLong("DOCUMENT_ID");
            loadDocument(documentId);
        }

        return view;
    }

    private void loadDocument(long documentId) {
        databaseExecutor.execute(() -> {
            MolMovementDocument document = molMovementDao.getDocumentById(documentId);
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

    public String getFromMol() {
        return fromMolEditText.getText().toString().trim();
    }

    public String getFromDepartment() {
        return fromDepartmentEditText.getText().toString().trim();
    }

    public String getFromOrganization() {
        return fromOrganizationEditText.getText().toString().trim();
    }

    public String getToMol() {
        return toMolEditText.getText().toString().trim();
    }

    public void setViewMode() {
        fromMolEditText.setEnabled(false);
        fromDepartmentEditText.setEnabled(false);
        fromOrganizationEditText.setEnabled(false);
        toMolEditText.setEnabled(false);
        createButton.setVisibility(View.GONE);
    }

    public void displayDocumentData(MolMovementDocument document) {
        if (document != null) {
            fromMolEditText.setText(document.fromMol);
            fromDepartmentEditText.setText(document.fromDepartment);
            fromOrganizationEditText.setText(document.fromOrganization);
            toMolEditText.setText(document.toMol);
        }
    }
}