package net.buggy.components;

import android.annotation.TargetApi;
import android.content.Context;
import android.os.Build;
import android.util.AttributeSet;
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

        final TagFlag tagFlag = createTagFlag();
        tagFlag.setColor(color);
        addView(tagFlag, newIndex);
    }

    public void setColors(Multiset<Integer> colors) {
        this.colors.clear();
        this.colors.addAll(colors);

        final ArrayList<Integer> colorsList = new ArrayList<>(this.colors);
        for (int i = 0; i < colorsList.size(); i++) {
            final Integer color = colorsList.get(i);

            final TagFlag tagFlag;
            if (getChildCount() <= i) {
                tagFlag = createTagFlag();
                addView(tagFlag);
            } else {
                tagFlag = (TagFlag) getChildAt(i);
            }

            if (tagFlag.getColor() != color) {
                tagFlag.setColor(color);
            }
        }

        while (getChildCount() > colorsList.size()) {
            removeViewAt(colorsList.size());
        }
    }

    private TagFlag createTagFlag() {
        final LinearLayout.LayoutParams tagLayoutParams = new LinearLayout.LayoutParams(
                tagWidth, ViewGroup.LayoutParams.MATCH_PARENT);
        tagLayoutParams.setMargins(tagMargin, 0, 0, 0);

        final TagFlag tagFlag = new TagFlag(getContext());
        tagFlag.setLayoutParams(tagLayoutParams);
        tagFlag.setBorderColor(flagBorderColor);

        return tagFlag;
    }
}
