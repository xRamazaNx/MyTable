package ru.developer.press.mytable.history.comands;

import java.util.ArrayList;
import java.util.List;

import ru.developer.press.mytable.history.Command;
import ru.developer.press.mytable.table.model.Cell;
import ru.developer.press.mytable.table.model.Column;
import ru.developer.press.mytable.table.model.Header;
import ru.developer.press.mytable.table.model.TableModel;

public class EditPrefs extends Command {
    public List<Integer> indexColumns;
    public List<Integer> indexHeaders;
    public List<int[]> indexCells;

    private List<Column> oldColumns;
    private List<Column> newColumns;

    private List<Header> oldHeaderPrefs;
    private List<Header> newHeaderPrefs;

    private List<Cell> oldCellPrefs;
    private List<Cell> newCellPrefs;

    public EditPrefs(List<Integer> indexColumn, List<Integer> indexHeaders, List<int[]> indexCells) {
        this.indexColumns = indexColumn;
        this.indexHeaders = indexHeaders;
        this.indexCells = indexCells;

        oldColumns = new ArrayList<>();
        newColumns = new ArrayList<>();

        oldHeaderPrefs = new ArrayList<>();
        newHeaderPrefs = new ArrayList<>();

        oldCellPrefs = new ArrayList<>();
        newCellPrefs = new ArrayList<>();
    }

    public void setOldPrefs(TableModel tableModel) {
        List<Column> columns = tableModel.getColumns();
        List<Header> headerPrefs = tableModel.getHeaders();

        for (int i = 0; i < indexColumns.size(); i++) {
            Integer index = indexColumns.get(i);
            Column oldPrefs = columns.get(index);
            this.oldColumns.add(new Column().copyColumn(oldPrefs));
        }
        for (int i = 0; i < indexHeaders.size(); i++) {
            Integer index = indexHeaders.get(i);
            Header oldPrefs = headerPrefs.get(index);
            this.oldHeaderPrefs.add(new Header().copyPrefs(oldPrefs));
        }
        for (int i = 0; i < indexCells.size(); i++) {
            int indexStroke = indexCells.get(i)[0];
            int indexColumn = indexCells.get(i)[1];
            Cell oldPrefs = headerPrefs.get(indexStroke).getCell(indexColumn);
            this.oldCellPrefs.add(new Cell().copyPrefs(oldPrefs));
        }
    }

    public void setNewColumns(TableModel tableModel) {

        List<Column> columns = tableModel.getColumns();
        List<Header> headerPrefs = tableModel.getHeaders();

        for (int i = 0; i < indexColumns.size(); i++) {
            Integer index = indexColumns.get(i);
            Column newPrefs = columns.get(index);
            this.newColumns.add(new Column().copyColumn(newPrefs));
        }
        for (int i = 0; i < indexHeaders.size(); i++) {
            Integer index = indexHeaders.get(i);
            Header newPrefs = headerPrefs.get(index);
            this.newHeaderPrefs.add(new Header().copyPrefs(newPrefs));
        }
        for (int i = 0; i < indexCells.size(); i++) {
            int indexStroke = indexCells.get(i)[0];
            int indexColumn = indexCells.get(i)[1];
            Cell newPrefs = headerPrefs.get(indexStroke).getCell(indexColumn);
            this.newCellPrefs.add(new Cell().copyPrefs(newPrefs));
        }
    }

    @Override
    public void undo(TableModel tableModel) {
        copyAll(tableModel, oldColumns, oldHeaderPrefs, oldCellPrefs);
        historyUpdateListener.undo(this);
    }

    @Override
    public void redo(TableModel tableModel) {
        copyAll(tableModel, newColumns, newHeaderPrefs, newCellPrefs);
        historyUpdateListener.redo(this);
    }

    @Override
    public String description(String description) {
        return description;
    }

    private void copyAll(TableModel tableModel,
                         List<Column> columns,
                         List<Header> headerPrefs,
                         List<Cell> cellPrefs) {

        for (int i = 0; i < indexColumns.size(); i++) {
            Column columnNew = columns.get(i);
            int index = indexColumns.get(i);
            Column columnOrigin = tableModel.getColumns().get(index);

            columnOrigin.copyColumn(columnNew);
        }
        for (int i = 0; i < indexHeaders.size(); i++) {
            Header headerPrefNew = headerPrefs.get(i);

            int index = indexHeaders.get(i);
            Header headerPrefOrigin = tableModel.getHeaders().get(index);
            headerPrefOrigin.copyPrefs(headerPrefNew);
        }
        for (int i = 0; i < indexCells.size(); i++) {
            Cell cellPrefNew = cellPrefs.get(i);

            int strokeIndex = indexCells.get(i)[0];
            int columnIndex = indexCells.get(i)[1];

            Cell cellPrefOrigin = tableModel.getHeaders().get(strokeIndex).getCell(columnIndex);
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
            Header oldHeaderPref = oldHeaderPrefs.get(i);
            Header newHeaderPref = newHeaderPrefs.get(i);

            toReturn = oldHeaderPref.isNotEquals(newHeaderPref);

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
