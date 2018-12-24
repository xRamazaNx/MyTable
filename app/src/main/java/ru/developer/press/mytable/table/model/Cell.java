package ru.developer.press.mytable.table.model;

import android.graphics.Color;

public class Cell extends CellAbstract {

    public int columnIndex;
    public long date;
    public int strokeIndex;

    public Cell() {
        super();
        text = "";
        sizeFont = 14;
        colorFont = Color.parseColor("#181818");
        height = 70;
        colorBack = Color.WHITE;
    }

    @Override
    protected int getHeight() {
        return (int) height;
    }


    public Cell copyPrefs(Cell cell) {
        super.copyPrefs(cell);
        return this;
    }
}
