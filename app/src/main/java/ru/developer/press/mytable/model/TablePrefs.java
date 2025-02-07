package ru.developer.press.mytable.model;

import java.util.Calendar;


public class TablePrefs {
    private transient boolean visibleTotalAmount = true;

    private int category;//
    private String dateCreated;
    private String dateModify;
    private boolean lockHeightCells;
    private boolean lockAlwaysFitToScreen;
    private int dateType;
    private boolean totalAmountEnable = false;

    public Row totalAmount;


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

    public boolean isLockHeightCells() {
        return lockHeightCells;
    }

    public void setLockHeightCells(boolean lockHeightCells) {
        this.lockHeightCells = lockHeightCells;
    }

    public boolean isLockAlwaysFitToScreen() {
        return lockAlwaysFitToScreen;
    }

    public void setLockAlwaysFitToScreen(boolean lockAlwaysFitToScreen) {
        this.lockAlwaysFitToScreen = lockAlwaysFitToScreen;
    }
    public void setVisibleTotalAmount(boolean visible) {
        this.visibleTotalAmount = visible;
    }

    public boolean isVisibleTotalAmount() {
        return visibleTotalAmount;
    }

    public boolean isTotalAmountEnable() {
        return totalAmountEnable;
    }

    public void setTotalAmountEnable(boolean totalAmountEnable) {
        this.totalAmountEnable = totalAmountEnable;
    }
}
