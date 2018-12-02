package ru.developer.press.mytable.interfaces;

import android.graphics.Canvas;

import ru.developer.press.mytable.model.CellAbstract;

public interface HeaderViewListener {
    void headerClick(float y);

    void drawHeaders(Canvas canvas, CellAbstract coordinateDraw);

}
