package ru.developer.press.mytable.interfaces;

import android.view.View;

import ru.developer.press.mytable.views.ControllerBottomMenu;

public interface BottomMenuClick {
    public void addColumn(ControllerBottomMenu.AddButtonEnum addButtonEnum);
    public void addStroke(ControllerBottomMenu.AddButtonEnum addButtonEnum);

    public void setWidth(View view);
    public void setHeightCells(View view);

    void settingTable();
}
