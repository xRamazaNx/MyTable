package ru.developer.press.mytable.interfaces;

import android.graphics.Canvas;

import ru.developer.press.mytable.helpers.Coordinate;

public interface TableViewListener {

    void draw(Canvas canvas, Coordinate coordinate);

    void scrollBy(float distanceX, float distanceY);

    void scrollTo(int x, int y);

    boolean touchCoordinate(float x, float y);

    void eventUp();

    void click(float v, float v1, Coordinate coordinate);

    void moveSelector(float x, float y, Coordinate coordinate);

    int getTableWidth();

    int getTableHeight();
}
