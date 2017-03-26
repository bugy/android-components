package net.buggy.components;

import android.annotation.TargetApi;
import android.content.Context;
import android.os.Build;
import android.support.annotation.NonNull;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import com.google.common.collect.Multiset;
import com.google.common.collect.SortedMultiset;
import com.google.common.collect.TreeMultiset;

import java.util.ArrayList;

public class TagFlagContainer extends LinearLayout {

    private final SortedMultiset<Integer> colors = TreeMultiset.create();

    private Integer tagWidth;
    private Integer tagMargin;
    private Integer flagBorderColor = TagFlag.DEF_BORDER_COLOR;
    private Integer maxCount;

    public TagFlagContainer(Context context) {
        super(context);

        init();
    }

    public TagFlagContainer(Context context, AttributeSet attrs) {
        super(context, attrs);

        init();
    }

    public TagFlagContainer(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);

        init();
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public TagFlagContainer(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);

        init();
    }

    private void init() {
        final Context context = getContext();

        tagWidth = ViewUtils.dpToPx(8, context);
        tagMargin = ViewUtils.dpToPx(8, context);
    }

    public void setTagWidth(Integer tagWidth) {
        this.tagWidth = tagWidth;
    }

    public void setTagMargin(Integer tagLeftMargin) {
        this.tagMargin = tagLeftMargin;
    }

    public void setFlagBorderColor(Integer flagBorderColor) {
        this.flagBorderColor = flagBorderColor;

        for (int i = 0; i < getChildCount(); i++) {
            final TagFlag tagFlag = (TagFlag) getChildAt(i);
            tagFlag.setBorderColor(flagBorderColor);
        }
    }

    public void addColor(Integer color) {
        colors.add(color);

        final ArrayList<Integer> colorsList = new ArrayList<>(colors);
        final int newIndex = colorsList.lastIndexOf(color);

        final TagFlag tagFlag = createTagFlag(newIndex == 0);
        tagFlag.setColor(color);
        addView(tagFlag, newIndex);

        if ((newIndex == 0) && (colors.size() > 1)) {
            final View previousFirstView = getChildAt(1);
            final LayoutParams layoutParams = createFlagLayoutParams(false);
            previousFirstView.setLayoutParams(layoutParams);
        }
    }

    public void setColors(Multiset<Integer> colors) {
        this.colors.clear();
        this.colors.addAll(colors);

        final ArrayList<Integer> colorsList = new ArrayList<>(this.colors);
        int colorsCount = maxCount != null
                ? Math.min(maxCount, colorsList.size())
                : colorsList.size();
        for (int i = 0; i < colorsCount; i++) {
            final Integer color = colorsList.get(i);

            final TagFlag tagFlag;
            if (getChildCount() <= i) {
                tagFlag = createTagFlag(i == 0);
                addView(tagFlag);
            } else {
                tagFlag = (TagFlag) getChildAt(i);
            }

            if (tagFlag.getColor() != color) {
                tagFlag.setColor(color);
            }
        }

        removeExcessiveFlags(colorsCount);
    }

    private void removeExcessiveFlags(int colorsCount) {
        while (getChildCount() > colorsCount) {
            removeViewAt(colorsCount);
        }
    }

    public void setMaxCount(Integer maxCount) {
        this.maxCount = maxCount;

        if ((maxCount != null) && (maxCount > getChildCount())) {
            removeExcessiveFlags(maxCount);
        }
    }

    private TagFlag createTagFlag(boolean first) {
        final LayoutParams tagLayoutParams = createFlagLayoutParams(first);

        final TagFlag tagFlag = new TagFlag(getContext());
        tagFlag.setLayoutParams(tagLayoutParams);
        tagFlag.setBorderColor(flagBorderColor);

        return tagFlag;
    }

    @NonNull
    private LayoutParams createFlagLayoutParams(boolean first) {
        final LayoutParams tagLayoutParams = new LayoutParams(
                tagWidth, ViewGroup.LayoutParams.MATCH_PARENT);
        tagLayoutParams.setMargins(first ? 0 : tagMargin, 0, 0, 0);
        return tagLayoutParams;
    }
}
