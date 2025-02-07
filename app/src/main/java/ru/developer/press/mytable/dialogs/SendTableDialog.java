package ru.developer.press.mytable.dialogs;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;
import androidx.core.content.FileProvider;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.LinearLayout;

import java.io.File;
import java.util.Objects;

import ru.developer.press.myTable.R;
import ru.developer.press.mytable.interfaces.SendTableGetter;

public class SendTableDialog extends DialogFragment {
    // геттер который берет то что выбрал пользователь (фото или ексел)
    SendTableGetter sendTableGetter;

    public static SendTableDialog get(SendTableGetter sendTableGetter) {
        SendTableDialog dateVariableDialog = new SendTableDialog();
        dateVariableDialog.setSendTableGetter(sendTableGetter);
        return dateVariableDialog;
    }

    public void setSendTableGetter(SendTableGetter sendTableGetter) {
        this.sendTableGetter = sendTableGetter;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        View view = LayoutInflater.from(getActivity()).inflate(R.layout.send_list_layout, null);
        LinearLayout pic = view.findViewById(R.id.send_pic);
        LinearLayout excel = view.findViewById(R.id.send_excel);
        View.OnClickListener clickPic = v -> {
            // берем файл картинки
            File file = sendTableGetter.getTablePicture();

            Intent intent = new Intent(Intent.ACTION_SEND);
            intent.setType("image/*");
            Uri uri = FileProvider.getUriForFile(getActivity(), "ru.developer.press.mytable_test.provider", file);
            intent.putExtra(Intent.EXTRA_STREAM, uri);
            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            startActivity(intent);
            dismiss();

        };
        View.OnClickListener clickExcel = v -> {

            // берем файл ексель
            File file = sendTableGetter.getTableExcel();
            Intent intent = new Intent(Intent.ACTION_SEND);
            intent.setType("document/*");
            Uri uri = FileProvider.getUriForFile(Objects.requireNonNull(getActivity()), "ru.developer.press.mytable_test.provider", file);
            intent.putExtra(Intent.EXTRA_STREAM, uri);
            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            startActivity(intent);
            dismiss();

        };
        pic.setOnClickListener(clickPic);
        // такжетназначаем клик и на дочерние обьекты
        for (int i = 0; i < pic.getChildCount(); i++) {
            pic.getChildAt(i).setOnClickListener(clickPic);
        }
        excel.setOnClickListener(clickExcel);
        // такжетназначаем клик и на дочерние обьекты
        for (int i = 0; i < excel.getChildCount(); i++) {
            excel.getChildAt(i).setOnClickListener(clickExcel);
        }
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

        builder.setTitle(R.string.change_format);
        builder.setView(view);


        return builder.create();
    }

    @Override
    public void onResume() {
        super.onResume();
        Window window = Objects.requireNonNull(getDialog().getWindow());
        window.setGravity(Gravity.CENTER);
        window.setLayout(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);

    }
}
