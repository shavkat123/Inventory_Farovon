package com.inventory.farovon.ui.writeoff;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.viewpager2.adapter.FragmentStateAdapter;

public class WriteOffPagerAdapter extends FragmentStateAdapter {

    private final WriteOffParamsFragment parametersFragment;
    private final WriteOffAssetsFragment assetsFragment;

    public WriteOffPagerAdapter(@NonNull FragmentActivity fragmentActivity) {
        super(fragmentActivity);
        parametersFragment = new WriteOffParamsFragment();
        assetsFragment = new WriteOffAssetsFragment();
    }

    @NonNull
    @Override
    public Fragment createFragment(int position) {
        if (position == 0) {
            return parametersFragment;
        } else {
            return assetsFragment;
        }
    }

    @Override
    public int getItemCount() {
        return 2;
    }

    public WriteOffParamsFragment getParametersFragment() {
        return parametersFragment;
    }

    public WriteOffAssetsFragment getAssetsFragment() {
        return assetsFragment;
    }
}
