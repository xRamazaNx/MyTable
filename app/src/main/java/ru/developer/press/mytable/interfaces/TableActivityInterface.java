package ru.developer.press.mytable.interfaces;

import android.view.View;

import ru.developer.press.mytable.model.ColumnPref;

public interface TableActivityInterface {
    // методы для обратных вызовов от презентера
    void showEditCellWindow(int typeCell, String text);

//    void setText(String s);

    void scrollTableBy(int x, int y);

    void scrollTableTo(int x, int y);

    void showMenuOfHeaders(boolean isEditHeader);

    void showDefaultMenu();

    void invalidate();
    void hideEditCellWindow();
    void showMenuOfColumns(boolean isEditColumn, int columnCount);

    void updateColumnHeight(float height);

    void showRenameColumnWin(ColumnPref columnPref);

    void showEditHeightCellsWin(View view, int heightCells, UpdateHeight updateHeight);

    void showEditWidthCellsWin(View view, UpdateWidth updateWidth);

    void showSettingColumnWin(SettColumnsListener settColumns);

    void showDatePickerWin(long date);

    void showSettingTableWin(SettingTableListener setting);
}
