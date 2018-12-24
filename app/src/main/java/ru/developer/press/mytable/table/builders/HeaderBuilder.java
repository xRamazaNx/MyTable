package ru.developer.press.mytable.table.builders;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;

import java.util.ArrayList;

import ru.developer.press.mytable.helpers.Coordinate;
import ru.developer.press.mytable.table.model.Cell;
import ru.developer.press.mytable.table.model.Header;
import ru.developer.press.mytable.table.model.TableModel;

public class HeaderBuilder extends Builder {
    public int selectHeaderCount = 0;
    private int index;

    public HeaderBuilder(Context context) {
        super(context);

    }

    public boolean selectHeader(int index, TableModel tableModel) {
        Header header = tableModel.getHeaders().get(index);

        if (header.isTouch)
            return false;
        else{
            unSelectAll(tableModel);
            header.select();
            selectHeaderCount ++;
        }
        return true;
    }


    public void draw(Canvas canvas, Coordinate coordinateDraw, TableModel tableModel) {
                    Paint paintSelectStrokeAColumnGray = this.paintSelectStrokeAColumnGray;
        int offY = tableModel.heightColumns;
        int coorStartX = (int) coordinateDraw.startX;
        float coordinateStartY = coordinateDraw.startY;
        float coordinateEndY = coordinateDraw.endY;

        ArrayList<Header> headers = tableModel.getHeaders();
        int size = headers.size();
        for (int i = 0; i < size; i++) {
            Header header = headers.get(i);

            float startY = header.startY;
            float endY = header.endY;

            if (endY < coordinateStartY || startY > coordinateEndY)
                continue;

            float endX = header.endX;

            Paint paintBack = header.paintBack;
            canvas.drawRect(coorStartX, startY + 1 + offY, endX + coorStartX, endY + offY, paintBack);
            // рисуем сырми хеадер если в его диапозоне есть выделенная ячейка
            ArrayList<Cell> cells = tableModel.getHeaders().get(i).getCells();
            for (Cell cell : cells) {
                boolean isTouched = cell.isTouch;
                if (isTouched) {
                    canvas.drawRect(coorStartX, startY + 1 + offY, endX + coorStartX, endY + offY, paintSelectStrokeAColumnGray);
                    break;
                }
            }

            boolean isTouched = header.isTouch;
            if (isTouched) {
                canvas.drawRect(coorStartX, startY + 1 + offY, endX + coorStartX, endY + offY, paintTouch);
            }

            canvas.save();
            drawText(canvas, header, offY, coorStartX);
            canvas.restore();
        }
    }

    private void drawText(Canvas canvas, Header header, int offY, int coorStartX) {
        int sY = (int) (header.startY + offY);
        int eX = (int) (header.endX + coorStartX);
        int eY = (int) (header.endY + offY);

        header.drawText(canvas, coorStartX,eX,sY,eY);
    }

    public void unSelectAll(TableModel tableModel) {
        ArrayList<Header> headers = tableModel.getHeaders();
        for (int i = 0; i < headers.size(); i++) {
            Header header = headers.get(i);
            if (header.isTouch) {
                header.unSelect();
            }

        }
        selectHeaderCount = 0;
    }

    @Override
    public Coordinate getSelectedCellCoordinate(TableModel tableModel) {
        int heightColumns = tableModel.heightColumns;
        Coordinate coordinate = new Coordinate();
        coordinate.startX = 0;
        coordinate.endX = tableModel.widthTable + tableModel.widthHeaders;
        coordinate.startY = tableModel.heightTable + heightColumns;
        coordinate.endY = 0;
        for (Header header: tableModel.getHeaders()) {
            if (header.isTouch){
                if (coordinate.startY > header.startY + heightColumns)
                    coordinate.startY = header.startY + heightColumns;
                if (coordinate.endY < header.endY + heightColumns)
                    coordinate.endY = header.endY + heightColumns;
            }
        }
        return coordinate;
    }

    @Override
    public Coordinate selectCellsOfSelector(float startX, float endX, float startY, float endY, TableModel tableModel) {
        unSelectAll(tableModel);
        selectedCellCoordinate.setBounds(endX, startX, endY, startY);

        for (int i = 0; i < tableModel.getHeaders().size(); i++) {
            Header header = tableModel.getHeaders().get(i);

            int strokeStart = (int) header.startY + tableModel.heightColumns;
            int strokeEnd = (int) header.endY + tableModel.heightColumns;
            if (strokeStart < endY && strokeEnd > startY) {
                if (selectedCellCoordinate.startY > strokeStart)
                    selectedCellCoordinate.startY = strokeStart;
                if (selectedCellCoordinate.endY < strokeEnd)
                    selectedCellCoordinate.endY = strokeEnd;
                header.select();
                selectHeaderCount++;
            }
        }
        return selectedCellCoordinate;
    }


    public void deleteHeaders(TableModel tableModel) {
        if (selectHeaderCount >= tableModel.getHeaders().size()) {
            return;
        }
        for (int i = 0; i < tableModel.getHeaders().size(); i++) {
            if (tableModel.getHeaders().get(i).isTouch) {
                tableModel.deleteStroke(i);
                i--;
            }
        }
        selectHeaderCount = 0;

    }

}
