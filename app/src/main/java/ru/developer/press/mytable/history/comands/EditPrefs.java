package ru.developer.press.mytable.history.comands;

import java.util.ArrayList;
import java.util.List;

import ru.developer.press.mytable.history.Command;
import ru.developer.press.mytable.model.Cell;
import ru.developer.press.mytable.model.Column;
import ru.developer.press.mytable.model.Row;
import ru.developer.press.mytable.model.TableModel;

public class EditPrefs extends Command {
    private List<Integer> indexColumns;
    private List<Integer> indexHeaders;
    private List<int[]> indexCells;

    private List<Column> oldColumns;
    private List<Column> newColumns;

    private List<Row> oldRowPrefs;
    private List<Row> newRowPrefs;

    private List<Cell> oldCellPrefs;
    private List<Cell> newCellPrefs;

    public EditPrefs(List<Integer> indexColumn, List<Integer> indexHeaders, List<int[]> indexCells) {
        this.indexColumns = indexColumn;
        this.indexHeaders = indexHeaders;
        this.indexCells = indexCells;

        oldColumns = new ArrayList<>();
        newColumns = new ArrayList<>();

        oldRowPrefs = new ArrayList<>();
        newRowPrefs = new ArrayList<>();

        oldCellPrefs = new ArrayList<>();
        newCellPrefs = new ArrayList<>();
    }

    public void setOldPrefs(TableModel tableModel) {
        List<Column> columns = tableModel.getColumns();
        List<Row> rowPrefs = tableModel.getRows();

        for (int i = 0; i < indexColumns.size(); i++) {
            Integer index = indexColumns.get(i);
            Column oldPrefs = columns.get(index);
            this.oldColumns.add(new Column().copyColumn(oldPrefs));
        }
        for (int i = 0; i < indexHeaders.size(); i++) {
            Integer index = indexHeaders.get(i);
            Row oldPrefs = rowPrefs.get(index);
            this.oldRowPrefs.add(new Row().copyPrefs(oldPrefs));
        }
        for (int i = 0; i < indexCells.size(); i++) {
            int indexStroke = indexCells.get(i)[0];
            int indexColumn = indexCells.get(i)[1];
            Cell oldPrefs = rowPrefs.get(indexStroke).getCellAtIndex(indexColumn);
            this.oldCellPrefs.add(new Cell().copyPrefs(oldPrefs));
        }
    }

    public void setNewPrefs(TableModel tableModel) {

        List<Column> columns = tableModel.getColumns();
        List<Row> rowPrefs = tableModel.getRows();

        for (int i = 0; i < indexColumns.size(); i++) {
            Integer index = indexColumns.get(i);
            Column newPrefs = columns.get(index);
            this.newColumns.add(new Column().copyColumn(newPrefs));
        }
        for (int i = 0; i < indexHeaders.size(); i++) {
            Integer index = indexHeaders.get(i);
            Row newPrefs = rowPrefs.get(index);
            this.newRowPrefs.add(new Row().copyPrefs(newPrefs));
        }
        for (int i = 0; i < indexCells.size(); i++) {
            int indexStroke = indexCells.get(i)[0];
            int indexColumn = indexCells.get(i)[1];
            Cell newPrefs = rowPrefs.get(indexStroke).getCellAtIndex(indexColumn);
            this.newCellPrefs.add(new Cell().copyPrefs(newPrefs));
        }
    }

    @Override
    public void undo(TableModel tableModel) {
        copyAll(tableModel, oldColumns, oldRowPrefs, oldCellPrefs);
        historyUpdateListener.undo(this);
    }

    @Override
    public void redo(TableModel tableModel) {
        copyAll(tableModel, newColumns, newRowPrefs, newCellPrefs);
        historyUpdateListener.redo(this);
    }

    @Override
    public String description(String description) {
        return description;
    }

    private void copyAll(TableModel tableModel,
                         List<Column> columns,
                         List<Row> rowPrefs,
                         List<Cell> cellPrefs) {

        for (int i = 0; i < indexColumns.size(); i++) {
            Column columnNew = columns.get(i);
            int index = indexColumns.get(i);
            Column columnOrigin = tableModel.getColumns().get(index);

            columnOrigin.copyColumn(columnNew);
        }
        for (int i = 0; i < indexHeaders.size(); i++) {
            Row rowPrefNew = rowPrefs.get(i);

            int index = indexHeaders.get(i);
            Row rowPrefOrigin = tableModel.getRows().get(index);
            rowPrefOrigin.copyPrefs(rowPrefNew);
        }
        for (int i = 0; i < indexCells.size(); i++) {
            Cell cellPrefNew = cellPrefs.get(i);

            int strokeIndex = indexCells.get(i)[0];
            int columnIndex = indexCells.get(i)[1];

            Cell cellPrefOrigin = tableModel.getRows().get(strokeIndex).getCellAtIndex(columnIndex);
            cellPrefOrigin.copyPrefs(cellPrefNew);
        }
    }

    public boolean isEditedPrefs() {
        boolean toReturn;
        for (int i = 0; i < indexColumns.size(); i++) {
            Column oCol = oldColumns.get(i);
            Column nCol = newColumns.get(i);

            toReturn = oCol.isNotEquals(nCol);
            if (toReturn) {
                return true;
            }
        }
        for (int i = 0; i < indexHeaders.size(); i++) {
            Row oldRowPref = oldRowPrefs.get(i);
            Row newRowPref = newRowPrefs.get(i);

            toReturn = oldRowPref.isNotEquals(newRowPref);

            if (toReturn)
                return true;
        }
        for (int i = 0; i < indexCells.size(); i++) {
            Cell oldCellPref = oldCellPrefs.get(i);
            Cell newCellPref = newCellPrefs.get(i);

            toReturn = oldCellPref.isNotEquals(newCellPref);

            if (toReturn)
                return true;
        }

        return false;
    }
}
