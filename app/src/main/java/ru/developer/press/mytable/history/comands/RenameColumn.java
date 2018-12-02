package ru.developer.press.mytable.history.comands;

import ru.developer.press.mytable.history.Command;
import ru.developer.press.mytable.model.TableModel;

public class RenameColumn extends Command {
    private String oldNameColumn;
    private String newNameColumn;
//    private String nameID;
    private int index;

//    public String getNameID() {
//        return nameID;
//    }

    public void setOldNameColumn(String oldNameColumn) {
        this.oldNameColumn = oldNameColumn;
    }
    public void setNewNameColumn(String newNameColumn) {
        this.newNameColumn = newNameColumn;
    }

    public RenameColumn(int index) {
        this.index = index;
    }

    @Override
    public void undo(TableModel tableModel) {
        tableModel.getColumnsPref().get(index).setName(oldNameColumn);
        historyUpdateListener.undo(index);
    }

    @Override
    public void redo(TableModel tableModel) {
        tableModel.getColumnsPref().get(index).setName(newNameColumn);
        historyUpdateListener.redo(index);    }

    @Override
    public String description(String description) {
        return null;
    }

    public int getIndex() {
        return index;
    }
}
