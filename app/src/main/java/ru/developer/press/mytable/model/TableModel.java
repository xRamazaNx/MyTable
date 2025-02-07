package ru.developer.press.mytable.model;

import java.io.Serializable;
import java.util.ArrayList;

import ru.developer.press.mytable.helpers.Coordinate;

public class TableModel extends TablePrefs implements Serializable {

    //    public static final String HEADER_ID = SchemaDB.Table.HEADER;
//    public static final String ID = SchemaDB.Table.TABLE_NAME;
//    public static final String COLUMN_ID = SchemaDB.Table.COLUMNS;
    public String openName = ""; // файл при открытии
    public transient int heightColumns = 0;
    public transient int widthRows = 0;
    private transient int widthView;
    private ArrayList<Column> columns;
    private ArrayList<Row> rows;
    private String nameTable;

    public TableModel() {
        super();
        columns = new ArrayList<>();
        rows = new ArrayList<>();
        totalAmount = new Row();
        totalAmount.text = "";

    }

//    public TableModel(TablePrefs tablePrefs) {
//        this();
//
//        setCategory(tablePrefs.getCategory());
//        setDateCreated(tablePrefs.getDateCreated());
//        setDateModify(tablePrefs.getDateModify());
//        setIsFill(tablePrefs.getIsFill());
//        setLockHeightCells(tablePrefs.isLockHeightCells());
//        setLockAlwaysFitToScreen(tablePrefs.isLockAlwaysFitToScreen());
//        setDateType(tablePrefs.getDateType());
//        setTotalAmountEnable(tablePrefs.isTotalAmountEnable());
//        totalAmount = tablePrefs.totalAmount;
//    }

