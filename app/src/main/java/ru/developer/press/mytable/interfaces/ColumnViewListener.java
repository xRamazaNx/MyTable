package ru.developer.press.mytable.interfaces;

import android.graphics.Canvas;

import ru.developer.press.mytable.model.CellAbstract;

public interface ColumnViewListener {

    void columnClick(float x);

    void drawColumns(Canvas canvas, CellAbstract coordinateDraw);
}
