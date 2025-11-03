package com.inventory.farovon.ui.returntowarehouse;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.viewpager2.adapter.FragmentStateAdapter;

public class ReturnToWarehousePagerAdapter extends FragmentStateAdapter {

    public ReturnToWarehousePagerAdapter(@NonNull FragmentActivity fragmentActivity) {
        super(fragmentActivity);
    }

    @NonNull
    @Override
    public Fragment createFragment(int position) {
        if (position == 0) {
            return new ReturnParametersFragment();
        }
        return new ReturnAssetsFragment();
    }

    @Override
    public int getItemCount() {
        return 2;
    }
}