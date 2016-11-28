package net.buggy.components;


import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

public class BorderDecorator extends LinearLayout {

    private Border leftBorder;
    private Border rightBorder;
    private Border topBorder;
    private Border bottomBorder;

    private final Paint paint;

    public BorderDecorator(Context context) {
        super(context);

        setWillNotDraw(false);

        paint = new Paint();
        paint.setStyle(Paint.Style.STROKE);
    }

    @Override
    public void addView(View child, int index, ViewGroup.LayoutParams params) {
        if (getChildCount() > 0) {
            throw new IllegalStateException();
        }

        LayoutParams overwrittenParams = new LayoutParams(
                LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT);

        super.addView(child, index, overwrittenParams);
    }

    public void setLeftBorder(Border leftBorder) {
        this.leftBorder = leftBorder;
    }

    public void setRightBorder(Border rightBorder) {
        this.rightBorder = rightBorder;
    }

    public void setTopBorder(Border topBorder) {
        this.topBorder = topBorder;
    }

    public void setBottomBorder(Border bottomBorder) {
        this.bottomBorder = bottomBorder;
    }

    public void setAllBorders(Border border) {
        this.leftBorder = border;
        this.rightBorder = border;
        this.topBorder = border;
        this.bottomBorder = border;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        if (leftBorder != null) {
            paint.setColor(leftBorder.color);
            paint.setStrokeWidth(leftBorder.width);

            canvas.drawLine(0, 0, 0, getHeight(), paint);
        }

        if (rightBorder != null) {
            paint.setColor(rightBorder.color);
            paint.setStrokeWidth(rightBorder.width);

            canvas.drawLine(getWidth()-1, 0, getWidth()-1, getHeight(), paint);
        }

        if (topBorder != null) {
            paint.setColor(topBorder.color);
            paint.setStrokeWidth(topBorder.width);

            canvas.drawLine(0, 0, getWidth(), 0, paint);
        }

        if (bottomBorder != null) {
            paint.setColor(bottomBorder.color);
            paint.setStrokeWidth(bottomBorder.width);

            canvas.drawLine(0, getHeight()-1, getWidth(), getHeight()-1, paint);
        }
    }

    public static class Border {
        private final int width;
        private final int color;

        public Border(int width, int color) {
            this.width = width;
            this.color = color;
        }
    }
}
