package ru.developer.press.mytable.interfaces.table.callback;

import android.graphics.Canvas;

import ru.developer.press.mytable.helpers.Coordinate;

public interface TableViewListener {

    void draw(Canvas canvas, Coordinate coordinate);

    boolean touchCoordinate(float x, float y);

    void eventUp();

    void click(float v, float v1, Coordinate coordinate);

    void moveSelector(float x, float y, Coordinate coordinate);

    int getTableWidth();

    int getTableHeight();

    void scrollToInTable();
}
