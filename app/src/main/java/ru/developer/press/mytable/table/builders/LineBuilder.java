package ru.developer.press.mytable.table.builders;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;

import java.util.ArrayList;

import ru.developer.press.myTable.R;
import ru.developer.press.mytable.helpers.Coordinate;
import ru.developer.press.mytable.helpers.StaticMethods;
import ru.developer.press.mytable.model.Column;
import ru.developer.press.mytable.model.Row;
import ru.developer.press.mytable.model.TableModel;

public class LineBuilder extends Builder {
    private final Paint paintLine;

    public LineBuilder(Context context) {
        super(context);
        paintLine = new Paint();
        paintLine.setColor(context.getResources().getColor(R.color.gray));
        paintLine.setStrokeWidth(StaticMethods.convertDpToPixels(1.3f, context));
    }

    @Override
    public void draw(Canvas canvas, Coordinate coordinate, TableModel tableModel) {
        int coordinateStartX = (int) coordinate.startX;
        float coordinateEndX = coordinate.endX;
        float coordinateStartY = coordinate.startY;
        float coordinateEndY = coordinate.endY;

        int widthHeaders = tableModel.widthRows;
        int heightColumns = tableModel.heightColumns;
        int widthTable = tableModel.getWidthTable();

        ArrayList<Row> rows = tableModel.getRows();
        int strokeSize = rows.size();
        float heightTotal = tableModel.totalAmount.heightStroke;


        float startY = 0;
        float endY = 0;

        for (int i = 0; i < strokeSize; i++) {
            Row row = rows.get(i);
            endY += row.heightStroke;
            startY = endY - row.heightStroke;
            // проверка на то  - отменить отрисовку горизонтальной линии ниже тотал или нет
            boolean isDownToTotalAmount = tableModel.isTotalAmountEnable() && endY > coordinateEndY - heightTotal - heightColumns;
            if (endY < coordinateStartY)
                continue;
            if (startY > coordinateEndY || isDownToTotalAmount)
                break;
            // если ширина таблицы меньше чем экран то рисуй линии до конца таблицы если больше то до конца экрана
            int endXLine = (int) ((widthTable + widthHeaders) < coordinateEndX ? widthTable + widthHeaders : coordinateEndX);
            canvas.drawLine(coordinateStartX, endY + heightColumns, endXLine + 1, endY + heightColumns, paintLine);
        }
        // первая горизонтальная линия
        canvas.drawLine(coordinateStartX, heightColumns + coordinateStartY, widthTable + coordinateStartX + widthHeaders + 1, heightColumns + coordinateStartY, paintLine);

        if (tableModel.isTotalAmountEnable() && tableModel.isVisibleTotalAmount()) { // линия над тотал
            if (coordinateEndY < tableModel.getHeightTable() + heightColumns)
                canvas.drawLine(coordinateStartX, coordinateEndY - heightTotal, widthTable + coordinateStartX + widthHeaders + 1, coordinateEndY - heightTotal, paintLine);
            // линия под тотал
            float endYBackTotal = coordinateEndY;
            int heightTable = tableModel.getHeightTable() + heightColumns;
            if (heightTable < coordinate.height)
                endYBackTotal = heightTable;
            canvas.drawLine(coordinateStartX, endYBackTotal, widthTable + coordinateStartX + widthHeaders + 1, endYBackTotal, paintLine);
        }
        //

        endY = (int) (tableModel.getHeightTable() + heightColumns < coordinate.endY ? tableModel.getHeightTable() + heightColumns : coordinate.endY);

        ArrayList<Column> columnsPref = tableModel.getColumns();
        int columnsSize = columnsPref.size();

        float colEndX = 0;
        float colStartX = 0;
        for (int i = 0; i < columnsSize; i++) {
            Column column = columnsPref.get(i);
            colEndX += column.getWidth();
            colStartX = colEndX - column.getWidth();

            if (colEndX < coordinateStartX)
                continue;
            if (colStartX > coordinateEndX)
                break;
            canvas.drawLine(colEndX + widthHeaders,
                    coordinateStartY,
                    colEndX + widthHeaders,
                    endY + 1,
                    paintLine);
        }
        // рисуем первую вертикальную полоску
        canvas.drawLine(widthHeaders + coordinateStartX, coordinateStartY,
                widthHeaders + coordinateStartX, endY + 1,
                paintLine);
    }

    @Override
    public void unSelectAll(TableModel tableModel) {

    }

    @Override
    public Coordinate getSelectedCellCoordinate(TableModel tableModel) {
        return null;
    }

    @Override
    public Coordinate selectCellsOfSelector(float startX, float endX, float startY, float endY, TableModel tableModel) {
        return null;
    }
}
