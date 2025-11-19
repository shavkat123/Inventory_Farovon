package com.inventory.farovon.ui.molmovement;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.viewpager2.adapter.FragmentStateAdapter;

public class MolMovementPagerAdapter extends FragmentStateAdapter {

    public MolMovementPagerAdapter(@NonNull FragmentActivity fragmentActivity) {
        super(fragmentActivity);
    }

    @NonNull
    @Override
    public Fragment createFragment(int position) {
        if (position == 0) {
            return new MolParametersFragment();
        }
        return new MolAssetsFragment();
    }

    @Override
    public int getItemCount() {
        return 2; // We have two tabs
    }
}