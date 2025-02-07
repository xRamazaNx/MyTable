package ru.developer.press.mytable.interfaces.table;

import ru.developer.press.mytable.helpers.Coordinate;

public interface TableScroller {

    void scrollToCell(Coordinate coordinate);
    void scrollToColumn(Coordinate coordinate);
    void scrollToStroke(Coordinate coordinate);
    void scrollToEndIfOutside(int width, int height);
}
