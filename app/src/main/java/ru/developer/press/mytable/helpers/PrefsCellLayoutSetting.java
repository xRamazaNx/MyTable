package ru.developer.press.mytable.helpers;

import android.content.Context;
import android.graphics.Color;
import android.view.View;
import android.view.animation.AnimationUtils;
import android.widget.AdapterView;
import android.widget.CheckBox;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ListPopupWindow;
import android.widget.Spinner;
import android.widget.TextView;

import com.jaredrummler.android.colorpicker.ColorPickerDialog;
import com.jaredrummler.android.colorpicker.ColorPickerDialogListener;

import java.lang.reflect.Field;
import java.util.List;

import ru.developer.press.myTable.R;
import ru.developer.press.mytable.TableActivity;
import ru.developer.press.mytable.interfaces.table.callback.DateVariableChangeListener;
import ru.developer.press.mytable.interfaces.table.callback.DialogShowListener;
import ru.developer.press.mytable.interfaces.table.callback.FormulaMakeListener;
import ru.developer.press.mytable.interfaces.table.callback.PrefCellsListener;
import ru.developer.press.mytable.model.Pref;
import ru.developer.press.mytable.table.TablePresenter.SelectMode;

public class PrefsCellLayoutSetting {

    private final Spinner spinnerPaddingLeft;
    private final Spinner spinnerPaddingRight;
    private final Spinner spinnerPaddingUp;
    private final Spinner spinnerPaddingDown;
    //    private final LinearLayout linearFromFormula;
    private final TextView typeFormula;

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

    // галочка для изменения и ячеек если мы находимся в режиме выбора строк или столбцов
    private CheckBox isCellEditCheck;

    private PrefCellsListener prefCellsListener;
    private DialogShowListener dialogShowListener;

