package ru.developer.press.mytable.history.comands;

import java.util.ArrayList;
import java.util.List;

import ru.developer.press.mytable.history.Command;
import ru.developer.press.mytable.model.ColumnPref;
import ru.developer.press.mytable.model.TableModel;

public class EditColumn extends Command {
    private int [] index;
    private List <ColumnPref> oldColumnPrefs;
    private List <ColumnPref> newColumnPrefs;
    public EditColumn (int [] index){
        this.index = index;
        oldColumnPrefs = new ArrayList<>();
        newColumnPrefs = new ArrayList<>();
    }

    public void setOldColumnPrefs(List<ColumnPref> oldColumnPrefs) {
        for (int i = 0; i < oldColumnPrefs.size(); i++) {
            this.oldColumnPrefs.add(new ColumnPref(oldColumnPrefs.get(i)));
        }
    }

    public void setNewColumnPrefs(List<ColumnPref> newColumnPrefs) {
        for (int i = 0; i < newColumnPrefs.size(); i++) {
            this.newColumnPrefs.add(new ColumnPref(newColumnPrefs.get(i)));
        }
    }

    @Override
    public void undo(TableModel tableModel) {
        copyAll(tableModel, oldColumnPrefs);
        historyUpdateListener.undo(null);
    }

    @Override
    public void redo(TableModel tableModel) {
        copyAll(tableModel, newColumnPrefs);
        historyUpdateListener.redo(null);
    }

    @Override
    public String description(String description) {
        return description;
    }

    private void copyAll(TableModel tableModel, List <ColumnPref> columnPrefs) {
        for (int i = 0; i < index.length; i++) {
            ColumnPref columnPrefNew = columnPrefs.get(i);
            ColumnPref columnPrefOrigin = tableModel.getColumnsPref().get(index[i]);

            columnPrefOrigin.copyColumn(columnPrefNew);
        }
    }

    public int[] getIndex() {
        return index;
    }

    public boolean isEditedColumns(){
            boolean toReturn;
        for (int i = 0; i < index.length; i++) {
            ColumnPref nCol = newColumnPrefs.get(i);
            ColumnPref oCol = oldColumnPrefs.get(i);

            toReturn = nCol.getInputType() == oCol.getInputType() &&
                    nCol.getFunction().equals(oCol.getFunction()) &&
                    nCol.getWidthColumn() == oCol.getWidthColumn() &&

                    nCol.getTextSizeCell() == oCol.getTextSizeCell() &&
                    nCol.getTextStyleCell() == oCol.getTextStyleCell() &&
                    nCol.getTextColorCell() == oCol.getTextColorCell() &&
                    nCol.getColorCellRect() == oCol.getColorCellRect() &&

                    nCol.getTextSizeTitle() == oCol.getTextSizeTitle() &&
                    nCol.getTextStyleTitle() == oCol.getTextStyleTitle() &&
                    nCol.getTextColorTitle() == oCol.getTextColorTitle() &&
                    nCol.getColorTitleRect() == oCol.getColorTitleRect();
            if (!toReturn)
                return true;
        }
        return false;
    }
}
