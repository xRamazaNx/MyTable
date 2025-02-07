package ru.developer.press.mytable.history.comands;

import ru.developer.press.mytable.history.Command;
import ru.developer.press.mytable.model.TableModel;

public class RenameColumn extends Command {
    private String oldNameColumn;
    private String newNameColumn;
//    private String nameID;
    public int index;

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
        tableModel.getColumns().get(index).text = oldNameColumn;
        historyUpdateListener.undo(this);
    }

    @Override
    public void redo(TableModel tableModel) {
        tableModel.getColumns().get(index).text = newNameColumn;
        historyUpdateListener.redo(this);    }

    @Override
    public String description(String description) {
        return null;
    }

}
