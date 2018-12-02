package ru.developer.press.mytable.interfaces;

public interface CellEditListener {

    void cellEdit(String oldText, String newText, long oldDate, long newDate, int[] index);
}
