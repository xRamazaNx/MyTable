package ru.developer.press.mytable.interfaces.table.callback;

import java.util.List;

import ru.developer.press.mytable.helpers.ColumnAttribute;
import ru.developer.press.mytable.helpers.Formula;

public interface FormulaMakeListener {
    List<ColumnAttribute> getColumnAttrs();
    void dismissFormulaDialog();

    boolean verifyIsCircledDepended(Formula formula);

}
