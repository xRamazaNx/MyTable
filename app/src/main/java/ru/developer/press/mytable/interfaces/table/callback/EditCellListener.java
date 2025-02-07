package ru.developer.press.mytable.interfaces.table.callback;

public interface EditCellListener {

    void cellEdit(String oldText, String newText, long oldDate, long newDate, int[] index);
}
