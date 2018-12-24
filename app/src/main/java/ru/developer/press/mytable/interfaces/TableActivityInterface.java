package ru.developer.press.mytable.interfaces;

import android.view.View;

import ru.developer.press.mytable.helpers.TableViewCoordinate;
import ru.developer.press.mytable.table.model.Column;

public interface TableActivityInterface {
    // методы для обратных вызовов от презентера
    void showEditCellWindow(int typeCell, String text);

//    void setText(String s);

    void scrollTableBy(float distanceX, float distanceY, int tableWidth, int tableHeight);

    void scrollTableTo(int x, int y);

    void showMenuOfHeaders(boolean isEditHeader);

    void showDefaultMenu();

    void invalidate();

    void hideEditCellWindow();

    void showMenuOfColumns(boolean isEditColumn, int columnCount);

    void showRenameColumnWin(Column column);

    void showEditHeightCellsWin(View view, int heightCells, UpdateHeight updateHeight);

    void showEditWidthCellsWin(View view, UpdateWidth updateWidth);

    void showSettingCellWin(PrefCellsListener settColumns);

    void showDatePickerWin(long date);

    void showSettingTableWin(SettingTableListener setting);

    void scrollToSelector(float x, float y, float widthTable, float heightTable, int widthHeader, int heightColumn);

    void showMenuOfCells();

    TableViewCoordinate getTableViewCoordinate ();
}
