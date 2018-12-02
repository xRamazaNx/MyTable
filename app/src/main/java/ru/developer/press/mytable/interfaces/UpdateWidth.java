package ru.developer.press.mytable.interfaces;

public interface UpdateWidth {
    void setWidthCell(int dx); //отклонение от начально заданного
    void setWidthForScreen(int tableWidth);
    int getGeneralWidth();
    void onDismiss();
}
