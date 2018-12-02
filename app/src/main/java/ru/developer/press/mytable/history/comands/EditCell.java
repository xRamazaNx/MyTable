package ru.developer.press.mytable.history.comands;

import ru.developer.press.mytable.history.Command;
import ru.developer.press.mytable.model.Cell;
import ru.developer.press.mytable.model.TableModel;

// нужно в момент до изменения подсунуть текущее положение а в момент когда закончилось редоктирование подсунуть измененную
public class EditCell extends Command {
    public final int[] index; // 0=entry 1=column
    public Cell oldCell;
    public Cell newCell;

    public EditCell(int[] index) {
        this.index = new int[2];
        this.index[0] = index[0];
        this.index[1] = index[1];
    }

    public void setOldCells(String oldText, long oldDate) {
        oldCell = new Cell();
        oldCell.text = oldText;
        oldCell.date = oldDate;
    }

    public void setNewCells(String newText, long newDate) {
        newCell = new Cell();
        newCell.text = newText;
        newCell.date = newDate;
    }

    @Override
    public void undo(TableModel tableModel) {
        Cell origin = tableModel.getEntries().get(index[0]).get(index[1]);
        origin.date = oldCell.date;
        origin.text = oldCell.text;
        historyUpdateListener.undo(index);
    }

    @Override
    public void redo(TableModel tableModel) {
        Cell origin = tableModel.getEntries().get(index[0]).get(index[1]);
        origin.date = newCell.date;
        origin.text = newCell.text;
        historyUpdateListener.redo(index);
    }

    @Override
    public String description(String description) {
        return description;
    }

}
