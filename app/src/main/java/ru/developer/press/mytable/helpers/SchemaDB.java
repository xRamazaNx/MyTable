package ru.developer.press.mytable.helpers;

import ru.developer.press.mytable.table.model.TableModel;

public class SchemaDB {

    public static final class Table {

        public static final String NAME = "cells";

        public static final String SETTING = "setting"; // добавляется в конец названия таблицы для настроек

        public static final class Cols {

            public static final String ID = "_id";

            public static final String ID_FOR_COLUMN = "id_column";
            public static final String NAME_FOR_COLUMN = "name_column";

            public static final String INPUTTYPE = "input_type";
            public static final String FUNCTION = "function";
            public static final String WIDTH = "width";
            public static final String HEIGHT = "height";


            public static final String SIZE_TEXT = "size_text";

            public static final String BOLD = "bold";

            public static final String COLOR_TEXT = "color_item";
            public static final String TEXT = "text";
            public static final String ITALIC = "italic";
            public static final String HEADER = "header";
            public static String COLOR_BACK = "color_back";
        }
    }


    // таблица создает ячейки
    public static String createTable() {
        return "create table " + TableModel.ID + " (" +
                Table.Cols.ID + " integer primary key autoincrement, " +

                Table.Cols.TEXT + ", " +

                Table.Cols.SIZE_TEXT + ", " +
                Table.Cols.BOLD + ", " +
                Table.Cols.ITALIC + ", " +
                Table.Cols.COLOR_TEXT + ", " +
                Table.Cols.COLOR_BACK +

                ");";

    }
    public static String createTableSetting() {

        return "create table " + TableModel.SETTING_ID+ " (" +
                Table.Cols.ID + " integer primary key autoincrement, " +

                Table.Cols.ID_FOR_COLUMN + ", " +

                Table.Cols.NAME_FOR_COLUMN + ", " +
                Table.Cols.INPUTTYPE + ", " +
                Table.Cols.FUNCTION + ", " +
                Table.Cols.WIDTH + ", " +

                Table.Cols.SIZE_TEXT + ", " +
                Table.Cols.BOLD + ", " +
                Table.Cols.ITALIC + ", " +
                Table.Cols.COLOR_TEXT + ", " +
                Table.Cols.COLOR_BACK +

                ");";

    }
    public static String createHeaders() {
        return "create table " + TableModel.HEADER_ID + " (" +
                Table.Cols.ID + " integer primary key autoincrement, " +
                Table.Cols.HEIGHT + ", " +
                Table.Cols.SIZE_TEXT + ", " +
                Table.Cols.BOLD + ", " +
                Table.Cols.ITALIC + ", " +
                Table.Cols.COLOR_TEXT + ", " +
                Table.Cols.COLOR_BACK +
                ");";

    }
}
