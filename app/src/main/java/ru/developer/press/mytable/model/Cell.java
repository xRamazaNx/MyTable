package ru.developer.press.mytable.model;

public class Cell extends CellAbstract{

    public int column;
    public String text;
    public float sizeFont;
    public int styleFont;
    public int colorFont;
    public int colorBack;
    public boolean isTouchedStrCol;
    public int type;
    public long date;

    public Cell(Cell other) {
        this.startX = other.startX;
        this.endX = other.endX;
        this.startY = other.startY;
        this.endY = other.endY;
        this.width = other.width;
        this.height = other.height;
        this.column = other.column;
        this.text = other.text;
        this.sizeFont = other.sizeFont;
        this.styleFont = other.styleFont;
        this.colorFont = other.colorFont;
        this.colorBack = other.colorBack;
        this.type = other.type;
        this.date = other.date;
    }

    public Cell() {
        text = "";
        sizeFont = 14;
    }

}
