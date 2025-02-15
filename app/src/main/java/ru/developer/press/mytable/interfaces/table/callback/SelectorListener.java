package ru.developer.press.mytable.interfaces.table.callback;

import ru.developer.press.mytable.helpers.Coordinate;

public interface SelectorListener {
    void selectZone(float startX, float endX, float startY, float endY);
    void scrollToSelectorOffside(Coordinate coordinate);
}
