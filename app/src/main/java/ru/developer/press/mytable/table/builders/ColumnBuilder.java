package ru.developer.press.mytable.table.builders;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;

import java.util.ArrayList;

import ru.developer.press.myTable.R;
import ru.developer.press.mytable.helpers.Coordinate;
import ru.developer.press.mytable.helpers.StaticMethods;
import ru.developer.press.mytable.interfaces.table.callback.RenameColumnListener;
import ru.developer.press.mytable.model.Cell;
import ru.developer.press.mytable.model.Column;
import ru.developer.press.mytable.model.Row;
import ru.developer.press.mytable.model.TableModel;

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
        float coordinateStartX = coordinateDraw.startX;
        float coordinateEndX = coordinateDraw.endX;
        float coordinateStartY = coordinateDraw.startY;

        int offX = tableModel.widthRows;
        float heightColumns = tableModel.heightColumns;
        int strokeSize = tableModel.getRows().size();


        ArrayList<Column> columnsPref = tableModel.getColumns();
        int columnsSize = columnsPref.size();

        float colStartX = 0;
        float colEndX = offX;
        float colEndY = coordinateStartY + heightColumns;
        for (int i = 0; i < columnsSize; i++) {
            Column column = columnsPref.get(i);

            colEndX += column.getWidth();
            colStartX = colEndX - column.getWidth();


            if (colEndX < coordinateStartX)
                continue;
            if (colStartX > coordinateEndX)
                break;

            Paint paintBack = column.paintBack;
            // фон
            canvas.drawRect(colStartX + 1, coordinateStartY,
                    colEndX, colEndY,
                    paintBack);

            boolean isTouched = column.isTouch;
            // если выделили
            if (isTouched) {
//                for (int j = 0; j < strokeSize; j++) {
//                    boolean isTouchedCellToColumn = tableModel.getRows().get(j).getCellAtIndex(i).isTouch;
//                    if (isTouchedCellToColumn) {
//                        canvas.drawRect(colStartX + 1, coordinateStartY,
//                                colEndX, colEndY,
//                                paintSelectStrokeAColumnGray);
//                        break;
//                    }
//                }
                canvas.drawRect(colStartX + 1, coordinateStartY,
                        colEndX, colEndY,
                        paintTouch);
            }
//            canvas.drawLine(column.endX, 0, column.endX, heightColumns, paintLine);
            drawText(canvas, column, colStartX, colEndX, coordinateStartY, colEndY);
        }
        // колона нумерации
        canvas.drawRect(coordinateStartX, coordinateStartY,
                coordinateStartX + offX, colEndY,
                paintNumberColumn);


    }

    private void drawText(Canvas canvas, Column column,
                          float startX, float endX, float startY, float endY) {
        column.drawText(canvas, (int) startX, (int) endX, (int) startY, (int) endY);

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
        int widthHeaders = tableModel.widthRows;
        Coordinate coordinate = new Coordinate();
        coordinate.startX = tableModel.getWidthTable() + tableModel.widthRows;
        coordinate.endX = 0;
        coordinate.startY = 0;
        coordinate.endY = tableModel.heightColumns + tableModel.getHeightTable();

        ArrayList<Column> columns = tableModel.getColumns();
        for (int i = 0; i < columns.size(); i++) {
            Column column = columns.get(i);
            if (column.isTouch) {
                Coordinate coordinateCell = tableModel.getCoordinateCell(0, i);
                if (coordinate.startX > coordinateCell.startX + widthHeaders)
                    coordinate.startX = coordinateCell.startX + widthHeaders;
                if (coordinate.endX < coordinateCell.endX + widthHeaders)
                    coordinate.endX = coordinateCell.endX + widthHeaders;
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

            Coordinate coordinateCell = tableModel.getCoordinateCell(0, i);
            int columnStart = (int) coordinateCell.startX + tableModel.widthRows;
            int columnEnd = (int) coordinateCell.endX + tableModel.widthRows;

            if (columnStart < endX && columnEnd > startX) {
                if (selectedCellCoordinate.startX > columnStart)
                    selectedCellCoordinate.startX = columnStart;
                if (selectedCellCoordinate.endX < columnEnd)
                    selectedCellCoordinate.endX = columnEnd;
                column.select(tableModel);
                selectColumnCount++;
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

    public boolean selectColumn(int index, TableModel tableModel) {

        Column column = tableModel.getColumns().get(index);
        if (column.isTouch) {
            return false;
        } else {
            unSelectAll(tableModel);
            column.select(tableModel);
            oldIndexColumn = index;
            oldNameColumn = column.text;
            selectColumnCount++;
        }
        return true;
    }

    public void select(Column column) {

    }

    public Column getSelectColumn(TableModel tableModel) {
        for (Column col : tableModel.getColumns()) {
            if (col.isTouch) {
                return col;
            }
        }
        return null;
    }


    public void init(TableModel tableModel) {
        for (int i = 0; i < tableModel.getColumns().size(); i++) {

            Column column = tableModel.getColumns().get(i);
            column.width = column.getWidth();
            column.index = i;
            float sizeTextTempColumn = column.pref.sizeFont * scaleDp + 0.5F;
            column.updateCell(sizeTextTempColumn);
        }
    }

    public void initColumn(TableModel tableModel, int indexColumn) {

        for (int i = 0; i < tableModel.getRows().size(); i++) {

            Column column = tableModel.getColumns().get(indexColumn);
            if (i == 0) {
                column.width = column.getWidth();
                column.index = indexColumn;
                float sizeTextTempColumn = column.pref.sizeFont * scaleDp + 0.5F;
                column.updateCell(sizeTextTempColumn);
            }
            Row row = tableModel.getRows().get(i);
            Cell cell = row.getCellAtIndex(indexColumn);
            cell.indexColumn = indexColumn;
            cell.indexRow = i;
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

        }
        initTotalAmountToColumn(tableModel, indexColumn);
    }

    public void updateHeightColumn(TableModel tableModel) {
        float height = 0;

        for (Column column : tableModel.getColumns()) {
            float textSize = column.pref.sizeFont * scaleDp + 0.5F;
            column.updateCell(textSize);
            float temp = column.getStaticHeight();

            if (height < temp) {
                height = temp;
            }
        }

        float minColumnHeight = 28 * scaleDp;
        if (height < minColumnHeight)
            height = minColumnHeight; //  если высота слишком маленькая то 28 дп

        tableModel.heightColumns = (int) height;
    }
}
