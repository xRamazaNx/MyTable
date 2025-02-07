package ru.developer.press.mytable.dialogs;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.graphics.Color;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.fragment.app.DialogFragment;
import ru.developer.press.myTable.R;
import ru.developer.press.mytable.helpers.ColumnAttribute;
import ru.developer.press.mytable.helpers.Formula;
import ru.developer.press.mytable.helpers.StaticMethods;
import ru.developer.press.mytable.interfaces.table.callback.FormulaMakeListener;

import static android.content.ContentValues.TAG;


public class FormulaDialog extends DialogFragment {

    FormulaMakeListener formulaListener;
    // текущая формула (мы не отправлем ни чего в обратной связи мы просто меняем формулу в колоне которую передали сюда)
    private Formula formula;
    // тут показывается формула
    private TextView formulaTextView;
    // это массив из значений и операндов чтоб потом по очереди опказать их в formulaTextView
    private ArrayList<String> strings = new ArrayList<>();
    // массив атрибутов
    private ArrayList<ColumnAttribute> columnsAttr = new ArrayList<>();
    // массив операндов
    private ArrayList<Character> operands = new ArrayList<>();
    // поле для ввода собственного значения
    private EditText customVal;
    // нужно для того чтобы обнулять customVal если выбрали колону
    // если обнулять то в TextWatcher будет добавляться значение в массив columnsAttr
    // поэтому надо перед добавлением колоны делать его фалсе а потом очищать customVal
    private boolean isCustomValMode = true;

    public static FormulaDialog get(Formula formula, FormulaMakeListener formulaListener) {
        FormulaDialog dateVariableDialog = new FormulaDialog();
        dateVariableDialog.setFormulaMakeListener(formulaListener);
        dateVariableDialog.setFormula(formula);

        return dateVariableDialog;
    }

    private void setFormula(Formula formula) {
        this.formula = formula;
    }

    public void setFormulaMakeListener(FormulaMakeListener formulaMakeListener) {
        this.formulaListener = formulaMakeListener;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {

        ConstraintLayout view = (ConstraintLayout) LayoutInflater.from(getActivity()).inflate(R.layout.function_column, null);
        // инитим все
        init(view);
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity()).
                setView(view).
                setPositiveButton(android.R.string.ok, null).
                setNegativeButton(R.string.cancel, (dialog, which) -> hideKeyboard());

        AlertDialog alertDialog = builder.create();
        // в этом методе можно переопределять кнопки позитив и негатив
        alertDialog.setOnShowListener(dialogInterface -> {

            // позитив батон
            Button button = alertDialog.getButton(AlertDialog.BUTTON_POSITIVE);
            button.setOnClickListener(view1 -> {
                // если формула не правильная
                if (!isValidFormula()) {
                    Toast.makeText(getActivity(), R.string.formula_not_valid, Toast.LENGTH_SHORT).show();
                    return;
                }
//                else if (circularDependency()) {
//                    Toast.makeText(getActivity(), R.string.find_circular_dependency, Toast.LENGTH_SHORT).show();
//                    return;
//                }
                // если все успешно до задаем значения и операнды
                formula.setColumnAttr(columnsAttr);
                formula.setOperands(operands);
                // закрываем клаву а то она не закрывалась чет
                hideKeyboard();
                // оповещаем листенер что окно закрылось
                formulaListener.dismissFormulaDialog();
                dismiss();
            });
        });

        return alertDialog;
    }
    // проверка на циклическую зависимость (пока не используется)
    private boolean circularDependency() {
        Formula formulaByVerify = new Formula();
        formulaByVerify.setColumnAttr(columnsAttr);
        formulaByVerify.setOperands(operands);
        formulaByVerify.columnID = formula.columnID;

        Log.d(TAG, "circularDependency: " + formulaByVerify.columnID);
        return formulaListener.verifyIsCircledDepended(formulaByVerify);
    }

    // проверка правильности формулы
    private boolean isValidFormula() {
        if (operands.size() == columnsAttr.size())
            removeLast();
        return columnsAttr.size() > 1;
    }

    private void hideKeyboard() {
        InputMethodManager inputMethodManager = (InputMethodManager) requireActivity().getSystemService(Activity.INPUT_METHOD_SERVICE);
        assert inputMethodManager != null;
        inputMethodManager.hideSoftInputFromWindow(customVal.getWindowToken(), 0);
    }

