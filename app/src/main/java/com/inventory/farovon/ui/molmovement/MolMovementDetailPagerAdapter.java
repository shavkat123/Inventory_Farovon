package com.inventory.farovon.ui.molmovement;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.viewpager2.adapter.FragmentStateAdapter;
import android.os.Bundle;

public class MolMovementDetailPagerAdapter extends FragmentStateAdapter {

    private final long documentId;
    private MolParametersFragment parametersFragment;

    public MolMovementDetailPagerAdapter(@NonNull FragmentActivity fragmentActivity, long documentId) {
        super(fragmentActivity);
        this.documentId = documentId;
    }

    @NonNull
    @Override
    public Fragment createFragment(int position) {
        if (position == 0) {
            parametersFragment = new MolParametersFragment();
            Bundle args = new Bundle();
            args.putLong("DOCUMENT_ID", documentId);
            parametersFragment.setArguments(args);
            return parametersFragment;
        }
        return MolAssetsDetailFragment.newInstance(documentId);
    }

    @Override
    public int getItemCount() {
        return 2;
    }

    public MolParametersFragment getParametersFragment() {
        return parametersFragment;
    }
}
