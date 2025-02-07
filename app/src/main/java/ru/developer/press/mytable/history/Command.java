package ru.developer.press.mytable.history;

import java.io.Serializable;

import ru.developer.press.mytable.interfaces.table.callback.HistoryUpdateListener;
import ru.developer.press.mytable.model.TableModel;

public abstract class Command implements Serializable{
    public abstract String description (String description);
    public HistoryUpdateListener historyUpdateListener;

    public abstract void undo(TableModel tableModel);
    public abstract void redo(TableModel tableModel);
    public void setOnHistoryListener(HistoryUpdateListener historyUpdateListener) {
        this.historyUpdateListener = historyUpdateListener;
    }
}
