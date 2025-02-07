package ru.developer.press.mytable.history.comands;

import java.util.ArrayList;
import java.util.List;

import ru.developer.press.mytable.history.Command;
import ru.developer.press.mytable.model.Cell;
import ru.developer.press.mytable.model.Column;
import ru.developer.press.mytable.model.TableModel;

public class DeleteColumn extends Command {
    public int[] index;
    private List<Column> columns;
    private ArrayList<ArrayList<Cell>> cellsOfColumns;

    public DeleteColumn(int[] index, List<Column> columns, ArrayList<ArrayList<Cell>> cellsOfColumns) {
        this.columns = columns;
        this.cellsOfColumns = cellsOfColumns;
        this.index = index;
    }

    @Override
    public void undo(TableModel tableModel) {
        float widthAddedColumns = 0;
        for (Column col : columns) {
            col.isTouch = false;
            widthAddedColumns += col.getWidth();
        }
        if (tableModel.isLockAlwaysFitToScreen())
            tableModel.fitToScreen(widthAddedColumns);

        for (int i = 0; i < index.length; i++) {
            int indexOfLocation = index[i]; // индекс куда добавлять
            Column column = columns.get(i); // столб который надо добавить
            tableModel.getColumns().add(indexOfLocation, column);

            for (int j = 0; j < tableModel.getRows().size(); j++) {

                tableModel.getRows().get(j).getCells().add(indexOfLocation, cellsOfColumns.get(i).get(j));
            }
        }
        historyUpdateListener.undo(this);
    }

    @Override
    public void redo(TableModel tableModel) {
        tableModel.getColumns().removeAll(columns);
        for (int i = 0; i < tableModel.getRows().size(); i++) {
            ArrayList<Cell> delCells = new ArrayList<>();
            for (int anIndex : index) {
                delCells.add(tableModel.getRows().get(i).getCellAtIndex(anIndex));
            }
            tableModel.getRows().get(i).getCells().removeAll(delCells);
        }
        if (tableModel.isLockAlwaysFitToScreen())
            tableModel.fitToScreen(0);
        historyUpdateListener.redo(this);
    }

    @Override
    public String description(String description) {
        return description;
    }
}
