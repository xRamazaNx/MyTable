package ru.developer.press.mytable.history.comands;

import java.util.ArrayList;
import java.util.List;

import ru.developer.press.mytable.history.Command;
import ru.developer.press.mytable.table.model.Cell;
import ru.developer.press.mytable.table.model.Column;
import ru.developer.press.mytable.table.model.TableModel;

public class DeleteColumn extends Command {
    private List<Column> columns;
    private ArrayList<ArrayList<Cell>> cellsOfColumns;
    public int[] index;

    public DeleteColumn(int[] index, List<Column> columns, ArrayList<ArrayList<Cell>> cellsOfColumns) {
        this.columns = columns;
        this.cellsOfColumns = cellsOfColumns;
        this.index = index;
    }

    @Override
    public void undo(TableModel tableModel) {
        for (Column col : columns) {
            col.isTouch = false;
        }
        for (int i = 0; i < index.length; i++) {
            int indexOfLocation = index[i]; // индекс куда добавлять
            Column column = columns.get(i); // столб который надо добавить
            tableModel.getColumns().add(indexOfLocation, column);

            for (int j = 0; j < tableModel.getHeaders().size(); j++) {

                tableModel.getHeaders().get(j).getCells().add(indexOfLocation, cellsOfColumns.get(i).get(j));
            }
        }
        historyUpdateListener.undo(this);
    }

    @Override
    public void redo(TableModel tableModel) {
        tableModel.getColumns().removeAll(columns);
        for (int i = 0; i < tableModel.getHeaders().size(); i++) {
            ArrayList<Cell> delCells = new ArrayList<>();
            for (int anIndex : index) {
                delCells.add(tableModel.getHeaders().get(i).getCell(anIndex));
            }
            tableModel.getHeaders().get(i).getCells().removeAll(delCells);
        }
        historyUpdateListener.redo(this);
    }

    @Override
    public String description(String description) {
        return description;
    }
}
