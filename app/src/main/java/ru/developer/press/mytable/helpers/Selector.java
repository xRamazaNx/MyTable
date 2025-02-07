package ru.developer.press.mytable.helpers;

import android.content.Context;

import ru.developer.press.mytable.table.TablePresenter;

public class Selector extends Coordinate {
    protected final int grabSize;
    protected float selectStartX = 0;
    protected float selectEndX = 0;
    protected float selectStartY = 0;
    protected float selectEndY = 0;

    protected boolean up;
    protected boolean right;
    protected boolean down;
    protected boolean left;
    protected float outOffset;
    protected boolean isTouchMove;

    public Selector(Context context) {
        grabSize = StaticMethods.convertDpToPixels(24, context);
        outOffset = grabSize / 2;
    }

    protected void setSelectCoordinate(float sX, float eX, float sY, float eY) {

        selectStartX = sX;
        selectEndX = eX;
        selectStartY = sY;
        selectEndY = eY;

        setBounds(selectStartX, selectEndX, selectStartY, selectEndY);
    }


    public void nullify() {

        selectStartX = 0;
        selectEndX = 0;
        selectStartY = 0;
        selectEndY = 0;

        startX = 0;
        startY = 0;
        endX = 0;
        endY = 0;

        up = false;
        down = false;
        right = false;
        left = false;
    }


    protected void moveTo(int x, int y) {
        if (up)
            startY = y;

        if (right)
            endX = x;

        if (left)
            startX = x;

        if (down)
            endY = y;

        if (endX < startX) {
            float tX = startX;
            startX = endX;
            endX = tX;

            boolean tb = left;
            left = right;
            right = tb;

        }
        if (endY < startY) {
            float tY = startY;
            startY = endY;
            endY = tY;

            boolean tb = up;
            up = down;
            down = tb;
        }

    }

    public void moveCoordinateToSelect() {
        if (!isTouchMove) return;
        setBounds(selectStartX, selectEndX, selectStartY, selectEndY);
        isTouchMove = false;
    }

    protected boolean findTouchZone(float touchX, float touchY, TablePresenter.SelectMode selectMode) {
        float innerLeftDist = selectStartX + (grabSize);
        float outLeftDist = selectStartX - outOffset;

        float innerUpDist = selectStartY + (grabSize);
        float outUpDist = selectStartY - outOffset;

        float innerRightDist = selectEndX - (grabSize);
        float outRightDist = selectEndX + outOffset;

        float innerDownDist = selectEndY - grabSize;
        float outDownDist = selectEndY + outOffset;

        if (selectMode == TablePresenter.SelectMode.row){
            outRightDist = touchX + 100;
            outLeftDist = touchX - 100; // просто пусть 100 будет
        }
        else if (selectMode == TablePresenter.SelectMode.column){
            outDownDist = touchY + 100;
            outUpDist = touchY - 100;
        }

        left = touchX < innerLeftDist && touchX > outLeftDist // если по х нажали в диапозоне в левой стороне
                && touchY < outDownDist && touchY > outUpDist; //  и если по не вышли выше или ниже

        right = touchX > innerRightDist && touchX < outRightDist
                && touchY < outDownDist && touchY > outUpDist;

        up = touchY < innerUpDist && touchY > outUpDist
                && touchX < outRightDist && touchX > outLeftDist;

        down = touchY > innerDownDist && touchY < outDownDist
                && touchX < outRightDist && touchX > outLeftDist;

        if (down && up) up = false;
        if (left && right) left = false;

        if (selectMode == TablePresenter.SelectMode.row) {
            left = false;
            right = false;
        } else if (selectMode == TablePresenter.SelectMode.column) {
            up = false;
            down = false;
        }
        isTouchMove = left || right || up || down;
        return isTouchMove;
    }
}
