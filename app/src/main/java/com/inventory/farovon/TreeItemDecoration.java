package com.inventory.farovon;

import android.graphics.Canvas;
import android.graphics.Paint;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.inventory.farovon.model.OrganizationItem;

public class TreeItemDecoration extends RecyclerView.ItemDecoration {
    private final Paint linePaint;
    private final int indentation;

    public TreeItemDecoration(int color, float strokeWidth, int indentation) {
        linePaint = new Paint();
        linePaint.setColor(color);
        linePaint.setStrokeWidth(strokeWidth);
        this.indentation = indentation;
    }

    @Override
    public void onDraw(@NonNull Canvas c, @NonNull RecyclerView parent, @NonNull RecyclerView.State state) {
        super.onDraw(c, parent, state);

        OrganizationAdapter adapter = (OrganizationAdapter) parent.getAdapter();
        if (adapter == null) {
            return;
        }

        for (int i = 0; i < parent.getChildCount(); i++) {
            View view = parent.getChildAt(i);
            int position = parent.getChildAdapterPosition(view);
            OrganizationItem item = adapter.getVisibleItem(position);
            OrganizationItem parentItem = adapter.getParentOf(item);

            if (parentItem != null) {
                float startX = view.getLeft() + (item.getLevel() * indentation) - (indentation / 2f);
                float startY = view.getTop();
                float stopY = view.getTop() + view.getHeight() / 2f;
                c.drawLine(startX, startY, startX, stopY, linePaint);

                float startXHorizontal = startX;
                float startYHorizontal = stopY;
                float stopXHorizontal = view.getLeft() + (item.getLevel() * indentation);
                c.drawLine(startXHorizontal, startYHorizontal, stopXHorizontal, startYHorizontal, linePaint);
            }
        }
    }
}