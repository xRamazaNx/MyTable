package ru.developer.press.mytable.helpers;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import ru.developer.press.mytable.helpers.SchemaDB.Table.Cols;
import ru.developer.press.mytable.helpers.setting_table.TableFileHelper;
import ru.developer.press.mytable.table.model.Cell;
import ru.developer.press.mytable.table.model.Column;
import ru.developer.press.mytable.table.model.Header;
import ru.developer.press.mytable.table.model.TableModel;

//import org.apache.poi.ss.usermodel.Cell;

public class TableLab {
    TableFileHelper tableFileHelper;
    private List<File> tableFiles;

//    public void setTableFiles(List<TableModel> tableFiles) { // потом убрать
//        this.tableFiles = tableFiles;
//    }

    private TableLab(Context context) {
        tableFileHelper = new TableFileHelper(context);
        updateTableList();
    }

    public static TableLab get(Context context) {

        return new TableLab(context);
    }

//    private void inflateDataForTables(TableModel table) {
//
//        SQLiteDatabase db = getDbForTableFile();
//        Cursor cursor = db.query(TableModel.ID, null, null, null, null, null, null);
//        ArrayList<Cell> entry = new ArrayList<>();
//
//        int sizeColumn = table.getColumns().size();
//        int temp = 0;
//        if (cursor.moveToFirst()) {
//            do {
//                temp++;
//
//                Cell cell = new Cell();
//                cell.text = cursor.getString(cursor.getColumnIndexOrThrow(Cols.TEXT)); // тут берем данные с колонок проходя по всем
//                if (cell.text == null)
//                    cell.text = "";
//
//                if (table.getColumns().get(temp - 1).getInputType() == 2) {
//                    if (cell.text.length() > 0) {
//                        try {
//                            cell.date = Long.parseLong(cell.text);
//                            cell.text = "";
//                        } catch (NumberFormatException n) {
//                            cell.date = -1;
//                        }
//                    }
//                }
//
//                cell.sizeFont = cursor.getInt(cursor.getColumnIndexOrThrow(Cols.SIZE_TEXT));
//                cell.bold = cursor.getInt(cursor.getColumnIndexOrThrow(Cols.BOLD));
//                cell.italic = cursor.getInt(cursor.getColumnIndexOrThrow(Cols.ITALIC));
//                cell.colorFont = cursor.getInt(cursor.getColumnIndexOrThrow(Cols.COLOR_TEXT));
//                cell.colorBack = cursor.getInt(cursor.getColumnIndexOrThrow(Cols.COLOR_BACK));
//
//                entry.add(cell);
//                if (sizeColumn == temp) {
//                    table.getStrokes().add(entry);
//                    entry = new ArrayList<>();
//                    temp = 0;
//                }
//
//            } while (cursor.moveToNext());
//        }
//        cursor.close();
//
//    } // 3

    private void inflateColumnsToTable(TableModel table) {
        SQLiteDatabase db = getDbForTableFile();
        Cursor cursor = db.query(TableModel.SETTING_ID, null, null, null, null, null, null);
        if (cursor.moveToFirst()) {
            do {
                Column column = new Column();

                String nameID = cursor.getString(cursor.getColumnIndexOrThrow(Cols.ID_FOR_COLUMN));
                String nameColumn = cursor.getString(cursor.getColumnIndexOrThrow(Cols.NAME_FOR_COLUMN));
                // ну и остальное потом будет заполняться
                int inputType = cursor.getInt(cursor.getColumnIndexOrThrow(Cols.INPUTTYPE));
                String function = cursor.getString(cursor.getColumnIndexOrThrow(Cols.FUNCTION));
                int width = cursor.getInt(cursor.getColumnIndexOrThrow(Cols.WIDTH));

                int sizeText = cursor.getInt(cursor.getColumnIndexOrThrow(Cols.SIZE_TEXT));
                int bold = cursor.getInt(cursor.getColumnIndexOrThrow(Cols.BOLD));
                int italic = cursor.getInt(cursor.getColumnIndexOrThrow(Cols.ITALIC));
                int colorText = cursor.getInt(cursor.getColumnIndexOrThrow(Cols.COLOR_TEXT));
                int colorBack = cursor.getInt(cursor.getColumnIndexOrThrow(Cols.COLOR_BACK));

                column.setNameIdColumn(nameID);
                column.text = nameColumn;
                column.setInputType(inputType);
                column.setFunction(function);
//
                column.width = width;
                column.sizeFont = sizeText;
                column.bold = bold;
                column.italic = italic;
                column.colorFont = colorText;
                column.colorBack = colorBack;

                table.getColumns().add(column);
            } while (cursor.moveToNext());
        }
        cursor.close();

    }

