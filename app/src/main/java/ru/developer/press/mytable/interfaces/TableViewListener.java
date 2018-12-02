package ru.developer.press.mytable.interfaces;

import android.graphics.Canvas;

import ru.developer.press.mytable.model.CellAbstract;

public interface TableViewListener {
    void cellClick(float x, float y);

    void drawCells(Canvas canvas, CellAbstract coordinate);

    void scrollBy(int x, int y);

    void scrollTo(int x, int y);

    int getTableWidth();

    int getTableHeight();


}
