package ru.developer.press.mytable.dialogs;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AlertDialog;
import android.widget.EditText;
import ru.developer.press.myTable.R;
import ru.developer.press.mytable.helpers.StaticMethods;


@SuppressLint("ValidFragment")
public class DialogNameTable extends DialogFragment {
    public static final String KEY_NAME_TABLE = "nametable";
    //    private LinearLayout view;
    private OnButtonClick onClick;
    private EditText editText;
    private String nameTable;


    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        assert getArguments() != null;
        nameTable = getArguments().getString(KEY_NAME_TABLE);
//        view = (LinearLayout) LayoutInflater.from(getActivity()).inflate(R.layout.cell_edit, null);
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        editText = new EditText(getContext());
        editText.setText(nameTable);
        int padding = StaticMethods.convertDpToPixels(16, getContext());
        editText.setPadding(padding,padding/2,padding,padding/2);
        editText.setSelection(nameTable.length());
//        editText.getRootView().setPadding(40, 0, 40,0);
//        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
//        params.setMargins(20, 10, 20, 0);
//        editText.setLayoutParams(params);
        editText.setFocusableInTouchMode(true);
        @SuppressWarnings("ConstantConditions")
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle(R.string.new_name);
        builder.setPositiveButton(R.string.rename, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                onClick.onDialogClickListener(editText.getText().toString());
                dialogInterface.dismiss();
            }
        });
        builder.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                dialogInterface.dismiss();
            }
        });
        builder.setView(editText);
        return builder.create();
    }

    public static DialogNameTable getDialog(String nameTable) {

        DialogNameTable dialogNameTable = new DialogNameTable();
        Bundle bundle = new Bundle();
        bundle.putString(KEY_NAME_TABLE, nameTable);
        dialogNameTable.setArguments(bundle);

        return dialogNameTable;
    }
//
//    private View.OnClickListener clickListener = new View.OnClickListener() {
//        @Override
//        public void onClick(View v) {
//            onClick.onDialogClickListener(editText.getText().toString());
//        }
//    };

    public interface OnButtonClick {
        void onDialogClickListener(String name);
    }

    @Override
    public void onDetach() {
        super.onDetach();
        onClick = null;
    }

    public void setClickInterface(OnButtonClick onClick){
        this.onClick = onClick;
    }

}
