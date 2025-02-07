package ru.developer.press.mytable.interfaces.table;

import ru.developer.press.mytable.dialogs.DialogNameTable;
import ru.developer.press.mytable.interfaces.SendTableGetter;
import ru.developer.press.mytable.interfaces.table.callback.UpdateHeight;
import ru.developer.press.mytable.interfaces.table.callback.WidthListener;
import ru.developer.press.mytable.interfaces.table.callback.PrefCellsListener;
import ru.developer.press.mytable.interfaces.table.callback.SettingTableListener;

public interface TableActivityInterface {
    // методы для обратных вызовов от презентера
    void showEditCellWindow(int typeCell, String text);

    void showMenuOfHeaders();

    void showDefaultMenu();

    void invalidate();

    void hideEditCellWindow();

    void showMenuOfColumns(boolean isEditColumn, int columnCount);

    void showEditHeightCells(int heightCells, UpdateHeight updateHeight);

    void showEditWidthCells(int widthHeader, WidthListener widthListener);

    void showSettingCell(PrefCellsListener settColumns);

    void showDatePicker(long date);

    void showSettingTable(SettingTableListener setting);

    void showMenuOfCells();

//    int getTableWidth();

    void updateTableForToolbar(String newName);

    void showRenameDialog(String openName, DialogNameTable.OnButtonClick buttonClick);

    void showSendDialog(SendTableGetter sendTableGetter);

    void tableOpened();
}
