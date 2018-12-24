package ru.developer.press.mytable.history.comands;

import ru.developer.press.mytable.history.Command;
import ru.developer.press.mytable.table.model.TableModel;

public class HeightStroke extends Command {
    private int [] oldHeight;
    private int [] newHeight;

    public HeightStroke(int [] oldHeight, int [] newHeight){
        this.oldHeight = oldHeight;
        this.newHeight = newHeight;
    }

    @Override
    public void undo(TableModel tableModel) {
        for (int i = 0; i < tableModel.getHeaders().size(); i++) {
            tableModel.getHeaders().get(i).height = oldHeight[i];
        }
        historyUpdateListener.undo(this);
    }

    @Override
    public void redo(TableModel tableModel) {
        for (int i = 0; i < tableModel.getHeaders().size(); i++) {
            tableModel.getHeaders().get(i).height = newHeight[i];
        }
        historyUpdateListener.redo(this);
    }

    @Override
    public String description(String description) {
        return null;
    }

    public boolean isEdited() {

        boolean toReturn = false;
        for (int i = 0; i < oldHeight.length; i++) {
            if (oldHeight[i] != newHeight[i])
                toReturn = true;
        }
        return toReturn;
    }
}
