package ru.developer.press.mytable.history.comands;

import java.util.List;

import ru.developer.press.mytable.history.Command;
import ru.developer.press.mytable.model.Cell;
import ru.developer.press.mytable.model.Column;
import ru.developer.press.mytable.model.TableModel;

public class AddColumn extends Command {

    public int index;
    private Column column;
    private List<Cell> cellsofColumn;

    public AddColumn(int index, Column column, List<Cell> cellsofColumn) {
        this.cellsofColumn = cellsofColumn;
        this.column = column;
        this.index = index;
    }

    @Override
    public void undo(TableModel tableModel) {
        tableModel.getColumns().remove(column);
        for (int i = 0; i < tableModel.getRows().size(); i++) {
            tableModel.getRows().get(i).getCells().remove(index);
        }
        if (tableModel.isLockAlwaysFitToScreen())
            tableModel.fitToScreen(0);
        historyUpdateListener.undo(this);

    }

    @Override
    public void redo(TableModel tableModel) {
        column.isTouch = false;
        if (tableModel.isLockAlwaysFitToScreen())
            tableModel.fitToScreen(column.getWidth());

        tableModel.getColumns().add(index, column);
        for (int i = 0; i < tableModel.getRows().size(); i++) {
            tableModel.getRows().get(i).getCells().add(index, cellsofColumn.get(i));
        }
//        super.redo(tableModel);
        historyUpdateListener.redo(this);

    }

    @Override
    public String description(String description) {
        return description;
    }
}