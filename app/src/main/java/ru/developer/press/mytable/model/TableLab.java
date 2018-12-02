package ru.developer.press.mytable.model;

import android.annotation.SuppressLint;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Environment;
import android.widget.Toast;

//import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import ru.developer.press.mytable.database.Database;
import ru.developer.press.mytable.database.SchemaDB;

public class TableLab {
    @SuppressLint("StaticFieldLeak")
    private static TableLab tableLab;
    private List<TableModel> tableList;


    public static TableLab get(Context context) {
        if (tableLab == null) {
            tableLab = new TableLab(context);
        }
        return tableLab;
    }


    private TableLab(Context context) {
        upddateTables(context);
//        for (TableModel tableModel : tableList) {
//            try {
//
//                String path = Environment.getExternalStoragePublicDirectory("MyTables").getPath();
//                new File(path).mkdir();
//                Workbook table = new HSSFWorkbook();
//                Sheet list = table.createSheet();
//                ArrayList<ArrayList<Cell>> entries = tableModel.getEntries();
//                for (int i = 0; i < entries.size(); i++) {
//                    ArrayList<Cell> header = entries.get(i);
//                    Row row = list.createRow(i);
//                    for (int j = 0; j < tableModel.getColumnsPref().size(); j++) {
//                        org.apache.poi.ss.usermodel.Cell cell = row.createCell(j);
//                        cell.setCellValue(header.get(j).text);
//                    }
//                }
//                table.write(new FileOutputStream(path+"/"+tableModel.getNameTable()+".xls"));
//                Toast.makeText(context, "да", Toast.LENGTH_SHORT).show();
////                table.close();
//            } catch (Exception ignored) {
//            }
//        }
//
    }

    public void upddateTables(Context context) {
        tableList = new ArrayList<>();

        SQLiteDatabase db = new Database(context).getWritableDatabase();
        // берем из актива данные о таблицах
        inflateInfoForTables(db);     // 1 - берем информацию о таблицах из ACTIVE
        inflateSettingForTables(db); // 2 - берем настройки таблицы
        inflateDataForTables(db);     // 3 - берем данные записей из каждой таблицы

        db.close();
    }


    private void inflateDataForTables(SQLiteDatabase db) {
        for (TableModel table : tableList) {
            Cursor cursor = db.query(table.getNameId(), null, null, null, null, null, null);
            if (cursor.moveToFirst()) {
                do {
                    ArrayList<Cell> entry = new ArrayList<>();
                    for (ColumnPref columnPref : table.getColumnsPref()) { // тут мы проходим по именам колон чтобы записать все данные из них в одну запись

                        Cell cell = new Cell();
                        cell.text = cursor.getString(cursor.getColumnIndexOrThrow(columnPref.getNameIdColumn())); // тут берем данные с колонок проходя по всем
                        if (cell.text == null)
                            cell.text = "";

                        if (columnPref.getInputType() == 2) {
                            if (cell.text.length() > 0) {
                                try {
                                    cell.date = Long.parseLong(cell.text);
                                    cell.text = "";
                                } catch (NumberFormatException n) {
                                    cell.date = -1;
                                }
                            }
                        }
                        entry.add(cell);
                    }
                    table.getEntries().add(entry);

                } while (cursor.moveToNext());
            }
            cursor.close();
        }
    } // 3

