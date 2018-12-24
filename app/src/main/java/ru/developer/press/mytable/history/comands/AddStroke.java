package ru.developer.press.mytable.history.comands;

import java.util.ArrayList;

import ru.developer.press.mytable.history.Command;
import ru.developer.press.mytable.table.model.Cell;
import ru.developer.press.mytable.table.model.Header;
import ru.developer.press.mytable.table.model.TableModel;

public class AddStroke extends Command {

    private Header headerPref;
    public int index;

    public AddStroke(int index, Header headerPref) {
        this.headerPref = headerPref;
        this.index = index;
    }

    @Override
    public void undo(TableModel tableModel) {
        tableModel.getHeaders().remove(headerPref);
//        super.undo(tableModel);
        historyUpdateListener.undo(this);
    }

    @Override
    public void redo(TableModel tableModel) {
//        for (Cell cell : stroke) {
//            cell.isTouchedStrCol = false;
//        }
        tableModel.getHeaders().add(index, headerPref);
//        super.redo(tableModel);
        historyUpdateListener.redo(this);
    }

    @Override
    public String description(String description) {
        return description;
    }
}