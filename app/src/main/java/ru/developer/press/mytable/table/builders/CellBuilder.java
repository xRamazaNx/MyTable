package ru.developer.press.mytable.table.builders;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.text.Layout;
import android.text.StaticLayout;
import android.text.TextPaint;

import java.util.ArrayList;

import ru.developer.press.myTable.R;
import ru.developer.press.mytable.StaticMetods;
import ru.developer.press.mytable.interfaces.CellEditListener;
import ru.developer.press.mytable.model.Cell;
import ru.developer.press.mytable.model.CellAbstract;
import ru.developer.press.mytable.model.ColumnPref;
import ru.developer.press.mytable.model.TableModel;

public class CellBuilder {
    private String oldText;
    private long oldDate;
    private float scaleDp;

    private int width;
    private int height;

    private int[] indexSelectCell = new int[2];

    private Paint paintLines;
    private Paint paintSelectCell;
    private Paint paintSelectStrokeAColumn;
    private TextPaint textPaint;
    private CellEditListener editCellListener;

    public CellBuilder(Context context, CellEditListener editCellListener) {
        this.editCellListener = editCellListener;

        scaleDp = context.getResources().getDisplayMetrics().density;
        paintLines = new Paint();
        paintLines.setStyle(Paint.Style.STROKE);
        paintLines.setStrokeWidth(1);
        paintLines.setColor(context.getResources().getColor(R.color.gray));

        textPaint = new TextPaint();
        textPaint.setAntiAlias(true);

        paintSelectCell = new Paint();
        paintSelectCell.setColor(context.getResources().getColor(R.color.color_select_cell));
        paintSelectCell.setStyle(Paint.Style.STROKE);
        paintSelectCell.setStrokeWidth(2);

        paintSelectStrokeAColumn = new Paint();
        paintSelectStrokeAColumn.setColor(context.getResources().getColor(R.color.color_select_cell_stroke_column));
        paintSelectStrokeAColumn.setStyle(Paint.Style.FILL);

    }

    public void init(TableModel tableModel) {
        width = 0;
        height = 0;

        float endX = 0; // endX - startX = widthColumn
        float endY = 0;

        boolean lock = tableModel.getLockHeightCells() == 1;
        for (int i = 0; i < tableModel.getEntries().size(); i++) {

            int heightStroke = lock ? tableModel.getHeightCells() : getMaxHeight(tableModel, i);

            height += heightStroke;
            endY += heightStroke;

            float startY = endY - heightStroke;

            float startX;
            for (int j = 0; j < tableModel.getColumnsPref().size(); j++) {
                ColumnPref columnPref = tableModel.getColumnsPref().get(j);

                endX = endX + columnPref.getWidthColumn();
                startX = endX - columnPref.getWidthColumn();

                Cell cell = tableModel.getEntries().get(i).get(j);

                cell.type = columnPref.getInputType();
                if (cell.type == 2) {
                    if (cell.date > 0)
                        cell.text = StaticMetods.getDateOfMillis(cell.date);
                }

                cell.sizeFont = columnPref.getTextSizeCell();
                cell.styleFont = columnPref.getTextStyleCell();
                cell.colorFont = columnPref.getTextColorCell();
                cell.colorBack = columnPref.getColorCellRect();

                cell.setBounds(startX, endX, startY, endY);
                cell.column = j;

                if (i == 0)
                    width += tableModel.getColumnsPref().get(j).getWidthColumn(); // один раз посчитать ширину таблицы

            }
            endX = 0;


        }
    }

    public void drawCells(Canvas canvas, CellAbstract coordinate, TableModel tableModel) {

        int widthTemp = 0;
        int heightTemp = 0;
        for (int i = 0; i < tableModel.getColumnsPref().size(); i++) {
            widthTemp += tableModel.getColumnsPref().get(i).getWidthColumn();
            if (widthTemp < coordinate.endX && widthTemp > coordinate.startX)
                canvas.drawLine(widthTemp, coordinate.startY, widthTemp,
                        height < coordinate.height ? height : coordinate.endY,
                        paintLines);
        }
        for (ArrayList<Cell> entry : tableModel.getEntries()) {
            int heightStroke = (int) entry.get(0).height;
            heightTemp += heightStroke;
            if (heightTemp - heightStroke < coordinate.endY && heightTemp > coordinate.startY)
                canvas.drawLine(coordinate.startX, heightTemp,
                        width < coordinate.width ? width : coordinate.endX,
                        heightTemp, paintLines);
            else continue;

            for (int i = 0; i < entry.size(); i++) {
                Cell cell = entry.get(i);
                if (cell.startX > coordinate.endX && cell.endX < coordinate.startX)
                    break;

                if (cell.isTouched) {
                    int x = (int) cell.startX, y = (int) cell.startY + 1;
                    if (x == 0)
                        x = 1;
                    else x++;

                    canvas.drawRect(x, y, cell.endX, cell.endY, paintSelectCell);
                    canvas.drawRect(x, cell.startY + 1, cell.endX - 1, cell.endY, paintSelectStrokeAColumn);
                } else if (cell.isTouchedStrCol) {
                    int x = (int) cell.startX;
                    if (cell.startX == 0)
                        x = 1;
                    canvas.drawRect(x, cell.startY + 1, cell.endX - 1, cell.endY, paintSelectStrokeAColumn);
                }
//            else {
//                int x = (int) cell.startX;
//                if (cell.startX == 0)
//                    x = 1;
//                paintColorRect.setColor(cell.colorBack);
//                canvas.drawRect(x, cell.startY + 1, cell.endX - 1, cell.endY, paintColorRect);
//
//            }
                if (cell.text.length() > 0) {
                    drawText(canvas, cell);
                }

            }


        }
    }

