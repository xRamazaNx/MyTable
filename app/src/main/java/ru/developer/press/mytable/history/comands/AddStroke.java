package ru.developer.press.mytable.history.comands;

import java.util.ArrayList;

import ru.developer.press.mytable.history.Command;
import ru.developer.press.mytable.model.Cell;
import ru.developer.press.mytable.model.TableModel;

public class AddStroke extends Command {

    private ArrayList<Cell> stroke;
    public int index;

    public AddStroke(int index, ArrayList<Cell> stroke) {
        this.stroke = stroke;
        this.index = index;
    }

    @Override
    public void undo(TableModel tableModel) {
        tableModel.getEntries().remove(stroke);
//        super.undo(tableModel);
        historyUpdateListener.undo(null);
    }

    @Override
    public void redo(TableModel tableModel) {
        for (Cell cell : stroke) {
            cell.isTouchedStrCol = false;
        }
        tableModel.getEntries().add(index, stroke);
//        super.redo(tableModel);
        historyUpdateListener.redo(null);
    }

    @Override
    public String description(String description) {
        return description;
    }
}