    private void inflateHeadersToTable(TableModel tableModel) {

        SQLiteDatabase db = getDbForTableFile();
        Cursor cursorHeader = db.query(TableModel.HEADER_ID, null, null, null, null, null, null);
        Cursor cursor = db.query(TableModel.ID, null, null, null, null, null, null);
        if (cursorHeader.moveToFirst()) {
            cursor.moveToFirst();
            do {
                Header headerPref = new Header();

                headerPref.height = cursorHeader.getInt(cursorHeader.getColumnIndexOrThrow(Cols.HEIGHT));
                headerPref.sizeFont = cursorHeader.getInt(cursorHeader.getColumnIndexOrThrow(Cols.SIZE_TEXT));
                headerPref.bold = cursorHeader.getInt(cursorHeader.getColumnIndexOrThrow(Cols.BOLD));
                headerPref.italic = cursorHeader.getInt(cursorHeader.getColumnIndexOrThrow(Cols.ITALIC));
                headerPref.colorFont = cursorHeader.getInt(cursorHeader.getColumnIndexOrThrow(Cols.COLOR_TEXT));
                headerPref.colorBack = cursorHeader.getInt(cursorHeader.getColumnIndexOrThrow(Cols.COLOR_BACK));


                int sizeColumn = tableModel.getColumns().size();

                for (int i = 0; i < sizeColumn; i++) {

                    Cell cell = new Cell();
                    cell.text = cursor.getString(cursor.getColumnIndexOrThrow(Cols.TEXT)); // тут берем данные с колонок проходя по всем
                    if (cell.text == null)
                        cell.text = "";

                    if (tableModel.getColumns().get(i).getInputType() == 2) {
                        if (cell.text.length() > 0) {
                            try {
                                cell.date = Long.parseLong(cell.text);
                                cell.text = "";
                            } catch (NumberFormatException n) {
                                cell.date = -1;
                            }
                        }
                    }
                    cell.sizeFont = cursor.getInt(cursor.getColumnIndexOrThrow(Cols.SIZE_TEXT));
                    cell.bold = cursor.getInt(cursor.getColumnIndexOrThrow(Cols.BOLD));
                    cell.italic = cursor.getInt(cursor.getColumnIndexOrThrow(Cols.ITALIC));
                    cell.colorFont = cursor.getInt(cursor.getColumnIndexOrThrow(Cols.COLOR_TEXT));
                    cell.colorBack = cursor.getInt(cursor.getColumnIndexOrThrow(Cols.COLOR_BACK));

                    headerPref.getCells().add(cell);

                    cursor.moveToNext();
                }
                tableModel.getHeaders().add(headerPref);
            } while (cursorHeader.moveToNext());
        }
        cursorHeader.close();
        cursor.close();
    }

    public List<File> getTableFiles() {
        updateTableList();
        return tableFiles;
    }

    public TableModel getTableForFile(File file) {
        tableFileHelper.openTableFile(file);

//        TableSharedPref tableSharedPref = new TableSharedPref(context);
//        TableModel table = tableSharedPref.getTableModel();
        String name = file.getName();
        TableModel table = tableFileHelper.getTableFromCache();
        table.setNameTable(name.substring(0, name.lastIndexOf(".")));

        inflateColumnsToTable(table);
        inflateHeadersToTable(table);

        return table;
    }

