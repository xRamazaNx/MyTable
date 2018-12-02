package ru.developer.press.mytable.interfaces;

import java.io.Serializable;

public interface HistoryUpdateListener extends Serializable{

    public void undo(Object argument);
    public void redo(Object argument);
}
