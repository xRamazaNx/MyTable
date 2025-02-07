package ru.developer.press.mytable.model;

import android.graphics.Color;

import ru.developer.press.mytable.helpers.Formula;

public class Cell extends CellAbstract {

    public transient int indexColumn;
    public transient int inputType;
    public transient int indexRow;
    public long date;
    public String valueFromFormula = "";

    public Cell() {
        super();
        pref.colorBack = Color.WHITE;
        text = "";
        height = 70;
        inputType = 0;
    }

    @Override
    protected int getHeight() {
        return (int) height;
    }


    public Cell copyPrefs(Cell cell) {
        super.copyPrefs(cell);
        return this;
    }

    public double getNumber() {
        if (inputType == 3)
            return Formula.parseStringToNumber(valueFromFormula);
        return Formula.parseStringToNumber(text);
    }
}
