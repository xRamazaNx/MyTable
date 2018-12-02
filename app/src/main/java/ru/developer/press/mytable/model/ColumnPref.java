package ru.developer.press.mytable.model;

import android.graphics.Color;

import java.util.Random;

import ru.developer.press.mytable.database.SchemaDB;

public class ColumnPref extends CellAbstract{

    private String nameIdColumn;
    private String name; //
    private int inputType; //
    private String function; //
    private int widthColumn; //

    private int textSizeCell; //
    private int textStyleCell; //
    private int textColorCell; //
    private int colorCellRect; //

    private int textSizeTitle; //
    private int textStyleTitle; //
    private int textColorTitle; //
    private int colorTitleRect; //

    public ColumnPref(ColumnPref other) {
        this.nameIdColumn = other.nameIdColumn;
        this.name = other.name;
        this.inputType = other.inputType;
        this.function = other.function;
        this.widthColumn = other.widthColumn;
        this.textSizeCell = other.textSizeCell;
        this.textStyleCell = other.textStyleCell;
        this.textColorCell = other.textColorCell;
        this.colorCellRect = other.colorCellRect;
        this.textSizeTitle = other.textSizeTitle;
        this.textStyleTitle = other.textStyleTitle;
        this.textColorTitle = other.textColorTitle;
        this.colorTitleRect = other.colorTitleRect;

    }

    public void copyColumn (ColumnPref copy){
        this.nameIdColumn = copy.nameIdColumn;
        this.name = copy.name;
        this.inputType = copy.inputType;
        this.function = copy.function;
        this.widthColumn = copy.widthColumn;
        this.textSizeCell = copy.textSizeCell;
        this.textStyleCell = copy.textStyleCell;
        this.textColorCell = copy.textColorCell;
        this.colorCellRect = copy.colorCellRect;
        this.textSizeTitle = copy.textSizeTitle;
        this.textStyleTitle = copy.textStyleTitle;
        this.textColorTitle = copy.textColorTitle;
        this.colorTitleRect = copy.colorTitleRect;
    }

    public ColumnPref() {


        name = "Новый столбец";
        int randomn = new Random().nextInt();
        if (randomn < 0) randomn = -randomn;
        nameIdColumn = SchemaDB.Table.Cols.NAME_FOR_COLUMN + randomn;

        inputType = 0;
        function = "0"; //  значит нет функций
        widthColumn = 150;
        textSizeCell = 14; // позиция в списке размеров шрифта
        textStyleCell = 0;
        textColorCell = Color.parseColor("#181818");
        colorCellRect = Color.WHITE;
        textSizeTitle = 16;
        textStyleTitle = 1;
        textColorTitle = Color.parseColor("#181818");
        colorTitleRect = Color.parseColor("#f1f1f1");

    }


    public String getNameIdColumn() {
        return nameIdColumn;
    }

    public void setNameIdColumn(String nameIdColumn) {
        this.nameIdColumn = nameIdColumn;
    }

    public String getName() {
        return name;
    }

    public ColumnPref setName(String name) {
        this.name = name;
        return this;
    }

    public int getInputType() {
        return inputType;
    }

    public void setInputType(int inputType) {
        this.inputType = inputType;
    }

    public String getFunction() {
        return function;
    }

    public void setFunction(String function) {
        this.function = function;
    }

    public int getWidthColumn() {
        return widthColumn;
    }

    public void setWidthColumn(int widthColumn) {
        this.widthColumn = widthColumn;
    }

    public int getTextSizeCell() {
        return textSizeCell;
    }

    public void setTextSizeCell(int textSizeCell) {
        this.textSizeCell = textSizeCell;
    }

    public int getTextStyleCell() {
        return textStyleCell;
    }

    public void setTextStyleCell(int textStyleCell) {
        this.textStyleCell = textStyleCell;
    }

    public int getTextColorCell() {
        return textColorCell;
    }

    public void setTextColorCell(int textColorCell) {
        this.textColorCell = textColorCell;
    }

    public int getTextSizeTitle() {
        return textSizeTitle;
    }

    public void setTextSizeTitle(int textSizeTitle) {
        this.textSizeTitle = textSizeTitle;
    }

    public int getTextStyleTitle() {
        return textStyleTitle;
    }

    public void setTextStyleTitle(int textStyleTitle) {
        this.textStyleTitle = textStyleTitle;
    }

    public int getTextColorTitle() {
        return textColorTitle;
    }

    public void setTextColorTitle(int textColorTitle) {
        this.textColorTitle = textColorTitle;
    }

    public int getColorCellRect() {
        return colorCellRect;
    }

    public void setColorCellRect(int colorCellRect) {
        this.colorCellRect = colorCellRect;
    }

    public int getColorTitleRect() {
        return colorTitleRect;
    }

    public void setColorTitleRect(int colorTitleRect) {
        this.colorTitleRect = colorTitleRect;
    }
}
