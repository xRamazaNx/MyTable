package ru.developer.press.mytable.table.model;

import java.util.Calendar;


public class TablePrefs {
    private int category;//
    private boolean isFill;//
    private String dateCreated;
    private String dateModify;
    private boolean lockHeightCells;
    private int dateType;


    public TablePrefs (){
        //дата создания
        dateCreated = String.valueOf(Calendar.getInstance().getTimeInMillis());
        //в дате изминения так же дата создания
        dateModify = dateCreated;
        //и ид таблицы будет дата создания

        category = 0;
        dateType = 0;
    }

    public int getDateType() {
        return dateType;
    }

    public void setDateType(int dateType) {
        this.dateType = dateType;
    }

    public int getCategory() {
        return category;
    }

    public void setCategory(int category) {
        this.category = category;
    }

    public boolean getIsFill() {
        return isFill;
    }

    public void setIsFill(boolean isFill) {
        this.isFill = isFill;
    }
    public String getDateCreated() {
        return dateCreated;
    }

    public void setDateCreated(String dateCreated) {
        this.dateCreated = dateCreated;
    }

    public String getDateModify() {
        return dateModify;
    }

    public void setDateModify(String dateModify) {
        this.dateModify = dateModify;
    }

    public boolean getLockHeightCells() {
        return lockHeightCells;
    }

    public void setLockHeightCells(boolean lockHeightCells) {
        this.lockHeightCells = lockHeightCells;
    }
}