    public PrefsCellLayoutSetting(LinearLayout layout, PrefCellsListener prefCellsListener, DialogShowListener dialogShowListener) {
        this.prefCellsListener = prefCellsListener;
        this.dialogShowListener = dialogShowListener;
        this.layout = layout;

        typeText = layout.findViewById(R.id.type_text);
        typeNumber = layout.findViewById(R.id.type_number);
        typeDate = layout.findViewById(R.id.type_date);
        typeFormula = layout.findViewById(R.id.type_formula);

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

        spinnerPaddingLeft = layout.findViewById(R.id.spinner_left_padding);
        spinnerPaddingRight = layout.findViewById(R.id.spinner_right_padding);
        spinnerPaddingUp = layout.findViewById(R.id.spinner_up_padding);
        spinnerPaddingDown = layout.findViewById(R.id.spinner_down_padding);

//        linearFromFormula = layout.findViewById(R.id.linear_from_formula);
        try {
            Field popup = Spinner.class.getDeclaredField("mPopup");
            popup.setAccessible(true);
            // Get private mPopup member variable and try cast to ListPopupWindow
            ListPopupWindow popupLeft = (ListPopupWindow) popup.get(spinnerPaddingLeft);
            ListPopupWindow popupRight = (ListPopupWindow) popup.get(spinnerPaddingRight);
            ListPopupWindow popupUp = (ListPopupWindow) popup.get(spinnerPaddingUp);
            ListPopupWindow popupDown = (ListPopupWindow) popup.get(spinnerPaddingDown);
            // Set popupLeft height to 500px
            popupLeft.setHeight(500);
            popupRight.setHeight(500);
            popupUp.setHeight(500);
            popupDown.setHeight(500);

        } catch (NoClassDefFoundError | ClassCastException | NoSuchFieldException | IllegalAccessException e) {
            // silently fail...
        }
        Pref pref = prefCellsListener.pref;
        spinnerPaddingLeft.setSelection(pref.paddingLeft / 2 - 1);
        spinnerPaddingRight.setSelection(pref.paddingRight / 2 - 1);
        spinnerPaddingUp.setSelection(pref.paddingUp / 2 - 1);
        spinnerPaddingDown.setSelection(pref.paddingDown / 2 - 1);

        if (pref.bold == 1)
            bold.setBackgroundResource(R.drawable.contur_change_typeface);
        if (pref.italic == 1)
            italic.setBackgroundResource(R.drawable.contur_change_typeface);

        sizeText.setText(String.valueOf(pref.sizeFont));
        colorText.setBackgroundColor(pref.colorFont);
        colorBack.setBackgroundColor(pref.colorBack);
        clickAll();

        LinearLayout linearFromTypes = layout.findViewById(R.id.linear_from_types);
        if (prefCellsListener.mode == SelectMode.cell) {
            isCellEditCheck.setVisibility(View.GONE);
            linearFromTypes.setVisibility(View.GONE);
//            linearFromFormula.setVisibility(View.GONE);
        } else if (prefCellsListener.mode == SelectMode.row) {
            linearFromTypes.setVisibility(View.GONE);
//            linearFromFormula.setVisibility(View.GONE);
        } else {
            if (prefCellsListener.getSelectedColumnSize() == 1) {
                if (prefCellsListener.type == 0)
                    typeText.setBackgroundResource(R.drawable.contur_change_typeface);
                else if (prefCellsListener.type == 1)
                    typeNumber.setBackgroundResource(R.drawable.contur_change_typeface);
                else if (prefCellsListener.type == 2)
                    typeDate.setBackgroundResource(R.drawable.contur_change_typeface);
                else if (prefCellsListener.type == 3) {
                    typeFormula.setBackgroundResource(R.drawable.contur_change_typeface);
                }
            } else if (prefCellsListener.getSelectedColumnSize() > 1) {
                typeFormula.setClickable(false);
                typeFormula.setTextColor(Color.GRAY);
            }
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

        // выбор отступов
        AdapterView.OnItemSelectedListener onItemSelectedListener = new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                // позиция начинается с нуля а мне надо умножать на 2
                position++;
                Pref pref = prefCellsListener.pref;
                switch (parent.getId()) {
                    case R.id.spinner_left_padding:
                        pref.paddingLeft = position * 2;
                        break;
                    case R.id.spinner_right_padding:
                        pref.paddingRight = position * 2;
                        break;
                    case R.id.spinner_up_padding:
                        pref.paddingUp = position * 2;
                        break;
                    case R.id.spinner_down_padding:
                        pref.paddingDown = position * 2;
                        break;
                }
                prefCellsListener.updatePadding();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        };
        spinnerPaddingLeft.setOnItemSelectedListener(onItemSelectedListener);
        spinnerPaddingRight.setOnItemSelectedListener(onItemSelectedListener);
        spinnerPaddingUp.setOnItemSelectedListener(onItemSelectedListener);
        spinnerPaddingDown.setOnItemSelectedListener(onItemSelectedListener);


        isCellEditCheck.setOnCheckedChangeListener((buttonView, isChecked) -> prefCellsListener.checkCellPrefs = isChecked);


        // клик по типу "формула"
        typeFormula.setOnClickListener(v -> dialogShowListener.showFormulaDialog(new FormulaMakeListener() {

            @Override
            public List<ColumnAttribute> getColumnAttrs() {
                return prefCellsListener.getColumnAttrs();
            }

            @Override
            public void dismissFormulaDialog() {
                // если при закрытии окна с формулой там есть формула
                if (prefCellsListener.formula.getColumnAttributes().size() > 0) {
                    prefCellsListener.setType(3);
                    typeFormula.setBackgroundResource(R.drawable.contur_change_typeface);
                    typeText.setBackgroundColor(Color.TRANSPARENT);
                    typeDate.setBackgroundColor(Color.TRANSPARENT);
                    typeNumber.setBackgroundColor(Color.TRANSPARENT);
                } else {
                    if (prefCellsListener.type == 3) {
                        prefCellsListener.setType(0);
                        typeText.setBackgroundResource(R.drawable.contur_change_typeface);

                        typeFormula.setBackgroundColor(Color.TRANSPARENT);
                        typeNumber.setBackgroundColor(Color.TRANSPARENT);
                        typeDate.setBackgroundColor(Color.TRANSPARENT);
                    }
                }
            }

            @Override
            public boolean verifyIsCircledDepended(Formula formula) {
                return prefCellsListener.verifyIsCircledDepended(formula);
            }
        }));
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
                        typeFormula.setBackgroundColor(Color.TRANSPARENT);
                        //передаем 0 как первое
                        prefCellsListener.setType(0);
                        break;
                    case R.id.type_number:
                        typeNumber.setBackgroundResource(R.drawable.contur_change_typeface);
                        typeText.setBackgroundColor(Color.TRANSPARENT);
                        typeDate.setBackgroundColor(Color.TRANSPARENT);
                        typeFormula.setBackgroundColor(Color.TRANSPARENT);
                        //
                        prefCellsListener.setType(1);
                        break;
                    case R.id.type_date:
                        DateVariableChangeListener dateVariableChangeListener = variable -> {
                            typeDate.setBackgroundResource(R.drawable.contur_change_typeface);
                            typeNumber.setBackgroundColor(Color.TRANSPARENT);
                            typeText.setBackgroundColor(Color.TRANSPARENT);
                            typeFormula.setBackgroundColor(Color.TRANSPARENT);
                            //
                            prefCellsListener.setDateVariable(variable);
                            prefCellsListener.setType(2);
                        };
                        dialogShowListener.showDateCheckDialog(prefCellsListener.dateVariable, dateVariableChangeListener);
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
                        color = prefCellsListener.pref.colorFont;
                        setColorPic(color, ColorChangeType.Text);
                        break;
                    case R.id.color_back_frame:
                        color = prefCellsListener.pref.colorBack;
                        setColorPic(color, ColorChangeType.back);
                        break;


                }

            }


            private void boldCellClickLogic() {
                int bold = prefCellsListener.pref.bold;
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
                int italic = prefCellsListener.pref.italic;
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
                TableActivity tableActivity = (TableActivity) context;
                colorPickerDialog.show(tableActivity.getSupportFragmentManager(), "color_pic");
                colorPickerDialog.setColorPickerDialogListener(new ColorPickerDialogListener() {
                    @Override
                    public void onColorSelected(int dialogId, int color) {
//                            String hexColor = String.format("#%06X", (0xFFFFFF & color));

                        if (colorChangeType == ColorChangeType.Text) {
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

    private enum ColorChangeType {
        Text, back
    }
}
