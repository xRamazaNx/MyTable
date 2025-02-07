package ru.developer.press.mytable.interfaces.table.callback;

public interface DialogShowListener {
    void showFormulaDialog(FormulaMakeListener formulaMakeListener);

    void showDateCheckDialog(int variable, DateVariableChangeListener dateVariableChangeListener);
}
