package ru.developer.press.mytable.interfaces.table.callback;

import ru.developer.press.mytable.model.TablePrefs;

// калбек для настроек таблицы
public abstract class SettingTableListener {
    public boolean lockHeight;
    public boolean lockAlwaysFitToScreen;
    public boolean totalAmountEnable;

    public SettingTableListener(TablePrefs tablePrefs) {
        lockHeight = tablePrefs.isLockHeightCells();
        lockAlwaysFitToScreen = tablePrefs.isLockAlwaysFitToScreen();
        totalAmountEnable = tablePrefs.isTotalAmountEnable();
    }

    public abstract void setLockHeight(boolean lockHeight);
    public abstract void setLockAlwaysFitToScreen(boolean lockAlwaysFitToScreen);
    public abstract void setTotalAmountEnable(boolean lockAlwaysFitToScreen);
}