    private SQLiteDatabase getDbForTableFile() {
        String name = tableFileHelper.getmCacheFolder() + TableFileHelper.TABLE_DB;
        return SQLiteDatabase.openOrCreateDatabase(new File(name), null);
    }

    public void deleteTableOfDB(File tableFile) {

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
        deleteCache();
        createTable(tm);
        tableFileHelper.saveTable(tm.openName);
        updateTableList();
    }

    public String getOpenName(String tableName) {
        return tableFileHelper.getmMyTableFolder() + tableName + TableFileHelper.TBL;
    }

    public void createTable(TableModel tm) {
        deleteCache();

        SQLiteDatabase db = getDbForTableFile();

        db.beginTransaction();

        db.execSQL(SchemaDB.createTable());  // создал
        db.execSQL(SchemaDB.createTableSetting());
        db.execSQL(SchemaDB.createHeaders());
        // добавляю инфу о столбцах
        try {
            for (Column column : tm.getColumns()) {

                ContentValues value = new ContentValues();
                value.put(Cols.ID_FOR_COLUMN, column.getNameIdColumn());
                value.put(Cols.NAME_FOR_COLUMN, column.text);
                value.put(Cols.INPUTTYPE, column.getInputType());
                value.put(Cols.FUNCTION, column.getFunction());

                value.put(Cols.WIDTH, column.width);

                value.put(Cols.SIZE_TEXT, column.sizeFont);
                value.put(Cols.BOLD, column.bold);
                value.put(Cols.ITALIC, column.italic);
                value.put(Cols.COLOR_TEXT, column.colorFont);
                value.put(Cols.COLOR_BACK, column.colorBack);

                db.insert(TableModel.SETTING_ID, null, value);
            }

            for (int str = 0; str < tm.getHeaders().size(); str++) {
                // set height of headers
                ContentValues valHeader = new ContentValues();
                Header headerPref = tm.getHeaders().get(str);

                valHeader.put(Cols.HEIGHT, headerPref.height);
                valHeader.put(Cols.SIZE_TEXT, headerPref.sizeFont);
                valHeader.put(Cols.BOLD, headerPref.bold);
                valHeader.put(Cols.ITALIC, headerPref.italic);
                valHeader.put(Cols.COLOR_TEXT, headerPref.colorFont);
                valHeader.put(Cols.COLOR_BACK, headerPref.colorBack);

                db.insert(TableModel.HEADER_ID, null, valHeader);

                // set all pref_cells
                for (int col = 0; col < tm.getColumns().size(); col++) {
                    ContentValues values = new ContentValues();
                    Cell cell = tm.getHeaders()
                            .get(str)
                            .getCell(col);

                    String text;
                    if (tm.getColumns().get(col).getInputType() == 2) {
                        text = String.valueOf(cell.date);
                    } else
                        text = cell.text;

                    values.put(Cols.TEXT, text);

                    values.put(Cols.SIZE_TEXT, cell.sizeFont);
                    values.put(Cols.BOLD, cell.bold);
                    values.put(Cols.ITALIC, cell.italic);
                    values.put(Cols.COLOR_TEXT, cell.colorFont);
                    values.put(Cols.COLOR_BACK, cell.colorBack);

                    db.insert(TableModel.ID, null, values);
                }
            }
            db.setTransactionSuccessful();
        } finally {
            db.endTransaction();
        }
        db.close();
        tm.openName = getOpenName(tm.getNameTable());

        tableFileHelper.createOrUpdatePrefs(tm);
        tableFileHelper.saveTable(tm.openName);
    }

    public void updateTableList() {
        tableFiles = tableFileHelper.getTableFiles();
    }

    public void deleteCache() {
        tableFileHelper.deleteCacheFiles();
    }
}
