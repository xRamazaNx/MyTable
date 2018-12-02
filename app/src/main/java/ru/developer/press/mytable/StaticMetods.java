package ru.developer.press.mytable;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.graphics.drawable.ColorDrawable;
import android.text.format.DateFormat;
import android.util.TypedValue;
import android.view.View;
import android.widget.PopupWindow;

import java.util.ArrayList;
import java.util.Calendar;

import ru.developer.press.mytable.model.Cell;
import ru.developer.press.mytable.model.CellAbstract;
import ru.developer.press.mytable.model.ColumnPref;
import ru.developer.press.myTable.R;
import ru.developer.press.mytable.model.TableModel;
import ru.developer.press.mytable.table.builders.CellBuilder;
import ru.developer.press.mytable.table.builders.ColumnBuilder;
import ru.developer.press.mytable.table.builders.HeaderBuilder;

public class StaticMetods {


    public static int convertDpToPixels(float dp, Context context) {
        return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp, context.getResources().getDisplayMetrics());
    }

    public static float convertSpToPixels(float dp, Context context) {

        return TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, dp, context.getResources().getDisplayMetrics());

    }


    public static String inputTypeOfIndex(Context context, int index) {
        String type = "";
        switch (index) {
            case 0:
                type = context.getString(R.string.text);
                break;
            case 1:
                type = context.getString(R.string.type_number);
                break;
            case 2:
                type = context.getString(R.string.date);
                break;
        }
        return type;
    }

//    public static void settingTitle(final LinearLayout title, ColumnPref column) {
//
//        int width = column.getWidthColumn();
//        int height = ViewGroup.LayoutParams.MATCH_PARENT;
//
//        float sizeText = column.getTextSizeTitle();
//        int styleText = column.getTextStyleTitle();
//        int colorText = column.getTextColorTitle();
//
//        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(width, height); // -1 для полоски между заголовками
//        title.setLayoutParams(params);
//        title.setBackgroundColor(column.getColorTitleRect());
//
//        TextView textView = (TextView) title.getChildAt(0);
////        textView.setGravity(Gravity.CENTER);
////        textView.setTextAlignment(View.TEXT_ALIGNMENT_CENTER);
//        textView.setTextSize(sizeText);
//        textView.setTypeface(null, styleText); // 0 обычный, 1 жирный, 2 италик, 3 жирный + италик
//        textView.setTextColor(colorText);
//        textView.setText(column.getName());
//
//
//    }

    public static String getDateOfMillis(long timeInMillis) {
        String date = "";
        try {
            Calendar calendar = Calendar.getInstance();
            calendar.setTimeInMillis(timeInMillis);
            date = DateFormat.format("dd.MM.yyyy", calendar.getTime()).toString();

            // конечно тут еще придется переделать под выбор даты
        } catch (Exception ignored) {
        }

        return date;
    }

    public static PopupWindow sreatePopupWindow(View layoutSettColumn, int width, int height) {
        PopupWindow popupWindow = new PopupWindow(layoutSettColumn, width, height, true);
        popupWindow.setBackgroundDrawable(new ColorDrawable());
        popupWindow.setAnimationStyle(R.style.animation_popup_show_hide);

        return popupWindow;
    }

    public static ArrayList<ColumnPref> copyColumns(ArrayList<ColumnPref> columnsSource) {

        ArrayList<ColumnPref> columnPref = new ArrayList<>();
        for (int i = 0; i < columnsSource.size(); i++) {
            columnPref.add(new ColumnPref(columnsSource.get(i)));
        }
        return columnPref;
    }

    public static ArrayList<ArrayList<Cell>> copyStroks(ArrayList<ArrayList<Cell>> stroksSorce) {
        ArrayList<ArrayList<Cell>> stroks = new ArrayList<>();
        for (int i = 0; i < stroksSorce.size(); i++) {
            ArrayList<Cell> sorceEntry = stroksSorce.get(i);
            ArrayList<Cell> entry = new ArrayList<>();
            for (int j = 0; j < sorceEntry.size(); j++) {
                entry.add(new Cell(sorceEntry.get(j)));
            }
            stroks.add(entry);
        }
        return stroks;
    }

    public static Typeface setTypeFace(int type) {
        Typeface typeface = Typeface.create(Typeface.SERIF, Typeface.NORMAL);
        switch (type) {
            case 1:
                typeface = Typeface.create(Typeface.SERIF, Typeface.BOLD);
                break;
            case 2:
                typeface = Typeface.create(Typeface.SERIF, Typeface.ITALIC);
                break;
            case 3:
                typeface = Typeface.create(Typeface.SERIF, Typeface.BOLD_ITALIC);
                break;

        }

        return typeface;
    }

    public static Bitmap getScreenTable(TableModel tableModel, Context context) {
        CellBuilder cellBuilder = new CellBuilder(context, null);
        cellBuilder.init(tableModel);
        Bitmap cells = Bitmap.createBitmap(cellBuilder.getWidth(), cellBuilder.getHeight(), Bitmap.Config.RGB_565);
        cells.eraseColor(Color.WHITE);
        CellAbstract coordinate = new CellAbstract();
        coordinate.setBounds(0, cellBuilder.getWidth(), 0, cellBuilder.getHeight());
        cellBuilder.drawCells(new Canvas(cells), coordinate, tableModel);

        HeaderBuilder headerBuilder = new HeaderBuilder(context);
        headerBuilder.init(tableModel);
        Bitmap header = Bitmap.createBitmap(convertDpToPixels(32, context), cellBuilder.getHeight(), Bitmap.Config.RGB_565);
        header.eraseColor(Color.WHITE);
        headerBuilder.drawHeaders(new Canvas(header), coordinate, tableModel);

        ColumnBuilder columnBuilder = new ColumnBuilder(context, null);
        columnBuilder.init(tableModel);
        Bitmap column = Bitmap.createBitmap(cellBuilder.getWidth(), (int) columnBuilder.heightColumns, Bitmap.Config.RGB_565);
        column.eraseColor(Color.WHITE);
        columnBuilder.drawColumns(new Canvas(column), coordinate, tableModel);

        int width = header.getWidth() + cellBuilder.getWidth();
        int height = (int) (columnBuilder.heightColumns + cellBuilder.getHeight());

        Bitmap result = Bitmap.createBitmap(width, height, Bitmap.Config.RGB_565);
        result.eraseColor(Color.WHITE);

        Canvas canvas = new Canvas(result);
        Paint paint = new Paint();
        canvas.drawBitmap(header, 0, column.getHeight(), paint);
        canvas.drawBitmap(column, header.getWidth(), 0, paint);
        canvas.drawBitmap(cells, header.getWidth(), column.getHeight(), paint);

        return result;
        // работает только полоски надо сделать под заголовками и с право от номеров строк
    }

}
