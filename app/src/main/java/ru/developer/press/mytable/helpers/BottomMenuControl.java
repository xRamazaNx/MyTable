package ru.developer.press.mytable.helpers;

import android.app.Activity;
import android.graphics.Color;
import androidx.core.content.ContextCompat;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageButton;
import android.widget.LinearLayout;

import ru.developer.press.myTable.R;
import ru.developer.press.mytable.interfaces.table.BottomMenuClick;

public class BottomMenuControl implements View.OnClickListener {
    private ImageButton addRightColumn; // также будет добавлять строку вниз
    private ImageButton addDownStroke; // будет добавлять колону в лево
    private Animation animationClick;
    private LinearLayout bottomMenu;
    private BottomMenuClick bMClickListener;

    private AddButtonEnum columnMode = AddButtonEnum.ADD_RIGHT_COLUMN;
    private AddButtonEnum strokeMode = AddButtonEnum.ADD_DOWN_STROKE;
    private ImageButton width;

    public void initView(Activity activity) {
        this.bottomMenu = activity.findViewById(R.id.bottom_menu);
        animationClick = AnimationUtils.loadAnimation(activity, R.anim.click_anim);

        addRightColumn = activity.findViewById(R.id.add_column_right_bm);
        addDownStroke = activity.findViewById(R.id.add_stroke_down_bm);

        ImageButton settingTable = activity.findViewById(R.id.setting_table);

        width = activity.findViewById(R.id.width_bm);
        ImageButton heightCells = activity.findViewById(R.id.height_cells_bm);

        addRightColumn.setOnClickListener(this);
        addDownStroke.setOnClickListener(this);
        settingTable.setOnClickListener(this);
        width.setOnClickListener(this);
        heightCells.setOnClickListener(this);
    }

    public void setAddButtonListener(BottomMenuClick bottomMenuClick) {
        bMClickListener = bottomMenuClick;
    }

    @Override
    public void onClick(final View v) {
        if (bMClickListener == null)
            return;
        switch (v.getId()) {
            case R.id.add_column_right_bm:
                if (columnMode == AddButtonEnum.ADD_UP_STROKE)
                    bMClickListener.addStroke(columnMode);
                else
                    bMClickListener.addColumn(columnMode);
                break;
            case R.id.add_stroke_down_bm:
                if (strokeMode == AddButtonEnum.ADD_LEFT_COLUMN)
                    bMClickListener.addColumn(strokeMode);
                else bMClickListener.addStroke(strokeMode);
                break;
            case R.id.setting_table:
                bMClickListener.settingTable();
                break;
            case R.id.width_bm:
                bMClickListener.setWidth(width);
                break;
            case R.id.height_cells_bm:
                bMClickListener.setHeightCells();
        }
        v.startAnimation(animationClick);
        animationClick.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {
                v.setBackgroundColor(ContextCompat.getColor(bottomMenu.getContext(), R.color.gray_light));
            }

            @Override
            public void onAnimationEnd(Animation animation) {
                v.setBackgroundColor(Color.TRANSPARENT);

            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });
    }

    public void showAddButtonOfStroke() {
        addDownStroke.animate().rotation(0);
        //получится добавить строку вверх
        addRightColumn.animate().rotation(-90);
        strokeMode = AddButtonEnum.ADD_DOWN_STROKE;
        columnMode = AddButtonEnum.ADD_UP_STROKE;
//        if (addRightColumn.getVisibility() == View.VISIBLE) {
//            addRightColumn.setVisibility(View.GONE);
//            addUpStroke.setVisibility(View.VISIBLE);
//            addUpStroke.startAnimation(animationShow);
//        }
//        if (addLeftColumn.getVisibility() == View.VISIBLE) {
//            hideAddColumnLeft();
//        }
    }

//    public void hideAddStrokeUpButton() {
//        addRightColumn.animate().rotation(0);
//
////        if (addUpStroke.getVisibility() == View.GONE) return;
////        addUpStroke.setVisibility(View.GONE);
////        addRightColumn.setVisibility(View.VISIBLE);
////        addRightColumn.startAnimation(animationShow);
//    }

    public void showAddButtonOfColumn() {
        // получится добавить колону влево
        addDownStroke.animate().rotation(90);
        // получится добавить колону вправо
        addRightColumn.animate().rotation(0);

        strokeMode = AddButtonEnum.ADD_LEFT_COLUMN;
        columnMode = AddButtonEnum.ADD_RIGHT_COLUMN;

//        if (addDownStroke.getVisibility() == View.VISIBLE) {
//            addDownStroke.setVisibility(View.GONE);
//            addLeftColumn.setVisibility(View.VISIBLE);
//            addLeftColumn.startAnimation(animationShow);
//        }
//        if (addUpStroke.getVisibility() == View.VISIBLE) {
//            hideAddStrokeUpButton();
//        }
    }

    public void showStandartAddButton() {
//        if (addLeftColumn.getVisibility() == View.GONE) return;
//        addLeftColumn.setVisibility(View.GONE);
//        addDownStroke.setVisibility(View.VISIBLE);
//        addDownStroke.startAnimation(animationShow);

        addDownStroke.animate().rotation(0);
        addRightColumn.animate().rotation(0);

        strokeMode = AddButtonEnum.ADD_DOWN_STROKE;
        columnMode = AddButtonEnum.ADD_RIGHT_COLUMN;

    }

    public enum AddButtonEnum {
        ADD_LEFT_COLUMN, ADD_RIGHT_COLUMN,
        ADD_DOWN_STROKE, ADD_UP_STROKE
    }
}
