package ru.developer.press.mytable.model;

import java.util.ArrayList;
import java.util.Calendar;

import ru.developer.press.mytable.database.SchemaDB;

public class TableModel {

    private ArrayList <ColumnPref> columnsPref;
    private ArrayList <ArrayList<Cell>> entries;
    private ArrayList<Cell> headers;


    private int isArhive;// 1 в архиве 0 в активе
    private int isFill;// 1 уместить на экране 0 отключить
    private String nameId;
    private String nameSettingId;
    private String nameTable;
    private String dateCreated;
    private String dateModify;
    private int heightCells;

    private int lockHeightCells;

    public TableModel(){
        columnsPref = new ArrayList<>();
        entries = new ArrayList<>();
        headers = new ArrayList<>();
        //дата создания
        dateCreated = String.valueOf(Calendar.getInstance().getTimeInMillis());
        //в дате изминения так же дата создания
        dateModify = dateCreated;
        //и ид таблицы будет дата создания

        isArhive = 0;
        isFill = 0;
        nameId = SchemaDB.Table.NAME+dateCreated;
        nameSettingId = SchemaDB.Table.SETTING+dateCreated;
        heightCells = 70;

    }

    public ArrayList<ColumnPref> getColumnsPref() {
        return columnsPref;
    }
    public void setColumnsPref(ArrayList<ColumnPref> columnPrefs) {
        this.columnsPref = columnPrefs;
    }

    public int getIsArhive() {
        return isArhive;
    }
    public void setIsArhive(int isArhive) {
        this.isArhive = isArhive;
    }

    public int getIsFill() {
        return isFill;
    }
    public void setIsFill(int isFill) {
        this.isFill = isFill;
    }

    public String getNameId() {
        return nameId;
    }
    public void setNameId(String nameId) {
        this.nameId = nameId;
    }

    public String getNameSettingId() {
        return nameSettingId;
    }
    public void setNameSettingId(String nameSettingId) {
        this.nameSettingId = nameSettingId;
    }

    public String getNameTable() {
        return nameTable;
    }
    public void setNameTable(String nameTable) {
        this.nameTable = nameTable;
    }

    public String getDateCreated() {
        return dateCreated;
    }
    public void setDateCreated(String dateCreated) {
        this.dateCreated = dateCreated;
    }

    public String getDateModify() {
        return dateModify;
    }
    public void setDateModify(String dateModify) {
        this.dateModify = dateModify;
    }

    public int getHeightCells() {
        return heightCells;
    }

    public void setHeightCells(int heightCells) {
        this.heightCells = heightCells;
    }

    public ArrayList<ArrayList<Cell>> getEntries() {
        return entries;
    }

    public void setEntries(ArrayList<ArrayList<Cell>> entries) {
        this.entries = entries;
    }

    public int getLockHeightCells() {
        return lockHeightCells;
    }

    public void setLockHeightCells(int lockHeightCells) {
        this.lockHeightCells = lockHeightCells;
    }

    public ArrayList<Cell> getHeaders() {
        return headers;
    }

    public void addStroke(int addStrokeIndex) {
        ArrayList <Cell> entry = new ArrayList<>();
        for (int i = 0; i < columnsPref.size(); i++) {
            entry.add(new Cell());
        }
        entries.add(addStrokeIndex, entry);
    }

    public void addColumn(int addColumnIndex, ColumnPref columnPref) {
        columnsPref.add(addColumnIndex, columnPref);
    }
}
