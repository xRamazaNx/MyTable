package ru.developer.press.mytable.history.comands;

import java.util.ArrayList;
import java.util.List;

import ru.developer.press.mytable.history.Command;
import ru.developer.press.mytable.table.model.Cell;
import ru.developer.press.mytable.table.model.Header;
import ru.developer.press.mytable.table.model.TableModel;

public class DeleteStroke extends Command {
    private List<Header> headerPrefs;
    public int[] index;

    public DeleteStroke(int[] index, ArrayList<Header> headerPrefs) {
        this.headerPrefs = headerPrefs;
        this.index = index;
    }

    @Override
    public void undo(TableModel tableModel) {

        for (int i = 0; i < index.length; i++) {
            int indexOfLocation = index[i]; // индекс куда добавлять
            Header headerPref = headerPrefs.get(i); // префс к строке который надо добавить
            tableModel.getHeaders().add(indexOfLocation, headerPref);
        }
        historyUpdateListener.undo(this);

    }

    @Override
    public void redo(TableModel tableModel) {
        tableModel.getHeaders().removeAll(headerPrefs);

        historyUpdateListener.redo(this);
    }

    @Override
    public String description(String description) {
        return description;
    }
}
