package com.audio.effiong.musicplayer.design;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.RecyclerView;
import android.view.View;

import com.audio.effiong.musicplayer.R;
import com.audio.effiong.musicplayer.utility.Helpers;

/**
 * Created by Victor on 8/23/2016.
 */

public class DividerItemDecoration extends RecyclerView.ItemDecoration {
    private Drawable mDivider;
    private int size;
    private boolean useFullPading = false;

    public DividerItemDecoration(Context context, int paddingLeft, boolean flag) {
        if ( Helpers.getATEKey(context).contains("light_theme"))
            //White theme
            mDivider = ContextCompat.getDrawable(context, R.drawable.line_divider);
        // drak theme

        else if( Helpers.getATEKey(context).contains("dark_theme")){
            mDivider = ContextCompat.getDrawable(context, R.drawable.line_divider2);
            mDivider.setAlpha(80);
        }

        this.size = paddingLeft;
        useFullPading = flag;
    }

    @Override
    public void getItemOffsets(Rect outRect, View view, RecyclerView parent, RecyclerView.State state) {
        super.getItemOffsets(outRect, view, parent, state);

        if (parent.getChildAdapterPosition(view) == 0) {
            return;
        }

        outRect.top = mDivider.getIntrinsicHeight();
    }

    @Override
    public void onDraw(Canvas canvas, RecyclerView parent, RecyclerView.State state) {

        int dividerLeft = parent.getPaddingLeft() + size;
       // int dividerLeftFirstChild = parent.getPaddingLeft();
        int dividerRight = parent.getWidth() - parent.getPaddingRight();

        int childCount = parent.getChildCount();
        for (int i = 0; i < childCount - 1; i++) {
            View child = parent.getChildAt(i);

            RecyclerView.LayoutParams params = (RecyclerView.LayoutParams) child.getLayoutParams();

            int dividerTop = child.getBottom() + params.bottomMargin;
            int dividerBottom = dividerTop + mDivider.getIntrinsicHeight();
//            if(i ==0 && useFullPading){
//               // if(i==0){
//                    mDivider.setBounds(dividerLeftFirstChild, dividerTop, dividerRight, dividerBottom);
//               // }
//            }
//            else
            mDivider.setBounds(dividerLeft, dividerTop, dividerRight, dividerBottom);
            mDivider.draw(canvas);
        }
    }
}