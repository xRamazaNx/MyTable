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
import ru.developer.press.mytable.interfaces.RenameColumnListener;
import ru.developer.press.mytable.model.Cell;
import ru.developer.press.mytable.model.CellAbstract;
import ru.developer.press.mytable.model.ColumnPref;
import ru.developer.press.mytable.model.TableModel;

public class ColumnBuilder {

    private final Paint paintTouchColumn;
    private final Paint paintLine;
    private final TextPaint textPaint;
    private final RenameColumnListener renameColumnListener;
    public int selectColumnCount = 0;
    public int index = 0;
    private float scaleDp;
    public float heightColumns;
    private String oldName;
    private int oldIndex;

    public ColumnBuilder(Context context, RenameColumnListener renameColumnListener) {
        this.renameColumnListener = renameColumnListener;
        paintLine = new Paint();
        paintLine.setColor(context.getResources().getColor(R.color.gray));

        paintTouchColumn = new Paint();
        paintTouchColumn.setColor(context.getResources().getColor(R.color.color_select_stroke_column));

        textPaint = new TextPaint();
        textPaint.setAntiAlias(true);

        scaleDp = context.getResources().getDisplayMetrics().density;


    }

    public void drawColumns(Canvas canvas, CellAbstract coordinateDraw, TableModel tableModel) {
        for (ColumnPref columnPref :
                tableModel.getColumnsPref()) {
            if (columnPref.endX < coordinateDraw.startX || columnPref.startX > coordinateDraw.endX )
                continue;
            if (columnPref.isTouched) {
                canvas.drawRect(columnPref.startX, columnPref.startY + 1, columnPref.endX, columnPref.endY, paintTouchColumn);
            }
            drawText(canvas, columnPref);
            canvas.drawLine(columnPref.endX, 0, columnPref.endX, heightColumns, paintLine);
        }

    }

    private void drawText(Canvas canvas, ColumnPref columnPref) {
        canvas.save();
//        if (cell.isTouched){
//            paintText.setColor();
//            canvas.drawRect(cell.startX, cell.startY,cell.endX,cell.endY,paintText);
//        }

        canvas.clipRect(columnPref.startX, 0, columnPref.startX + columnPref.width, heightColumns - 1);

        float sizeTextTemp = columnPref.getTextSizeTitle() * scaleDp + 0.5F;

        textPaint.setColor(columnPref.getTextColorTitle());
        textPaint.setTextSize(sizeTextTemp);
        textPaint.setTypeface(StaticMetods.setTypeFace(columnPref.getTextStyleTitle()));

        int xPos = (int) columnPref.startX + 4;
        int yPos = (int) columnPref.startY + 4;

        String textCell = columnPref.getName();

//        if (cell.type == 2) {
//            textCell = StaticMetods.getDateOfMillis(cell.date);
//        }

        StaticLayout staticLayout = new StaticLayout(textCell, textPaint, columnPref.getWidthColumn() - 8, Layout.Alignment.ALIGN_CENTER, 1, -3, false);

        float height = staticLayout.getHeight();
        if (height < columnPref.height) { // если высота текста меньше чем высота ячейки
            yPos = (int) (yPos + (columnPref.height / 2) - height / 2) - 4; // рассчет середины для позиции y
        }
        canvas.translate(xPos, yPos);
        staticLayout.draw(canvas);

        canvas.restore();

    }

    public void init(TableModel tableModel) {
        heightColumns = getMaxHeight(tableModel);
        int startX;
        int endX = 0;

        for (int i = 0; i < tableModel.getColumnsPref().size(); i++) {
            ColumnPref columnPref = tableModel.getColumnsPref().get(i);
            endX += columnPref.getWidthColumn();
            startX = endX - columnPref.getWidthColumn();
            columnPref.setBounds(startX, endX, 0, heightColumns);
        }
    }

    private int getMaxHeight(TableModel tableModel) {
        float height = 0;
        TextPaint textPaint = new TextPaint();

        for (ColumnPref columnPref : tableModel.getColumnsPref()) {
            textPaint.setTextSize(columnPref.getTextSizeTitle() * scaleDp + 0.5F);
            textPaint.setTypeface(StaticMetods.setTypeFace(columnPref.getTextStyleTitle()));
            StaticLayout staticLayout = new StaticLayout(columnPref.getName(), textPaint, columnPref.getWidthColumn() - 8, Layout.Alignment.ALIGN_CENTER, 1, -3, false);

            float temp = staticLayout.getHeight();

            if (height < temp) {
                height = temp;
            }
        }
//        height += 8;// для отступов сверху и снизу
        if (height + 4 < 28 * scaleDp)
            height = (int) (28 * scaleDp); //  если высота слишком маленькая то 28 дп
        return (int) height + 8;
    }

    public void unSelectAll(TableModel tableModel) {
        ArrayList<ColumnPref> columnsPref = tableModel.getColumnsPref();
        for (int i = 0; i < columnsPref.size(); i++) {
            ColumnPref column = columnsPref.get(i);
            if (column.isTouched) {
                selectColumn(i, tableModel);
            }
        }
    }

    public void selectColumn(int index, TableModel tableModel) {
        ColumnPref columnPref = tableModel.getColumnsPref().get(index);

        if (columnPref.isTouched) {
            selectColumnCount--;
            columnPref.isTouched = false;

        } else {
            selectColumnCount++;
            columnPref.isTouched = true;
        }

        // выделяем ячейки в этой колоне ну или снимаем, смотря что там у колоны
        for (ArrayList<Cell> cellsInColumn : tableModel.getEntries()) {
            cellsInColumn.get(index).isTouchedStrCol = columnPref.isTouched;
        }

        if (selectColumnCount == 1) {
            for (int i = 0; i < tableModel.getColumnsPref().size(); i++) {
                ColumnPref col = tableModel.getColumnsPref().get(i);
                if (col.isTouched) {
                    oldIndex = i;
                    oldName = col.getName();
                }
            }
        }
        if (selectColumnCount != 1) {
            renameColumnListener.renameColumn(oldName, tableModel.getColumnsPref().get(oldIndex).getName(), oldIndex);
        }
    }

    public ColumnPref getSelectColumn(TableModel tableModel) {
        for (ColumnPref col : tableModel.getColumnsPref()) {
            if (col.isTouched) {
                return col;
            }
        }
        return null;
    }

    public void deleteSelectedColumn(TableModel tableModel) {
        if (tableModel.getColumnsPref().size() <= selectColumnCount)
            return;
        for (int i = 0; i < tableModel.getColumnsPref().size(); i++) {
            if (tableModel.getColumnsPref().get(i).isTouched) {
                tableModel.getColumnsPref().remove(i);
                i--;
            }
        }
        for (ArrayList<Cell> stroke : tableModel.getEntries()) {
            for (int i = 0; i < stroke.size(); i++) {
                if (stroke.get(i).isTouchedStrCol) {
                    stroke.remove(i);
                    i--;
                }
            }
        }
        selectColumnCount = 0;
    }
}
