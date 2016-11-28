package net.buggy.components;

import android.annotation.TargetApi;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.os.Build;
import android.util.AttributeSet;
import android.view.View;

public class TagFlag extends View {

    private static final int MIN_WIDTH = 5;
    private static final int CUT_HEIGHT = 5;

    private static final int DEF_COLOR = Color.BLACK;

    private int color;
    private int cutHeight;
    private Paint fillPaint;
    private Path polygon;
    private Paint strokePaint;

    public TagFlag(Context context) {
        super(context);

        init(context, null);
    }

    public TagFlag(Context context, AttributeSet attrs) {
        super(context, attrs);

        init(context, attrs);
    }

    public TagFlag(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);

        init(context, attrs);
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public TagFlag(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);

        init(context, attrs);
    }

    private void init(Context context, AttributeSet attrs) {
        this.cutHeight = ViewUtils.dpToPx(CUT_HEIGHT, context);

        setMinimumWidth(ViewUtils.dpToPx(MIN_WIDTH, context));
        setMinimumHeight(ViewUtils.dpToPx(CUT_HEIGHT + 2, context));


        if (attrs != null) {
            setColor(attrs.getAttributeIntValue(android.R.attr.color, DEF_COLOR));
        } else {
            setColor(DEF_COLOR);
        }

        polygon = new Path();

        strokePaint = new Paint();
        strokePaint.setFlags(Paint.ANTI_ALIAS_FLAG);
        strokePaint.setColor(Color.BLACK);
        strokePaint.setStyle(Paint.Style.STROKE);
    }

    public void setColor(int color) {
        this.color = color;

        fillPaint = new Paint();
        fillPaint.setFlags(Paint.ANTI_ALIAS_FLAG);
        fillPaint.setColor(color);
        fillPaint.setStyle(Paint.Style.FILL);

        invalidate();
    }

    public int getColor() {
        return color;
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        final int prevHeight = getMeasuredHeight();
        final int prevWidth = getMeasuredWidth();

        super.onMeasure(widthMeasureSpec, heightMeasureSpec);

        final int newHeight = getMeasuredHeight();
        final int newWidth = getMeasuredWidth();
        if ((prevHeight != newHeight) || (prevWidth != newWidth)) {
            updatePolygon(newWidth, newHeight);
        }
    }

    private void updatePolygon(int width, int height) {
        int minY = getPaddingTop();
        int maxY = height - getPaddingBottom();
        int minX = getPaddingLeft();
        int maxX = width - getPaddingRight();


        polygon.reset();
        polygon.moveTo(minX, minY);
        polygon.lineTo(minX, maxY);
        polygon.lineTo(minX + (maxX - minX) / 2, maxY - cutHeight);
        polygon.lineTo(maxX, maxY);
        polygon.lineTo(maxX, minY);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        canvas.drawPath(polygon, fillPaint);
        canvas.drawPath(polygon, strokePaint);
    }
}
