package ru.developer.press.mytable.dialogs;

import android.app.AlertDialog;
import android.app.Dialog;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;
import android.widget.Toast;
import ru.developer.press.myTable.R;

import java.util.Calendar;

import ru.developer.press.mytable.helpers.StaticMethods;
import ru.developer.press.mytable.interfaces.table.callback.DateVariableChangeListener;

public class DateVariableDialog extends DialogFragment {
    private static final String VARIABLE_KEY = "variable";
    DateVariableChangeListener dateVariableChangeListener;
    private int variable;

    public static DateVariableDialog get(int variable, DateVariableChangeListener dateVariableChangeListener) {
        DateVariableDialog dateVariableDialog = new DateVariableDialog();
        Bundle bundle = new Bundle();
        bundle.putInt(VARIABLE_KEY, variable);
        dateVariableDialog.setArguments(bundle);
        dateVariableDialog.setDateVariableChangeListener(dateVariableChangeListener);

        return dateVariableDialog;
    }

    public void setDateVariableChangeListener(DateVariableChangeListener dateVariableChangeListener) {
        this.dateVariableChangeListener = dateVariableChangeListener;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        if (getArguments() != null) {
            variable = getArguments().getInt(VARIABLE_KEY);
        } else {
            variable = 0;
            Toast.makeText(getActivity(), "Не удалось получить текущую настройку", Toast.LENGTH_SHORT).show();
        }
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

        Calendar calendar = Calendar.getInstance();

        // заполняем массив вариантами даты для выбора
        String[] dates = new String[12];
        for (int i = 0; i < dates.length; i++) {
            String date = StaticMethods.getDateOfMillis(calendar.getTimeInMillis(),i);
            dates[i] = date;
        }
        builder.setTitle(R.string.date_variable_change).
                // штатный инструмент для показа списка который мы заполнили выше
                setSingleChoiceItems(dates, variable, (dialog, which) -> variable = which).
                // отправляем в листенер текущее значение которое выбрано
                setPositiveButton(android.R.string.ok, (dialog, which) -> dateVariableChangeListener.setVariable(variable)).
                setNegativeButton(R.string.cancel, (dialog, which) -> dismiss());



        return builder.create();
    }

}