    private void inflateSettingForTables(SQLiteDatabase db) {
        for (TableModel table : tableList) {
            Cursor cursor = db.query(table.getNameSettingId(), null, null, null, null, null, null);
            if (cursor.moveToFirst()) {
                do {
                    ColumnPref settingModel = new ColumnPref();

                    String idColumns = cursor.getString(cursor.getColumnIndexOrThrow(SchemaDB.Table.Cols.ID_FOR_COLUMN));
                    String nameColumn = cursor.getString(cursor.getColumnIndexOrThrow(SchemaDB.Table.Cols.NAME_FOR_COLUMN));
                    // ну и остальное потом будет заполняться
                    int inputType = cursor.getInt(cursor.getColumnIndexOrThrow(SchemaDB.Table.Cols.INPUTTYPE));
                    String function = cursor.getString(cursor.getColumnIndexOrThrow(SchemaDB.Table.Cols.FUNCTION));
                    int width = cursor.getInt(cursor.getColumnIndexOrThrow(SchemaDB.Table.Cols.WIDTH));
                    int sizeTextColumn = cursor.getInt(cursor.getColumnIndexOrThrow(SchemaDB.Table.Cols.SIZETEXT_COL));
                    int styleTextColumn = cursor.getInt(cursor.getColumnIndexOrThrow(SchemaDB.Table.Cols.STYLE_COL));
                    int colorTextColumn = cursor.getInt(cursor.getColumnIndexOrThrow(SchemaDB.Table.Cols.COLOR_COL));
                    int colorColumnRect = cursor.getInt(cursor.getColumnIndexOrThrow(SchemaDB.Table.Cols.COLOR_COL_RECT));

                    int sizeTextItem = cursor.getInt(cursor.getColumnIndexOrThrow(SchemaDB.Table.Cols.SIZETEXT_ITEM));
                    int styleTextItem = cursor.getInt(cursor.getColumnIndexOrThrow(SchemaDB.Table.Cols.STYLE_ITEM));
                    int colorTextItem = cursor.getInt(cursor.getColumnIndexOrThrow(SchemaDB.Table.Cols.COLOR_ITEM));
                    int colorItemRect = cursor.getInt(cursor.getColumnIndexOrThrow(SchemaDB.Table.Cols.COLOR_ITEM_RECT));

                    settingModel.setNameIdColumn(idColumns);
                    settingModel.setName(nameColumn);
                    settingModel.setInputType(inputType);
                    settingModel.setFunction(function);
//                    settingModel.setWidthColumn(15);            ////////////////////////////////////////////////////////////////////////
                    settingModel.setWidthColumn(width);
                    settingModel.setTextSizeTitle(sizeTextColumn);
                    settingModel.setTextStyleTitle(styleTextColumn);
                    settingModel.setTextColorTitle(colorTextColumn);
                    settingModel.setColorTitleRect(colorColumnRect);
                    settingModel.setTextSizeCell(sizeTextItem);
                    settingModel.setTextStyleCell(styleTextItem);
                    settingModel.setTextColorCell(colorTextItem);
                    settingModel.setColorCellRect(colorItemRect);

                    table.getColumnsPref().add(settingModel);
                } while (cursor.moveToNext());
            }
            cursor.close();
        }
        //                Table.Cols.ID_FOR_COLUMN + ", " +
        //
        //                Table.Cols.NAME_FOR_COLUMN + ", " +
        //                Table.Cols.FUNCTION + ", " +
        //                Table.Cols.WIDTH + ", " +
        //
        //
        //                Table.Cols.SIZETEXT_COL + ", " +
        //                Table.Cols.STYLE_COL + ", " +
        //                Table.Cols.COLOR_COL + ", " +
        //
        //                Table.Cols.SIZETEXT_ITEM + ", " +
        //                Table.Cols.STYLE_ITEM + ", " +
        //                Table.Cols.COLOR_ITEM +
    } // 2

    private void inflateInfoForTables(SQLiteDatabase db) {
        Cursor cursor = db.query(SchemaDB.Table.NAME_ACTIVE, null, null, null, null, null, null);
        if (cursor.moveToFirst()) {
            do {
                TableModel tableModel = new TableModel();

                String isArhive = cursor.getString(cursor.getColumnIndexOrThrow(SchemaDB.Table.Cols.IS_ARHIVE));
                String isFill = cursor.getString(cursor.getColumnIndexOrThrow(SchemaDB.Table.Cols.IS_FILL));
                String nameId = cursor.getString(cursor.getColumnIndexOrThrow(SchemaDB.Table.Cols.ID_FOR_TABLE));
                String nameSettingId = cursor.getString(cursor.getColumnIndexOrThrow(SchemaDB.Table.Cols.ID_FOR_TABLE_SETTING));
                String nameTable = cursor.getString(cursor.getColumnIndexOrThrow(SchemaDB.Table.Cols.NAME_FOR_TABLE));
                int height = cursor.getInt(cursor.getColumnIndexOrThrow(SchemaDB.Table.Cols.HEIGHT));
                String dateCreated = cursor.getString(cursor.getColumnIndexOrThrow(SchemaDB.Table.Cols.DATE_CREATE_TABLE));
                String dateModify = cursor.getString(cursor.getColumnIndexOrThrow(SchemaDB.Table.Cols.DATE_MODIFY_TABLE));
                int lockHeight = cursor.getInt(cursor.getColumnIndexOrThrow(SchemaDB.Table.Cols.LOCK_HEIGHT_CELLS));

                tableModel.setIsArhive(Integer.parseInt(isArhive));
                tableModel.setIsFill(Integer.parseInt(isFill));
                tableModel.setNameId(nameId);
                tableModel.setNameSettingId(nameSettingId);
                tableModel.setNameTable(nameTable);
                tableModel.setHeightCells(height);         ////////////////////////////////////////////////////////////////////////////
                tableModel.setDateCreated(dateCreated);
                tableModel.setDateModify(dateModify);
                tableModel.setLockHeightCells(lockHeight);

                tableList.add(tableModel);

            } while (cursor.moveToNext());
        }
        cursor.close();
    } // 1

//    SchemaDB.Table.Cols.ID + " integer prima
//    SchemaDB.Table.Cols.IS_ARHIVE + ", " + /
//    SchemaDB.Table.Cols.ID_FOR_TABLE + ", "
//    SchemaDB.Table.Cols.ID_FOR_TABLE_SETTING
//    SchemaDB.Table.Cols.NAME_FOR_TABLE + ",
//    SchemaDB.Table.Cols.DATE_CREATE_TABLE +
//    SchemaDB.Table.Cols.DATE_MODIFY_TABLE +

    public List<TableModel> getTableList() {
        return tableList;
    }

