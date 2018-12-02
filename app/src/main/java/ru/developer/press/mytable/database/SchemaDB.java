package ru.developer.press.mytable.database;

public class SchemaDB {

    public static final String PREFERENCES = "preferences";

    public static final class Table {

        public static final String NAME = "name";
        public static final String COLUMN = "column";

        public static final String NAME_ACTIVE = "ACTIVE";
        public static final String NAME_ARHIVE = "ARCHIVE";
        public static final String NAME_SETTING = "namesetting";

        public static final String SETTING = "setting"; // добавляется в конец названия таблицы для настроек

        public static final class Cols {

            public static final String ID = "_id";
            public static final String IS_ARHIVE = "isarhive";
            public static final String IS_FILL = "isfill"; // по ширине экрана

            public static final String ID_FOR_TABLE = "id_name";
            public static final String ID_FOR_TABLE_SETTING = "id_name_setting";
            public static final String NAME_FOR_TABLE = "nametable";

            public static final String DATE_CREATE_TABLE = "datecreated";
            public static final String DATE_MODIFY_TABLE = "datemodify";

            public static final String ID_FOR_COLUMN = "idcolumn";
            public static final String NAME_FOR_COLUMN = "namecolumn";

            public static final String INPUTTYPE = "inputtype";
            public static final String FUNCTION = "function";
            public static final String WIDTH = "width";
            public static final String HEIGHT = "height";


            public static final String SIZETEXT_COL = "sizetextcol";
            public static final String SIZETEXT_ITEM = "sizetextitem";

            public static final String STYLE_COL = "stylecol";
            public static final String STYLE_ITEM = "styleitem";

            public static final String COLOR_COL = "colorcol";
            public static final String COLOR_ITEM = "coloritem";
            public static final String LOCK_HEIGHT_CELLS = "lockheightcells";
            public static String COLOR_ITEM_RECT = "coloritemrect";
            public static String COLOR_COL_RECT = "colorcolrect";
        }
    }

    public static String createTable(String name) {
        return "create table " + name + " (" +
                Table.Cols.ID + " integer primary key autoincrement" +
                ");";
    }


    public static String createTableSetting(String name) {

        return "create table " + name + " (" +
                Table.Cols.ID + " integer primary key autoincrement, " +

                Table.Cols.ID_FOR_COLUMN + ", " +

                Table.Cols.NAME_FOR_COLUMN + ", " +
                Table.Cols.INPUTTYPE + ", " +
                Table.Cols.FUNCTION + ", " +
                Table.Cols.WIDTH + ", " +

                Table.Cols.SIZETEXT_COL + ", " +
                Table.Cols.STYLE_COL + ", " +
                Table.Cols.COLOR_COL + ", " +
                Table.Cols.COLOR_COL_RECT + ", " +

                Table.Cols.SIZETEXT_ITEM + ", " +
                Table.Cols.STYLE_ITEM + ", " +
                Table.Cols.COLOR_ITEM + ", " +
                Table.Cols.COLOR_ITEM_RECT +

                ");";

    }

    public static String addColumn(String table, String column) {
        return "alter table " + table + " add column " + column;
    }
}