    public ArrayList<Row> getRows() {
        return rows;
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

    public void addStroke(int addStrokeIndex, int copyPrefStrokeIndex) {
        boolean isNewTable = copyPrefStrokeIndex < 0;

        Row row = new Row();

        for (int i = 0; i < columns.size(); i++) {
            Cell cell = new Cell();
            if (!isNewTable) {
                Cell copyLastEntryCell = rows.get(copyPrefStrokeIndex).getCellAtIndex(i);
                cell.copyPrefs(copyLastEntryCell);
            }
            row.getCells().add(cell);
        }

        if (!isNewTable) {
            Row copyRowPrefLast = rows.get(copyPrefStrokeIndex);
            row.copyPrefs(copyRowPrefLast);
            rows.add(addStrokeIndex, row);
        } else {
            rows.add(row);
        }

    }

    public void addColumn(int addColumnIndex,
                          int copyPrefsColumnIndex) {

        // добавляем ячейки в столб
        for (int i = 0; i < rows.size(); i++) {
            Cell cell = new Cell();
            Cell copyPref = rows.get(i).getCellAtIndex(copyPrefsColumnIndex);
            cell.copyPrefs(copyPref);
            rows.get(i).getCells().add(addColumnIndex, cell);
        }
        // добавляем саму колону
        Column column = new Column();
        column.copyColumn(columns.get(copyPrefsColumnIndex));
        columns.add(addColumnIndex, column);
        // добавляем ячейку для тоталамоунт
        Cell totalAmountCell = new Cell();
        totalAmountCell.copyPrefs(totalAmount.getCellAtIndex(copyPrefsColumnIndex));
        totalAmount.getCells().add(addColumnIndex, totalAmountCell);
//        // добавляем в тотал ячейку
//        Cell totalAmountCell = new Cell();
//        Cell copyPref = totalAmount.getCellAtIndex(copyPrefsColumnIndex);
//        totalAmountCell.copyPrefs(copyPref);
//        totalAmount.getCells().add(addColumnIndex, totalAmountCell);

    }

//    public TablePrefs getPrefs() {
//
//        TablePrefs tablePrefs = new TablePrefs();
//        tablePrefs.setCategory(getCategory());
//        tablePrefs.setDateCreated(getDateCreated());
//        tablePrefs.setDateModify(getDateModify());
//        tablePrefs.setIsFill(getIsFill());
//        tablePrefs.setLockHeightCells(isLockHeightCells());
//        tablePrefs.setLockAlwaysFitToScreen(isLockAlwaysFitToScreen());
//        tablePrefs.setDateType(getDateType());
//        tablePrefs.setTotalAmountEnable(isTotalAmountEnable());
//        tablePrefs.totalAmount = totalAmount;
//
//        return tablePrefs;
//    }

    public void deleteStroke(int i) {
        rows.remove(i);
    }

    public Column getColumnAtId(String id) {
        for (Column column :
                columns) {
            if (column.getNameIdColumn().equals(id))
                return column;
        }
        return null;
    }

    public void fitToScreen(float ignoredOffset) {

        float tableWidth = widthView - widthRows - ignoredOffset;
        int width = getWidthTable();
        int checkWidth = 0;
        // если ширина таблицы больше чем экран
        while (width > tableWidth) {
            width = 0;
            // проходимся по каждой колоне и минисуем на 1 пока ширина таблицы не получится меньше чем экран
            for (int i = 0; i < columns.size(); i++) {
                Column column = columns.get(i);
                column.setWidth(this, -30);
                width += column.getWidth();
            }
            // когда столбов слишком много то по ширине экрана не получается изза паддингов поэтому ширина ни когда не будет равняться  ширине экрана
            if (width == checkWidth)
                break;
            checkWidth = width;
//            Log.d(TAG, "fitToScreen: " + width);
        }
        // если ширина таблицы меньше чем экран
        // +1 для сдвига на 1 пиксель в лево от правой части экрана
        while (width + 1 < tableWidth) {
            // проходим по каждому столбцу плюсуя на 1 попутно проверяя после каждого измененного столбца
            for (int i = 0; i < columns.size(); i++) {
                Column column = columns.get(i);
                column.setWidth(this, 1);
                width++;
                if (width == tableWidth) break;
            }
            // когда столбов слишком много то по ширине экрана не получается изза паддингов поэтому ширина ни когда не будет равняться  ширине экрана
            if (width == checkWidth)
                break;
            checkWidth = width;
//            Log.d(TAG, "fitToScreen: add +1 " + width);
        }
    }

    public int getWidthTable() {
        int widthTable = 0;
        for (Column col :
                columns) {
            widthTable += col.getWidth();
        }
        return widthTable;
    }

    public int getHeightTable() {
        int heightTable = 0;
        for (Row row :
                rows) {
            heightTable += row.heightStroke;
        }
        if (isTotalAmountEnable())
            heightTable += totalAmount.heightStroke;
//        heightTable += heightColumns;

        return heightTable;
    }

    public int getWidthView() {
        return widthView;
    }

    public void setWidthView(int widthPixels) {
        widthView = widthPixels;
    }

    public Coordinate getCoordinateCell(int indexRow, int indexColumn) {
        Coordinate coordinate = new Coordinate();

        for (int i = 0; i < indexRow + 1; i++) { //  +1 чтоб учитывала и саму строку
            Row row = rows.get(i);
            coordinate.endY += row.heightStroke;
            coordinate.startY = coordinate.endY - row.heightStroke;
        }
        for (int i = 0; i < indexColumn + 1; i++) {
            Column column = columns.get(i);
            coordinate.endX += column.getWidth();
            coordinate.startX = coordinate.endX - column.getWidth();
        }

        return coordinate;
    }

    public int findIndexRowFormCoordinate(float y) {
        int index = -1;
        float strokeEnd = 0;
        for (int i = 0; i < rows.size(); i++) {
            strokeEnd += rows.get(i).heightStroke;
            float v = y - heightColumns;
            if (strokeEnd > v) {
                index = i;
                break;
            }
        }
        return index;
    }

    public int[] findIndexCellFormCoordinate(float x, float y) {
        int[] index = new int[2];
        int strokeEnd = 0;
        for (int i = 0; i < rows.size(); i++) {
            strokeEnd += rows.get(i).heightStroke;
            if (strokeEnd > y - heightColumns) {
                index[0] = i;
                break;
            }
        }
        int columnEnd = 0;
        for (int i = 0; i < columns.size(); i++) {
            columnEnd += (int) columns.get(i).getWidth();
            if (columnEnd > x - widthRows) {
                index[1] = i;
                break;
            }
        }
        return index;
    }

    public int findIndexColumnFormCoordinate(float x) {
        int index = -1;
        float endX = 0;
        for (int i = 0; i < columns.size(); i++) {
            Column column = columns.get(i);
            endX += column.getWidth();
            if (x - widthRows <= endX) {
                index = i;
                break;
            }
        }
        return index;
    }

    public void deleteColumn(Column column) {
        int indexColumn = column.index;
        for (Row row : rows) {
            row.getCells().remove(indexColumn);
        }
        columns.remove(column);
        totalAmount.getCells().remove(indexColumn);
    }

    public void addNewColumn(Column column) {
        columns.add(column);
        Cell e = new Cell();
        e.pref.copyPref(column.pref);
        totalAmount.getCells().add(e);

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
