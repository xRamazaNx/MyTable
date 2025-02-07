package ru.developer.press.mytable.history.comands;

import ru.developer.press.mytable.history.Command;
import ru.developer.press.mytable.model.Row;
import ru.developer.press.mytable.model.TableModel;

public class AddStroke extends Command {

    private Row rowPref;
    public int index;

    public AddStroke(int index, Row rowPref) {
        this.rowPref = rowPref;
        this.index = index;
    }

    @Override
    public void undo(TableModel tableModel) {
        tableModel.getRows().remove(rowPref);
//        super.undo(tableModel);
        historyUpdateListener.undo(this);
    }

    @Override
    public void redo(TableModel tableModel) {
//        for (Cell cell : stroke) {
//            cell.isTouchedStrCol = false;
//        }
        tableModel.getRows().add(index, rowPref);
//        super.redo(tableModel);
        historyUpdateListener.redo(this);
    }

    @Override
    public String description(String description) {
        return description;
    }
}