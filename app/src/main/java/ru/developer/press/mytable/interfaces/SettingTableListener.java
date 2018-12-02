package ru.developer.press.mytable.interfaces;

import ru.developer.press.mytable.model.TableModel;

public abstract class SettingTableListener {
    public int lockHeight = 0;

    public SettingTableListener(TableModel tableModel) {
        lockHeight = tableModel.getLockHeightCells();
    }

    public abstract void setLockHeight(int lockHeight);
}
