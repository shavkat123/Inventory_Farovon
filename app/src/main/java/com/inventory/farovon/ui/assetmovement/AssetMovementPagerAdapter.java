package com.inventory.farovon.ui.assetmovement;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.viewpager2.adapter.FragmentStateAdapter;

public class AssetMovementPagerAdapter extends FragmentStateAdapter {

    public AssetMovementPagerAdapter(@NonNull FragmentActivity fragmentActivity) {
        super(fragmentActivity);
    }

    @NonNull
    @Override
    public Fragment createFragment(int position) {
        if (position == 0) {
            return new ParametersFragment();
        }
        return new AssetsFragment();
    }

    @Override
    public int getItemCount() {
        return 2; // We have two tabs
    }
}