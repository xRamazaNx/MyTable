package ru.developer.press.mytable.table.builders;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.support.v4.content.ContextCompat;
import android.text.Layout;
import android.text.StaticLayout;
import android.text.TextPaint;

import java.util.ArrayList;

import ru.developer.press.myTable.R;
import ru.developer.press.mytable.StaticMetods;
import ru.developer.press.mytable.model.Cell;
import ru.developer.press.mytable.model.CellAbstract;
import ru.developer.press.mytable.model.TableModel;

public class HeaderBuilder {
    private final Paint paintTouchCell;
    private final Paint paintLine;
    private final TextPaint textPaint;
    private float widthMap;
    public int selectHeaderCount = 0;

    public HeaderBuilder(Context context) {
        float sizeTextTemp = StaticMetods.convertSpToPixels(14, context);
        paintLine = new Paint();
        paintLine.setColor(context.getResources().getColor(R.color.gray));

        paintTouchCell = new Paint();
        paintTouchCell.setColor(context.getResources().getColor(R.color.color_select_stroke_column));

        textPaint = new TextPaint();
        textPaint.setColor(ContextCompat.getColor(context, R.color.gray));
//        textPaint.setColor(Color.parseColor(cell.colorFont));
        textPaint.setTextSize(sizeTextTemp);
        textPaint.setAntiAlias(true);
//        textPaint.setTypeface(setTypeFace(cell.styleFont));

        widthMap = StaticMetods.convertDpToPixels(32, context);
    }

    public void init(TableModel tableModel) {
        ArrayList<Integer> index = new ArrayList<>();
        ArrayList<Cell> headers = tableModel.getHeaders();
        for (int i = 0; i < headers.size(); i++) {
            if (headers.get(i).isTouched)
                index.add(i);
        }
        headers.clear();
        int endY = 0;
        int startY;
        for (int i = 0; i < tableModel.getEntries().size(); i++) {
            int heightStroke = (int) tableModel.getEntries().get(i).get(0).height;
            endY += heightStroke;
            startY = endY - heightStroke;

            Cell cell = new Cell();
            cell.text = String.valueOf(i + 1);
            cell.setBounds(0, widthMap, startY, endY);
            headers.add(cell);
        }
        for (int in : index) {
            headers.get(in).isTouched = true;
        }
    }

    public void selectHeader(int index, TableModel tableModel) {
        Cell header = tableModel.getHeaders().get(index);
        if (header.isTouched) {
            header.isTouched = false;
            selectHeaderCount--;
        } else {
            selectHeaderCount++;
            header.isTouched = true;
        }

        // выделяем ячейки в этой строке или наоборот
        for (Cell cellIsInHeader : tableModel.getEntries().get(index)) {
            cellIsInHeader.isTouchedStrCol = header.isTouched;
        }
    }

    public void drawHeaders(Canvas canvas, CellAbstract coordinateDraw, TableModel tableModel) {
        for (Cell header : tableModel.getHeaders()) {
            if (header.endY < coordinateDraw.startY || header.startY > coordinateDraw.endY)
                continue;
            canvas.save();

            if (header.isTouched) {
                canvas.drawRect(header.startX, header.startY + 1, header.endX, header.endY, paintTouchCell);
            }
            drawText(canvas, header);
            canvas.restore();

            canvas.drawLine(0, header.endY, widthMap, header.endY, paintLine);
        }
    }

    private void drawText(Canvas canvas, Cell cell) {
        canvas.clipRect(cell.startX, cell.startY, cell.startX + cell.width, cell.startY + cell.height - 2);

        int xPos = (int) cell.startX + 2;
        int yPos = (int) cell.startY + 2;

        StaticLayout staticLayout = new StaticLayout(cell.text, textPaint, (int) cell.width - 4, Layout.Alignment.ALIGN_CENTER, 1, 0, false);

        float height = staticLayout.getHeight();
        if (height < cell.height) { // если высота текста меньше чем высота ячейки
            yPos = (int) (yPos + (cell.height / 2) - height / 2) - 2; // рассчет середины для позиции y
        }

        canvas.translate(xPos, yPos);
        staticLayout.draw(canvas);

    }

    public void unSelectAll(TableModel tableModel) {
        ArrayList<Cell> headers = tableModel.getHeaders();
        for (int i = 0; i < headers.size(); i++) {
            Cell header = headers.get(i);
            if (header.isTouched) {
                selectHeader(i, tableModel);
            }

        }
    }

    public void deleteHeaders(TableModel tableModel) {
        if (selectHeaderCount >= tableModel.getHeaders().size()) {
            return;
        }
        for (int i = 0; i < tableModel.getEntries().size(); i++) {
            if (tableModel.getHeaders().get(i).isTouched) {
                tableModel.getHeaders().remove(i);
                tableModel.getEntries().remove(i);
                i--;
            }
        }
        selectHeaderCount = 0;

    }
}
