package ru.developer.press.mytable.table;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.drawable.Drawable;

import ru.developer.press.myTable.R;
import ru.developer.press.mytable.helpers.Coordinate;
import ru.developer.press.mytable.helpers.Selector;
import ru.developer.press.mytable.helpers.StaticMethods;
import ru.developer.press.mytable.interfaces.table.callback.SelectorListener;


public class Selection extends Selector {
    private Paint pant;
    private SelectorListener selectorListener;
    private float touchX;
    private float touchY;

    private Bitmap grabRoundMap;
    private float offXToCircle;
    private float offYToCircle;
    private boolean startYInOut; // вверх ушел за границы экрана или нет
    private boolean endYInOut; // низ ушел за границы экрана или нет
    private boolean startXInOut; // левая сторона ушел за границы экрана или нет
    private boolean endXInOut; // правая сторона ушел за границы экрана или нет
//    private Coordinate scrollToSelectorCoordinate;

    Selection(Context context, SelectorListener selectorListener) {
        super(context);
        this.selectorListener = selectorListener;
//        scrollToSelectorCoordinate = new Coordinate();

        pant = new Paint();
        pant.setColor(context.getResources().getColor(R.color.color_select_cell));
        pant.setStyle(Paint.Style.STROKE);
        pant.setStrokeWidth(StaticMethods.convertDpToPixels(1.7F, context));

        Drawable drawable = context.getResources().getDrawable(R.drawable.grab_oval);

        // рисуем на битмап кружочек
        grabRoundMap = Bitmap.createBitmap(drawable.getIntrinsicWidth(), drawable.getIntrinsicHeight(), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(grabRoundMap);
        drawable.setBounds(0, 0, canvas.getWidth(), canvas.getHeight());
        drawable.draw(canvas);

        offXToCircle = (grabRoundMap.getWidth() >> 1);
        offYToCircle = (grabRoundMap.getHeight() >> 1);
    }


    // когда печатаешь например или еще что, обновляем позицию ячейки чтоб она всегда оставалась на виду
    Coordinate getCoordinateScroll() {
        Coordinate coordinate = new Coordinate();
        coordinate.setBounds(selectStartX, selectEndX, selectStartY, selectEndY);
        return coordinate;
    }

    boolean isTouchInSelector(float x, float y, TablePresenter.SelectMode selectMode) {
        touchX = x;
        touchY = y;
        return findTouchZone(touchX, touchY, selectMode);
    }

    void moveSelector(float x, float y) {
        touchX = x;
        touchY = y;
        moveTo((int) touchX, (int) touchY);

        float sx = left ? startX : startXInOut ? selectStartX : startX;
        float ex = right ? endX : endXInOut ? selectEndX : endX;
        float sy = up ? startY : startYInOut ? selectStartY : startY;
        float ey = down ? endY : endYInOut ? selectEndY : endY;

        selectorListener.selectZone(sx, ex, sy, ey);
    }

    // перемещает координаты на края когда рисование квадрата выходит за пределы колон и хеадеров
    private void moveSelector(Coordinate coordinate, int headerOff, int columnOff) {

        if (!isTouchMove) {
            endX = selectEndX;
            startY = selectStartY;
            endY = selectEndY;
            startX = selectStartX;
        }

        startXInOut = true;
        endXInOut = true;
        startYInOut = true;
        endYInOut = true;
        if (selectStartY <= columnOff && startY <= columnOff) {
            startY = columnOff;
        } else if (selectStartY >= coordinate.endY && startY >= coordinate.endY)
            startY = coordinate.endY - 1;
        else startYInOut = false;

        if (selectEndY <= columnOff && endY <= columnOff)
            endY = columnOff + 1;
        else if (selectEndY >= coordinate.endY && endY >= coordinate.endY)
            endY = coordinate.endY;
        else endYInOut = false;


        if (selectStartX <= headerOff && startX <= headerOff)
            startX = headerOff;
        else if (selectStartX >= coordinate.endX && startX >= coordinate.endX)
            startX = coordinate.endX - 1;
        else startXInOut = false;

        if (selectEndX <= headerOff && endX <= headerOff)
            endX = headerOff + 1;
        else if (selectEndX >= coordinate.endX && endX >= coordinate.endX)
            endX = coordinate.endX;
        else endXInOut = false;

    }

    public void draw(Canvas canvas, Coordinate coordinate, int widthHeader, int columnHeight, TablePresenter.SelectMode selectMode) {
        moveSelector(coordinate, widthHeader + (int) coordinate.startX, (int) (columnHeight + coordinate.startY));

        if (selectMode == TablePresenter.SelectMode.row) {
            startX = coordinate.startX + 2;
            endX = coordinate.endX - 2;
        } else if (selectMode == TablePresenter.SelectMode.column) {
            startY = coordinate.startY + 3;
            endY = coordinate.endY - 2;
        }

        canvas.drawRect(startX
                , startY
                , endX
                , endY
                , pant);
        canvas.drawBitmap(grabRoundMap, startX - offXToCircle, startY - offYToCircle, pant);
        canvas.drawBitmap(grabRoundMap, endX - offXToCircle, startY - offYToCircle, pant);
        canvas.drawBitmap(grabRoundMap, endX - offXToCircle, endY - offYToCircle, pant);
        canvas.drawBitmap(grabRoundMap, startX - offXToCircle, endY - offYToCircle, pant);

    }

    void setSelectionCoordinate(Coordinate selectorZone) {
        if (selectorZone == null)
            return;

        selectStartX = selectorZone.startX;
        selectEndX = selectorZone.endX;
        selectStartY = selectorZone.startY;
        selectEndY = selectorZone.endY;
    }

    void setCoordinateForSelectCell(float sX, float eX, float sY, float eY) {
        setSelectCoordinate(sX, eX, sY, eY);
    }

    // пока не работает, над этим поработаем потом как ни будь
//    public void scrollToMoveCoordinate(Coordinate coordinate, int widthRows, int heightColumns) {
//        int columnOff = (int) (coordinate.startY + heightColumns);
//        int headerOff = (int) (coordinate.startX + widthRows);
//        int scrollDistance = 300;
//
//        scrollToSelectorCoordinate.startY = startY;
//        scrollToSelectorCoordinate.endX = endX;
//        scrollToSelectorCoordinate.startX = startX;
//        scrollToSelectorCoordinate.endY = endY;
//
//        if (startY < coordinate.startY + columnOff + scrollDistance && up)
//            scrollToSelectorCoordinate.startY -= scrollDistance/3;
//        else scrollToSelectorCoordinate.startY = endY;
//
//        if (endY > coordinate.endY - scrollDistance && down)
//            scrollToSelectorCoordinate.endY += scrollDistance/3;
//        else  scrollToSelectorCoordinate.endY = startY;
//
//        if (startX < coordinate.startX + headerOff + scrollDistance && left)
//            scrollToSelectorCoordinate.startX -= scrollDistance/3;
//        else scrollToSelectorCoordinate.startX = endX;
//
//        if (endX > coordinate.endX - scrollDistance && right)
//            scrollToSelectorCoordinate.endX += scrollDistance/3;
//        else scrollToSelectorCoordinate.endX = startX;
//
//        scrollToSelectorCoordinate.height = scrollToSelectorCoordinate.endY - scrollToSelectorCoordinate.startY;
//        scrollToSelectorCoordinate.width = scrollToSelectorCoordinate.endX - scrollToSelectorCoordinate.startX;
//
//        selectorListener.scrollToSelectorOffside(scrollToSelectorCoordinate);
//    }
}
