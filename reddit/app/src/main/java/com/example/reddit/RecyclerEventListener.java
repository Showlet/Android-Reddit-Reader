package com.example.reddit;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;

/**
 * Created by vincent on 2016-01-29.
 */
public class RecyclerEventListener implements RecyclerView.OnItemTouchListener {

    GestureDetector gestureDetector;

    public RecyclerEventListener(Context context, final RecyclerView recyclerView, final IEventListener clickListener) {

        gestureDetector = new GestureDetector(context, new GestureDetector.SimpleOnGestureListener() {
            @Override
            public boolean onSingleTapConfirmed(MotionEvent e) {
                View child = recyclerView.findChildViewUnder(e.getX(), e.getY());

                if (child != null && clickListener != null) {
                    clickListener.onClick(child, recyclerView.getChildLayoutPosition(child));
                    return true;
                }
                return false;
            }

            @Override
            public boolean onDoubleTap(MotionEvent e) {
                View child = recyclerView.findChildViewUnder(e.getX(), e.getY());

                if (child != null && clickListener != null) {
                    clickListener.onDoubleTap(child, recyclerView.getChildLayoutPosition(child));
                    return true;
                }
                return false;
            }
        });
    }

    @Override
    public boolean onInterceptTouchEvent(RecyclerView rv, MotionEvent e) {

        if (gestureDetector.onTouchEvent(e))
            return true;

        return false;
    }

    @Override
    public void onTouchEvent(RecyclerView rv, MotionEvent e) {
    }

    @Override
    public void onRequestDisallowInterceptTouchEvent(boolean disallowIntercept) {
    }

    public interface IEventListener {
        void onClick(View view, int position);
        void onLongClick(View view, int position);
        void onDoubleTap(View view, int position);
    }
}
