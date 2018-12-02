package ru.developer.press.mytable.views;

import android.content.Context;
import android.graphics.Color;
import android.view.View;
import android.view.animation.AnimationUtils;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.jaredrummler.android.colorpicker.ColorPickerDialog;
import com.jaredrummler.android.colorpicker.ColorPickerDialogListener;

import ru.developer.press.mytable.TableActivity;
import ru.developer.press.mytable.interfaces.SettColumnsListener;
import ru.developer.press.myTable.R;

public class SettingButtonView {

    private LinearLayout layout;

    private TextView typeText;
    private TextView typeNumber;
    private TextView typeDate;


    private TextView sizeTitle;
    private ImageButton downSizeTitle;
    private ImageButton upSizeTitle;

    private TextView boldTitle;
    private TextView italicTitle;
    private ImageButton colorTitle;

    private TextView sizeCell;
    private ImageButton downSizeCell;
    private ImageButton upSizeCell;

    private TextView boldCell;
    private TextView italicCell;
    private ImageButton colorCell;

    private SettColumnsListener settColumns;

    public SettingButtonView(LinearLayout layout, SettColumnsListener settColumns) {
        this.settColumns = settColumns;
        this.layout = layout;

        typeText = layout.findViewById(R.id.type_text_edit_table);
        typeNumber = layout.findViewById(R.id.type_number_edit_table);
        typeDate = layout.findViewById(R.id.type_date_edit_table);

        sizeTitle = layout.findViewById(R.id.size_title);
        downSizeTitle = layout.findViewById(R.id.down_size_title);
        upSizeTitle = layout.findViewById(R.id.up_size_title);

        boldTitle = layout.findViewById(R.id.bold_title);
        italicTitle = layout.findViewById(R.id.italic_title);
        colorTitle = layout.findViewById(R.id.color_title_imagebutton);

        sizeCell = layout.findViewById(R.id.size_cell);
        downSizeCell = layout.findViewById(R.id.down_size_cell);
        upSizeCell = layout.findViewById(R.id.up_size_cell);

        boldCell = layout.findViewById(R.id.bold_cell);
        italicCell = layout.findViewById(R.id.italic_cell);
        colorCell = layout.findViewById(R.id.color_cell_imagebutton);

        if (settColumns.type == 0) typeText.setBackgroundResource(R.drawable.contur_change_typeface);
        else if (settColumns.type == 1) typeNumber.setBackgroundResource(R.drawable.contur_change_typeface);
        else if (settColumns.type == 2) typeDate.setBackgroundResource(R.drawable.contur_change_typeface);

        int typeFaceTitle = settColumns.styleTitle;
        if (typeFaceTitle == 1 || typeFaceTitle == 3)
            boldTitle.setBackgroundResource(R.drawable.contur_change_typeface);
        if (typeFaceTitle > 1)
            italicTitle.setBackgroundResource(R.drawable.contur_change_typeface);

        int typeFaceCell = settColumns.styleCell;
        if (typeFaceCell == 1 || typeFaceCell == 3)
            boldCell.setBackgroundResource(R.drawable.contur_change_typeface);
        if (typeFaceCell > 1)
            italicCell.setBackgroundResource(R.drawable.contur_change_typeface);

        sizeTitle.setText(String.valueOf(settColumns.sizeTitle));
        sizeCell.setText(String.valueOf(settColumns.sizeCell));

        colorTitle.setColorFilter(settColumns.colorTitle);
        colorCell.setColorFilter(settColumns.colorCell);

        clickAll();
    }

    private void clickAll() {

        typeText.setOnClickListener(onClickChanged());
        typeNumber.setOnClickListener(onClickChanged());
        typeDate.setOnClickListener(onClickChanged());

        downSizeTitle.setOnClickListener(onClickChanged());
        upSizeTitle.setOnClickListener(onClickChanged());

        downSizeCell.setOnClickListener(onClickChanged());
        upSizeCell.setOnClickListener(onClickChanged());

        boldTitle.setOnClickListener(onClickChanged());
        boldCell.setOnClickListener(onClickChanged());

        italicTitle.setOnClickListener(onClickChanged());
        italicCell.setOnClickListener(onClickChanged());

        colorTitle.setOnClickListener(onClickChanged());
        colorCell.setOnClickListener(onClickChanged());
    }

