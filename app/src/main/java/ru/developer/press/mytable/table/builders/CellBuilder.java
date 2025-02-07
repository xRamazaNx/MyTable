package ru.developer.press.mytable.table.builders;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;

import java.util.ArrayList;

import ru.developer.press.mytable.helpers.ColumnAttribute;
import ru.developer.press.mytable.helpers.Coordinate;
import ru.developer.press.mytable.helpers.Formula;
import ru.developer.press.mytable.interfaces.table.callback.EditCellListener;
import ru.developer.press.mytable.model.Cell;
import ru.developer.press.mytable.model.Column;
import ru.developer.press.mytable.model.Row;
import ru.developer.press.mytable.model.TableModel;

public class CellBuilder extends Builder {
    private String oldText;
    private long oldDate;

    private int[] indexOneCell = new int[2];
    private ArrayList<int[]> indexSelectCell;

    private EditCellListener editCellListener;
    private ArrayList<Integer> strokes = new ArrayList<>();
    private ArrayList<Integer> columns = new ArrayList<>();

    public CellBuilder(Context context, EditCellListener editCellListener) {
        super(context);

        this.editCellListener = editCellListener;
        indexSelectCell = new ArrayList<>();

    }

    public int getSelectCellSize() {
        return indexSelectCell.size();
    }

    public void draw(Canvas canvas, Coordinate coordinate, TableModel tableModel) {
        float coordinateStartX = coordinate.startX;
        float coordinateEndX = coordinate.endX;
        float coordinateStartY = coordinate.startY;
        float coordinateEndY = coordinate.endY;

        int offX = tableModel.widthRows;
        int offY = tableModel.heightColumns;

        ArrayList<Row> rows = tableModel.getRows();
        ArrayList<Column> columns = tableModel.getColumns();

        int columnSize = tableModel.getColumns().size();
        int strokeSize = rows.size();


        // рисуем и горизонтальны полоски и все ячейки
        float endY = offY;
        float startY;
        for (int index = 0; index < strokeSize; index++) {
            Row row = rows.get(index);

            endY += row.heightStroke;
            startY = endY - row.heightStroke;
            // если ячейка сверху от экрана не видно то листать дальше цикл
            if (endY < coordinateStartY)
                continue;
            // если ячейку не видно снизу от экрана то листать не зачем останавливаем цикл
            if (startY > coordinateEndY)
                break;

//            // фон для ров
//            Paint paintBack = row.paintBack;
//            canvas.drawRect(coordinateStartX, startY + 1, offX, endY, paintBack);
//
//            // если выделен ров
//            boolean isTouchedRow = row.isTouch;
//            if (isTouchedRow) {
//                canvas.drawRect(coordinateStartX, startY + 1, offX, endY, paintTouch);
//            }
//            // текст для ров
//            canvas.save();
//            row.drawText(canvas, coordinateStartX, offX, (int) startY, (int) endY);
//            canvas.restore();


            ArrayList<Cell> entry = row.getCells();
            float endX = offX;
            float startX;
            for (int i = 0; i < columnSize; i++) {
                Column column = columns.get(i);

                endX += column.getWidth();
                startX = endX - column.getWidth();
                // если ячейка слева от экрана не видно то листать дальше цикл
                if (endX < coordinateStartX)
                    continue;
                // если ячейку не видно с право от экрана то листать не зачем останавливаем цикл
                if (startX > coordinateEndX)
                    break;
                Cell cell = entry.get(i);
//                cell.setBounds(
//                        startX-offX,
//                        endX - offX,
//                        startY - offY,
//                        endY - offY);

//                int sX = (int) startX,
//                        eX = (int) endX,
//                        sY = (int) startY,
//                        eY = (int) endY;
//
//                if (sX == 0)
//                    sX = 1;
//                if (sY == 0)
//                    sY = 1;

                canvas.drawRect(startX, startY, endX, endY, cell.paintBack);
                boolean isTouched = cell.isTouch;
                if (isTouched) {
                    canvas.drawRect(startX, startY, endX, endY, paintTouch);
                    // фон для ров этой ячейки серым
//                    canvas.drawRect(coordinateStartX, startY + 1, endX, endY, paintSelectStrokeAColumnGray);
                    // фон для колумн этой ячейки серым
//                    canvas.drawRect(startX, coordinateStartY, endX, coordinateStartY + tableModel.heightColumns, paintSelectStrokeAColumnGray);
                }
                drawText(canvas, cell, startX, endX, startY, endY);

//                // рисуем колоны
//                if (strokeSize - 1 == index) {
//                    canvas.drawRect(startX, coordinateStartY, endX, coordinateStartY + tableModel.heightColumns, column.paintBack);
//                    if (column.isTouch)
//                        canvas.drawRect(startX, coordinateStartY, endX, coordinateStartY + tableModel.heightColumns, paintTouch);
//
//                    column.drawText(canvas, startX, endX, coordinateStartY, coordinateStartY + tableModel.heightColumns);
//                }
            }

            // рисуем сырми хеадер если в его диапозоне есть выделенная ячейка
//            ArrayList<Cell> cells = row.getCells();
//            for (Cell cell : cells) {
//                boolean isTouched = cell.isTouch;
//                if (isTouched) {
//                    canvas.drawRect(coordinateStartX, startY + 1, endX, endY, paintSelectStrokeAColumnGray);
//                    break;
//                }
//            }
        }
    }

