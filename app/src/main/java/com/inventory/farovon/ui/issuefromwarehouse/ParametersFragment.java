package com.inventory.farovon.ui.issuefromwarehouse;

import android.os.Bundle;
import android.text.TextUtils;
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

import java.util.Date;
import java.util.List;
import java.util.UUID;

public class ParametersFragment extends Fragment {

    private IssueDocument document;
    private boolean isEditMode = false;

    private EditText fromIssuer, fromIssuerDepartment, fromOrganization, fromLocation;
    private EditText toRecipient, toRecipientDepartment, toOrganization, toLocation;

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

        fromIssuer = view.findViewById(R.id.from_issuer);
        fromIssuerDepartment = view.findViewById(R.id.from_issuer_department);
        fromOrganization = view.findViewById(R.id.from_organization);
        fromLocation = view.findViewById(R.id.from_location);
        toRecipient = view.findViewById(R.id.to_recipient);
        toRecipientDepartment = view.findViewById(R.id.to_recipient_department);
        toOrganization = view.findViewById(R.id.to_organization);
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

        fromIssuer.setText(document.getFromIssuer());
        fromIssuerDepartment.setText(document.getFromIssuerDepartment());
        fromOrganization.setText(document.getFromOrganization());
        fromLocation.setText(document.getFromLocation());
        toRecipient.setText(document.getToRecipient());
        toRecipientDepartment.setText(document.getToRecipientDepartment());
        toOrganization.setText(document.getToOrganization());
        toLocation.setText(document.getToLocation());

        // Make fields non-editable
        fromIssuer.setEnabled(false);
        fromIssuerDepartment.setEnabled(false);
        fromOrganization.setEnabled(false);
        fromLocation.setEnabled(false);
        toRecipient.setEnabled(false);
        toRecipientDepartment.setEnabled(false);
        toOrganization.setEnabled(false);
        toLocation.setEnabled(false);
    }

    public boolean validateFields() {
        boolean isValid = true;
        if (TextUtils.isEmpty(fromIssuer.getText())) {
            fromIssuer.setError("Поле не должно быть пустым");
            isValid = false;
        }
        if (TextUtils.isEmpty(fromIssuerDepartment.getText())) {
            fromIssuerDepartment.setError("Поле не должно быть пустым");
            isValid = false;
        }
        if (TextUtils.isEmpty(fromOrganization.getText())) {
            fromOrganization.setError("Поле не должно быть пустым");
            isValid = false;
        }
        if (TextUtils.isEmpty(fromLocation.getText())) {
            fromLocation.setError("Поле не должно быть пустым");
            isValid = false;
        }
        if (TextUtils.isEmpty(toRecipient.getText())) {
            toRecipient.setError("Поле не должно быть пустым");
            isValid = false;
        }
        if (TextUtils.isEmpty(toRecipientDepartment.getText())) {
            toRecipientDepartment.setError("Поле не должно быть пустым");
            isValid = false;
        }
        if (TextUtils.isEmpty(toOrganization.getText())) {
            toOrganization.setError("Поле не должно быть пустым");
            isValid = false;
        }
        if (TextUtils.isEmpty(toLocation.getText())) {
            toLocation.setError("Поле не должно быть пустым");
            isValid = false;
        }
        return isValid;
    }

    public IssueDocument getDocumentData(List<Asset> assets) {
        return new IssueDocument(
                UUID.randomUUID().toString(),
                new Date(),
                fromIssuer.getText().toString(),
                fromIssuerDepartment.getText().toString(),
                fromOrganization.getText().toString(),
                fromLocation.getText().toString(),
                toRecipient.getText().toString(),
                toRecipientDepartment.getText().toString(),
                toOrganization.getText().toString(),
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