    public TableModel getTableForNameId(String nameId) {
        for (TableModel table : tableList) {
            if (table.getNameId().equals(nameId)) {
                return table;
            }
        }
        return null;
    }

    // метод наверно нужен при выходе из активити таблицы для заполнения базы данных всеми изминениями
    public void updateTableOfDB(Context context, TableModel tableModel) {

        SQLiteDatabase db = new Database(context).getWritableDatabase();
        String nameTable = tableModel.getNameTable();
        String nameId = tableModel.getNameId();
        String nameSetting = tableModel.getNameSettingId();
        int heightCell = tableModel.getHeightCells();
        int lockHeightCells = tableModel.getLockHeightCells();


        db.beginTransaction();
        //обновление информации
        ContentValues contentValues = new ContentValues();
        contentValues.put(SchemaDB.Table.Cols.IS_ARHIVE, tableModel.getIsArhive());
        contentValues.put(SchemaDB.Table.Cols.NAME_FOR_TABLE, nameTable);
        contentValues.put(SchemaDB.Table.Cols.HEIGHT, heightCell);
        contentValues.put(SchemaDB.Table.Cols.DATE_MODIFY_TABLE, tableModel.getDateModify());
        contentValues.put(SchemaDB.Table.Cols.LOCK_HEIGHT_CELLS, lockHeightCells);

        db.update(SchemaDB.Table.NAME_ACTIVE, contentValues, SchemaDB.Table.Cols.ID_FOR_TABLE + " = ?", new String[]{nameId});
        //обнуление
        db.execSQL("DROP TABLE IF EXISTS " + nameId); // удалил
        db.execSQL(SchemaDB.createTable(nameId));  // создал

        db.delete(tableModel.getNameSettingId(), null, null); // удалил всю инфу о столбах
        // добавляю инфу о столбцах
        try {

            for (ColumnPref columnPref : tableModel.getColumnsPref()) {

                ContentValues value = new ContentValues();
                value.put(SchemaDB.Table.Cols.ID_FOR_COLUMN, columnPref.getNameIdColumn());
                value.put(SchemaDB.Table.Cols.NAME_FOR_COLUMN, columnPref.getName());
                value.put(SchemaDB.Table.Cols.INPUTTYPE, columnPref.getInputType());
                value.put(SchemaDB.Table.Cols.FUNCTION, columnPref.getFunction());

                value.put(SchemaDB.Table.Cols.WIDTH, columnPref.getWidthColumn());

                value.put(SchemaDB.Table.Cols.SIZETEXT_COL, columnPref.getTextSizeTitle());
                value.put(SchemaDB.Table.Cols.STYLE_COL, columnPref.getTextStyleTitle());
                value.put(SchemaDB.Table.Cols.COLOR_COL, columnPref.getTextColorTitle());
                value.put(SchemaDB.Table.Cols.COLOR_COL_RECT, columnPref.getColorTitleRect());

                value.put(SchemaDB.Table.Cols.SIZETEXT_ITEM, columnPref.getTextSizeCell());
                value.put(SchemaDB.Table.Cols.STYLE_ITEM, columnPref.getTextStyleCell());
                value.put(SchemaDB.Table.Cols.COLOR_ITEM, columnPref.getTextColorCell());
                value.put(SchemaDB.Table.Cols.COLOR_ITEM_RECT, columnPref.getColorCellRect());


                db.insert(nameSetting, null, value);
            }
            //добавление колонок в обычную таблицу
            for (ColumnPref columnPref : tableModel.getColumnsPref()) {
                String idColumn = columnPref.getNameIdColumn();
                db.execSQL(SchemaDB.addColumn(nameId, idColumn));
            }

            for (ArrayList<Cell> entry : tableModel.getEntries()) {
                ContentValues values = new ContentValues();
                for (int i = 0; i < tableModel.getColumnsPref().size(); i++) {
                    Cell cell = entry.get(i);
                    if (tableModel.getColumnsPref().get(i).getInputType() == 2) {
                        values.put(tableModel.getColumnsPref().get(i).getNameIdColumn(), String.valueOf(cell.date));
                    } else
                        values.put(tableModel.getColumnsPref().get(i).getNameIdColumn(), cell.text);
                }
                db.insert(nameId, null, values);
            }
            db.setTransactionSuccessful();
        } finally {
            db.endTransaction();
        }
        db.close();

    }

    public void deleteTableOfDB(Context context, TableModel tableModel) {
        SQLiteDatabase db = new Database(context).getWritableDatabase();
        String nameId = tableModel.getNameId();
        String nameSetting = tableModel.getNameSettingId();

        db.beginTransaction();
        try {

            db.delete(SchemaDB.Table.NAME_ACTIVE, SchemaDB.Table.Cols.ID_FOR_TABLE + " = ?", new String[]{nameId});
            db.execSQL("DROP TABLE IF EXISTS " + nameId);
            db.execSQL("DROP TABLE IF EXISTS " + nameSetting);

            db.setTransactionSuccessful();
        } finally {
            db.endTransaction();

        }
    }


}
