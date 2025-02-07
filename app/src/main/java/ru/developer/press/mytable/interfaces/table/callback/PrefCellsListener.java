package ru.developer.press.mytable.interfaces.table.callback;

import java.util.List;

import ru.developer.press.mytable.helpers.ColumnAttribute;
import ru.developer.press.mytable.helpers.Formula;
import ru.developer.press.mytable.model.Pref;
import ru.developer.press.mytable.table.TablePresenter;

public abstract class PrefCellsListener {
    public int type;

    public Pref pref = new Pref();
//    public int sizeText;
//    public int bold;
//    public int italic;
//    public int colorText;
//    public int colorBack;
//    public int paddingLeft;
//    public int paddingRight;
//    public int paddingUp;
//    public int paddingDown;

    public Formula formula;
    public TablePresenter.SelectMode mode;
    public boolean checkCellPrefs = true;
    public int dateVariable;

    public abstract void setType(int type);

    public abstract void setFormula(Formula formula);

    public abstract void setSizeText(int size);

    public abstract void setBold(int bold);

    public abstract void setItalic(int italic);

    public abstract void setColorText(int color);

    public abstract void setColorBack(int color);

    public abstract void updatePadding();

    public abstract void closeWindow();

    public abstract List<ColumnAttribute> getColumnAttrs();

    public abstract void update();

    public abstract boolean verifyIsCircledDepended(Formula formula);

    public abstract int getSelectedColumnSize();

    public abstract void setDateVariable(int variable);
}
