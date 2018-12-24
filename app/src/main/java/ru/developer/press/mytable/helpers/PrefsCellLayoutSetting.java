package ru.developer.press.mytable.helpers;

import android.content.Context;
import android.graphics.Color;
import android.view.View;
import android.view.animation.AnimationUtils;
import android.widget.CheckBox;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.jaredrummler.android.colorpicker.ColorPickerDialog;
import com.jaredrummler.android.colorpicker.ColorPickerDialogListener;

import ru.developer.press.mytable.TableActivity;
import ru.developer.press.mytable.interfaces.PrefCellsListener;
import ru.developer.press.myTable.R;

public class PrefsCellLayoutSetting {

    private LinearLayout layout;

    private TextView typeText;
    private TextView typeNumber;
    private TextView typeDate;

    private TextView sizeText;
    private ImageButton downSizeText;
    private ImageButton upSizeText;

    private TextView bold;
    private TextView italic;
    private FrameLayout colorText;
    private FrameLayout colorBack;
    private FrameLayout colorTextFrame;
    private FrameLayout colorBackFrame;

    public CheckBox isCellEditCheck;

    private PrefCellsListener prefCellsListener;

    public PrefsCellLayoutSetting(LinearLayout layout, PrefCellsListener prefCellsListener) {
        this.prefCellsListener = prefCellsListener;
        this.layout = layout;

        typeText = layout.findViewById(R.id.type_text);
        typeNumber = layout.findViewById(R.id.type_number);
        typeDate = layout.findViewById(R.id.type_date);

        sizeText = layout.findViewById(R.id.size_text);
        downSizeText = layout.findViewById(R.id.down_size_text);
        upSizeText = layout.findViewById(R.id.up_size_text);

        bold = layout.findViewById(R.id.bold);
        italic = layout.findViewById(R.id.italic);
        colorText = layout.findViewById(R.id.color_text);
        colorBack = layout.findViewById(R.id.color_back);
        colorTextFrame = layout.findViewById(R.id.color_text_frame);
        colorBackFrame = layout.findViewById(R.id.color_back_frame);

        isCellEditCheck = layout.findViewById(R.id.is_edit_cell_check);

        if (prefCellsListener.type == 0)
            typeText.setBackgroundResource(R.drawable.contur_change_typeface);
        else if (prefCellsListener.type == 1)
            typeNumber.setBackgroundResource(R.drawable.contur_change_typeface);
        else if (prefCellsListener.type == 2)
            typeDate.setBackgroundResource(R.drawable.contur_change_typeface);

        if (prefCellsListener.bold == 1)
            bold.setBackgroundResource(R.drawable.contur_change_typeface);
        if (prefCellsListener.italic == 1)
            italic.setBackgroundResource(R.drawable.contur_change_typeface);

        sizeText.setText(String.valueOf(prefCellsListener.sizeText));
        colorText.setBackgroundColor(prefCellsListener.colorText);
        colorBack.setBackgroundColor(prefCellsListener.colorBack);
        clickAll();

        LinearLayout linearFromTypes = layout.findViewById(R.id.linear_from_types);
        if (prefCellsListener.isCellsEditOnly) {
            isCellEditCheck.setVisibility(View.GONE);
            linearFromTypes.setVisibility(View.GONE);
        }
        if (prefCellsListener.isHeaderEdit) {
            linearFromTypes.setVisibility(View.GONE);
        }
    }

