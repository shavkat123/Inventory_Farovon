package com.inventory.farovon.ui.issuefromwarehouse;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.viewpager2.adapter.FragmentStateAdapter;
import com.inventory.farovon.model.Asset;
import com.inventory.farovon.model.IssueDocument;

import java.util.List;

public class IssueFromWarehousePagerAdapter extends FragmentStateAdapter {

    private IssueDocument document;
    private boolean isEditMode;

    public IssueFromWarehousePagerAdapter(@NonNull FragmentActivity fragmentActivity, IssueDocument document, boolean isEditMode) {
        super(fragmentActivity);
        this.document = document;
        this.isEditMode = isEditMode;
    }

    @NonNull
    @Override
    public Fragment createFragment(int position) {
        if (position == 0) {
            return ParametersFragment.newInstance(document, isEditMode);
        }
        return AssetsFragment.newInstance(document != null ? document.getAssets() : new java.util.ArrayList<>());
    }

    @Override
    public int getItemCount() {
        return 2; // We have two tabs
    }
}