    private void drawText(Canvas canvas, Cell cell) {
        canvas.save();
//        if (cell.isTouched){
//            paintText.setColor();
//            canvas.drawRect(cell.startX, cell.startY,cell.endX,cell.endY,paintText);
//        }
        canvas.clipRect(cell.startX, cell.startY + 1, cell.startX + cell.width, cell.startY + cell.height - 4);

        float sizeTextTemp = cell.sizeFont * scaleDp + 0.5F;

        textPaint.setColor(cell.colorFont);
        textPaint.setTextSize(sizeTextTemp);
        textPaint.setTypeface(StaticMetods.setTypeFace(cell.styleFont));

        int xPos = (int) cell.startX + 4;
//        int xPos = (int) cell.startX;
        int yPos = (int) cell.startY + 4;

//        if (cell.type == 2) {
//            textCell = StaticMetods.getDateOfMillis(cell.date);
//        }

        StaticLayout staticLayout = new StaticLayout(cell.text, textPaint, (int) cell.width - 8, Layout.Alignment.ALIGN_CENTER, 1, -3, false);

        float height = staticLayout.getHeight();
        if (height < cell.height) { // если высота текста меньше чем высота ячейки
            yPos = (int) (yPos + (cell.height / 2) - height / 2) - 4; // рассчет середины для позиции y
        }
//        else yPos -= 3;

        canvas.translate(xPos, yPos);
        staticLayout.draw(canvas);

        canvas.restore();
    }

    private int getMaxHeight(TableModel tableModel, int stroke) {
        float height = 0;
        TextPaint textPaint = new TextPaint();

        ArrayList<Cell> get = tableModel.getEntries().get(stroke);
        for (int i = 0; i < get.size(); i++) {
            ColumnPref column = tableModel.getColumnsPref().get(i);
            Cell cell = get.get(i);
            String text = cell.text;
            //для того чтобы если есть даты, чтоб и их учитывала в высоте строки
            if (cell.type == 2) {
                if (cell.date > 0)
                    text = StaticMetods.getDateOfMillis(cell.date);
            }
            textPaint.setTextSize(column.getTextSizeCell() * scaleDp + 0.5F);
            textPaint.setTypeface(StaticMetods.setTypeFace(column.getTextStyleCell()));
            StaticLayout staticLayout = new StaticLayout(text, textPaint,
                    (column.getWidthColumn() - 8),
                    Layout.Alignment.ALIGN_CENTER, 1, -3, false);

            float temp = staticLayout.getHeight();

            if (height < temp && cell.text.length() > 0) {
                height = temp;
            }
        }
//        height += 4;// для отступов снизу
        if (height < tableModel.getHeightCells())
            height = tableModel.getHeightCells(); //  если высота слишком маленькая то как задали
        return (int) height + 4;
    }

    public boolean selectCell(float x, float y, TableModel tableModel) {
        int stroke = 0;
        for (int i = 0; i < tableModel.getEntries().size(); i++) {
            int strokeEnd = (int) tableModel.getEntries().get(i).get(0).endY;
            if (strokeEnd > y) {
                stroke = i;
                break;
            }
        }
        if (stroke > tableModel.getEntries().size() - 1) return false;

        ArrayList<Cell> entry = tableModel.getEntries().get(stroke);
        for (int i = 0; i < entry.size(); i++) {
            Cell cell = entry.get(i);
            boolean isTouchedCell = x - cell.startX < cell.width && y - cell.startY < cell.height;

            if (isTouchedCell) {
                cell.isTouched = true;
                indexSelectCell[0] = stroke;
                indexSelectCell[1] = i;
                oldText = cell.text;
                oldDate = cell.date;
                return true;
            }
        }

        return false;
    }

    public Cell getSelectCell(TableModel tableModel) {
        return tableModel.getEntries().get(indexSelectCell[0]).get(indexSelectCell[1]);
    }

    public int getWidth() {
        return width;
    }

    public int getHeight() {
        return height;
    }

    public void unSelectCell(TableModel tableModel) {
        for (ArrayList<Cell> stroke : tableModel.getEntries()) {
            for (Cell cell : stroke) {
                if (cell.isTouched) {
                    cell.isTouched = false;
                    String newText = cell.text;
                    long newDate = cell.date;
                    editCellListener.cellEdit(oldText, newText, oldDate, newDate, indexSelectCell);
                    return;
                }
            }
        }
    }

    public void clearText(TableModel tableModel) {
        Cell cell = getSelectCell(tableModel);
        if (cell.type == 2) {
            cell.date = 0;
        }
        cell.text = "";
    }
}
