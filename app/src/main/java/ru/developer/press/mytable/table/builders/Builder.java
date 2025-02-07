package ru.developer.press.mytable.table.builders;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;

import java.util.ArrayList;

import ru.developer.press.myTable.R;
import ru.developer.press.mytable.helpers.Coordinate;
import ru.developer.press.mytable.helpers.Formula;
import ru.developer.press.mytable.helpers.StaticMethods;
import ru.developer.press.mytable.model.Cell;
import ru.developer.press.mytable.model.Column;
import ru.developer.press.mytable.model.Row;
import ru.developer.press.mytable.model.TableModel;

public abstract class Builder {

    public final float widthHeaders;
    final Coordinate selectedCellCoordinate;
    final Paint paintTouch;
    final Paint paintSelectStrokeAColumnGray;
    final float scaleDp;

    Builder(Context context) {
        scaleDp = context.getResources().getDisplayMetrics().density;
        selectedCellCoordinate = new Coordinate();

        paintTouch = new Paint();
        paintTouch.setColor(context.getResources().getColor(R.color.color_select_cell_stroke_column));
        paintTouch.setStyle(Paint.Style.FILL);
        paintTouch.setAlpha(50);

        paintSelectStrokeAColumnGray = new Paint();
        paintSelectStrokeAColumnGray.setColor(context.getResources().getColor(R.color.gray_center));
        paintSelectStrokeAColumnGray.setStyle(Paint.Style.FILL);
        paintSelectStrokeAColumnGray.setAlpha(50);

        widthHeaders = StaticMethods.convertDpToPixels(32, context);
    }

//    public void init(TableModel tableModel) {
////        long start = System.currentTimeMillis();
////        long stop = System.currentTimeMillis();
////        Log.d(TAG, "TablePresenter: " + (stop - start));
//
//        ArrayList<Row> rows = tableModel.getRows();
//        // rows
//        for (int i = 0; i < rows.size(); i++) {
//            Row row = rows.get(i);
//            row.width = widthHeaders; // надо для работы статиклайот
//            float sizeTextTempHeader = scaleDp * row.pref.sizeFont + 0.5F;
//            row.text = String.valueOf(i + 1);
//            row.updateCell(sizeTextTempHeader);
//
//            for (int j = 0; j < row.getCells().size(); j++) {
//                Column column = tableModel.getColumns().get(j);
//                if (i == 0) {
//                    column.width = column.getWidth();
//                    column.index = j;
//                    float sizeTextTempColumn = column.pref.sizeFont * scaleDp + 0.5F;
//                    column.updateCell(sizeTextTempColumn);
//                }
//                Cell cell = row.getCellAtIndex(j);
//
//                cell.indexColumn = j;
//                cell.indexRow = i;
//                cell.inputType = column.getInputType();
//                if (column.getInputType() == 2) {
//                    if (cell.date > 0)
//                        cell.text = StaticMethods.getDateOfMillis(cell.date, tableModel.getDateType());
//                }
//                if (column.getInputType() == 3) {
//                    cell.text = column.getFormula().getValueFromFormula(tableModel, i);
//                }
//
//                float sizeTextTemp = cell.pref.sizeFont * scaleDp + 0.5F;
//                cell.updateCell(sizeTextTemp);
//            }
//
//        }
//
//        initTotalAmount(tableModel);
//
//    }

    public void initTotalAmountToColumn(TableModel tableModel, int index) {
        Row totalRow = tableModel.totalAmount;
        Cell totalAmountToColumn = totalRow.getCellAtIndex(index);
        totalAmountToColumn.text = "";
        double summa = 0;
        for (int j = 0; j < tableModel.getRows().size(); j++) {
            { // блок для подсчета тотал
                Cell cellAtIndex = tableModel.getRows().get(j).getCellAtIndex(index);
                if (cellAtIndex.text.equals(""))
                    continue;
                double cellNumber = Formula.parseStringToNumber(cellAtIndex.text);
                summa += cellNumber;
            }
        }
        if (summa != 0)
            totalAmountToColumn.text = Formula.numberFormat.format(summa);
        else
            totalAmountToColumn.text = "";

        Column column = tableModel.getColumns().get(index);
        totalAmountToColumn.width = column.getWidth();
        float sizeRealFont = totalAmountToColumn.pref.sizeFont * scaleDp + 0.5F;
        totalAmountToColumn.updateCell(sizeRealFont);
    }

    public float getTotalAmountMaxHeight(Row totalRow) {

        float heightStroke = totalRow.height;
        float height = 0;

        ArrayList<Cell> get = totalRow.getCells();
        for (int i = 0; i < get.size(); i++) {
            Cell cell = get.get(i);
            float textSize = cell.pref.sizeFont * scaleDp + 0.5F;
            cell.updateCell(textSize);
            float temp = cell.getStaticHeight();

            if (height < temp && cell.text.length() > 0) {
                height = temp;
            }
        }
        if (height < heightStroke)
            height = heightStroke; //  если высота слишком маленькая то как задали
        return (int) height;
    }
    public void updateHeightTotalAmount(TableModel tableModel){
        Row totalRow = tableModel.totalAmount;
        totalRow.heightStroke = tableModel.isVisibleTotalAmount() ?
                (int) getTotalAmountMaxHeight(totalRow) : 0;
        totalRow.width = widthHeaders;
    }

    // Нужно чтоб оптимизировать писанину на ячейке
    public void updateHeightStroke(TableModel tableModel, int index) {

        int height = getMaxHeightStroke(tableModel, index);
        Row rowPref = tableModel.getRows().get(index);
        rowPref.heightStroke = height;
    }

    private int getMaxHeightStroke(TableModel tableModel, int stroke) {
        int heightStroke = (int) tableModel.getRows().get(stroke).height;
        if (tableModel.isLockHeightCells())
            return heightStroke;
        float height = 0;
        ArrayList<Cell> get = tableModel.getRows().get(stroke).getCells();
        for (int i = 0; i < get.size(); i++) {
            Column column = tableModel.getColumns().get(i);
            Cell cell = get.get(i);
            if (cell.text.length() == 0)
                continue;
            cell.width = column.width;
            //для того чтобы если есть даты, чтоб и их учитывала в высоте строки
            if (column.getInputType() == 2) {
                if (cell.date > 0) {
                    cell.text = StaticMethods.getDateOfMillis(cell.date, tableModel.getDateType());
                }
            } else if (column.getInputType() == 3) {
                cell.text = column.getFormula().getValueFromFormula(tableModel, stroke);
            }
            float textSize = cell.pref.sizeFont * scaleDp + 0.5F;

            cell.updateCell(textSize);
            float temp = cell.getStaticHeight();

            if (height < temp && cell.text.length() > 0) {
                height = temp;
            }
        }
        if (height < heightStroke)
            height = heightStroke; //  если высота слишком маленькая то как задали
        return (int) height;
    }

    public abstract void draw(Canvas canvas, Coordinate coordinate, TableModel tableModel);

    public abstract void unSelectAll(TableModel tableModel);

    public abstract Coordinate getSelectedCellCoordinate(TableModel tableModel);

    public abstract Coordinate selectCellsOfSelector(float startX, float endX, float startY, float endY, TableModel tableModel);
}
