package ru.developer.press.mytable.helpers;

import android.content.Context;

import java.io.File;
import java.util.List;

import ru.developer.press.mytable.model.TableModel;

public class TableLab {
    private TableFileHelper tableFileHelper;
    private List<File> tableFiles;

    private TableLab(Context context) {
        tableFileHelper = new TableFileHelper(context);
        updateTableList();
    }

    public static TableLab get(Context context) {
        return new TableLab(context);
    }

    public List<File> getTableFiles() {
        updateTableList();
        return tableFiles;
    }

    public TableModel getTableForFile(String path) {
        File tableFile = new File(path);
        String name = tableFile.getName();
        TableModel table = tableFileHelper.getTable(tableFile);
        table.setNameTable(name.substring(0, name.lastIndexOf(".")));
        table.openName = tableFile.getPath();


        return table;
    }

    public void deleteTable(File tableFile) {

        for (File file :
                tableFiles) {
            if (file.getName().equals(tableFile.getName())) {
                if (tableFile.delete()) {
//                    tableFiles.remove(file);
                    return;
                }
            }

        }

    }

    public void saveTable(TableModel tm) {
        createTable(tm); //
        updateTableList();
    }

    private String getOpenName(String tableName) {
        return tableFileHelper.getMyTableFolder() + tableName + TableFileHelper.TBL;
    }

    public void createTable(TableModel tm) {
        tm.openName = getOpenName(tm.getNameTable());
        tableFileHelper.createOrUpdateTableFile(tm);
    }

    private void updateTableList() {
        tableFiles = tableFileHelper.getTableFiles();
    }

}
