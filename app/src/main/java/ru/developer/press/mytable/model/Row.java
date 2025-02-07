package ru.developer.press.mytable.model;

import java.util.ArrayList;


public class Row extends CellAbstract {
    // нужна для обознаяения высоты
    // независимо от того какие настройки включены
    // это просто высота на данный момент у строки
    public transient int heightStroke;
    public float height;
    private ArrayList<Cell> cells;

    public Row() {
        super();
        height = super.height;
        cells = new ArrayList<>();
    }

    // каждый из вариантов (селл или колумн) выдает свое значение
    // у хеадер это heightStroke у остальных просто height, почему?
    // потому что у хеадер height это независимая высота ячейки а у остальных назначаемая при init()
    @Override
    protected int getHeight() {
        return heightStroke;
    }

    public Row copyPrefs(Row row) {
        height = row.height;
        heightStroke = row.heightStroke;
        super.copyPrefs(row);
        return this;
    }

    public ArrayList<Cell> getCells() {
        return cells;
    }

    public Cell getCellAtIndex(int i) {
        return cells.get(i);
    }

    public void select() {
        super.select();
        for (Cell cell : cells) {
            cell.select();
        }
    }

    public void unSelect() {
        super.unSelect();
        for (Cell cell : cells) {
            cell.unSelect();
        }
    }
}
