package ru.developer.press.mytable.interfaces;

import java.io.Serializable;

import ru.developer.press.mytable.history.Command;

public interface HistoryUpdateListener extends Serializable{

    public void undo(Command command);
    public void redo(Command command);
}
