package ru.developer.press.mytable.history.comands;

import java.util.ArrayList;

import ru.developer.press.mytable.history.Command;
import ru.developer.press.mytable.model.ColumnPref;
import ru.developer.press.mytable.model.TableModel;

public class WidthColumn extends Command {

    private int[] newWidths;
    private int[] oldWidths;

    public void setNewWidths(int[] newWidths) {
        this.newWidths = newWidths;
    }

    public void setOldWidths(int[] oldWidths) {
        this.oldWidths = oldWidths;
    }

    @Override
    public void undo(TableModel tableModel) {
        for (int i = 0; i < tableModel.getColumnsPref().size(); i++) {
            tableModel.getColumnsPref().get(i).setWidthColumn(oldWidths[i]);
        }
        historyUpdateListener.undo(null);
    }

    @Override
    public void redo(TableModel tableModel) {
        for (int i = 0; i < tableModel.getColumnsPref().size(); i++) {
            tableModel.getColumnsPref().get(i).setWidthColumn(newWidths[i]);
        }
        historyUpdateListener.redo(null);
    }

    @Override
    public String description(String description) {
        return description;
    }

    public ArrayList<Integer> getCurrentWidth(TableModel tableModel) {
        ArrayList<Integer> width = new ArrayList<>();
        for (ColumnPref col :
                tableModel.getColumnsPref()) {
            width.add(col.getWidthColumn());
        }
        return width;
    }

    public boolean isEdited() {
        boolean toReturn = false;
        for (int i = 0; i < oldWidths.length; i++) {
            if (oldWidths[i] != newWidths[i])
                toReturn = true;
        }
        return toReturn;
    }
}
