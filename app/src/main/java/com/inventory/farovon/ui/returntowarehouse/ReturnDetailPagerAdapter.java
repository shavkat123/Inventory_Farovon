package com.inventory.farovon.ui.returntowarehouse;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.viewpager2.adapter.FragmentStateAdapter;

public class ReturnDetailPagerAdapter extends FragmentStateAdapter {

    private final long documentId;
    private ReturnParametersFragment parametersFragment;

    public ReturnDetailPagerAdapter(@NonNull FragmentActivity fragmentActivity, long documentId) {
        super(fragmentActivity);
        this.documentId = documentId;
    }

    @NonNull
    @Override
    public Fragment createFragment(int position) {
        if (position == 0) {
            parametersFragment = new ReturnParametersFragment();
            Bundle args = new Bundle();
            args.putLong("DOCUMENT_ID", documentId);
            parametersFragment.setArguments(args);
            return parametersFragment;
        }
        return ReturnAssetsDetailFragment.newInstance(documentId);
    }

    @Override
    public int getItemCount() {
        return 2;
    }

    public ReturnParametersFragment getParametersFragment() {
        return parametersFragment;
    }
}
