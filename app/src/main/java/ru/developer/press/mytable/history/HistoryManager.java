package ru.developer.press.mytable.history;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import ru.developer.press.mytable.history.comands.AddColumn;
import ru.developer.press.mytable.history.comands.AddStroke;
import ru.developer.press.mytable.history.comands.DeleteColumn;
import ru.developer.press.mytable.history.comands.DeleteStroke;
import ru.developer.press.mytable.history.comands.EditColumn;
import ru.developer.press.mytable.history.comands.RenameColumn;
import ru.developer.press.mytable.model.Cell;
import ru.developer.press.mytable.model.ColumnPref;
import ru.developer.press.mytable.model.TableModel;

public class HistoryManager implements Serializable {
    private ArrayList<Command> undoCommands;
    private ArrayList<Command> redoCommands;
    private int moreLenght = 30;

    public HistoryManager() {
        undoCommands = new ArrayList<>();
        redoCommands = new ArrayList<>();
    }

    public List<Command> getUndoCommands() {
        return undoCommands;
    }

    public List<Command> getRedoCommands() {
        return redoCommands;
    }

    public void undo(TableModel tableModel) {
        //последняя команда
        int lastComand = undoCommands.size() - 1;
        // если размер массива больше ноля
        if (lastComand >= 0) {
            // последняя команда
            Command command = undoCommands.get(lastComand);
            // отмена команды
            command.undo(tableModel);
            // удаления из стека undo
            redoCommands.add(command);
            // добавление в стек redo
            undoCommands.remove(lastComand);
        }
    }

    public void redo(TableModel tableModel) {
        //последняя команда
        int lastComand = redoCommands.size() - 1;
        // если размер массива больше ноля
        if (lastComand >= 0) {
            // последняя команда
            Command command = redoCommands.get(lastComand);
            // отмена команды
            command.redo(tableModel);
            // удаления из стека undo
            undoCommands.add(command);
            // добавление в стек redo
            redoCommands.remove(lastComand);
        }
    }

    public AddColumn addColumn(int index, TableModel tableModel) {
        ArrayList<Cell> cellsOfColumn = new ArrayList<>();
        for (int i = 0; i < tableModel.getEntries().size(); i++) {
            cellsOfColumn.add(tableModel.getEntries().get(i).get(index));
        }
        AddColumn addColumn = new AddColumn(
                index,
                tableModel.getColumnsPref().get(index),
                cellsOfColumn);
        undoCommands.add(addColumn);
        removeMoreComand();
        return addColumn;
    }

    public Command getLastUndoCommand() {
        return undoCommands.get(undoCommands.size() - 1);
    }

    public DeleteColumn deleteColumn(int[] index, List<ColumnPref> columnPrefs, TableModel tableModel) {
        // это не как ентриес
        ArrayList<ArrayList<Cell>> cellsOfColumns = new ArrayList<>();
        for (int anIndex : index) {
            ArrayList<Cell> cells = new ArrayList<>();
            for (int i = 0; i < tableModel.getEntries().size(); i++) {
                Cell cell = tableModel.getEntries().get(i).get(anIndex);
                cells.add(cell);
            }
            cellsOfColumns.add(cells);
        }
        DeleteColumn deleteColumn = new DeleteColumn(index, columnPrefs, cellsOfColumns);
        undoCommands.add(deleteColumn);
        removeMoreComand();
        return deleteColumn;
    }

    public AddStroke addStroke(int index, TableModel tableModel) {
        AddStroke addStroke = new AddStroke(index, tableModel.getEntries().get(index));
        undoCommands.add(addStroke);

        removeMoreComand();
        return addStroke;
    }

    public DeleteStroke deleteStroke(int[] index, TableModel tableModel) {
        ArrayList<ArrayList<Cell>> stroks = new ArrayList<>();
        for (int anIndex : index) {
            stroks.add(tableModel.getEntries().get(anIndex));
        }
        DeleteStroke deleteStroke = new DeleteStroke(index, stroks);
        undoCommands.add(deleteStroke);
        removeMoreComand();
        return deleteStroke;
    }

    public void editCell(Command editCell) {
        undoCommands.add(editCell);
        removeMoreComand();
    }

    public void editColumns(EditColumn editColumns) {
        if (editColumns.isEditedColumns()) {
            undoCommands.add(editColumns);
            removeMoreComand();
        }
    }

    private void removeMoreComand() {
        redoCommands.clear();
        if (undoCommands.size() > moreLenght) { //  в будущем будет зависеть от настроек
            undoCommands.remove(0);
        }
    }

    public void setWidth(Command widtForScreen) {
        undoCommands.add(widtForScreen);
        removeMoreComand();
    }

    public void setHeightStroke(Command heightStroke) {
        undoCommands.add(heightStroke);
        removeMoreComand();
    }

    public void renameColumn(RenameColumn renameColumn) {
        undoCommands.add(renameColumn);
        removeMoreComand();
    }
}
