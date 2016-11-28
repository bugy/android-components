package net.buggy.components.list;


import android.content.Context;
import android.graphics.Canvas;
import android.graphics.PorterDuff;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.support.annotation.Nullable;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;

import net.buggy.components.R;

import static android.view.ViewGroup.LayoutParams.MATCH_PARENT;

public class SwipeToRemoveHandler extends ItemTouchHelper.SimpleCallback {

    private Drawable background;
    private boolean initiated;
    private RecyclerView recyclerView;
    private final int backgroundColor;
    private Integer foregroundColor;

    private DeletionHandler deletionHandler;
    private RemovedCellFactory activeRemovedCellFactory;

    public SwipeToRemoveHandler(int backgroundColor) {
        super(0, ItemTouchHelper.RIGHT);

        this.backgroundColor = backgroundColor;
    }

    public void setForegroundColor(Integer foregroundColor) {
        this.foregroundColor = foregroundColor;
    }

    public void attach(RecyclerView recyclerView) {
        this.recyclerView = recyclerView;

        ItemTouchHelper touchHelper = new ItemTouchHelper(this);
        touchHelper.attachToRecyclerView(recyclerView);

        setUpAnimationDecoratorHelper();
    }

    public void setDeletionHandler(DeletionHandler deletionHandler) {
        this.deletionHandler = deletionHandler;
    }

    @Override
    public boolean onMove(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder, RecyclerView.ViewHolder target) {
        return false;
    }

    @Override
    public int getSwipeDirs(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder) {
        if (activeRemovedCellFactory != null) {
            return 0;
        }

        if ((deletionHandler != null) && (!deletionHandler.canDelete(viewHolder.getAdapterPosition()))) {
            return 0;
        }

        return super.getSwipeDirs(recyclerView, viewHolder);
    }

    @Override
    public void onSwiped(final RecyclerView.ViewHolder viewHolder, int swipeDir) {
        final int swipedPosition = viewHolder.getAdapterPosition();

        if (deletionHandler != null) {

            final int itemHeight = viewHolder.itemView.getMeasuredHeight();

            final DeletionCallback deletionCallback = new DeletionCallback(swipedPosition);

            activeRemovedCellFactory = new RemovedCellFactory(backgroundColor, foregroundColor, itemHeight, deletionCallback);
            getAdapter().setCustomFactory(activeRemovedCellFactory, swipedPosition);

            deletionHandler.onDelete(swipedPosition, deletionCallback);

        } else {
            getAdapter().remove(swipedPosition);
        }
    }

    private FactoryBasedAdapter getAdapter() {
        return (FactoryBasedAdapter) recyclerView.getAdapter();
    }

    @Override
    public void onChildDraw(Canvas c, RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder, float dX, float dY, int actionState, boolean isCurrentlyActive) {
        View itemView = viewHolder.itemView;

        // not sure why, but this method get's called for viewHolder that are already swiped away
        if (viewHolder.getAdapterPosition() == -1) {
            // not interested in those
            return;
        }

        if (!initiated) {
            init();
        }

        // draw background
        background.setBounds(itemView.getLeft(), itemView.getTop(), (int) dX, itemView.getBottom());
        background.draw(c);

        super.onChildDraw(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive);
    }

    @Override
    public float getSwipeThreshold(RecyclerView.ViewHolder viewHolder) {
        return 0.7f;
    }

    @Override
    public float getSwipeEscapeVelocity(float defaultValue) {
        return 1500;
    }

    private void init() {
        background = new ColorDrawable(backgroundColor);
        initiated = true;
    }

    private void setUpAnimationDecoratorHelper() {
        recyclerView.addItemDecoration(new RecyclerView.ItemDecoration() {

            @Override
            public void onDraw(Canvas c, RecyclerView parent, RecyclerView.State state) {

                if (!initiated) {
                    init();
                }


                // only if animation is in progress
                if (parent.getItemAnimator().isRunning()) {

                    // some items might be animating down and some items might be animating up to close the gap left by the removed item
                    // this is not exclusive, both movement can be happening at the same time
                    // to reproduce this leave just enough items so the first one and the last one would be just a little off screen
                    // then remove one from the middle

                    // find first child with translationY > 0
                    // and last one with translationY < 0
                    // we're after a rect that is not covered in recycler-view views at this point in time
                    View lastViewComingDown = null;
                    View firstViewComingUp = null;

                    // this is fixed
                    int left = 0;
                    int right = parent.getWidth();

                    // this we need to find out
                    int top = 0;
                    int bottom = 0;

                    // find relevant translating views
                    int childCount = parent.getLayoutManager().getChildCount();
                    for (int i = 0; i < childCount; i++) {
                        View child = parent.getLayoutManager().getChildAt(i);
                        if (child.getTranslationY() < 0) {
                            // view is coming down
                            lastViewComingDown = child;
                        } else if (child.getTranslationY() > 0) {
                            // view is coming up
                            if (firstViewComingUp == null) {
                                firstViewComingUp = child;
                            }
                        }
                    }

                    if (lastViewComingDown != null && firstViewComingUp != null) {
                        // views are coming down AND going up to fill the void
                        top = lastViewComingDown.getBottom() + (int) lastViewComingDown.getTranslationY();
                        bottom = firstViewComingUp.getTop() + (int) firstViewComingUp.getTranslationY();
                    } else if (lastViewComingDown != null) {
                        // views are going down to fill the void
                        top = lastViewComingDown.getBottom() + (int) lastViewComingDown.getTranslationY();
                        bottom = lastViewComingDown.getBottom();
                    } else if (firstViewComingUp != null) {
                        // views are coming up to fill the void
                        top = firstViewComingUp.getTop();
                        bottom = firstViewComingUp.getTop() + (int) firstViewComingUp.getTranslationY();
                    }

                    background.setBounds(left, top, right, bottom);
                    background.draw(c);

                }
                super.onDraw(c, parent, state);
            }

        });
    }

