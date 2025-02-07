package ru.developer.press.mytable.table.builders;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;

import java.util.ArrayList;

import ru.developer.press.mytable.helpers.Coordinate;
import ru.developer.press.mytable.helpers.StaticMethods;
import ru.developer.press.mytable.model.Cell;
import ru.developer.press.mytable.model.Column;
import ru.developer.press.mytable.model.Row;
import ru.developer.press.mytable.model.TableModel;

public class RowBuilder extends Builder {
    public int selectRowCount = 0;

    public RowBuilder(Context context) {
        super(context);

    }

    public boolean selectRow(int index, TableModel tableModel) {
        Row row = tableModel.getRows().get(index);

        if (row.isTouch)
            return false;
        else {
            unSelectAll(tableModel);
            row.select();
            selectRowCount++;
        }
        return true;
    }


    public void draw(Canvas canvas, Coordinate coordinateDraw, TableModel tableModel) {
//        Paint paintSelectStrokeAColumnGray = this.paintSelectStrokeAColumnGray;
        int coorStartX = (int) coordinateDraw.startX;
        float coordinateStartY = coordinateDraw.startY;
        float coordinateEndY = coordinateDraw.endY;

        int offY = tableModel.heightColumns;

        ArrayList<Row> rows = tableModel.getRows();
        int size = rows.size();

        float startY;
        float endY = offY;
        float endX = widthHeaders + coorStartX;
        for (int i = 0; i < size; i++) {
            Row row = rows.get(i);

            endY += row.heightStroke;
            startY = endY - row.heightStroke;

            if (endY < coordinateStartY)
                continue;
            if (startY > coordinateEndY)
                break;


            Paint paintBack = row.paintBack;
            canvas.drawRect(coorStartX, startY + 1, endX, endY, paintBack);
            // рисуем сырми хеадер если в его диапозоне есть выделенная ячейка
//            ArrayList<Cell> cells = row.getCells();
//            for (Cell cell : cells) {
//                boolean isTouched = cell.isTouch;
//                if (isTouched) {
//                    canvas.drawRect(coorStartX, startY + 1, endX, endY, paintSelectStrokeAColumnGray);
//                    break;
//                }
//            }

            boolean isTouched = row.isTouch;
            if (isTouched) {

                canvas.drawRect(coorStartX, startY + 1, endX, endY, paintTouch);
            }

            canvas.save();
            row.text = String.valueOf(i + 1);
            drawText(canvas, row,
                    coorStartX,
                    endX,
                    startY,
                    endY);
            canvas.restore();
        }
    }

    private void drawText(Canvas canvas, Row row, int startX, float endX, float startY, float endY) {
        row.drawText(canvas, startX, (int) endX, (int) startY, (int) endY);

    }

    public void unSelectAll(TableModel tableModel) {
        ArrayList<Row> rows = tableModel.getRows();
        for (int i = 0; i < rows.size(); i++) {
            Row row = rows.get(i);
            if (row.isTouch) {
                row.unSelect();
            }

        }
        selectRowCount = 0;
    }

    @Override
    public Coordinate getSelectedCellCoordinate(TableModel tableModel) {
        int heightColumns = tableModel.heightColumns;
        Coordinate coordinate = new Coordinate();
        coordinate.startX = 0;
        coordinate.endX = tableModel.getWidthTable() + tableModel.widthRows;
        coordinate.startY = tableModel.getHeightTable() + heightColumns;
        coordinate.endY = 0;

        ArrayList<Row> rows = tableModel.getRows();
        for (int i = 0; i < rows.size(); i++) {
            Row row = rows.get(i);
            if (row.isTouch) {
                Coordinate coordinateCell = tableModel.getCoordinateCell(i, 0);
                if (coordinate.startY > coordinateCell.startY + heightColumns)
                    coordinate.startY = coordinateCell.startY + heightColumns;
                if (coordinate.endY < coordinateCell.endY + heightColumns)
                    coordinate.endY = coordinateCell.endY + heightColumns;
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

            Coordinate coordinateCell = tableModel.getCoordinateCell(i, 0);
            int strokeStart = (int) coordinateCell.startY + tableModel.heightColumns;
            int strokeEnd = (int) coordinateCell.endY + tableModel.heightColumns;
            if (strokeStart < endY && strokeEnd > startY) {
                if (selectedCellCoordinate.startY > strokeStart)
                    selectedCellCoordinate.startY = strokeStart;
                if (selectedCellCoordinate.endY < strokeEnd)
                    selectedCellCoordinate.endY = strokeEnd;
                row.select();
                selectRowCount++;
            }
        }
        return selectedCellCoordinate;
    }


    public void deleteRows(TableModel tableModel) {
        if (selectRowCount >= tableModel.getRows().size()) {
            return;
        }
        for (int i = 0; i < tableModel.getRows().size(); i++) {
            if (tableModel.getRows().get(i).isTouch) {
                tableModel.deleteStroke(i);
                i--;
            }
        }
        selectRowCount = 0;

    }

    public void updateRow(TableModel tableModel, int indexRow) {

        Row row = tableModel.getRows().get(indexRow);
        row.width = widthHeaders; // надо для работы статиклайот
        float sizeTextTempHeader = scaleDp * row.pref.sizeFont + 0.5F;
        row.text = String.valueOf(indexRow + 1);
        row.updateCell(sizeTextTempHeader);

        for (int i = 0; i < tableModel.getColumns().size(); i++) {
            Column column = tableModel.getColumns().get(i);
            Cell cell = row.getCellAtIndex(i);
            cell.indexRow = indexRow;
            cell.indexColumn = i;
            cell.inputType = column.getInputType();
            if (column.getInputType() == 2) {
                if (cell.date > 0)
                    cell.text = StaticMethods.getDateOfMillis(cell.date, tableModel.getDateType());
            }
            if (column.getInputType() == 3) {
                cell.text = column.getFormula().getValueFromFormula(tableModel, i);
            }

            float sizeTextTemp = cell.pref.sizeFont * scaleDp + 0.5F;
            cell.updateCell(sizeTextTemp);
            if (cell.text.length() > 0)
                initTotalAmountToColumn(tableModel, i);
        }
        updateHeightStroke(tableModel, indexRow);
    }


}
