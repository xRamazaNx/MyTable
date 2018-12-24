package ru.developer.press.mytable.table.builders;

import android.content.Context;
import android.graphics.Canvas;

import java.util.ArrayList;

import ru.developer.press.mytable.helpers.Coordinate;
import ru.developer.press.mytable.interfaces.CellEditListener;
import ru.developer.press.mytable.table.model.Cell;
import ru.developer.press.mytable.table.model.Header;
import ru.developer.press.mytable.table.model.TableModel;

public class CellBuilder extends Builder {
    private String oldText;
    private long oldDate;

    private int[] indexOneCell = new int[2];
    private ArrayList<int[]> indexSelectCell;

    private CellEditListener editCellListener;
    private ArrayList<Integer> strokes = new ArrayList<>();
    private ArrayList<Integer> columns = new ArrayList<>();

    public CellBuilder(Context context, CellEditListener editCellListener) {
        super(context);

        this.editCellListener = editCellListener;
        indexSelectCell = new ArrayList<>();

    }

    public int getSelectCellSize() {
        return indexSelectCell.size();
    }

    public void draw(Canvas canvas, Coordinate coordinate, TableModel tableModel) {
        int offX = tableModel.widthHeaders;
        int offY = tableModel.heightColumns;
        int columnSize = tableModel.getColumns().size();

        ArrayList<Header> headers = tableModel.getHeaders();

        int strokeSize = headers.size();
        // рисуем и горизонтальны полоски и все ячейки
        for (int index = 0; index < strokeSize; index++) {
            ArrayList<Cell> entry = headers.get(index).getCells(); // строка
            float headerEndY = headers.get(index).endY;
            float coordinateStartY = coordinate.startY;
            // если ячейка сверху от экрана не видно то листать дальше цикл
            if (headerEndY < coordinateStartY)
                continue;

            float headerStartY = headers.get(index).startY;
            float coordinateEndY = coordinate.endY;
            // если ячейку не видно снизу от экрана то листать не зачем останавливаем цикл
            if (headerStartY > coordinateEndY)
                break;

            for (int i = 0; i < columnSize; i++) {
                Cell cell = entry.get(i);
                float cellEndX = cell.endX;
                float coordinateStartX = coordinate.startX;
                // если ячейка слева от экрана не видно то листать дальше цикл
                if (cellEndX < coordinateStartX)
                    continue;

                float cellStartX = cell.startX;
                float coordinateEndX = coordinate.endX;
                // если ячейку не видно с право от экрана то листать не зачем останавливаем цикл
                if (cellStartX > coordinateEndX)
                    break;

                int cellStartY = (int) cell.startY;
                float cellEndY = cell.endY;
                int sX = (int) cellStartX + offX,
                        eX = (int) cellEndX + offX,
                        sY = cellStartY + offY,
                        eY = (int) cellEndY + offY;

                if (sX == 0)
                    sX = 1;
                if (sY == 0)
                    sY = 1;

                canvas.drawRect(sX, sY, eX, eY, cell.paintBack);
                boolean isTouched = cell.isTouch;
                if (isTouched) {
                    canvas.drawRect(sX, sY, eX, eY, paintTouch);
                }
                drawText(canvas, cell, sX, eX, sY, eY);


            }
        }
    }

    @Override
    public void unSelectAll(TableModel tableModel) {

        for (int[] index : indexSelectCell) {
            Cell cell = tableModel.getHeaders().get(index[0]).getCell(index[1]);
            cell.isTouch = false;
        }
        if (indexSelectCell.size() == 1) {
            Cell cell = tableModel.getHeaders().get(indexOneCell[0]).getCell(indexOneCell[1]);
            String newText = cell.text;
            long newDate = cell.date;
            editCellListener.cellEdit(oldText, newText, oldDate, newDate, indexOneCell);
        }

        indexSelectCell.clear();
    }

    private void drawText(Canvas canvas, Cell cell, int sX, int eX, int sY, int eY) {
        cell.drawText(canvas, sX, eX, sY, eY);
    }

    public boolean selectCell(int stroke, int column, TableModel tableModel) {

        Cell cell = tableModel.getHeaders().get(stroke).getCell(column);
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
        return tableModel.getHeaders().get(stroke).getCell(column);

    }

    public void clearText(TableModel tableModel) {
        Cell cell = getSelectCell(tableModel);
        if (tableModel.getColumns().get(cell.columnIndex).getInputType() == 2) {
            cell.date = 0;
        }
        cell.text = "";
    }

    public Coordinate selectCellsOfSelector(float startX, float endX, float startY, float endY, TableModel tableModel) {
        strokes.clear();
        columns.clear();

        unSelectAll(tableModel);
        selectedCellCoordinate.setBounds(endX, startX, endY, startY);

        for (int i = 0; i < tableModel.getHeaders().size(); i++) {
            int strokeStart = (int) tableModel.getHeaders().get(i).getCell(0).startY + tableModel.heightColumns;
            int strokeEnd = (int) tableModel.getHeaders().get(i).getCell(0).endY + tableModel.heightColumns;
            if (strokeStart < endY && strokeEnd > startY) {
                strokes.add(i);
                if (selectedCellCoordinate.startY > strokeStart)
                    selectedCellCoordinate.startY = strokeStart;
                if (selectedCellCoordinate.endY < strokeEnd)
                    selectedCellCoordinate.endY = strokeEnd;
            }
        }
        for (int i = 0; i < tableModel.getColumns().size(); i++) {
            int columnStart = (int) tableModel.getColumns().get(i).startX + tableModel.widthHeaders;
            int columnEnd = (int) tableModel.getColumns().get(i).endX + tableModel.widthHeaders;
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
                Cell cell = tableModel.getHeaders().get(stroke).getCell(column);
                cell.isTouch = true;
                indexOneCell[0] = stroke;
                indexOneCell[1] = column;

                indexSelectCell.add(indexOneCell.clone());
            }
        }

        return selectedCellCoordinate;
    }

    public Coordinate getSelectedCellCoordinate(TableModel tableModel) {

        int stroke = indexOneCell[0];
        int column = indexOneCell[1];
        Cell cell = tableModel.getHeaders().get(stroke).getCell(column);

        float startX = cell.startX + tableModel.widthHeaders;
        float startY = cell.startY + tableModel.heightColumns;
        float endX = cell.endX + tableModel.widthHeaders;
        float endY = cell.endY + tableModel.heightColumns;

        selectedCellCoordinate.setBounds(startX, endX, startY, endY);

        return selectedCellCoordinate;
    }
}