    private View.OnClickListener onClickChanged() {
        final Context context = layout.getContext();
        return new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                int size;

                int id = v.getId();
                int color;
                switch (id) {
                    case R.id.type_text_edit_table:
                        typeText.setBackgroundResource(R.drawable.contur_change_typeface);
                        typeNumber.setBackgroundColor(Color.TRANSPARENT);
                        typeDate.setBackgroundColor(Color.TRANSPARENT);
                        //передаем 0 как первое
                        settColumns.setType(0);
                        break;
                    case R.id.type_number_edit_table:
                        typeNumber.setBackgroundResource(R.drawable.contur_change_typeface);
                        typeText.setBackgroundColor(Color.TRANSPARENT);
                        typeDate.setBackgroundColor(Color.TRANSPARENT);
                        //
                        settColumns.setType(1);
                        break;
                    case R.id.type_date_edit_table:
                        typeDate.setBackgroundResource(R.drawable.contur_change_typeface);
                        typeNumber.setBackgroundColor(Color.TRANSPARENT);
                        typeText.setBackgroundColor(Color.TRANSPARENT);
                        //
                        settColumns.setType(2);
                        break;

                    case R.id.down_size_title:
                        size = Integer.parseInt(sizeTitle.getText().toString());
                        if (size < 7) break;
                        size -= 1;
                        settColumns.setTextSizeTitle(size);
                        sizeTitle.setText(String.valueOf(size));
                        sizeTitle.startAnimation(AnimationUtils.loadAnimation(v.getContext(), R.anim.click_anim));
                        break;

                    case R.id.up_size_title:
                        size = Integer.parseInt(sizeTitle.getText().toString());
                        if (size > 31) break;
                        size += 1;
                        settColumns.setTextSizeTitle(size);
                        sizeTitle.setText(String.valueOf(size));
                        sizeTitle.startAnimation(AnimationUtils.loadAnimation(v.getContext(), R.anim.click_anim));
                        break;

                    case R.id.down_size_cell:
                        size = Integer.parseInt(sizeCell.getText().toString());
                        if (size < 7) break;
                        size -= 1;
                        sizeCell.setText(String.valueOf(size));
                        sizeCell.startAnimation(AnimationUtils.loadAnimation(v.getContext(), R.anim.click_anim));
                        settColumns.setTextSizeCell(size);
                        break;

                    case R.id.up_size_cell:
                        size = Integer.parseInt(sizeCell.getText().toString());
                        if (size > 31) break;
                        size += 1;
                        sizeCell.setText(String.valueOf(size));
                        sizeCell.startAnimation(AnimationUtils.loadAnimation(v.getContext(), R.anim.click_anim));
                        settColumns.setTextSizeCell(size);
                        break;

                    case R.id.bold_title:
                        boldTitleClickLogic();
                        break;
                    case R.id.bold_cell:
                        boldCellClickLogic();
                        break;
                    case R.id.italic_title:
                        italicTitleLogic();
                        break;
                    case R.id.italic_cell:
                        italicCellLogic();
                        break;

                    case R.id.color_title_imagebutton:
                        color = settColumns.colorTitle;
                        setColorPic(color, id);
                        break;
                    case R.id.color_cell_imagebutton:
                        color = settColumns.colorCell;
                        setColorPic(color, id);
                        break;


                }

            }

            private void boldTitleClickLogic() {
                int typeFaceTitle = settColumns.styleTitle;
                if (typeFaceTitle == 1 || typeFaceTitle == 3) {
                    boldTitle.setBackgroundColor(Color.TRANSPARENT);
                    if (typeFaceTitle == 3)
                        typeFaceTitle = 2;
                    else typeFaceTitle = 0;
                } else {
                    boldTitle.setBackgroundResource(R.drawable.contur_change_typeface);
                    if (typeFaceTitle == 2)
                        typeFaceTitle = 3;
                    else typeFaceTitle = 1;
                }
                settColumns.setStyleTitle(typeFaceTitle);
            }

            private void boldCellClickLogic() {
                int typeFaceCell = settColumns.styleCell;
                if (typeFaceCell == 1 || typeFaceCell == 3) {
                    boldCell.setBackgroundColor(Color.TRANSPARENT);
                    if (typeFaceCell == 3)
                        typeFaceCell = 2;
                    else typeFaceCell = 0;
                } else {
                    boldCell.setBackgroundResource(R.drawable.contur_change_typeface);
                    if (typeFaceCell == 2)
                        typeFaceCell = 3;
                    else typeFaceCell = 1;
                }
                settColumns.setStyleCell(typeFaceCell);
            }

            private void italicTitleLogic() {
                int typeFaceTitle = settColumns.styleTitle;
                if (typeFaceTitle > 1) {
                    italicTitle.setBackgroundColor(Color.TRANSPARENT);
                    if (typeFaceTitle == 3)
                        typeFaceTitle = 1;
                    else typeFaceTitle = 0;
                } else {
                    italicTitle.setBackgroundResource(R.drawable.contur_change_typeface);
                    if (typeFaceTitle == 1)
                        typeFaceTitle = 3;
                    else typeFaceTitle = 2;
                }
                settColumns.setStyleTitle(typeFaceTitle);
            }

            private void italicCellLogic() {
                int typeFaceCell = settColumns.styleCell;
                if (typeFaceCell > 1) {
                    italicCell.setBackgroundColor(Color.TRANSPARENT);
                    if (typeFaceCell == 3)
                        typeFaceCell = 1;
                    else typeFaceCell = 0;
                } else {
                    italicCell.setBackgroundResource(R.drawable.contur_change_typeface);
                    if (typeFaceCell == 1)
                        typeFaceCell = 3;
                    else typeFaceCell = 2;
                }
                settColumns.setStyleCell(typeFaceCell);
            }

            private void setColorPic(int colorPic, final int id) {
                ColorPickerDialog colorPickerDialog = ColorPickerDialog.newBuilder().
                        setColor(colorPic).
                        setShowAlphaSlider(false).
                        create();
                colorPickerDialog.show((((TableActivity)context).getFragmentManager()), "color_pic");
                colorPickerDialog.setColorPickerDialogListener(new ColorPickerDialogListener() {
                    @Override
                    public void onColorSelected(int dialogId, int color) {
//                            String hexColor = String.format("#%06X", (0xFFFFFF & color));
                        switch (id) {
                            case R.id.color_title_imagebutton:
                                colorTitle.setColorFilter(color);
                                settColumns.setColorTitle(color);
                                break;
                            case R.id.color_cell_imagebutton:
                                colorCell.setColorFilter(color);
                                settColumns.setColorCell(color);
                                break;
                        }

                    }
                    @Override
                    public void onDialogDismissed(int dialogId) {
                    }
                });

            }

        };
    }
}
