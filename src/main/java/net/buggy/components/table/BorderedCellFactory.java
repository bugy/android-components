package net.buggy.components.table;


import android.content.Context;
import android.view.View;

import net.buggy.components.BorderDecorator;

public class BorderedCellFactory implements CellFactory{

    private final CellFactory mainFactory;

    private BorderDecorator.Border leftBorder;
    private BorderDecorator.Border rightBorder;
    private BorderDecorator.Border topBorder;
    private BorderDecorator.Border bottomBorder;

    public BorderedCellFactory(CellFactory mainFactory) {
        this.mainFactory = mainFactory;
    }

    public void setLeftBorder(BorderDecorator.Border leftBorder) {
        this.leftBorder = leftBorder;
    }

    public void setRightBorder(BorderDecorator.Border rightBorder) {
        this.rightBorder = rightBorder;
    }

    public void setTopBorder(BorderDecorator.Border topBorder) {
        this.topBorder = topBorder;
    }

    public void setBottomBorder(BorderDecorator.Border bottomBorder) {
        this.bottomBorder = bottomBorder;
    }

    @Override
    public View createCell(Object value, int row, int column, Context context) {
        View cell = mainFactory.createCell(value, row, column, context);

        BorderDecorator borderDecorator = new BorderDecorator(context);
        borderDecorator.setLeftBorder(leftBorder);
        borderDecorator.setRightBorder(rightBorder);
        borderDecorator.setTopBorder(topBorder);
        borderDecorator.setBottomBorder(bottomBorder);
        borderDecorator.addView(cell);

        return borderDecorator;
    }
}
