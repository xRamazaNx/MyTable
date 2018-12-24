package ru.developer.press.mytable.interfaces;

import ru.developer.press.mytable.table.model.TablePrefs;

public abstract class SettingTableListener {
    public boolean lockHeight;
    public int variable;

    public SettingTableListener(TablePrefs tablePrefs) {
        lockHeight = tablePrefs.getLockHeightCells();
        variable = tablePrefs.getDateType();
    }

    public abstract void setLockHeight(boolean lockHeight);
    public abstract void setDateVariable(int variable);
}