    private void init(ConstraintLayout view) {
        formulaTextView = view.findViewById(R.id.function_textView);
        ImageButton clear = view.findViewById(R.id.operand_back);
        clear.setOnClickListener(v -> removeLast());
        customVal = view.findViewById(R.id.edit_custom_value_of_formula);
        customVal.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                // проверяем мы правда задаем значение или просто очищаем перед тем как выбрать колону
                if (!isCustomValMode)
                    return;
                if (operands.size() == columnsAttr.size()) {
                    columnsAttr.add(new ColumnAttribute());
                    addString("");
                }

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void afterTextChanged(Editable s) {
                if (!isCustomValMode)
                    return;
                removeLast();
                ColumnAttribute columnAttributes = new ColumnAttribute();
                columnAttributes.setName(s.toString());
                columnsAttr.add(columnAttributes);
                addString(s.toString());

            }
        });

        LinearLayout columnContainer = view.findViewById(R.id.column_container);
        ImageButton plus = view.findViewById(R.id.operand_plus);
        ImageButton minus = view.findViewById(R.id.operand_minus);
        ImageButton multiply = view.findViewById(R.id.operand_multiply);
        ImageButton divide = view.findViewById(R.id.operand_divide);
        TextView percent = view.findViewById(R.id.operand_percent);

        plus.setOnClickListener(onOperandClick());
        minus.setOnClickListener(onOperandClick());
        multiply.setOnClickListener(onOperandClick());
        divide.setOnClickListener(onOperandClick());
        percent.setOnClickListener(onOperandClick());

        // список колон которые можно выбрать
        List<ColumnAttribute> allColumnNames = formulaListener.getColumnAttrs();

        // параметры текствью для отображения колон
        int width = StaticMethods.convertDpToPixels(75, requireContext());
        int height = StaticMethods.convertDpToPixels(35, requireContext());
        int marg = StaticMethods.convertDpToPixels(4, requireContext());
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(width, height);
        params.setMargins(marg, marg, marg, marg);
        for (ColumnAttribute attr : allColumnNames) {
            // если колона это не колона а значение то переходим дальше
            if (attr.getNameId().equals(ColumnAttribute.VALUE))
                continue;
            TextView textView = new TextView(getActivity());
            textView.setTextColor(Color.BLACK);
            textView.setText(attr.getName());
            textView.setTag(attr);
            textView.setPadding(marg, marg, marg, marg);
            textView.setBackgroundResource(R.drawable.counter_card);
            textView.setLayoutParams(params);
            textView.setGravity(Gravity.CENTER);
            textView.setOnClickListener(onValueClick());
            textView.setSingleLine(true);
//            if (attr.getType() != 1 && attr.getType() != 3) {
//                textView.setClickable(false);
//                textView.setTextColor(getActivity().getResources().getColor(R.color.color_normal));
//            }

            columnContainer.addView(textView);
        }

        // есди формула не пустая
        if (formula.getColumnAttributes().size() > 0) {
            columnsAttr = new ArrayList<>(formula.getColumnAttributes());
            operands = new ArrayList<>(formula.getOperands());
            // задаем сначала первую колону (значение)
            strings.add(columnsAttr.get(0).getName());
            // а потом в массиве проходим по всем
            for (int i = 1; i < columnsAttr.size(); i++) {
                // сначала операнд (так как перед ним всегда есть колона)
                strings.add(operands.get(i - 1).toString());
                strings.add(columnsAttr.get(i).getName());
            }
            updateFormulaText();
        }


    }

    private void removeLast() {
        if (columnsAttr.size() == 0)
            return;
        // если колличество операндов и значений равно
        if (operands.size() == columnsAttr.size()) {
            // удаляем последний операнд
            operands.remove(operands.size() - 1);
        } else {
            // если нет то удаляем значение
            columnsAttr.remove(columnsAttr.size() - 1);
        }
        deleteString();

    }

    private void updateFormulaText() {
        StringBuilder stringBuilder = new StringBuilder();
        for (int i = 0; i < strings.size(); i++) {
            stringBuilder.append(strings.get(i)).append("  ");
        }
        formulaTextView.setText(stringBuilder);
    }

    private void addString(String string) {
        strings.add(string);
        updateFormulaText();
    }

    private void deleteString() {
        if (strings.size() > 0)
            strings.remove(strings.size() - 1);
        updateFormulaText();
    }


    private View.OnClickListener onValueClick() {
        return v -> {
            // сначала фалсе чтоб customVal не портил там чего
            isCustomValMode = false;
            customVal.setText("");
            // и опять тру
            isCustomValMode = true;
            TextView textView = (TextView) v;
            String text = textView.getText().toString();
            // берем аттрибут из таг (мы их задаем при создании текствью)
            ColumnAttribute attrFromTag = (ColumnAttribute) textView.getTag();
            if (strings.size() > 0)
                if (!lastValIsOperand()) {
                    removeLast();
                }
            addString(text);
            columnsAttr.add(attrFromTag);
        };
    }

    private View.OnClickListener onOperandClick() {
        return v -> {
            isCustomValMode = false;
            customVal.setText("");
            isCustomValMode = true;
            char operand = '+';
            switch (v.getId()) {
                case R.id.operand_plus:
                    operand = Formula.PLUS;
                    break;
                case R.id.operand_minus:
                    operand = Formula.MINUS;
                    break;
                case R.id.operand_divide:
                    operand = Formula.DIVIDE;
                    break;
                case R.id.operand_multiply:
                    operand = Formula.MULTIPLY;
                    break;
                case R.id.operand_percent:
                    operand = Formula.PERCENT;
                    break;
            }

// логика для удаления последнего если это надо
            if (columnsAttr.size() > 0) {
                if (operands.size() > 0) {

                    if (lastValIsOperand()) {
                        removeLast();
                    }
                }
                operands.add(operand);
                addString(String.valueOf(operand));
            }
        };
    }


    private boolean lastValIsOperand() {

        boolean lastEqualPlus = strings.get(strings.size() - 1).equals(String.valueOf(Formula.PLUS));
        boolean lastEqualMinus = strings.get(strings.size() - 1).equals(String.valueOf(Formula.MINUS));
        boolean lastEqualMultiply = strings.get(strings.size() - 1).equals(String.valueOf(Formula.MULTIPLY));
        boolean lastEqualDivide = strings.get(strings.size() - 1).equals(String.valueOf(Formula.DIVIDE));
        boolean lastEqualPercent = strings.get(strings.size() - 1).equals(String.valueOf(Formula.PERCENT));

        return lastEqualPlus ||
                lastEqualMinus ||
                lastEqualMultiply ||
                lastEqualDivide ||
                lastEqualPercent;
    }
}
