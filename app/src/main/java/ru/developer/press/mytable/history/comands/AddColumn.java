package ru.developer.press.mytable.history.comands;

import java.util.List;

import ru.developer.press.mytable.history.Command;
import ru.developer.press.mytable.table.model.Cell;
import ru.developer.press.mytable.table.model.Column;
import ru.developer.press.mytable.table.model.TableModel;

public class AddColumn extends Command {

    private Column column;
    private List <Cell> cellsofColumn;
    public int index;

    public AddColumn(int index, Column column, List <Cell> cellsofColumn) {
        this.cellsofColumn = cellsofColumn;
        this.column = column;
        this.index = index;
    }

    @Override
    public void undo(TableModel tableModel) {
        tableModel.getColumns().remove(column);
        for (int i = 0; i < tableModel.getHeaders().size(); i++) {
            tableModel.getHeaders().get(i).getCells().remove(index);
        }
        historyUpdateListener.undo(this);

    }

    @Override
    public void redo(TableModel tableModel) {
        column.isTouch = false;
        tableModel.getColumns().add(index, column);
        for (int i = 0; i < tableModel.getHeaders().size(); i++) {
            tableModel.getHeaders().get(i).getCells().add(index, cellsofColumn.get(i));
        }
//        super.redo(tableModel);
        historyUpdateListener.redo(this);

    }

    @Override
    public String description(String description) {
        return description;
    }
}