package com.inventory.farovon.ui.assetmovement;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.viewpager2.adapter.FragmentStateAdapter;

public class AssetMovementPagerAdapter extends FragmentStateAdapter {

    private AssetMovementParametersFragment parametersFragment;
    private AssetMovementAssetsFragment assetsFragment;

    public AssetMovementPagerAdapter(@NonNull FragmentActivity fragmentActivity) {
        super(fragmentActivity);
    }

    @NonNull
    @Override
    public Fragment createFragment(int position) {
        if (position == 0) {
            if (parametersFragment == null) {
                parametersFragment = new AssetMovementParametersFragment();
            }
            return parametersFragment;
        }
        if (assetsFragment == null) {
            assetsFragment = new AssetMovementAssetsFragment();
        }
        return assetsFragment;
    }

    @Override
    public int getItemCount() {
        return 2;
    }

    public AssetMovementParametersFragment getParametersFragment() {
        return parametersFragment;
    }

    public AssetMovementAssetsFragment getAssetsFragment() {
        return assetsFragment;
    }
}
