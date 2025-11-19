package com.inventory.farovon.ui.molmovement;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.viewpager2.adapter.FragmentStateAdapter;

public class MolMovementPagerAdapter extends FragmentStateAdapter {

    private MolParametersFragment parametersFragment;
    private MolAssetsFragment assetsFragment;

    public MolMovementPagerAdapter(@NonNull FragmentActivity fragmentActivity) {
        super(fragmentActivity);
    }

    @NonNull
    @Override
    public Fragment createFragment(int position) {
        if (position == 0) {
            if (parametersFragment == null) {
                parametersFragment = new MolParametersFragment();
            }
            return parametersFragment;
        }
        if (assetsFragment == null) {
            assetsFragment = new MolAssetsFragment();
        }
        return assetsFragment;
    }

    @Override
    public int getItemCount() {
        return 2;
    }

    public MolParametersFragment getParametersFragment() {
        return parametersFragment;
    }

    public MolAssetsFragment getAssetsFragment() {
        return assetsFragment;
    }
}