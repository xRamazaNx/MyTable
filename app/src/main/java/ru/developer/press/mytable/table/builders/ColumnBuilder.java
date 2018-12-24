package ru.developer.press.mytable.table.builders;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;

import java.util.ArrayList;

import ru.developer.press.myTable.R;
import ru.developer.press.mytable.helpers.Coordinate;
import ru.developer.press.mytable.interfaces.RenameColumnListener;
import ru.developer.press.mytable.table.model.Column;
import ru.developer.press.mytable.table.model.Header;
import ru.developer.press.mytable.table.model.TableModel;

public class ColumnBuilder extends Builder {

    private final RenameColumnListener renameColumnListener;
    public int selectColumnCount = 0;
    public int index = 0;
    private String oldNameColumn;
    private int oldIndexColumn;
    private Paint paintNumberColumn;

    public ColumnBuilder(Context context, RenameColumnListener renameColumnListener) {
        super(context);
        this.renameColumnListener = renameColumnListener;

        paintNumberColumn = new Paint();
        paintNumberColumn.setColor(context.getResources().getColor(R.color.gray_light));

    }


    public void draw(Canvas canvas, Coordinate coordinateDraw, TableModel tableModel) {

        int offX = tableModel.widthHeaders;
        float heightColumns = tableModel.heightColumns;
        int strokeSize = tableModel.getHeaders().size();


        ArrayList<Column> columnsPref = tableModel.getColumns();
        int columnsSize = columnsPref.size();
        float coordinateStartX = coordinateDraw.startX;
        float coordinateEndX = coordinateDraw.endX;
        float coordinateStartY = coordinateDraw.startY;

        for (int i = 0; i < columnsSize; i++) {
            Column column = columnsPref.get(i);
            float colEndX = column.endX;
            float colStartX = column.startX;

            if (colEndX < coordinateStartX || colStartX > coordinateEndX)
                continue;
            Paint paintBack = column.paintBack;
            // фон
            canvas.drawRect(colStartX + offX + 1, coordinateStartY,
                    colEndX + offX, coordinateStartY + heightColumns,
                    paintBack);

            for (int j = 0; j < strokeSize; j++) {
                boolean isTouched = tableModel.getHeaders().get(j).getCell(i).isTouch;
                if (isTouched) {
                    canvas.drawRect(colStartX + offX + 1, coordinateStartY,
                            colEndX + offX, coordinateStartY + heightColumns,
                            paintSelectStrokeAColumnGray);
                    break;
                }
            }
            boolean isTouched = column.isTouch;
            if (isTouched) {
                // если выделили
                canvas.drawRect(colStartX + offX + 1, coordinateStartY,
                        colEndX + offX, coordinateStartY + heightColumns,
                        paintTouch);
            }
//            canvas.drawLine(column.endX, 0, column.endX, heightColumns, paintLine);
            drawText(canvas, column, coordinateDraw, offX);
        }
        // колона нумерации
        canvas.drawRect(coordinateStartX, coordinateStartY,
                coordinateStartX + offX, coordinateStartY + heightColumns,
                paintNumberColumn);

    }

    private void drawText(Canvas canvas, Column column, Coordinate coordinateDraw, int offX) {

        int sX = (int) (column.startX + offX);

        int eX = (int) (column.endX + offX);
        int eY = (int) (column.endY + coordinateDraw.startY);

        column.drawText(canvas, sX, eX, (int) coordinateDraw.startY, eY);
    }


    public void unSelectAll(TableModel tableModel) {
        ArrayList<Column> columnsPref = tableModel.getColumns();
        for (int i = 0; i < columnsPref.size(); i++) {
            Column column = columnsPref.get(i);
            if (column.isTouch) {
                column.unSelect(tableModel);
            }
        }
        selectColumnCount = 0;
    }

    @Override
    public Coordinate getSelectedCellCoordinate(TableModel tableModel) {
        int widthHeaders = tableModel.widthHeaders;
        Coordinate coordinate = new Coordinate();
        coordinate.startX = tableModel.widthTable + tableModel.widthHeaders;
        coordinate.endX = 0;
        coordinate.startY = tableModel.heightColumns;
        coordinate.endY = 0;

        for (Column column: tableModel.getColumns()) {
            if (column.isTouch){
                if (coordinate.startX > column.startX + widthHeaders)
                    coordinate.startX = column.startX + widthHeaders;
                if (coordinate.endX < column.endX + widthHeaders)
                    coordinate.endX = column.endX + widthHeaders;
            }
        }
        return coordinate;
    }

    @Override
    public Coordinate selectCellsOfSelector(float startX, float endX, float startY, float endY, TableModel tableModel) {
        unSelectAll(tableModel);
        selectedCellCoordinate.setBounds(endX, startX, endY, startY);

        for (int i = 0; i < tableModel.getColumns().size(); i++) {
            Column column = tableModel.getColumns().get(i);

            int columnStart = (int) column.startX + tableModel.widthHeaders;
            int columnEnd = (int) column.endX + tableModel.widthHeaders;

            if (columnStart < endX && columnEnd > startX) {
                if (selectedCellCoordinate.startX > columnStart)
                    selectedCellCoordinate.startX = columnStart;
                if (selectedCellCoordinate.endX < columnEnd)
                    selectedCellCoordinate.endX = columnEnd;
                column.select(tableModel);
                selectColumnCount ++;
            }
        }
        if (selectColumnCount == 1) {
            for (int i = 0; i < tableModel.getColumns().size(); i++) {
                Column col = tableModel.getColumns().get(i);
                if (col.isTouch) {
                    oldIndexColumn = i;
                    oldNameColumn = col.text;
                    break;
                }
            }
        } else
            renameColumnListener.renameColumn(oldNameColumn, tableModel.getColumns().get(oldIndexColumn).text, oldIndexColumn);

        return selectedCellCoordinate;
    }

    public boolean selectColumn(float x, TableModel tableModel) {
        int index = 0;
        for (int i = 0; i < tableModel.getColumns().size(); i++) {
            Column column = tableModel.getColumns().get(i);
            if (x - tableModel.widthHeaders <= column.endX) {
                index = i;
                break;
            }
        }
        Column column = tableModel.getColumns().get(index);
        if (column.isTouch) {
            return false;
        } else {
            unSelectAll(tableModel);
            column.select(tableModel);
            oldIndexColumn = index;
            oldNameColumn = column.text;
            selectColumnCount ++;
        }
        return true;
    }

    public Column getSelectColumn(TableModel tableModel) {
        for (Column col : tableModel.getColumns()) {
            if (col.isTouch) {
                return col;
            }
        }
        return null;
    }

    public void deleteSelectedColumn(TableModel tableModel) {
        if (tableModel.getColumns().size() <= selectColumnCount)
            return;
        for (int i = 0; i < tableModel.getColumns().size(); i++) {
            if (tableModel.getColumns().get(i).isTouch) {
                tableModel.getColumns().remove(i);
                i--;
            }
        }
        for (Header header : tableModel.getHeaders()) {
            for (int i = 0; i < header.getCells().size(); i++) {
                if (header.getCell(i).isTouch) {
                    header.getCells().remove(i);
                    i--;
                }
            }
        }
        selectColumnCount = 0;
    }
}
