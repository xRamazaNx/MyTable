package ru.developer.press.mytable.history.comands;

import ru.developer.press.mytable.history.Command;
import ru.developer.press.mytable.model.TableModel;

public class HeightStroke extends Command {
    private int oldHeight;
    private int newHeight;

    public HeightStroke(int oldHeight, int newHeight){
        this.oldHeight = oldHeight;
        this.newHeight = newHeight;
    }

    @Override
    public void undo(TableModel tableModel) {
        tableModel.setHeightCells(oldHeight);
        historyUpdateListener.undo(null);
    }

    @Override
    public void redo(TableModel tableModel) {
        tableModel.setHeightCells(newHeight);
        historyUpdateListener.redo(null);
    }

    @Override
    public String description(String description) {
        return null;
    }

    public boolean isEdited() {

        return oldHeight != newHeight;
    }
}
