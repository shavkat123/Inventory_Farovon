package com.inventory.farovon.ui.home;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;

import com.inventory.farovon.R;
import com.inventory.farovon.databinding.FragmentHomeBinding;
import com.inventory.farovon.ui.login.LoginDialogFragment;

public class HomeFragment extends Fragment {

    private FragmentHomeBinding binding;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {

        binding = FragmentHomeBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        final Button defineDepartmentBtn = binding.defineDepartment;
        defineDepartmentBtn.setOnClickListener(v -> {
            NavController navController = Navigation.findNavController(v);
            navController.navigate(R.id.nav_gallery);
        });

        final Button settingsButton = binding.button2;
        settingsButton.setOnClickListener(v -> {
            LoginDialogFragment dialogFragment = new LoginDialogFragment();
            dialogFragment.show(getParentFragmentManager(), "LoginDialogFragment");
        });

        return root;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}