    private void clickAll() {

        typeText.setOnClickListener(onClickChanged());
        typeNumber.setOnClickListener(onClickChanged());
        typeDate.setOnClickListener(onClickChanged());

        downSizeText.setOnClickListener(onClickChanged());
        upSizeText.setOnClickListener(onClickChanged());

        bold.setOnClickListener(onClickChanged());

        italic.setOnClickListener(onClickChanged());

        colorTextFrame.setOnClickListener(onClickChanged());
        colorBackFrame.setOnClickListener(onClickChanged());

        isCellEditCheck.setOnCheckedChangeListener((buttonView, isChecked) -> prefCellsListener.isCellsEdit = isChecked);
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
                    case R.id.type_text:
                        typeText.setBackgroundResource(R.drawable.contur_change_typeface);
                        typeNumber.setBackgroundColor(Color.TRANSPARENT);
                        typeDate.setBackgroundColor(Color.TRANSPARENT);
                        //передаем 0 как первое
                        prefCellsListener.setType(0);
                        break;
                    case R.id.type_number:
                        typeNumber.setBackgroundResource(R.drawable.contur_change_typeface);
                        typeText.setBackgroundColor(Color.TRANSPARENT);
                        typeDate.setBackgroundColor(Color.TRANSPARENT);
                        //
                        prefCellsListener.setType(1);
                        break;
                    case R.id.type_date:
                        typeDate.setBackgroundResource(R.drawable.contur_change_typeface);
                        typeNumber.setBackgroundColor(Color.TRANSPARENT);
                        typeText.setBackgroundColor(Color.TRANSPARENT);
                        //
                        prefCellsListener.setType(2);
                        break;

                    case R.id.down_size_text:
                        size = Integer.parseInt(sizeText.getText().toString());
                        if (size < 7) break;
                        size -= 1;
                        sizeText.setText(String.valueOf(size));
                        sizeText.startAnimation(AnimationUtils.loadAnimation(v.getContext(), R.anim.click_anim));
                        prefCellsListener.setSizeText(size);
                        break;

                    case R.id.up_size_text:
                        size = Integer.parseInt(sizeText.getText().toString());
                        if (size > 31) break;
                        size += 1;
                        sizeText.setText(String.valueOf(size));
                        sizeText.startAnimation(AnimationUtils.loadAnimation(v.getContext(), R.anim.click_anim));
                        prefCellsListener.setSizeText(size);
                        break;

                    case R.id.bold:
                        boldCellClickLogic();
                        break;

                    case R.id.italic:
                        italicCellLogic();
                        break;

                    case R.id.color_text_frame:
                        color = prefCellsListener.colorText;
                        setColorPic(color, ColorChangeType.Text);
                        break;
                    case R.id.color_back_frame:
                        color = prefCellsListener.colorBack;
                        setColorPic(color, ColorChangeType.back);
                        break;


                }

            }


            private void boldCellClickLogic() {
                int bold = prefCellsListener.bold;
                if (bold == 1) {
                    PrefsCellLayoutSetting.this.bold.setBackgroundColor(Color.TRANSPARENT);
                    bold = 0;
                } else {
                    PrefsCellLayoutSetting.this.bold.setBackgroundResource(R.drawable.contur_change_typeface);
                    bold = 1;
                }
                prefCellsListener.setBold(bold);
            }

            private void italicCellLogic() {
                int italic = prefCellsListener.italic;
                if (italic == 1) {
                    PrefsCellLayoutSetting.this.italic.setBackgroundColor(Color.TRANSPARENT);
                    italic = 0;
                } else {
                    PrefsCellLayoutSetting.this.italic.setBackgroundResource(R.drawable.contur_change_typeface);
                    italic = 1;
                }
                prefCellsListener.setItalic(italic);
            }

            private void setColorPic(int colorPic, ColorChangeType colorChangeType) {
                ColorPickerDialog colorPickerDialog = ColorPickerDialog.newBuilder().
                        setColor(colorPic).
                        setShowAlphaSlider(false).
                        create();
                colorPickerDialog.show((((TableActivity) context).getFragmentManager()), "color_pic");
                colorPickerDialog.setColorPickerDialogListener(new ColorPickerDialogListener() {
                    @Override
                    public void onColorSelected(int dialogId, int color) {
//                            String hexColor = String.format("#%06X", (0xFFFFFF & color));

                        if (colorChangeType == ColorChangeType.Text){
                            prefCellsListener.setColorText(color);
                            colorText.setBackgroundColor(color);
                        } else {
                            prefCellsListener.setColorBack(color);
                            colorBack.setBackgroundColor(color);
                        }

                    }

                    @Override
                    public void onDialogDismissed(int dialogId) {
                    }
                });

            }

        };
    }

    private enum ColorChangeType{
        Text, back
    }
}