    public interface DeletionHandler {
        boolean canDelete(int position);

        void onDelete(int position, DeletionCallback callback);
    }

    public final class DeletionCallback {
        private final RecyclerView.AdapterDataObserver observer;
        private int viewPosition;

        public DeletionCallback(final int viewPosition) {
            this.viewPosition = viewPosition;

            observer = new RecyclerView.AdapterDataObserver() {
                @Override
                public void onItemRangeInserted(int positionStart, int itemCount) {
                    if (positionStart <= viewPosition) {
                        DeletionCallback.this.viewPosition += itemCount;
                    }
                }

                @Override
                public void onItemRangeRemoved(int positionStart, int itemCount) {
                    if (positionStart <= viewPosition) {
                        DeletionCallback.this.viewPosition -= itemCount;
                    }
                }

                @Override
                public void onItemRangeMoved(int fromPosition, int toPosition, int itemCount) {
                    if ((fromPosition <= viewPosition) && (toPosition >= viewPosition)) {
                        DeletionCallback.this.viewPosition -= itemCount;
                    } else if ((fromPosition >= viewPosition) && (toPosition <= viewPosition)) {
                        DeletionCallback.this.viewPosition += itemCount;
                    }
                }
            };

            getAdapter().registerAdapterDataObserver(observer);
        }

        public void delete() {
            activeRemovedCellFactory = null;

            getAdapter().unregisterAdapterDataObserver(observer);
            getAdapter().setCustomFactory(null, viewPosition);
            getAdapter().remove(viewPosition);
        }

        public void askConfirmation(String text) {
            activeRemovedCellFactory.setConfirmation(text);

            getAdapter().notifyItemChanged(viewPosition);
        }

        public void cancel() {
            activeRemovedCellFactory = null;

            getAdapter().unregisterAdapterDataObserver(observer);
            getAdapter().setCustomFactory(null, viewPosition);
        }
    }

    public final static class RemovedCellFactory implements CellFactory<Object, LinearLayout> {
        private final int backgroundColor;
        private final Integer foregroundColor;
        private final int itemHeight;
        private final DeletionCallback deletionCallback;

        private boolean confirmationActive = false;
        private String confirmationText = null;

        public RemovedCellFactory(
                int backgroundColor,
                @Nullable Integer foregroundColor,
                int itemHeight,
                DeletionCallback deletionCallback) {

            this.backgroundColor = backgroundColor;
            this.foregroundColor = foregroundColor;
            this.itemHeight = itemHeight;
            this.deletionCallback = deletionCallback;
        }

        @Override
        public LinearLayout createEmptyCell(Context context, ViewGroup parent) {
            final LayoutInflater inflater = LayoutInflater.from(context);
            final LinearLayout layout = (LinearLayout) inflater.inflate(R.layout.confirmation_item, parent, false);

            layout.setLayoutParams(new ViewGroup.LayoutParams(MATCH_PARENT, itemHeight));
            layout.setBackgroundColor(backgroundColor);

            for (int i = 0; i < layout.getChildCount(); i++) {
                final View child = layout.getChildAt(i);
                child.setVisibility(View.GONE);
            }

            return layout;
        }

        @Override
        public void fillCell(Object value, LinearLayout view, ChangeListener<Object> listener, boolean selected, boolean enabled) {
            if (confirmationActive) {
                final TextView confirmationTextView = (TextView) view.findViewById(R.id.confirmation_item_text_field);
                final ImageButton confirmButton = (ImageButton) view.findViewById(R.id.confirmation_item_confirm_button);
                final ImageButton cancelButton = (ImageButton) view.findViewById(R.id.confirmation_item_cancel_button);

                cancelButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        deletionCallback.cancel();
                    }
                });

                confirmButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        deletionCallback.delete();
                    }
                });

                if (foregroundColor != null) {
                    confirmationTextView.setTextColor(foregroundColor);

                    cancelButton.setColorFilter(foregroundColor, PorterDuff.Mode.SRC_IN);
                    confirmButton.setColorFilter(foregroundColor, PorterDuff.Mode.SRC_IN);
                } else {
                    final int textColor = confirmationTextView.getCurrentTextColor();

                    cancelButton.setColorFilter(textColor, PorterDuff.Mode.SRC_IN);
                    confirmButton.setColorFilter(textColor, PorterDuff.Mode.SRC_IN);
                }

                confirmationTextView.setText(confirmationText);

                for (int i = 0; i < view.getChildCount(); i++) {
                    final View child = view.getChildAt(i);
                    child.setVisibility(View.VISIBLE);
                }
            }
        }

        public void setConfirmation(String text) {
            confirmationActive = true;
            confirmationText = text;
        }
    }
}
