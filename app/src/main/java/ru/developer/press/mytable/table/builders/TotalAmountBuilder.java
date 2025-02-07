package ru.developer.press.mytable.table.builders;

import android.content.Context;
import android.graphics.Canvas;

import java.util.ArrayList;

import ru.developer.press.mytable.helpers.Coordinate;
import ru.developer.press.mytable.model.Cell;
import ru.developer.press.mytable.model.Column;
import ru.developer.press.mytable.model.Row;
import ru.developer.press.mytable.model.TableModel;

public class TotalAmountBuilder extends Builder {
    public int selectTotalCount = 0;

    public TotalAmountBuilder(Context context) {
        super(context);

    }

    public boolean selectAmount(int index, TableModel tableModel) {
        Row row = tableModel.getRows().get(index);

        if (row.isTouch)
            return false;
        else {
            unSelectAll(tableModel);
            row.select();
            selectTotalCount++;
        }
        return true;
    }


    public void draw(Canvas canvas, Coordinate coordinateDraw, TableModel tableModel) {

        Row totalAmount = tableModel.totalAmount;

        int endY = tableModel.getHeightTable() + tableModel.heightColumns;
        if (endY > coordinateDraw.endY)
            endY = (int) coordinateDraw.endY;
        int startY = endY - totalAmount.heightStroke;

        //

        int endX = tableModel.widthRows;
        int startX = 0;
        ArrayList<Cell> cells = totalAmount.getCells();
        for (int i = 0; i < cells.size(); i++) {
            Cell cell = cells.get(i);
            Column column = tableModel.getColumns().get(i);

            endX += column.getWidth();
            startX = (int) (endX - column.getWidth());

            canvas.drawRect(startX, startY, endX, endY, cell.paintBack);
            cell.drawText(canvas, startX, endX, startY, endY);
        }
        float coordinateStartX = coordinateDraw.startX;
        canvas.drawRect(coordinateStartX, startY, coordinateStartX + tableModel.widthRows, endY, totalAmount.paintBack);

//        float endX = coordinateDraw.startX + tableModel.widthRows;
//        canvas.drawRect(coordinateDraw.startX, startY + 1, endX, endY, totalAmount.paintBack);
//        canvas.save();
//        drawText(canvas, totalAmount, (int) coordinateDraw.startX, (int) endX, startY, endY);
//        canvas.restore();

    }

    private void drawText(Canvas canvas, Row totalAmount, int startX, int endX, int startY, int endY) {

        totalAmount.drawText(canvas, startX, endX, startY, endY);
    }

    public void unSelectAll(TableModel tableModel) {
        Row row = tableModel.totalAmount;
        if (row.isTouch) {
            row.unSelect();
        }
        selectTotalCount = 0;
    }

    @Override
    public Coordinate getSelectedCellCoordinate(TableModel tableModel) {
        int heightColumns = tableModel.heightColumns;
        Coordinate coordinate = new Coordinate();
        coordinate.startX = 0;
        coordinate.endX = tableModel.getWidthTable() + tableModel.widthRows;
        coordinate.startY = tableModel.getHeightTable() + heightColumns;
        coordinate.endY = 0;
        for (Row row : tableModel.getRows()) {
            if (row.isTouch) {
                if (coordinate.startY > row.startY + heightColumns)
                    coordinate.startY = row.startY + heightColumns;
                if (coordinate.endY < row.endY + heightColumns)
                    coordinate.endY = row.endY + heightColumns;
            }
        }
        return coordinate;
    }

    @Override
    public Coordinate selectCellsOfSelector(float startX, float endX, float startY, float endY, TableModel tableModel) {
        unSelectAll(tableModel);
        selectedCellCoordinate.setBounds(endX, startX, endY, startY);

        for (int i = 0; i < tableModel.getRows().size(); i++) {
            Row row = tableModel.getRows().get(i);

            int strokeStart = (int) row.startY + tableModel.heightColumns;
            int strokeEnd = (int) row.endY + tableModel.heightColumns;
            if (strokeStart < endY && strokeEnd > startY) {
                if (selectedCellCoordinate.startY > strokeStart)
                    selectedCellCoordinate.startY = strokeStart;
                if (selectedCellCoordinate.endY < strokeEnd)
                    selectedCellCoordinate.endY = strokeEnd;
                row.select();
                selectTotalCount++;
            }
        }
        return selectedCellCoordinate;
    }

    public void initTotalAmount(TableModel tableModel) {
        Row totalRow = tableModel.totalAmount;
        for (int i = 0; i < tableModel.getColumns().size(); i++) {
            initTotalAmountToColumn(tableModel, i);
        }
        updateHeightTotalAmount(tableModel);
        float sizeRealFont = totalRow.pref.sizeFont * scaleDp + 0.5F;
        totalRow.updateCell(sizeRealFont);
    }
}
