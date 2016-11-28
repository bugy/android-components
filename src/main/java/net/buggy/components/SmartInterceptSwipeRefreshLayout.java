package net.buggy.components;


import android.content.Context;
import android.support.v4.widget.SwipeRefreshLayout;
import android.util.AttributeSet;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.ViewConfiguration;

public class SmartInterceptSwipeRefreshLayout extends SwipeRefreshLayout {
    private final int mTouchSlop;
    private final GestureDetector mGestureDetector;

    public SmartInterceptSwipeRefreshLayout(Context context) {
        super(context);

        mGestureDetector = new GestureDetector(context, new YScrollDetector());
        setFadingEdgeLength(0);

        mTouchSlop = ViewConfiguration.get(context).getScaledTouchSlop();
    }

    public SmartInterceptSwipeRefreshLayout(Context context, AttributeSet attrs) {
        super(context, attrs);

        mGestureDetector = new GestureDetector(context, new YScrollDetector());
        setFadingEdgeLength(0);

        mTouchSlop = ViewConfiguration.get(context).getScaledTouchSlop();
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        return super.onInterceptTouchEvent(ev) && mGestureDetector.onTouchEvent(ev);
    }

    private class YScrollDetector extends GestureDetector.SimpleOnGestureListener {
        @Override
        public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
            final float absDistanceY = Math.abs(distanceY);

            if (absDistanceY < mTouchSlop) {
                return false;
            }

            return absDistanceY > Math.abs(distanceX);
        }
    }
}
