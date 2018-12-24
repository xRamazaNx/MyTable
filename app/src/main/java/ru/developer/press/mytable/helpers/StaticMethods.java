package ru.developer.press.mytable.helpers;

import android.content.Context;
import android.graphics.Typeface;
import android.graphics.drawable.ColorDrawable;
import android.support.annotation.NonNull;
import android.text.format.DateFormat;
import android.util.TypedValue;
import android.view.View;
import android.widget.PopupWindow;

import java.util.Calendar;
import java.util.UUID;

import ru.developer.press.myTable.R;
import ru.developer.press.mytable.table.model.CellAbstract;

public class StaticMethods {
    private static String[] sDayOfWeek = {"вс, ", "пн, ", "вт, ", "ср, ", "чт, ", "пт, ", "сб, "};


    public static int convertDpToPixels(float dp, Context context) {
        return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp, context.getResources().getDisplayMetrics());
    }

    public static float convertSpToPixels(float dp, Context context) {

        return TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, dp, context.getResources().getDisplayMetrics());

    }


    public static String getDateOfMillis(long timeInMillis, int variable) {
        String date = "";
        if (timeInMillis > 1000000)
            date = getDate(variable, timeInMillis);
        return date;
    }

    public static PopupWindow createPopupWindow(View layoutSettColumn, int width, int height) {
        PopupWindow popupWindow = new PopupWindow(layoutSettColumn, width, height, true);
        popupWindow.setBackgroundDrawable(new ColorDrawable());
        popupWindow.setAnimationStyle(R.style.animation_popup_show_hide);

        return popupWindow;
    }

    public static Typeface setTypeFace(CellAbstract cell) {
        Typeface typeface = Typeface.create(Typeface.SERIF, Typeface.NORMAL);
        boolean b = cell.bold == 1;
        boolean i = cell.italic == 1;
        if (b && i)
            typeface = Typeface.create(Typeface.SERIF, Typeface.BOLD_ITALIC);
        else {
            if (b)
                typeface = Typeface.create(Typeface.SERIF, Typeface.BOLD);
            else if (i)
                typeface = Typeface.create(Typeface.SERIF, Typeface.ITALIC);

        }

        return typeface;
    }

    // дата
    private static String getDate(int variantDate, long time) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(time);

        String dayName = sDayOfWeek[calendar.get(Calendar.DAY_OF_WEEK) - 1];
        String timeFormat = "";
        switch (variantDate) {
            case 0:
                timeFormat = "dd.MM.yy";
                break;
            case 1:
                timeFormat = "dd.MM.yy kk:mm";
                break;
            case 2:
                timeFormat = dayName + "dd.MM.yy";
                break;
            case 3:
                timeFormat = dayName + "dd.MM.yy kk:mm";
                break;
            case 4:
                timeFormat = "dd.MM.yyyy";
                break;
            case 5:
                timeFormat = "dd.MM.yyyy kk:mm";
                break;
            case 6:
                timeFormat = dayName + "dd.MM.yyyy";
                break;
            case 7:
                timeFormat = dayName + "dd.MM.yyyy kk:mm";
                break;
            case 8:
                timeFormat = "dd MMMM yyyy";
                break;
            case 9:
                timeFormat = "dd MMMM yyyy kk:mm";
                break;
            case 10:
                timeFormat = "dd.MM.yyyy_kk-mm-ss";
                break;
            case 11:
                timeFormat = "dd.MM.yyyy kk:mm";
                break;
        }
        return DateFormat.format(timeFormat, calendar.getTime()).toString();
    }

//    public static Bitmap getScreenTable(TableModel tableModel, Context context) {
//        CellBuilder cellBuilder = new CellBuilder(context, null);
//        cellBuilder.init(tableModel);
//        Bitmap cells = Bitmap.createBitmap(cellBuilder.getWidth(), cellBuilder.getHeight(), Bitmap.Config.RGB_565);
//        cells.eraseColor(Color.WHITE);
//        CellAbstract coordinate = new CellAbstract();
//        coordinate.setBounds(0, cellBuilder.getWidth(), 0, cellBuilder.getHeight());
//        cellBuilder.draw(new Canvas(cells), coordinate, tableModel);
//
//        HeaderBuilder headerBuilder = new HeaderBuilder(context);
//        headerBuilder.init(tableModel);
//        Bitmap header = Bitmap.createBitmap(convertDpToPixels(32, context), cellBuilder.getHeight(), Bitmap.Config.RGB_565);
//        header.eraseColor(Color.WHITE);
//        headerBuilder.draw(new Canvas(header), coordinate, tableModel);
//
//        ColumnBuilder columnBuilder = new ColumnBuilder(context, null);
//        columnBuilder.init(tableModel);
//        Bitmap column = Bitmap.createBitmap(cellBuilder.getWidth(), (int) columnBuilder.heightColumns, Bitmap.Config.RGB_565);
//        column.eraseColor(Color.WHITE);
//        columnBuilder.draw(new Canvas(column), coordinate, tableModel);
//
//        int width = header.getWidth() + cellBuilder.getWidth();
//        int height = (int) (columnBuilder.heightColumns + cellBuilder.getHeight());
//
//        Bitmap result = Bitmap.createBitmap(width, height, Bitmap.Config.RGB_565);
//        result.eraseColor(Color.WHITE);
//
//        Canvas canvas = new Canvas(result);
//        Paint paint = new Paint();
//        canvas.drawBitmap(header, 0, column.getHeight(), paint);
//        canvas.drawBitmap(column, header.getWidth(), 0, paint);
//        canvas.drawBitmap(cells, header.getWidth(), column.getHeight(), paint);
//
//        return result;
//        // работает только полоски надо сделать под заголовками и с право от номеров строк
//    }

    @NonNull
    public static String getRandomKey() {
        String key = UUID.randomUUID().toString();
        return "key_" + key;
    }

}
