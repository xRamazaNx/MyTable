package ru.developer.press.mytable.history.comands;

import java.util.ArrayList;
import java.util.List;

import ru.developer.press.mytable.history.Command;
import ru.developer.press.mytable.model.Cell;
import ru.developer.press.mytable.model.ColumnPref;
import ru.developer.press.mytable.model.TableModel;

public class DeleteColumn extends Command {
    private List<ColumnPref> columnPrefs;
    private ArrayList<ArrayList<Cell>> cellsOfColumns;
    public int[] index;

    public DeleteColumn(int[] index, List<ColumnPref> columnPrefs, ArrayList<ArrayList<Cell>> cellsOfColumns) {
        this.columnPrefs = columnPrefs;
        this.cellsOfColumns = cellsOfColumns;
        this.index = index;
    }

    @Override
    public void undo(TableModel tableModel) {
        for (ColumnPref col : columnPrefs) {
            col.isTouched = false;
        }
        for (int i = 0; i < index.length; i++) {
            int indexOfLocation = index[i]; // индекс куда добавлять
            ColumnPref columnPref = columnPrefs.get(i); // столб который надо добавить
            tableModel.getColumnsPref().add(indexOfLocation, columnPref);

            for (int j = 0; j < tableModel.getEntries().size(); j++) {

                tableModel.getEntries().get(j).add(indexOfLocation, cellsOfColumns.get(i).get(j));
            }
        }
        historyUpdateListener.undo(null);
    }

    @Override
    public void redo(TableModel tableModel) {
        tableModel.getColumnsPref().removeAll(columnPrefs);
        for (int i = 0; i < tableModel.getEntries().size(); i++) {
            ArrayList<Cell> delCells = new ArrayList<>();
            for (int anIndex : index) {
                delCells.add(tableModel.getEntries().get(i).get(anIndex));
            }
            tableModel.getEntries().get(i).removeAll(delCells);
        }
        historyUpdateListener.redo(null);
    }

    @Override
    public String description(String description) {
        return description;
    }
}
