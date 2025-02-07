package ru.developer.press.mytable.history.comands;

import java.util.ArrayList;
import java.util.List;

import ru.developer.press.mytable.history.Command;
import ru.developer.press.mytable.model.Row;
import ru.developer.press.mytable.model.TableModel;

public class DeleteStroke extends Command {
    private List<Row> rowPrefs;
    public int[] index;

    public DeleteStroke(int[] index, ArrayList<Row> rowPrefs) {
        this.rowPrefs = rowPrefs;
        this.index = index;
    }

    @Override
    public void undo(TableModel tableModel) {

        for (int i = 0; i < index.length; i++) {
            int indexOfLocation = index[i]; // индекс куда добавлять
            Row rowPref = rowPrefs.get(i); // префс к строке который надо добавить
            tableModel.getRows().add(indexOfLocation, rowPref);
        }
        historyUpdateListener.undo(this);

    }

    @Override
    public void redo(TableModel tableModel) {
        tableModel.getRows().removeAll(rowPrefs);

        historyUpdateListener.redo(this);
    }

    @Override
    public String description(String description) {
        return description;
    }
}
