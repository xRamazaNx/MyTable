package ru.developer.press.mytable.interfaces.table.callback;

// калбек для настроек ширины таблицы
public interface WidthListener {
    void setWidthCell(int dx); //отклонение от начально заданного
    void setWidthForScreen();
    int getGeneralWidth();
    void onDismiss();

    void stopTracking();
}
