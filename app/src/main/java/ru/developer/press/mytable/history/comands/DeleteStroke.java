package ru.developer.press.mytable.history.comands;

import java.util.ArrayList;
import java.util.List;

import ru.developer.press.mytable.history.Command;
import ru.developer.press.mytable.model.Cell;
import ru.developer.press.mytable.model.TableModel;

public class DeleteStroke extends Command {
    private List<ArrayList<Cell>> stroks;
    public int[] index;

    public DeleteStroke(int[] index, List<ArrayList<Cell>> stroks) {

        this.stroks = stroks;
        this.index = index;
    }

    @Override
    public void undo(TableModel tableModel) {

        for (int i = 0; i < index.length; i++) {
            int indexOfLocation = index[i]; // индекс куда добавлять
            ArrayList<Cell> stroke = stroks.get(i); // столб который надо добавить
            tableModel.getEntries().add(indexOfLocation, stroke);
        }
        historyUpdateListener.undo(null);

    }

    @Override
    public void redo(TableModel tableModel) {
        tableModel.getEntries().removeAll(stroks);
        historyUpdateListener.redo(null);
    }

    @Override
    public String description(String description) {
        return description;
    }
}
