package ru.developer.press.mytable.table.model;

import android.graphics.Color;
import java.util.Random;
import ru.developer.press.mytable.helpers.SchemaDB;

public class Column extends CellAbstract {

    private String nameIdColumn;
    private int inputType; //
    private String function; //
//    public int type = 0;

    public Column copyColumn(Column copy) {
        copyPrefs(copy);

        inputType = copy.inputType;
        function = copy.function;
//        type = copy.type;
        return this;
    }

    public Column() {
        super();
        text = "Новый столбец";
        int random = new Random().nextInt();
        if (random < 0) random = -random;
        nameIdColumn = SchemaDB.Table.Cols.NAME_FOR_COLUMN + random;


        inputType = 0;
        function = "0"; //  значит нет функций
        width = 150;
        sizeFont = 16; // позиция в списке размеров шрифта
        colorFont = Color.parseColor("#181818");
        colorBack = Color.parseColor("#f1f1f1");

    }


    public String getNameIdColumn() {
        return nameIdColumn;
    }

    public void setNameIdColumn(String nameIdColumn) {
        this.nameIdColumn = nameIdColumn;
    }

    public int getInputType() {
        return inputType;
    }

    public void setInputType(int inputType) {
        this.inputType = inputType;
    }

    public String getFunction() {
        return function;
    }

    public void setFunction(String function) {
        this.function = function;
    }

    public boolean isNotEquals(Column col) {
        return super.isNotEquals(col) ||
//                type != col.type ||
                !function.equals(col.function) ||
                inputType != col.inputType;
    }

    @Override
    protected int getHeight() {
        return (int) height;
    }

    public void select(TableModel tableModel){
        int index = 0;
        for (int i = 0; i < tableModel.getColumns().size(); i++) {
            if (this == tableModel.getColumns().get(i)) {
                index = i;
                break;
            }
        }
        super.select();
        for (int i = 0; i < tableModel.getHeaders().size(); i++) {
            Header header = tableModel.getHeaders().get(i);
            header.getCell(index).select();
        }
    }
    public void unSelect(TableModel tableModel){
        int index = 0;
        for (int i = 0; i < tableModel.getColumns().size(); i++) {
            if (this == tableModel.getColumns().get(i)) {
                index = i;
                break;
            }
        }
        super.unSelect();
        for (int i = 0; i < tableModel.getHeaders().size(); i++) {
            Header header = tableModel.getHeaders().get(i);
            header.getCell(index).unSelect();
        }
    }
}
