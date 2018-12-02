package ru.developer.press.mytable.database;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class Database extends SQLiteOpenHelper {

    private static final int VERSION = 2;//
    private static final String DATABASE_NAME = "myDataBase.db";

    public Database(Context context) {
        super(context, DATABASE_NAME, null, VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {

        updateDB(db, 0);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        updateDB(db, oldVersion);
    }


    private void updateDB(SQLiteDatabase db, int old) {

        if (old < 1) {
            db.execSQL("create table " + SchemaDB.Table.NAME_ACTIVE + " (" +
                    SchemaDB.Table.Cols.ID + " integer primary key autoincrement, " +
                    SchemaDB.Table.Cols.IS_FILL + ", " + // 1 в архиве 0 в активе
                    SchemaDB.Table.Cols.IS_ARHIVE + ", " + // 1 в архиве 0 в активе
                    SchemaDB.Table.Cols.ID_FOR_TABLE + ", " +
                    SchemaDB.Table.Cols.ID_FOR_TABLE_SETTING + ", " +
                    SchemaDB.Table.Cols.NAME_FOR_TABLE + ", " +
                    SchemaDB.Table.Cols.HEIGHT + ", " +
                    SchemaDB.Table.Cols.DATE_CREATE_TABLE + ", " +
                    SchemaDB.Table.Cols.DATE_MODIFY_TABLE +
                    ");"
            );
//            db.execSQL("create table " + SchemaDB.Table.NAME_ARHIVE + " (" +
//                    SchemaDB.Table.Cols.ID + " integer primary key autoincrement, " +
//                    SchemaDB.Table.Cols.ID_FOR_TABLE + ", " +
//                    SchemaDB.Table.Cols.ID_FOR_TABLE_SETTING + ", " +
//                    SchemaDB.Table.Cols.NAME_FOR_TABLE + ", " +
//                    SchemaDB.Table.Cols.DATE_CREATE_TABLE + ", " +
//                    SchemaDB.Table.Cols.DATE_MODIFY_TABLE +
//                    ");"
//            );

//            db.execSQL("create table " + SchemaDB.Table.NAME_SETTING + " (" +
//                    SchemaDB.Table.Cols.ID + " integer primary key autoincrement, " +
//                    SchemaDB.Table.Cols.ID_FOR_TABLE +
//                    ");"
//            );
        }
        if (old < 2) {
            db.execSQL("ALTER TABLE "+SchemaDB.Table.NAME_ACTIVE +" ADD COLUMN " + SchemaDB.Table.Cols.LOCK_HEIGHT_CELLS + " INTEGER DEFAULT 0");
        }
    }
}
