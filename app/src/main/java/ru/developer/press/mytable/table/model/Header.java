package ru.developer.press.mytable.table.model;

import java.util.ArrayList;


public class Header extends CellAbstract {
    public int heightStroke;
    private ArrayList<Cell> stroke;

    @Override
    protected int getHeight() {
        return heightStroke;
    }

    public Header() {
        stroke = new ArrayList<>();
    }

    public Header copyPrefs(Header header) {
        super.copyPrefs(header);
        return this;
    }

    public ArrayList<Cell> getCells() {
        return stroke;
    }

    public Cell getCell(int i) {
        return stroke.get(i);
    }

    public void select(){
        super.select();
        for (Cell cell : stroke) {
            cell.select();
        }
    }
    public void unSelect(){
        super.unSelect();
        for (Cell cell : stroke) {
            cell.unSelect();
        }
    }
}
