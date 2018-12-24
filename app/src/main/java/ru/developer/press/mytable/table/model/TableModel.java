package ru.developer.press.mytable.table.model;

import java.io.Serializable;
import java.util.ArrayList;

import ru.developer.press.mytable.helpers.SchemaDB;

public class TableModel extends TablePrefs implements Serializable {

    public static final String HEADER_ID = SchemaDB.Table.Cols.HEADER;
    public static final String ID = SchemaDB.Table.NAME;
    public static final String SETTING_ID = SchemaDB.Table.SETTING;

    public int heightTable;
    public int widthTable;
    private ArrayList<Column> columns;
    private ArrayList<Header> headers;

    public String openName = ""; // файл при открытии
    private String nameTable;

    public int heightColumns = 0;
    public int widthHeaders = 0;


    public TableModel() {
        columns = new ArrayList<>();
        headers = new ArrayList<>();

    }

    public TableModel(TablePrefs tablePrefs) {
        this();

        setCategory(tablePrefs.getCategory());
        setDateCreated(tablePrefs.getDateCreated());
        setDateModify(tablePrefs.getDateModify());
        setIsFill(tablePrefs.getIsFill());
        setLockHeightCells(tablePrefs.getLockHeightCells());
        setDateType(tablePrefs.getDateType());
    }

    public ArrayList<Header> getHeaders() {
        return headers;
    }

    public ArrayList<Column> getColumns() {
        return columns;
    }


    public String getNameTable() {
        return nameTable;
    }

    public void setNameTable(String nameTable) {
        this.nameTable = nameTable;
    }

    public void addNewStroke(int addStrokeIndex, int copyPrefStrokeIndex) {
        boolean isNewTable = addStrokeIndex < 0;

        Header header = new Header();

        for (int i = 0; i < columns.size(); i++) {
            Cell cell = new Cell();
            if (!isNewTable) {
                Cell copyLastEntryCell = headers.get(copyPrefStrokeIndex).getCell(i);
                cell.copyPrefs(copyLastEntryCell);
            }
            header.getCells().add(cell);
        }

        if (!isNewTable) {
            Header copyHeaderPrefLast = headers.get(copyPrefStrokeIndex);
            header.copyPrefs(copyHeaderPrefLast);
        }

        if (isNewTable) {
            headers.add(header);
        }else {
            headers.add(addStrokeIndex, header);
        }


    }

    public Column addColumn(int addColumnIndex, int copyPrefsColumnIndex) {

        // добавляем ячейки в столб
        for (int i = 0; i < headers.size(); i++) {
            Cell cell = new Cell();
            Cell copyPref = headers.get(i).getCell(copyPrefsColumnIndex);
            cell.copyPrefs(copyPref);
            headers.get(i).getCells().add(addColumnIndex, cell);
        }

        Column column = new Column();
        column.copyColumn(columns.get(copyPrefsColumnIndex));
        columns.add(addColumnIndex, column);

        return column;
    }

    public TablePrefs getPrefs() {

        TablePrefs tablePrefs = new TablePrefs();
        tablePrefs.setCategory(getCategory());
        tablePrefs.setDateCreated(getDateCreated());
        tablePrefs.setDateModify(getDateModify());
        tablePrefs.setIsFill(getIsFill());
        tablePrefs.setLockHeightCells(getLockHeightCells());
        tablePrefs.setDateType(getDateType());

        return tablePrefs;
    }

    public void deleteStroke(int i) {
        headers.remove(i);
    }

    /*
            в маин активити (в он ресум обязаловка)
    1 проходим по циклу в папке MyTable
    2 ищем только файлы с расширением .tbl
    3 берем от туда название таблицы (пока) (потом может будет что то еще)
    4 показываем это все.
            в табле активити
    1 открываем файл с этим именем.
    2 извлекаем все по категориям и папкам.
    3 пишем все это в модель и отображаем.
    4 в он паус в отдельном потоке сохраняем файл всегда.
        удаляем предыдущий и сохраняем новый.
     */
}
