package com.inventory.farovon.ui.issuefromwarehouse;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import com.inventory.farovon.R;
import com.inventory.farovon.model.Asset;
import com.inventory.farovon.model.IssueDocument;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.UUID;

public class ParametersFragment extends Fragment {

    private IssueDocument document;
    private boolean isEditMode = false;

    private EditText fromResponsible, fromDepartment, fromOrganization, toLocation;

    public static ParametersFragment newInstance(IssueDocument document, boolean isEditMode) {
        ParametersFragment fragment = new ParametersFragment();
        Bundle args = new Bundle();
        args.putSerializable("DOCUMENT", document);
        args.putBoolean("IS_EDIT_MODE", isEditMode);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            document = (IssueDocument) getArguments().getSerializable("DOCUMENT");
            isEditMode = getArguments().getBoolean("IS_EDIT_MODE");
        }
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_parameters, container, false);

        fromResponsible = view.findViewById(R.id.from_responsible);
        fromDepartment = view.findViewById(R.id.from_department);
        fromOrganization = view.findViewById(R.id.from_organization);
        toLocation = view.findViewById(R.id.to_location);

        if (isEditMode) {
            populateUI();
        }

        View fromHeader = view.findViewById(R.id.from_header);
        View fromBody = view.findViewById(R.id.from_body);
        View fromChevron = view.findViewById(R.id.from_chevron);

        View toHeader = view.findViewById(R.id.to_header);
        View toBody = view.findViewById(R.id.to_body);
        View toChevron = view.findViewById(R.id.to_chevron);

        fromHeader.setOnClickListener(v -> toggleSection(fromBody, fromChevron));
        toHeader.setOnClickListener(v -> toggleSection(toBody, toChevron));

        return view;
    }

    private void populateUI() {
        if (document == null) return;

        fromResponsible.setText(document.getFromResponsible());
        fromDepartment.setText(document.getFromDepartment());
        fromOrganization.setText(document.getFromOrganization());
        toLocation.setText(document.getToLocation());

        // Make fields non-editable
        fromResponsible.setEnabled(false);
        fromDepartment.setEnabled(false);
        fromOrganization.setEnabled(false);
        toLocation.setEnabled(false);
    }

    public IssueDocument getDocumentData(List<Asset> assets) {
        return new IssueDocument(
                UUID.randomUUID().toString(),
                new Date(),
                fromResponsible.getText().toString(),
                fromDepartment.getText().toString(),
                fromOrganization.getText().toString(),
                toLocation.getText().toString(),
                "Выдан",
                assets
        );
    }

    private void toggleSection(View body, View chevron) {
        boolean isVisible = body.getVisibility() == View.VISIBLE;
        body.setVisibility(isVisible ? View.GONE : View.VISIBLE);
        chevron.animate().rotation(isVisible ? 0f : 180f).setDuration(200).start();
    }
}
