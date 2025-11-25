package com.inventory.farovon.ui.assetmovement;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.viewpager2.adapter.FragmentStateAdapter;
import android.os.Bundle;
import com.inventory.farovon.ui.molmovement.MolAssetsDetailFragment;

public class AssetMovementDetailPagerAdapter extends FragmentStateAdapter {

    private final long documentId;
    private AssetMovementParametersFragment parametersFragment;

    public AssetMovementDetailPagerAdapter(@NonNull FragmentActivity fragmentActivity, long documentId) {
        super(fragmentActivity);
        this.documentId = documentId;
    }

    @NonNull
    @Override
    public Fragment createFragment(int position) {
        if (position == 0) {
            parametersFragment = new AssetMovementParametersFragment();
            Bundle args = new Bundle();
            args.putLong("DOCUMENT_ID", documentId);
            parametersFragment.setArguments(args);
            return parametersFragment;
        }
        // Note: Ideally we should create AssetMovementAssetsDetailFragment to use AssetMovementDao.
        // MolAssetsDetailFragment uses MolMovementDao. So I can't reuse it directly if I want to fetch from asset_movement_items.
        // I need to implement AssetMovementAssetsDetailFragment.
        return AssetMovementAssetsDetailFragment.newInstance(documentId);
    }

    @Override
    public int getItemCount() {
        return 2;
    }

    public AssetMovementParametersFragment getParametersFragment() {
        return parametersFragment;
    }
}
