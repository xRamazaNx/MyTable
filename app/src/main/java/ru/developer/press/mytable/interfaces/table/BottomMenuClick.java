package ru.developer.press.mytable.interfaces.table;

import android.view.View;

import ru.developer.press.mytable.helpers.BottomMenuControl;

public interface BottomMenuClick {
    public void addColumn(BottomMenuControl.AddButtonEnum addButtonEnum);
    public void addStroke(BottomMenuControl.AddButtonEnum addButtonEnum);

    public void setWidth(View view);
    public void setHeightCells();

    void settingTable();
}
