package ru.developer.press.mytable.interfaces;

import ru.developer.press.mytable.model.Cell;
import ru.developer.press.mytable.model.ColumnPref;

public interface ScrollerTo {

    void scrollToCell(Cell cell);
    void scrollToColumn(ColumnPref columnPref);
    void scrollToStroke(Cell header);
    void scrollToEndIfOutside(int width, int height);
}
