package ru.developer.press.mytable.model;

import android.graphics.Color;

import com.google.gson.annotations.SerializedName;

import java.util.Random;

import ru.developer.press.mytable.helpers.Formula;

public class Column extends CellAbstract {
    public transient int index;

    private String nameIdColumn;
    private int inputType; //
    private Formula formula; //
    @SerializedName("wc")
    private float widthColumn;

    public float getWidth() {
        return widthColumn;
    }

    public void setWidth(float widthColumn) {
        this.widthColumn = widthColumn;
        width = widthColumn;
    }

    public Column copyColumn(Column copy) {
        copyPrefs(copy);

        inputType = copy.inputType;
        formula = new Formula().copy(copy.formula);
        widthColumn = copy.widthColumn;
//        type = copy.type;
        return this;
    }

    public Column() {
        super();
        text = "Новый столбец";
        long random = new Random().nextLong();
        if (random < 0) random = -random;
        nameIdColumn = "column_" + random;

        inputType = 0;
        width = 150;
        widthColumn = width;
        pref.sizeFont = 16; // позиция в списке размеров шрифта
        pref.colorFont = Color.parseColor("#181818");

        formula =  new Formula();
        formula.columnID = nameIdColumn;
    }

    public String getNameIdColumn() {
        return nameIdColumn;
    }

    public int getInputType() {
        return inputType;
    }

    public void setInputType(int inputType) {
        this.inputType = inputType;
    }

    public Formula getFormula() {
        return formula;
    }

    public void setFormula(Formula formula) {
        formula.columnID = nameIdColumn;
        this.formula = formula;
    }

    public boolean isNotEquals(Column col) {
        boolean isFormulaNotEqual = !formula.equals(col.formula);
        return super.isNotEquals(col) ||
                isFormulaNotEqual ||
                inputType != col.inputType;
    }

    @Override
    protected int getHeight() {
        return (int) height;
    }

    public void select(TableModel tableModel){
        super.select();
        for (int i = 0; i < tableModel.getRows().size(); i++) {
            Row row = tableModel.getRows().get(i);
            row.getCellAtIndex(index).select();
        }
    }
    public void unSelect(TableModel tableModel){
        super.unSelect();
        for (int i = 0; i < tableModel.getRows().size(); i++) {
            Row row = tableModel.getRows().get(i);
            row.getCellAtIndex(index).unSelect();
        }
    }

    public void setWidth(TableModel tableModel, int dx) {
        int tempWidth = (int) (widthColumn + dx);
        int paddingSize = pref.paddingRight + pref.paddingLeft;
        for (int j = 0; j < tableModel.getRows().size(); j++) {
            Cell cell = tableModel.getRows().get(j).getCellAtIndex(index);
            int paddingSizeCell = cell.pref.paddingRight + cell.pref.paddingLeft;
            if (paddingSize < paddingSizeCell)
                paddingSize = paddingSizeCell;
        }
        if (tempWidth < paddingSize)
            tempWidth = paddingSize + 2;
        setWidth(tempWidth);
    }
}