    @Override
    public void unSelectAll(TableModel tableModel) {

        for (int[] index : indexSelectCell) {
            Cell cell = tableModel.getRows().get(index[0]).getCellAtIndex(index[1]);
            cell.isTouch = false;
        }
        if (indexSelectCell.size() == 1) {
            Cell cell = tableModel.getRows().get(indexOneCell[0]).getCellAtIndex(indexOneCell[1]);
            String newText = cell.text;
            long newDate = cell.date;
            editCellListener.cellEdit(oldText, newText, oldDate, newDate, indexOneCell);
        }

        indexSelectCell.clear();
    }

    private void drawText(Canvas canvas, Cell cell, float sX, float eX, float sY, float eY) {
        if (cell.text.length() > 0)
            cell.drawText(canvas, sX, eX, sY, eY);
    }

    public boolean selectCell(int stroke, int column, TableModel tableModel) {

        Cell cell = tableModel.getRows().get(stroke).getCellAtIndex(column);
        if (cell.isTouch)
            return false; //

        unSelectAll(tableModel);

        indexOneCell[0] = stroke;
        indexOneCell[1] = column;

        indexSelectCell.add(indexOneCell.clone());

        cell.isTouch = true;
        oldText = cell.text; // c этим надо разобраться
        oldDate = cell.date;
        return true;

    }

    public Cell getSelectCell(TableModel tableModel) {
        int stroke = indexOneCell[0];
        int column = indexOneCell[1];
        return tableModel.getRows().get(stroke).getCellAtIndex(column);

    }

    public void clearText(TableModel tableModel) {
        Cell cell = getSelectCell(tableModel);
        int inputType = tableModel.getColumns().get(cell.indexColumn).getInputType();
        if (inputType == 2) {
            cell.date = 0;
        } else if (inputType == 3)
            cell.valueFromFormula = "";
        else
            cell.text = "";
        updateCell(cell);
    }

    public Coordinate selectCellsOfSelector(float startX, float endX, float startY, float endY, TableModel tableModel) {
        strokes.clear();
        columns.clear();

        unSelectAll(tableModel);
        selectedCellCoordinate.setBounds(endX, startX, endY, startY);

        for (int i = 0; i < tableModel.getRows().size(); i++) {
            Coordinate coordinateCell = tableModel.getCoordinateCell(i, 0);
            int strokeStart = (int) coordinateCell.startY + tableModel.heightColumns;
            int strokeEnd = (int) coordinateCell.endY + tableModel.heightColumns;
            if (strokeStart < endY && strokeEnd > startY) {
                strokes.add(i);
                if (selectedCellCoordinate.startY > strokeStart)
                    selectedCellCoordinate.startY = strokeStart;
                if (selectedCellCoordinate.endY < strokeEnd)
                    selectedCellCoordinate.endY = strokeEnd;
            }
        }
        for (int i = 0; i < tableModel.getColumns().size(); i++) {
            Coordinate coordinateCell = tableModel.getCoordinateCell(0, i);
            int columnStart = (int) coordinateCell.startX + tableModel.widthRows;
            int columnEnd = (int) coordinateCell.endX + tableModel.widthRows;
            if (columnStart < endX && columnEnd > startX) {
                columns.add(i);
                if (selectedCellCoordinate.startX > columnStart)
                    selectedCellCoordinate.startX = columnStart;
                if (selectedCellCoordinate.endX < columnEnd)
                    selectedCellCoordinate.endX = columnEnd;
            }
        }
        for (int stroke : strokes) {
            for (int column : columns) {
                Cell cell = tableModel.getRows().get(stroke).getCellAtIndex(column);
                cell.isTouch = true;
                indexOneCell[0] = stroke;
                indexOneCell[1] = column;

                indexSelectCell.add(indexOneCell.clone());

                if (strokes.size() == 1 && columns.size() == 1) {
                    oldText = cell.text;
                    oldDate = cell.date;
                }
            }
        }

        return selectedCellCoordinate;
    }

    public Coordinate getSelectedCellCoordinate(TableModel tableModel) {

        float startX = tableModel.getWidthTable();
        float startY = tableModel.getHeightTable();
        float endX = 0;
        float endY = 0;

        for (int[] index : indexSelectCell) {
            int stroke = index[0];
            int column = index[1];
            Coordinate coordinateCell = tableModel.getCoordinateCell(stroke, column);
            if (startX > coordinateCell.startX)
                startX = coordinateCell.startX;
            if (startY > coordinateCell.startY)
                startY = coordinateCell.startY;
            if (endX < coordinateCell.endX)
                endX = coordinateCell.endX;
            if (endY < coordinateCell.endY)
                endY = coordinateCell.endY;
        }

        selectedCellCoordinate.setBounds(startX + tableModel.widthRows,
                endX + tableModel.widthRows,
                startY + tableModel.heightColumns,
                endY + tableModel.heightColumns);

        return selectedCellCoordinate;
    }

    public void updateCell(Cell cell) {

        float sizeTextTemp = cell.pref.sizeFont * scaleDp + 0.5F;
        cell.updateCell(sizeTextTemp);
    }

    // обновляет значение в ячейках в формулах которых фигурирует эта ячейка
    public void updateFormulaAtCell(TableModel tableModel, Cell cell) {
        int indexRow = cell.indexRow;
        int indexColumn = cell.indexColumn;

        Column column = tableModel.getColumns().get(indexColumn);
        for (Column col : tableModel.getColumns()) {
            if (col.getInputType() != 3)
                continue;
            Formula formula = col.getFormula();
            for (ColumnAttribute colAttr : formula.getColumnAttributes()) {
                if (colAttr.getNameId().equals(column.getNameIdColumn())) {
                    Cell c = tableModel.getRows().get(indexRow).getCellAtIndex(col.index);
                    c.text = col.getFormula().getValueFromFormula(tableModel, indexRow);
                    initTotalAmountToColumn(tableModel, c.indexColumn);
                }
            }
        }
    }
}
