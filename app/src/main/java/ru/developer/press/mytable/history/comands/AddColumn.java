package ru.developer.press.mytable.history.comands;

import java.util.List;

import ru.developer.press.mytable.history.Command;
import ru.developer.press.mytable.model.Cell;
import ru.developer.press.mytable.model.ColumnPref;
import ru.developer.press.mytable.model.TableModel;

public class AddColumn extends Command {

    private ColumnPref columnPref;
    private List <Cell> cellsofColumn;
    public int index;

    public AddColumn(int index, ColumnPref columnPref, List <Cell> cellsofColumn) {
        this.cellsofColumn = cellsofColumn;
        this.columnPref = columnPref;
        this.index = index;
    }

    @Override
    public void undo(TableModel tableModel) {
        tableModel.getColumnsPref().remove(columnPref);
        for (int i = 0; i < tableModel.getEntries().size(); i++) {
            tableModel.getEntries().get(i).remove(index);
        }
        historyUpdateListener.undo(null);

    }

    @Override
    public void redo(TableModel tableModel) {
        columnPref.isTouched = false;
        tableModel.getColumnsPref().add(index, columnPref);
        for (int i = 0; i < tableModel.getEntries().size(); i++) {
            tableModel.getEntries().get(i).add(index, cellsofColumn.get(i));
        }
//        super.redo(tableModel);
        historyUpdateListener.redo(null);

    }

    @Override
    public String description(String description) {
        return description;
    }
}