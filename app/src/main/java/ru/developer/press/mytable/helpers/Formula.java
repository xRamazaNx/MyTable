package ru.developer.press.mytable.helpers;

import com.google.gson.annotations.SerializedName;

import org.apache.commons.lang.builder.EqualsBuilder;

import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.ArrayList;
import java.util.Locale;

import ru.developer.press.mytable.model.Cell;
import ru.developer.press.mytable.model.Column;
import ru.developer.press.mytable.model.TableModel;

public class Formula {
    public static final char PLUS = '+';
    public static final char MINUS = '-';
    public static final char MULTIPLY = '*';
    public static final char DIVIDE = '/';
    public static final char PERCENT = '%';
    public static DecimalFormat numberFormat = getNumberFormat();
    //ид колоны в которой эта формула
    @SerializedName("cID")
    public String columnID;
    @SerializedName("ca")
    private ArrayList<ColumnAttribute> columnAttributes = new ArrayList<>();
    @SerializedName("op")
    private ArrayList<Character> operands = new ArrayList<>();

    // превращает строку в цифры если это возможно
    public static Double parseStringToNumber(String number) {

        // некоторая логика для точек с запятыми
        if (number.length() > 0) {
            if (number.equals(".") || number.equals(","))
                number = "0.";
        } else number = "0";
        try {
            double d = Double.parseDouble(number);
            return Double.parseDouble(numberFormat.format(d));

        } catch (NumberFormatException n) {
            return Double.parseDouble(numberFormat.format(0));
        }
    }

    // копирование формулы
    public Formula copy(Formula other) {
        columnAttributes.clear();
        operands.clear();

        columnID = other.columnID;
        for (ColumnAttribute columnAttr : other.columnAttributes) {
            columnAttributes.add(new ColumnAttribute(columnAttr));
        }
        operands.addAll(other.operands);
        return this;
    }

    public String getValueFromFormula(TableModel tableModel, int indexRow) {
        double value;
        // лист где собираются значение для того что бы потом по операндам пройтись и вычеслить
        ArrayList<Double> valuesNumber = new ArrayList<>();
        try {
            // проходим по аттрибутам колон которые есть в формуле
            for (int i = 0; i < columnAttributes.size(); i++) {
                // текущая формула
                ColumnAttribute columnAttributes = this.columnAttributes.get(i);
                double val;
                // проверка атрибут является значением или нет
                if (columnAttributes.getNameId().equals(ColumnAttribute.VALUE)) {
                    val = Long.parseLong(columnAttributes.getName());
                    // если атрибут не значение а колона
                } else {
                    // колона из формулы
                    Column col = tableModel.getColumnAtId(columnAttributes.getNameId());
                    // ячейка в этой колоне (в строке которую задали в методе)
                    Cell cellAtIndex = tableModel.getRows().get(indexRow).getCellAtIndex(col.index);
                    // сначала пишем значение самой ячейки
                    val = cellAtIndex.getNumber();
                    // если ячейка сама яаляется формулой и не находится в колоне с формулой
                    // то берем значение по формуле
                    if (col.getInputType() == 3 && !col.getNameIdColumn().equals(columnID)) {
                        val = parseStringToNumber(col.getFormula().getValueFromFormula(tableModel, indexRow));
                    }
                }
                valuesNumber.add(val);
            }

            // назначаем сначала первое значение
            value = valuesNumber.get(0);
            for (int i = 1; i < valuesNumber.size(); i++) {
                // проходим по значениям и пользуем для них операнды
                value = operating(value, operands.get(i - 1), valuesNumber.get(i));
            }

        } catch (NumberFormatException e) {
            value = 0;
        } catch (IndexOutOfBoundsException indexOut) {
            value = 0;
        } catch (NullPointerException n) {
            return "Ошибка, проверьте формулу!";
        }
        return numberFormat.format(value);
    }

    // разовая операция вычесления
    private double operating(double val1, char operand, double val2) {
        double val = 0;
        switch (operand) {
            case MULTIPLY:
                val = val1 * val2;
                break;
            case DIVIDE:
                if (val1 == 0 || val2 == 0)
                    val = 0;
                else
                    val = val1 / val2;
                break;
            case PLUS:
                val = val1 + val2;
                break;
            case MINUS:
                val = val1 - val2;
                break;
            case PERCENT:
                val = (val1 / 100) * val2;
                break;
        }
        return val;
    }

    public void setColumnAttr(ArrayList<ColumnAttribute> columnIds) {
        this.columnAttributes = columnIds;
    }

    public ArrayList<ColumnAttribute> getColumnAttributes() {
        return columnAttributes;
    }

    public ArrayList<Character> getOperands() {
        return operands;
    }

    public void setOperands(ArrayList<Character> operands) {
        this.operands = operands;
    }

    public boolean equals(Formula formula) {

        if (!columnID.equals(formula.columnID))
            return false;

        if (!EqualsBuilder.reflectionEquals(columnAttributes, formula.columnAttributes))
            return false;
        return EqualsBuilder.reflectionEquals(operands, formula.operands);

    }

    // обновляет имена колон перед тем как показать имена колон а то пока захотим изменить формулу мало ли там изменили имена
    public void updateColumnNames(TableModel tableModel) {
        for (ColumnAttribute colAttr : columnAttributes) {
            Column columnAtId = tableModel.getColumnAtId(colAttr.getNameId());
            if (columnAtId != null)
                colAttr.setName(columnAtId.text);
        }
    }

    // создание децимал
    private static DecimalFormat getNumberFormat(){
        DecimalFormat decimalFormat  = new DecimalFormat("0.##", DecimalFormatSymbols.getInstance(Locale.US));
        decimalFormat.setMaximumFractionDigits(3);
        decimalFormat.setRoundingMode(RoundingMode.HALF_EVEN);
        decimalFormat.setGroupingUsed(false);
        return decimalFormat;
    }
}
