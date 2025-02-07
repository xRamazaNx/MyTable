package ru.developer.press.mytable.dialogs;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;
import androidx.appcompat.app.AlertDialog;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import java.io.File;
import java.util.List;
import java.util.Objects;

import ru.developer.press.myTable.R;
import ru.developer.press.mytable.helpers.StaticMethods;
import ru.developer.press.mytable.helpers.TableFileHelper;
import ru.developer.press.mytable.helpers.TableLab;


@SuppressLint("ValidFragment")
public class DialogNameTable extends DialogFragment {
    public static final String KEY_NAME_TABLE = "nametable";
    //    private LinearLayout view;
    // листенер который легче обработать там от куда открыли это окно
    private OnButtonClick onClick;
    private EditText editText;
    private String nameTable;

    // инстант с отправкой аргументов в качестве имени и листенера для обработки позитивБатона
    public static DialogNameTable getDialog(String nameTable, OnButtonClick buttonClick) {

        DialogNameTable dialogNameTable = new DialogNameTable();
        Bundle bundle = new Bundle();
        bundle.putString(KEY_NAME_TABLE, nameTable);
        dialogNameTable.setArguments(bundle);
        dialogNameTable.setClickInterface(buttonClick);

        return dialogNameTable;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        assert getArguments() != null;
        nameTable = getArguments().getString(KEY_NAME_TABLE);
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        editText = new EditText(getContext());
        // задаем текущее имя
        editText.setText(nameTable);
        // паддинг для едиттекст чтоб не был в притык в левой части
        int padding = StaticMethods.convertDpToPixels(16, Objects.requireNonNull(getContext()));
        editText.setPadding(padding, padding / 2, padding, padding / 2);
        // перейти в конец
        editText.setSelection(nameTable.length());
        // включаем фокусирование
        editText.setFocusableInTouchMode(true);
        @SuppressWarnings("ConstantConditions")
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle(R.string.new_name);
        builder.setView(editText);
        builder.setPositiveButton(R.string.rename, null).
                setNegativeButton(R.string.cancel, null);


        AlertDialog alertDialog = builder.create();
        alertDialog.setOnShowListener(dialogInterface -> {
            // позитив батон
            Button button = alertDialog.getButton(AlertDialog.BUTTON_POSITIVE);
            button.setOnClickListener(view1 -> {
                String newName = editText.getText().toString();

                String newNameTableFile = newName + TableFileHelper.TBL;

                boolean isFindEqualNname = false;
                List<File> arrayList = TableLab.get(getActivity()).getTableFiles();
                for (File file : arrayList) {
                    if (file.getName().equals(newNameTableFile)) {
                        isFindEqualNname = true;
                        break;
                    }
                }
//                String nameTable = this.nameTable + TableFileHelper.TBL;
//                if (nameTable.isNoEquals(newNameTableFile))
//                    isFindEqualNname = false;

                if (!isFindEqualNname) {
                    onClick.onDialogClickListener(newName);
                    dialogInterface.dismiss();

                } else
                    Toast.makeText(getActivity(), "Таблица с таким именем уже существует!", Toast.LENGTH_SHORT).show();
            });

        });
        return alertDialog;
    }

    @Override
    public void onDetach() {
        super.onDetach();
        onClick = null;
    }

    // тут назначаем естественно батонлистенер
    private void setClickInterface(OnButtonClick onClick) {
        this.onClick = onClick;
    }

    public interface OnButtonClick {
        void onDialogClickListener(String name);
    }

}
