package ru.developer.press.mytable.helpers;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.ColorDrawable;
import androidx.annotation.NonNull;
import android.text.format.DateFormat;
import android.util.TypedValue;
import android.view.View;
import android.widget.PopupWindow;

import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.UUID;
import java.util.concurrent.Callable;

import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;
import ru.developer.press.myTable.R;
import ru.developer.press.mytable.interfaces.TaskListener;
import ru.developer.press.mytable.model.CellAbstract;
import ru.developer.press.mytable.model.Column;
import ru.developer.press.mytable.model.Row;
import ru.developer.press.mytable.model.TableModel;
import ru.developer.press.mytable.table.builders.CellBuilder;
import ru.developer.press.mytable.table.builders.ColumnBuilder;
import ru.developer.press.mytable.table.builders.LineBuilder;
import ru.developer.press.mytable.table.builders.RowBuilder;
import ru.developer.press.mytable.table.builders.TotalAmountBuilder;

public class StaticMethods {
    private static final String[] sDayOfWeek = {"вс, ", "пн, ", "вт, ", "ср, ", "чт, ", "пт, ", "сб, "};


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
        boolean b = cell.pref.bold == 1;
        boolean i = cell.pref.italic == 1;
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
            case 11:
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
        }
        return DateFormat.format(timeFormat, calendar.getTime()).toString();
    }

    public static Bitmap getScreenTable(TableModel tableModel, Context context) {

        CellBuilder cellBuilder = new CellBuilder(context, null);
        ColumnBuilder columnBuilder = new ColumnBuilder(context, null);
        RowBuilder rowBuilder = new RowBuilder(context);
        LineBuilder lineBuilder = new LineBuilder(context);
        TotalAmountBuilder amountBuilder = new TotalAmountBuilder(context);

        columnBuilder.init(tableModel);
        columnBuilder.updateHeightColumn(tableModel);
        for (int i = 0; i < tableModel.getRows().size(); i++) {
            rowBuilder.updateRow(tableModel, i);
            cellBuilder.updateHeightStroke(tableModel, i);
        }
        amountBuilder.initTotalAmount(tableModel);

//        cellBuilder.init(tableModel);
//        for (int i = 0; i < tableModel.getRows().size(); i++) {
//            cellBuilder.updateHeightStroke(tableModel, i);
//        }
//        columnBuilder.updateHeightColumn(tableModel);
//        cellBuilder.init(tableModel);

        tableModel.widthRows = (int) rowBuilder.widthHeaders;

        int width = tableModel.getWidthTable() + tableModel.widthRows;
        int height = tableModel.getHeightTable() + tableModel.heightColumns;

        Bitmap bitmap = Bitmap.createBitmap(width,
                height, Bitmap.Config.ARGB_8888);
        bitmap.eraseColor(Color.WHITE);
        Canvas canvas = new Canvas(bitmap);

        Coordinate coordinate = new Coordinate();
        coordinate.setBounds(0, width, 0, height);
        cellBuilder.draw(canvas, coordinate, tableModel);
        rowBuilder.draw(canvas, coordinate, tableModel);
        columnBuilder.draw(canvas, coordinate, tableModel);
        lineBuilder.draw(canvas, coordinate, tableModel);
        return bitmap;
        // работает только полоски надо сделать под заголовками и с право от номеров строк
    }

    public static Workbook getTableExcel(TableModel tableModel) {
//        CellBuilder cellBuilder = new CellBuilder(context, null);
//        cellBuilder.init(tableModel);
//        for (int i = 0; i < tableModel.getRows().size(); i++) {
//            cellBuilder.updateHeightStroke(tableModel, i);
//        }
//        cellBuilder.updateHeightColumn(tableModel);
//        cellBuilder.init(tableModel);

        Workbook workbook = new HSSFWorkbook();
        Sheet sheet = workbook.createSheet();

        ArrayList<Row> rows = tableModel.getRows();
        ArrayList<Column> columns = tableModel.getColumns();
        org.apache.poi.ss.usermodel.Row rowColumn = sheet.createRow(0);
        for (int i = 0; i < columns.size(); i++) {
            Column column = columns.get(i);
            Cell cellExcel = rowColumn.createCell(i + 1);
            cellExcel.setCellValue(column.text);
            cellExcel.setCellType(CellType.STRING);
            CellStyle cellStyle = cellExcel.getCellStyle();
            Font font = workbook.createFont();
            font.setColor((short) column.pref.colorFont);
            font.setFontHeight((short) column.pref.sizeFont);
            font.setItalic(column.pref.italic == 1);
            font.setBold(column.pref.bold == 1);

            cellStyle.setFont(font);
            cellExcel.setCellStyle(cellStyle);
        }
        for (int stroke = 0; stroke < rows.size(); stroke++) {
            Row header = rows.get(stroke);
            org.apache.poi.ss.usermodel.Row row = sheet.createRow(stroke + 1);

            Cell cellHeader = row.createCell(0);
            cellHeader.setCellValue(header.text);
            cellHeader.setCellType(CellType.STRING);
            CellStyle cellStyleHeader = cellHeader.getCellStyle();
            Font fontHeader = workbook.createFont();
            fontHeader.setColor((short) header.pref.colorFont);
            fontHeader.setFontHeight((short) header.pref.sizeFont);
            fontHeader.setItalic(header.pref.italic == 1);
            fontHeader.setBold(header.pref.bold == 1);
            cellStyleHeader.setFont(fontHeader);
            cellHeader.setCellStyle(cellStyleHeader);
            for (int column = 0; column < columns.size(); column++) {
                ru.developer.press.mytable.model.Cell cell = header.getCellAtIndex(column);

                Cell cellExcel = row.createCell(column + 1);
                cellExcel.setCellValue(cell.text);
                cellExcel.setCellType(CellType.STRING);
                CellStyle cellStyle = cellExcel.getCellStyle();
                Font font = workbook.createFont();
                font.setColor((short) cell.pref.colorFont);
                font.setFontHeight((short) cell.pref.sizeFont);
                font.setItalic(cell.pref.italic == 1);
                font.setBold(cell.pref.bold == 1);
                cellStyle.setFont(font);
                cellExcel.setCellStyle(cellStyle);
            }
        }

        return workbook;
    }

    @NonNull
    public static String getRandomKey() {
        String key = UUID.randomUUID().toString();
        return "key_" + key;
    }

    @SuppressLint("CheckResult")
    public static void getBackTask(TaskListener taskListener) {
        taskListener.preExecute();
        Observable.fromCallable((Callable<Object>) () -> {
            taskListener.doOnBackground();
            return 0;
        }).subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(o -> {
                    AppEvents appEvents = AppEvents.get();
                    if (appEvents.isStartOpenTable() || appEvents.isSaveStart()) {
                        taskListener.main();
                        AppEvents.destroy();
                    }

                });

//        new AsyncTask<Void, Void, Void>() {
//            @Override
//            protected void onPreExecute() {
//                taskListener.preExecute();
//            }
//
//            @Override
//            protected Void doInBackground(Void... voids) {
//                taskListener.doOnBackground();
//                return null;
//            }
//
//            @Override
//            protected void onPostExecute(Void aVoid) {
//                taskListener.main();
//            }
//        }.execute();
//        Single.fromCallable(() -> {
//            return 0;
//        }).subscribeOn(Schedulers.io())
//                .observeOn(AndroidSchedulers.mainThread())
//                .subscribe(integer ->
//                );
    }

}
