package ru.developer.press.mytable.history.comands;

import java.util.ArrayList;

import ru.developer.press.mytable.history.Command;
import ru.developer.press.mytable.model.Column;
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
        for (int i = 0; i < tableModel.getColumns().size(); i++) {
            tableModel.getColumns().get(i).setWidth(oldWidths[i]);
        }
        historyUpdateListener.undo(this);
    }

    @Override
    public void redo(TableModel tableModel) {
        for (int i = 0; i < tableModel.getColumns().size(); i++) {
            tableModel.getColumns().get(i).setWidth(newWidths[i]);
        }
        historyUpdateListener.redo(this);
    }

    @Override
    public String description(String description) {
        return description;
    }

    public ArrayList<Integer> getCurrentWidth(TableModel tableModel) {
        ArrayList<Integer> width = new ArrayList<>();
        for (Column col :
                tableModel.getColumns()) {
            width.add((int) col.getWidth());
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
