package ru.developer.press.mytable.history;

import java.io.Serializable;

import ru.developer.press.mytable.interfaces.HistoryUpdateListener;
import ru.developer.press.mytable.table.model.TableModel;

public abstract class Command implements Serializable{
    public abstract String description (String description);
    public HistoryUpdateListener historyUpdateListener;

    public void undo(TableModel tableModel){
    }
    public void redo(TableModel tableModel){
    }
    public void setOnHistoryListener(HistoryUpdateListener historyUpdateListener) {
        this.historyUpdateListener = historyUpdateListener;
    }
}
