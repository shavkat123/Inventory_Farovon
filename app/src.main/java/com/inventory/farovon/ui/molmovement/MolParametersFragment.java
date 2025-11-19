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
import com.google.android.material.textfield.TextInputEditText;
import com.inventory.farovon.db.AppDatabase;
import com.inventory.farovon.db.MolMovementDao;
import com.inventory.farovon.db.MolMovementDocument;
import com.inventory.farovon.MolMovementActivity;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class MolParametersFragment extends Fragment {

    private TextInputEditText fromMolEditText;
    private TextInputEditText toMolEditText;
    private View createButton;
    private MolMovementDao molMovementDao;
    private ExecutorService databaseExecutor;
    private Handler handler = new Handler(Looper.getMainLooper());

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        AppDatabase db = AppDatabase.getDatabase(requireContext().getApplicationContext());
        molMovementDao = db.molMovementDao();
        databaseExecutor = Executors.newSingleThreadExecutor();
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_mol_parameters, container, false);

        fromMolEditText = view.findViewById(R.id.from_mol);
        toMolEditText = view.findViewById(R.id.to_mol);
        createButton = view.findViewById(R.id.button_create_document);

        // ... (rest of the view setup)

        createButton.setOnClickListener(v -> {
            if (getActivity() instanceof MolMovementActivity) {
                ((MolMovementActivity) requireActivity()).saveDocument();
            }
        });

        if (getArguments() != null && getArguments().containsKey("DOCUMENT_ID")) {
            long documentId = getArguments().getLong("DOCUMENT_ID");
            loadDocument(documentId);
            setViewMode();
        }

        return view;
    }

    private void loadDocument(long documentId) {
        databaseExecutor.execute(() -> {
            MolMovementDocument document = molMovementDao.getDocumentById(documentId);
            handler.post(() -> displayDocumentData(document));
        });
    }

    public String getFromMol() {
        return fromMolEditText.getText().toString().trim();
    }

    public String getToMol() {
        return toMolEditText.getText().toString().trim();
    }

    public void setViewMode() {
        fromMolEditText.setEnabled(false);
        toMolEditText.setEnabled(false);
        createButton.setVisibility(View.GONE);
    }

    public void displayDocumentData(MolMovementDocument document) {
        if (document != null) {
            fromMolEditText.setText(document.fromMol);
            toMolEditText.setText(document.toMol);
        }
    }
}