package ru.developer.press.mytable.table;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.view.View;

import com.google.android.gms.common.util.ArrayUtils;

import org.apache.poi.ss.usermodel.Workbook;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import ru.developer.press.mytable.dialogs.DialogNameTable;
import ru.developer.press.mytable.helpers.BottomMenuControl;
import ru.developer.press.mytable.helpers.ColumnAttribute;
import ru.developer.press.mytable.helpers.Coordinate;
import ru.developer.press.mytable.helpers.Formula;
import ru.developer.press.mytable.helpers.StaticMethods;
import ru.developer.press.mytable.helpers.TableFileHelper;
import ru.developer.press.mytable.helpers.TableLab;
import ru.developer.press.mytable.history.Command;
import ru.developer.press.mytable.history.HistoryManager;
import ru.developer.press.mytable.history.comands.AddColumn;
import ru.developer.press.mytable.history.comands.AddStroke;
import ru.developer.press.mytable.history.comands.DeleteColumn;
import ru.developer.press.mytable.history.comands.DeleteStroke;
import ru.developer.press.mytable.history.comands.EditCell;
import ru.developer.press.mytable.history.comands.EditPrefs;
import ru.developer.press.mytable.history.comands.HeightStroke;
import ru.developer.press.mytable.history.comands.RenameColumn;
import ru.developer.press.mytable.history.comands.WidthColumn;
import ru.developer.press.mytable.interfaces.SendTableGetter;
import ru.developer.press.mytable.interfaces.table.BottomMenuClick;
import ru.developer.press.mytable.interfaces.table.TableActivityInterface;
import ru.developer.press.mytable.interfaces.table.TableScroller;
import ru.developer.press.mytable.interfaces.table.callback.EditCellListener;
import ru.developer.press.mytable.interfaces.table.callback.HistoryUpdateListener;
import ru.developer.press.mytable.interfaces.table.callback.PrefCellsListener;
import ru.developer.press.mytable.interfaces.table.callback.RenameColumnListener;
import ru.developer.press.mytable.interfaces.table.callback.SelectorListener;
import ru.developer.press.mytable.interfaces.table.callback.SettingTableListener;
import ru.developer.press.mytable.interfaces.table.callback.TableViewListener;
import ru.developer.press.mytable.interfaces.table.callback.UpdateHeight;
import ru.developer.press.mytable.interfaces.table.callback.WidthListener;
import ru.developer.press.mytable.model.Cell;
import ru.developer.press.mytable.model.CellAbstract;
import ru.developer.press.mytable.model.Column;
import ru.developer.press.mytable.model.Row;
import ru.developer.press.mytable.model.TableModel;
import ru.developer.press.mytable.table.builders.CellBuilder;
import ru.developer.press.mytable.table.builders.ColumnBuilder;
import ru.developer.press.mytable.table.builders.LineBuilder;
import ru.developer.press.mytable.table.builders.RowBuilder;
import ru.developer.press.mytable.table.builders.TotalAmountBuilder;

import static ru.developer.press.mytable.helpers.BottomMenuControl.AddButtonEnum.ADD_LEFT_COLUMN;
import static ru.developer.press.mytable.helpers.BottomMenuControl.AddButtonEnum.ADD_UP_STROKE;

public class TablePresenter implements TableViewListener, BottomMenuClick, EditCellListener, RenameColumnListener, HistoryUpdateListener {

    private static TablePresenter tablePresenter;
    private TableModel tableModel;
    private HistoryManager history;

    private TableActivityInterface activityInterface;
    private CellBuilder cellBuilder;
    private RowBuilder rowBuilder;
    private ColumnBuilder columnBuilder;
    private TotalAmountBuilder amountBuilder;

    private LineBuilder lineBuilder;

    private SelectMode selectMode = SelectMode.disable;
    private TableScroller scroller;
    private Selection selection;


    private TablePresenter(Context context) {
        history = new HistoryManager();
        columnBuilder = new ColumnBuilder(context, this);
        cellBuilder = new CellBuilder(context, this);
        rowBuilder = new RowBuilder(context);
        amountBuilder = new TotalAmountBuilder(context);
        lineBuilder = new LineBuilder(context);

        selection = new Selection(context, new SelectorListener() {
            @Override
            public void selectZone(float startX, float endX, float startY, float endY) {
                Coordinate selectZone = null;
                if (selectMode == SelectMode.cell) {
                    selectZone = cellBuilder.selectCellsOfSelector(startX, endX, startY, endY, tableModel);
                    if (cellBuilder.getSelectCellSize() == 1) {
                        Cell selectCell = cellBuilder.getSelectCell(tableModel);
                        activityInterface.showEditCellWindow(selectCell.inputType, selectCell.text);
                        //
                    } else {
                        activityInterface.hideEditCellWindow();
                    }
                } else if (selectMode == SelectMode.row)
                    selectZone = rowBuilder.selectCellsOfSelector(startX, endX, startY, endY, tableModel);
                else if (selectMode == SelectMode.column) {
                    selectZone = columnBuilder.selectCellsOfSelector(startX, endX, startY, endY, tableModel);
                    int columnCount = columnBuilder.selectColumnCount;
                    // сначало показываем меню если выделелили первую (потом будет false)
                    activityInterface.showMenuOfColumns(false, columnCount);
                }
                selection.setSelectionCoordinate(selectZone);
            }

            @Override
            public void scrollToSelectorOffside(Coordinate coordinate) {
                scrollToCell();
            }
        });

//        // высота тотал
//        cellBuilder.init(tableModel);
//        for (int i = 0; i < tableModel.getRows().size(); i++) {
//            cellBuilder.updateHeightStroke(tableModel, i);
//        }
//        cellBuilder.updateHeightColumn(tableModel);
////        cellBuilder.init(tableModel);
    }

    public static TablePresenter get(Context context) {
        if (tablePresenter == null)
            tablePresenter = new TablePresenter(context);
        return tablePresenter;
    }

    private void showMenuOfCell() {
        if (selectMode == SelectMode.cell)
            return;
        Cell selectCell = cellBuilder.getSelectCell(tableModel);
        activityInterface.showEditCellWindow(selectCell.inputType, selectCell.text);
        activityInterface.showMenuOfCells();
    }

    @Override
    public void click(float x, float y, Coordinate coordinate) {
        // если нажали в области тотал
        if (tableModel.isTotalAmountEnable())
            if (y > coordinate.endY - tableModel.totalAmount.heightStroke)
                return;

        float startX = tableModel.widthRows + coordinate.startX;
        float startY = tableModel.heightColumns + coordinate.startY;
        int widthTable = tableModel.getWidthTable() + tableModel.widthRows;
        int heightTable = tableModel.getHeightTable() + tableModel.heightColumns;
        // чтоб не выделяла ячейку когда нажимаешь на общую строку

        if (tableModel.isTotalAmountEnable())
            heightTable -= tableModel.totalAmount.heightStroke;

        if (x > startX && x < widthTable
                && y > startY && y < heightTable)
            cellClick(x, y);
        else if (x < startX && y > startY)
            rowClick(y);
        else if (y < startY && x > startX)
            columnClick(x);
    }

    @Override
    public void moveSelector(float x, float y, Coordinate coordinate) {

        if (selectMode == SelectMode.cell)
            selection.moveSelector(x, y);
        else if (selectMode == SelectMode.row)
            selection.moveSelector(0, y);
        else if (selectMode == SelectMode.column)
            selection.moveSelector(x, 0);
        invalidate();

//        selection.scrollToMoveCoordinate(coordinate, tableModel.widthRows, tableModel.heightColumns);
    }

    @Override
    public int getTableWidth() {
        int width = 0;
        if (tableModel != null)
            width = tableModel.getWidthTable() + tableModel.widthRows;

        return width;
    }

    @Override
    public int getTableHeight() {
        int height = 0;
        if (tableModel != null)
            height = tableModel.getHeightTable() + tableModel.heightColumns;
        return height;
    }

    @Override
    public void scrollToInTable() {
        // если включена панель общей суммы то ее высоту включаем в просчет высоты таблицы
        scroller.scrollToEndIfOutside(tableModel.getWidthTable() + tableModel.widthRows,
                tableModel.getHeightTable() + tableModel.heightColumns);
    }

    private void cellClick(float x, float y) {
        int[] touchCellIndex = tableModel.findIndexCellFormCoordinate(x, y);
        int stroke = touchCellIndex[0];
        int column = touchCellIndex[1];

        if (stroke > tableModel.getRows().size() - 1
                || stroke < 0
                || column > tableModel.getColumns().size() - 1
                || column < 0)
            return;

        if (cellBuilder.selectCell(stroke, column, tableModel)) {
            // отменяем все другие выделения если имеются
            if (selectMode == SelectMode.row) {
                rowBuilder.unSelectAll(tableModel);

            } else if (selectMode == SelectMode.column) {
                columnBuilder.unSelectAll(tableModel);
            }
            if (selectMode != SelectMode.cell)
                activityInterface.showMenuOfCells();
            selectMode = SelectMode.cell;
            //обновляем
            invalidate();

            Coordinate coordinate = tableModel.getCoordinateCell(stroke, column);
            selection.setCoordinateForSelectCell(
                    coordinate.startX + tableModel.widthRows,
                    coordinate.endX + tableModel.widthRows,
                    coordinate.startY + tableModel.heightColumns,
                    coordinate.endY + tableModel.heightColumns
            );
            scrollToCell();
            Cell selectCell = tableModel.getRows().get(stroke).getCellAtIndex(column);
            int inputType = selectCell.inputType;
            String text = selectCell.text;
            if (selectCell.inputType == 3)
                text = selectCell.valueFromFormula;
//            if (inputType == 3) {
//                if (coordinate.number != 0)
//                    text = Formula.parseStringToNumber(coordinate.number, false);
//                else text = "";
//
//            }
            activityInterface.showEditCellWindow(inputType, text);
            // задаем первоначальные координаты
        } else { // если нажали на выделенную область
            if (selectMode == SelectMode.cell) {

            } else if (selectMode == SelectMode.row) {

            } else if (selectMode == SelectMode.column) {

            }
            // showFormulaDialog menu to pref_cells
//            activityInterface.showMenuCell(x, y, iterface);
        }
        /*
        при клике или добавлении строки, столба - изменяется высота ячеек
        при клике выделяются все, надо делать если нажато на не выделенный участок то убрать выделения перед выделением
        да и вообще работы еще много
         */
    }

    private void scrollToCell() {
        Coordinate coordinate = new Coordinate();
        coordinate.setBounds(selection.getCoordinateScroll());
        coordinate.startX -= tableModel.widthRows;
        coordinate.startY -= tableModel.heightColumns;
        scroller.scrollToCell(coordinate);
    }

    private void rowClick(float y) {
        int index = tableModel.findIndexRowFormCoordinate(y);
        if (index >= tableModel.getRows().size() || index < 0)
            return;

        if (selectMode == SelectMode.cell) {
            cellBuilder.unSelectAll(tableModel);
            activityInterface.hideEditCellWindow();

        } else if (selectMode == SelectMode.column) {
            columnBuilder.unSelectAll(tableModel);
        }
        // если мы не были в режиме строк то показать меню
        if (selectMode != SelectMode.row)
            activityInterface.showMenuOfHeaders();
        // вернет истину если нажали на область где не выделен row а если на выделенное место то нужно показать меню
        if (rowBuilder.selectRow(index, tableModel)) {

            selectMode = SelectMode.row;
            invalidate();
            Coordinate coordinate = rowBuilder.getSelectedCellCoordinate(tableModel);
            selection.setSelectionCoordinate(coordinate);

            // для скроллинга убераем значение высоты колоны потому что оно не должно существовать когда идет просчет величины))
            coordinate.startY -= tableModel.heightColumns;
            // приходится считать и тотал
            if (tableModel.isTotalAmountEnable() &&
                    tableModel.isVisibleTotalAmount())
                coordinate.endY += tableModel.totalAmount.heightStroke;
            scroller.scrollToStroke(coordinate);
            invalidate();
        } else {

        }

    }

    private void columnClick(float x) {
        int index = tableModel.findIndexColumnFormCoordinate(x);
        // если нажали дальше чем есть колоны то ничего не происходит
        if (index < 0)
            return;

        if (selectMode == SelectMode.cell) {
            cellBuilder.unSelectAll(tableModel);
        } else if (selectMode == SelectMode.row) {
            rowBuilder.unSelectAll(tableModel);
        }
        if (columnBuilder.selectColumn(index, tableModel)) {
            activityInterface.hideEditCellWindow();
            // последовательность важна
            int columnCount = columnBuilder.selectColumnCount;
            activityInterface.showMenuOfColumns(selectMode != SelectMode.column, columnCount);
            selectMode = SelectMode.column;
            invalidate();
            Coordinate coordinate = columnBuilder.getSelectedCellCoordinate(tableModel);
            selection.setSelectionCoordinate(coordinate);
            // для правильного подсчета отнимаем ширину строки чтоб отчет с нуля был как в скроллстроке
            coordinate.startX -= tableModel.widthRows;
            scroller.scrollToColumn(coordinate);
        }
    }

    public void setInterfaces(Context context) {
        activityInterface = (TableActivityInterface) context;
        scroller = (TableScroller) context;
    }

    @Override
    public void draw(Canvas canvas, Coordinate coordinate) {
        if (tableModel == null)
            return;
        if (tableModel.getRows().size() > 0) {
            cellBuilder.draw(canvas, coordinate, tableModel);
            rowBuilder.draw(canvas, coordinate, tableModel);
            columnBuilder.draw(canvas, coordinate, tableModel);


            int heightTable = tableModel.getHeightTable() + tableModel.heightColumns;
            // если высота таблицы меньше чем вью то высота таблицы будет конечным значением для endY
            if (heightTable < coordinate.height)
                coordinate.endY = heightTable;
            // если ширина таблицы меньше чем вью то ширина таблицы будет конечным значением для endX
            int widthTable = tableModel.getWidthTable() + tableModel.widthRows;
            if (widthTable < coordinate.width)
                coordinate.endX = widthTable;

            boolean totalAmountEnable = tableModel.isTotalAmountEnable();
            if (totalAmountEnable)
                amountBuilder.draw(canvas, coordinate, tableModel);
            lineBuilder.draw(canvas, coordinate, tableModel);
            // тут для селектора уменьщаем конец по Y чтоб не выделял тотал
            if (totalAmountEnable) {
                // брать всегда выше тотал
                coordinate.endY -= tableModel.totalAmount.heightStroke;
            }

            if (selectMode == SelectMode.cell)
                selection.draw(canvas, coordinate, tableModel.widthRows, tableModel.heightColumns, SelectMode.cell);
            else if (selectMode == SelectMode.row)
                selection.draw(canvas, coordinate, tableModel.widthRows, tableModel.heightColumns, SelectMode.row);
            else if (selectMode == SelectMode.column)
                selection.draw(canvas, coordinate, tableModel.widthRows, tableModel.heightColumns, SelectMode.column);


        }
    }

    @Override
    public boolean touchCoordinate(float x, float y) {
//        SelectMode selectMode = null;
//        if (selectMode == SelectMode.cell) selectMode = SelectMode.cell;
//        else if (selectMode == SelectMode.row) selectMode = SelectMode.row;
//        else if (columnMode) selectMode = SelectMode.column;

        if (selectMode == SelectMode.cell || selectMode == SelectMode.row || selectMode == SelectMode.column)
            return selection.isTouchInSelector(x, y, selectMode);
//        else if (headerMode){
//            return  true
//        } else if (columnMode){
//            return true;
//        }
        return false;
    }

    @Override
    public void eventUp() {
        if (tableModel == null)
            return;
        selection.moveCoordinateToSelect();
        if (selectMode == SelectMode.cell) {
            if (cellBuilder.getSelectCellSize() == 0) {
                defaultState();
            }
        }
        invalidate();
    }

    @Override
    public void addColumn(BottomMenuControl.AddButtonEnum addButtonEnum) {
        // новая колона
        int startSelect = 0;
        int endSelect = 0;
        // на конец если мы не в режиме выделения стоблцов и не добавляем в левую сторону
        int addColumnIndex = tableModel.getColumns().size(); // от какого заголовка мы будем добавляться самомго левого или самого правого (по умолчанию последним)
        int copyPrefsColumn;

        // узнаем первую и последнюю выделенную ячейку
        boolean start = true;
        for (int i = 0; i < tableModel.getColumns().size(); i++) {
            Column columnSelect = tableModel.getColumns().get(i);
            if (columnSelect.isTouch) {
                if (start) {
                    startSelect = i;
                    start = false;
                }
                endSelect = i;
            }
        }

        // определяемся куда добавлять влево или вправо
        if (addButtonEnum == ADD_LEFT_COLUMN) {
            addColumnIndex = startSelect;
            copyPrefsColumn = startSelect;
        } else {
            if (selectMode == SelectMode.column) { // если мы в режиме выделения столбцов то ищем какой выделен последний
                addColumnIndex = endSelect + 1;
                copyPrefsColumn = endSelect;
            } else {
                copyPrefsColumn = addColumnIndex - 1; // если просто добавляем то последний столбец берется как настраиваемый по ширине
            }
        }

        // убираем выделения
        columnBuilder.unSelectAll(tableModel);

        // добавляем и возвращаем добавленный
        tableModel.addColumn(addColumnIndex, copyPrefsColumn);
        // нормализация высоты столбцов после инициализации
        columnBuilder.updateHeightColumn(tableModel);
        // инициализация колоны
        columnBuilder.initColumn(tableModel, addColumnIndex);
        if (tableModel.isLockAlwaysFitToScreen())
            fitToScreen();
        //выделяем
        Coordinate coordinateCell = tableModel.getCoordinateCell(0, addColumnIndex);
        columnClick(coordinateCell.endX + tableModel.widthRows);
//        скролим (по клику скролит)
//        scroller.scrollToColumn(coordinateCell);
//
        // добавление в историю
        final AddColumn addColumn = history.addColumn(addColumnIndex, tableModel);
        addColumn.setOnHistoryListener(this);
    }

    // тут идет логика добавления строки, смотря куда добавить, вниз или вверх
    @Override
    public void addStroke(BottomMenuControl.AddButtonEnum addButtonEnum) {
        ArrayList<Row> rows = tableModel.getRows();


        int addStrokeIndex = rows.size(); // от какой cтроки мы будем добавляться верхнего или самого нижнего
        // узнаем первую и последнюю выделенную ячейку
        int upSelect = 0;
        int downSelect = 0;
        // на конец если мы не в режиме выделения строк и не добавляем верх
        boolean start = true;
        for (int i = 0; i < rows.size(); i++) {
            Row row = rows.get(i);
            if (row.isTouch) { // не забываем что у строк используется isTouch
                if (start) {
                    upSelect = i;
                    start = false;
                }
                downSelect = i;
            }
        }

        int copyPrefsStrokeIndex = addStrokeIndex - 1; // изначально ее значение = размеру строк
        if (addButtonEnum == ADD_UP_STROKE) {
            addStrokeIndex = upSelect;
            copyPrefsStrokeIndex = upSelect;
        } else {
            if (selectMode == SelectMode.row) { // если мы в режиме выделения столбцов то ищем какой выделен последний
                // это нужно для того что бы добавлять строку после выделенной ячейки а не на ее место
                addStrokeIndex = downSelect + 1;
                copyPrefsStrokeIndex = downSelect;
            }
        }
        if (selectMode == SelectMode.row)
            rowBuilder.unSelectAll(tableModel);

        tableModel.addStroke(addStrokeIndex, copyPrefsStrokeIndex);
        int finalAddStrokeIndex = addStrokeIndex;

        cellBuilder.updateHeightStroke(tableModel, finalAddStrokeIndex);
        rowBuilder.updateRow(tableModel, finalAddStrokeIndex);

        Coordinate coordinateCell = tableModel.getCoordinateCell(finalAddStrokeIndex, 0);
        rowClick(coordinateCell.startY + 1 + tableModel.heightColumns);

//        activityInterface.invalidate();

        final AddStroke addStrokeHistory = history.addStroke(finalAddStrokeIndex, tableModel);
        addStrokeHistory.setOnHistoryListener(this);

//        Single.fromCallable(() -> {
//            // инициализация всего
//            // инитим строку отдельно
//            return finalAddStrokeIndex;
//        }).subscribeOn(Schedulers.io())
//                .observeOn(AndroidSchedulers.mainThread())
//                .subscribe(myObject -> {
//                });
        // выделить строку
//        scroller.scrollToStroke(rows.get(addStrokeIndex));

        // добавление в историю
    }

    @Override
    public void setWidth(View view) { // view is'nt by here
        final ArrayList<Integer> selectIndex = new ArrayList<>();
        if (selectMode == SelectMode.column) {
            for (int i = 0; i < tableModel.getColumns().size(); i++) {
                if (tableModel.getColumns().get(i).isTouch)
                    selectIndex.add(i);
            }
        } else {
            for (int i = 0; i < tableModel.getColumns().size(); i++) {
                selectIndex.add(i);
            }
        }
        final WidthColumn widthStory = new WidthColumn();
        ArrayList<Integer> width = widthStory.getCurrentWidth(tableModel);
        widthStory.setOldWidths(ArrayUtils.toPrimitiveArray(width));

        // интерфейс для обратной связи
        WidthListener widthListener = new WidthListener() {

            @Override // тут задается смещение на которое сместился ползунок
            public void setWidthCell(int dx) {

                ArrayList<Column> columnsPref = tableModel.getColumns();
                for (int i = 0; i < selectIndex.size(); i++) {
                    Column column = columnsPref.get(selectIndex.get(i));
                    column.setWidth(tableModel, dx);
                }
                update();

            }

            @Override // кнопка для того чтобы настроить ширину всех столбцов под экран
            public void setWidthForScreen() {

//                StaticMethods.getBackTask(new TaskListener() {
//                    @Override
//                    public void preExecute() {
//
//                    }
//
//                    @Override
//                    public void doOnBackground()
//                    {
//                    }
//
//                    @Override
//                    public void main() {
//                    }
//                });
                if (tableModel.isLockAlwaysFitToScreen())
                    return;

                tableModel.fitToScreen(0);
                update();
                stopTracking();
                setWidth(view);
            }

            // этот метод служит для того что бы всегда получать усредненную ширину всех столбцов
            @Override
            public int getGeneralWidth() {
                int width = 0;
                if (selectMode == SelectMode.column) {
                    for (int i = 0; i < selectIndex.size(); i++) {
                        Column column = tableModel.getColumns().get(selectIndex.get(i));
                        width += column.getWidth();
                    }
                    width /= selectIndex.size();
                } else {

                    for (Column col : tableModel.getColumns()) {
                        width += col.getWidth();
                    }
                    width /= tableModel.getColumns().size();
                }
                return width;
            }

            @Override
            public void onDismiss() {
                ArrayList<Integer> width = widthStory.getCurrentWidth(tableModel);
                widthStory.setNewWidths(ArrayUtils.toPrimitiveArray(width));
                widthStory.setOnHistoryListener(TablePresenter.this);
                if (widthStory.isEdited())
                    history.setWidth(widthStory);

            }

            @Override
            public void stopTracking() {
                init();
                update();
            }

            private void init() {
                for (int i = 0; i < tableModel.getRows().size(); i++) {
                    cellBuilder.updateHeightStroke(tableModel, i);
                }
                for (int index :
                        selectIndex) {
                    amountBuilder.initTotalAmountToColumn(tableModel, index);
                }
                columnBuilder.updateHeightColumn(tableModel);
                amountBuilder.updateHeightTotalAmount(tableModel);
            }

            private void update() {

//                for (int index :
//                        selectIndex) {
//                    columnBuilder.initColumn(tableModel, index);
//                }
                if (tableModel.isLockAlwaysFitToScreen())
                    fitToScreen();
                updateSelectionCoordinate(); //
                invalidate();
                scrollToInTable();
            }
        };
        activityInterface.showEditWidthCells(tableModel.widthRows, widthListener);
    }

    @Override
    public void setHeightCells() {
        final ArrayList<Integer> selectIndex = new ArrayList<>();
        if (selectMode == SelectMode.row) {
            for (int i = 0; i < tableModel.getRows().size(); i++) {
                if (tableModel.getRows().get(i).isTouch)
                    selectIndex.add(i);
            }
        } else {
            for (int i = 0; i < tableModel.getRows().size(); i++) {
                selectIndex.add(i);
            }
        }
        int genHeight = 0;
        for (int index :
                selectIndex) {
            genHeight += tableModel.getRows().get(index).height;
        }
        genHeight = genHeight / selectIndex.size();


        ArrayList<Row> rowPrefs = tableModel.getRows();
        int[] oldHeight = new int[rowPrefs.size()];
        for (int i = 0; i < rowPrefs.size(); i++) {
            Row row = rowPrefs.get(i);
            oldHeight[i] = (int) row.height;
        }

        UpdateHeight updateHeight = new UpdateHeight() {
            @Override
            public void setHeight(int height) {

                for (int index : selectIndex) {
                    Row rowPref = tableModel.getRows().get(index);
                    int paddingSize = rowPref.pref.paddingDown + rowPref.pref.paddingUp;
                    for (Cell cell : tableModel.getRows().get(index).getCells()) {
                        int paddingSizeCell = cell.pref.paddingDown + cell.pref.paddingUp;
                        if (paddingSize < paddingSizeCell)
                            paddingSize = paddingSizeCell;
                    }
                    if (height < paddingSize) height = paddingSize + 2;

                    rowPref.height = height;
                    rowPref.heightStroke = height;
                }
                // просто рисуем какую высоту мы задали
                update();
            }

            private void update() {
//                cellBuilder.init(tableModel);
                updateSelectionCoordinate();
                invalidate();
                scrollToInTable();
            }

            @Override
            public void onDismiss() {

                int[] newHeight = new int[rowPrefs.size()];
                for (int i = 0; i < rowPrefs.size(); i++) {
                    Row row = rowPrefs.get(i);
                    newHeight[i] = (int) row.height;
                }
                HeightStroke heightStroke = new HeightStroke(oldHeight, newHeight);
                heightStroke.setOnHistoryListener(TablePresenter.this);
                if (heightStroke.isEdited())
                    history.setHeightStroke(heightStroke);
            }

            @Override
            public void stopTracking() {
                // делаем как надо с учетом блокировки высоты
                for (int index : selectIndex) {
//                    Row headerPref = tableModel.getRows().get(index);
//                    headerPref.height = headerPref.heightStroke;
                    cellBuilder.updateHeightStroke(tableModel, index);
                }
                update();
            }
        };
        activityInterface.showEditHeightCells(genHeight, updateHeight);
    }

    @Override
    public void settingTable() {
        SettingTableListener setting = new SettingTableListener(tableModel) {

            @Override
            public void setLockHeight(boolean lockHeight) {
                tableModel.setLockHeightCells(lockHeight);

                for (int i = 0; i < tableModel.getRows().size(); i++) {
                    cellBuilder.updateHeightStroke(tableModel, i);
                }
//                cellBuilder.init(tableModel);
//                resume();
                updateSelectionCoordinate();
                invalidate();
                scrollToInTable();
            }

            @Override
            public void setLockAlwaysFitToScreen(boolean lockAlwaysFitToScreen) {
                tableModel.setLockAlwaysFitToScreen(lockAlwaysFitToScreen);
                if (lockAlwaysFitToScreen) {

                    fitToScreen();
                    invalidate();
                    scrollToInTable();
                }
            }

            @Override
            public void setTotalAmountEnable(boolean totalAmountEnable) {
                tableModel.setTotalAmountEnable(totalAmountEnable);
                invalidate();
//                cellBuilder.init(tableModel);
                scrollToInTable();
            }
        };
        activityInterface.showSettingTable(setting);
    }

    private void fitToScreen() {
        if (tableModel.getWidthTable() + tableModel.widthRows == tableModel.getWidthView()
                || !tableModel.isLockAlwaysFitToScreen())
            return;

        tableModel.fitToScreen((float) 0);
        for (int i = 0; i < tableModel.getRows().size(); i++) {
            cellBuilder.updateHeightStroke(tableModel, i);
        }
        for (int i = 0; i < tableModel.getColumns().size(); i++) {

            amountBuilder.initTotalAmountToColumn(tableModel, i);

        }
        columnBuilder.updateHeightColumn(tableModel);
        amountBuilder.updateHeightTotalAmount(tableModel);
//        cellBuilder.init(tableModel);
    }

    public void updateTable(Context context) {

        if (tableModel != null && !history.isSaved) {
            TableLab.get(context).saveTable(tableModel);
            history.isSaved = true;
        }
//        else
//            AppEvents.get().tableFileSaved();
    }

    public void setText(String s) {

        if (selectMode == SelectMode.cell) {
            Cell cell = cellBuilder.getSelectCell(tableModel);
//            Column column = tableModel.getColumns().get(cell.indexColumn);
//            int inputType = column.getInputType();

//            if (inputType == 1) {
//                cell.number = Formula.parseStringToNumber(text);
//            }
//            else
//            if (inputType == 3) {
//                double number = column.getFormula().getValueFromFormula(tableModel, cell.indexRow);
//                text = Formula.parseStringToNumber(number, true);
//            }
            String oldText = cell.text;
            int oldHeight = (int) cell.height;

            if (cell.inputType == 3) {
                cell.valueFromFormula = s;
            } else {
                cell.text = s;
            }
            cellBuilder.updateHeightStroke(tableModel, cell.indexRow);
//            cellBuilder.init(tableModel);
            if (!oldText.equals(cell.text)) {
                updateSelectionCoordinate();
                if (oldHeight != cell.height
                        || s.length() > 0 && s.charAt(s.length() - 1) == '\n') {
                    scrollToCell();
                    scrollToInTable();
                }
                cellBuilder.initTotalAmountToColumn(tableModel, cell.indexColumn);
                cellBuilder.updateCell(cell);
                cellBuilder.updateFormulaAtCell(tableModel, cell);
            }
        } else if (selectMode == SelectMode.column) {

            Column renameColumn = columnBuilder.getSelectColumn(tableModel);
            if (!s.equals(renameColumn.text)) {
                renameColumn.text = s;
                // обновляем высоту у колумнвью
                columnBuilder.updateHeightColumn(tableModel);
                //надо обновить чтоб высота была что нужно
//                columnBuilder.init(tableModel);
            }
        }

        invalidate();
    }

    public void openKeyboardEvent(boolean isOpen) {
//        if (cellMode)
        if (isOpen) {
            if (selectMode == SelectMode.cell)
                scrollToCell();
        } else
            scrollToInTable();
    }

    public void destroyed() {
        tablePresenter = null;
    }

    private void updateSelectionCoordinate() {
        Coordinate coordinate = null;
        if (selectMode == SelectMode.cell) {
            coordinate = cellBuilder.getSelectedCellCoordinate(tableModel);
        } else if (selectMode == SelectMode.row) {
            coordinate = rowBuilder.getSelectedCellCoordinate(tableModel);
        } else if (selectMode == SelectMode.column) {
            coordinate = columnBuilder.getSelectedCellCoordinate(tableModel);
        }
        if (coordinate != null) {
            selection.setSelectionCoordinate(coordinate);
        }
        invalidate();
    }

    public void resume() {
        if (tableModel == null)
            return;

        if (tableModel.isLockAlwaysFitToScreen())
            fitToScreen();
        Coordinate coordinate = null;
        if (selectMode == SelectMode.cell) {
            coordinate = cellBuilder.getSelectedCellCoordinate(tableModel);
            if (cellBuilder.getSelectCellSize() == 1)
                showMenuOfCell();

        } else if (selectMode == SelectMode.row) {
            coordinate = rowBuilder.getSelectedCellCoordinate(tableModel);
            activityInterface.showMenuOfHeaders();
        } else if (selectMode == SelectMode.column) {
            coordinate = columnBuilder.getSelectedCellCoordinate(tableModel);
            activityInterface.showMenuOfColumns(true, columnBuilder.selectColumnCount);
        }
        if (coordinate != null) {
            selection.setSelectionCoordinate(coordinate);
        }
        invalidate();
    }

    public boolean defaultState() {
        boolean toReturn = true;

        if (selectMode == SelectMode.cell) {
            cellBuilder.unSelectAll(tableModel);
            activityInterface.hideEditCellWindow();
            scrollToInTable();
        } else if (selectMode == SelectMode.row) {
            rowBuilder.unSelectAll(tableModel);
        } else if (selectMode == SelectMode.column) {
            columnBuilder.unSelectAll(tableModel);
            activityInterface.hideEditCellWindow();

        } else {
            toReturn = false;
        }
        selectMode = SelectMode.disable;

        if (toReturn)
            activityInterface.showDefaultMenu();
        invalidate();
        return toReturn;
    }

    public void getColumnRenameWin() {
        Column column = columnBuilder.getSelectColumn(tableModel);
        if (column != null)
            activityInterface.showEditCellWindow(0, column.text);
    }

    public void prefCells() {
//
        final ArrayList<int[]> indexCells = new ArrayList<>();
        final ArrayList<Integer> indexColumns = new ArrayList<>();
        final ArrayList<Integer> indexHeaderPrefs = new ArrayList<>();

        for (int i = 0; i < tableModel.getColumns().size(); i++) {
            if (tableModel.getColumns().get(i).isTouch) {
                indexColumns.add(i);
            }
        }
        for (int i = 0; i < tableModel.getRows().size(); i++) {
            if (tableModel.getRows().get(i).isTouch)
                indexHeaderPrefs.add(i);
        }
        for (int i = 0; i < tableModel.getRows().size(); i++) {
            for (int j = 0; j < tableModel.getColumns().size(); j++) {
                Cell cell = tableModel.getRows().get(i).getCellAtIndex(j);
                if (cell.isTouch) {
                    int[] index = new int[2];
                    index[0] = i;
                    index[1] = j;
                    indexCells.add(index);

                }
            }
        }
        // обновляем имена в формуле так как они не меняются когда переименовывается столбец
        for (int index : indexColumns) {
            Column column = tableModel.getColumns().get(index);
            column.getFormula().updateColumnNames(tableModel);
        }

        final EditPrefs editColumn = new EditPrefs(indexColumns, indexHeaderPrefs, indexCells);
        editColumn.setOnHistoryListener(this);
        editColumn.setOldPrefs(tableModel);
//
        PrefCellsListener prefCellsListener = new PrefCellsListener() {

            public void update() {
                if (selectMode == SelectMode.cell) {
                    int index = -1;
                    for (int i = 0; i < indexCells.size(); i++) {
                        int[] indexCell = indexCells.get(i);
                        int indexRow = indexCell[0];
                        int indexColumn = indexCell[1];
                        Cell cell = tableModel.getRows().get(indexRow).getCellAtIndex(indexColumn);
                        cellBuilder.updateCell(cell);
                        //типа опимизация
                        if (indexRow != index)
                            cellBuilder.updateHeightStroke(tableModel, indexRow);
                        index = indexRow;
                    }
//                    cellBuilder.init(tableModel);
                } else if (selectMode == SelectMode.row) {
                    for (int index : indexHeaderPrefs) {
                        Row row = tableModel.getRows().get(index);
                        for (Cell cell : row.getCells()) {
                            cellBuilder.updateCell(cell);
                        }
                        cellBuilder.updateHeightStroke(tableModel, index);
                    }
//                    cellBuilder.init(tableModel);
                } else if (selectMode == SelectMode.column) {
                    for (int i = 0; i < tableModel.getRows().size(); i++) {
                        for (int index : indexColumns) {
                            Cell cell = tableModel.getRows().get(i).getCellAtIndex(index);
                            cellBuilder.updateCell(cell);
                        }
                        cellBuilder.updateHeightStroke(tableModel, i);
                    }
                    columnBuilder.updateHeightColumn(tableModel);
//                    cellBuilder.init(tableModel);
                }
                updateSelectionCoordinate();
                invalidate();
            }

            @Override
            public boolean verifyIsCircledDepended(Formula formula) {
                Column columnPref = tableModel.getColumns().get(indexColumns.get(0));
//                Formula formula = tableModel.getColumns().get(indexColumns.get(0)).getFormula();
                ArrayList<ColumnAttribute> columnAtr = formula.getColumnAttributes();
                for (int i = 0; i < columnAtr.size(); i++) {
                    Column column = tableModel.getColumnAtId(columnAtr.get(i).getNameId());
                    if (column != null)
                        if (column.getInputType() == 3) {
                            for (ColumnAttribute colAttr : column.getFormula().getColumnAttributes()) {
                                if (colAttr.getNameId().equals(columnPref.getNameIdColumn()))
                                    return true;
                            }
                        }
                }
                return false;
            }

            @Override
            public int getSelectedColumnSize() {
                return indexColumns.size();
            }

            @Override
            public void setDateVariable(int variable) {
                dateVariable = variable;
                tableModel.setDateType(variable);
            }

            @Override
            public void setType(int type) {
                if (selectMode != SelectMode.column)
                    return;
                this.type = type;
                for (int i = 0; i < indexColumns.size(); i++) {
                    Column column = tableModel.getColumns().get(indexColumns.get(i));
                    column.setInputType(type);
                    if (type == 3) {
//                        if (column.getInputType() == 3) {
//                        for (int j = 0; j < tableModel.getRows().size(); j++) {
//                            Cell cell = tableModel.getRows().get(j).getCellAtIndex(column.index);
//                            cell.getNumber();
//                        }
//                        }
                    }
//                    if (type != 1 && type != 3) {
//                        if (column.getInputType() == 1 || column.getInputType() == 3) {
//                            for (int j = 0; j < tableModel.getRows().size(); j++) {
//                                Cell cell = tableModel.getRows().get(j).getCellAtIndex(column.index);
//                                cell.text = Formula.parseStringToNumber(cell.number, true);
////                                cell.number = 0;
//                            }
//                        }
//                    }
                    columnBuilder.initColumn(tableModel, indexColumns.get(i));
                }
                update();
            }

            @Override
            public void setFormula(Formula formula) {
                if (selectMode != SelectMode.column)
                    return;
                this.formula = formula;
                for (int i = 0; i < indexColumns.size(); i++) {
                    Column column = tableModel.getColumns().get(indexColumns.get(i));
                    column.setFormula(formula);
                }
                update();
            }

            @Override
            public void setSizeText(int size) {
                this.pref.sizeFont = size;
                for (int i = 0; i < indexColumns.size(); i++) {
                    Column column = tableModel.getColumns().get(indexColumns.get(i));
                    column.pref.sizeFont = size;
                }
                for (int index : indexHeaderPrefs) {
                    Row row = tableModel.getRows().get(index);
                    row.pref.sizeFont = size;
                }
                if (checkCellPrefs)
                    for (int[] index : indexCells) {
                        Cell cell = tableModel.getRows().get(index[0]).getCellAtIndex(index[1]);
                        cell.pref.sizeFont = size;
                    }
                update();
            }

            @Override
            public void setBold(int bold) {
                this.pref.bold = bold;
                for (int i = 0; i < indexColumns.size(); i++) {
                    Column column = tableModel.getColumns().get(indexColumns.get(i));
                    column.pref.bold = bold;
                }
                for (int index : indexHeaderPrefs) {
                    Row row = tableModel.getRows().get(index);
                    row.pref.bold = bold;
                }
                if (checkCellPrefs)
                    for (int[] index : indexCells) {
                        Cell cell = tableModel.getRows().get(index[0]).getCellAtIndex(index[1]);
                        cell.pref.bold = bold;
                    }
                update();
            }

            @Override
            public void setItalic(int italic) {
                this.pref.italic = italic;
                for (int i = 0; i < indexColumns.size(); i++) {
                    Column column = tableModel.getColumns().get(indexColumns.get(i));
                    column.pref.italic = italic;
                }
                for (int index : indexHeaderPrefs) {
                    Row row = tableModel.getRows().get(index);
                    row.pref.italic = italic;
                }
                if (checkCellPrefs)
                    for (int[] index : indexCells) {
                        Cell cell = tableModel.getRows().get(index[0]).getCellAtIndex(index[1]);
                        cell.pref.italic = italic;
                    }
                update();
            }

            @Override
            public void setColorText(int color) {
                this.pref.colorFont = color;
                for (int i = 0; i < indexColumns.size(); i++) {
                    Column column = tableModel.getColumns().get(indexColumns.get(i));
                    column.pref.colorFont = pref.colorFont;
                }
                for (int index : indexHeaderPrefs) {
                    Row row = tableModel.getRows().get(index);
                    row.pref.colorFont = pref.colorFont;
                }
                if (checkCellPrefs)
                    for (int[] index : indexCells) {
                        Cell cell = tableModel.getRows().get(index[0]).getCellAtIndex(index[1]);
                        cell.pref.colorFont = pref.colorFont;
                    }
                update();
            }

            @Override
            public void setColorBack(int color) {
                this.pref.colorBack = color;
                for (int i = 0; i < indexColumns.size(); i++) {
                    Column column = tableModel.getColumns().get(indexColumns.get(i));
                    column.pref.colorBack = pref.colorBack;
                }
                for (int index : indexHeaderPrefs) {
                    Row row = tableModel.getRows().get(index);
                    row.pref.colorBack = pref.colorBack;
                }
                if (checkCellPrefs)
                    for (int[] index : indexCells) {
                        Cell cell = tableModel.getRows().get(index[0]).getCellAtIndex(index[1]);
                        cell.pref.colorBack = pref.colorBack;
                    }
                update();
            }

            @Override
            public void updatePadding() {
                for (int i = 0; i < indexColumns.size(); i++) {
                    Column column = tableModel.getColumns().get(indexColumns.get(i));
                    column.pref.paddingLeft = pref.paddingLeft;
                    column.pref.paddingRight = pref.paddingRight;
                    column.pref.paddingUp = pref.paddingUp;
                    column.pref.paddingDown = pref.paddingDown;
                }

                if (checkCellPrefs)
                    for (int[] index : indexCells) {
                        Cell cell = tableModel.getRows().get(index[0]).getCellAtIndex(index[1]);
                        cell.pref.paddingLeft = pref.paddingLeft;
                        cell.pref.paddingRight = pref.paddingRight;
                        cell.pref.paddingUp = pref.paddingUp;
                        cell.pref.paddingDown = pref.paddingDown;
                    }
                updateWidthColumns();
                update();
            }

            private void updateWidthColumns() {
                for (int i = 0; i < indexColumns.size(); i++) {
                    Column column = tableModel.getColumns().get(indexColumns.get(i));
                    // второй параметр типа ничего не добавлять (он кстати не назначает а добавляет к имеющейся)
                    column.setWidth(tableModel, 0);
                }
            }

            @Override
            public void closeWindow() {
                editColumn.setNewPrefs(tableModel);
                history.editPrefs(editColumn);
            }

            @Override
            public List<ColumnAttribute> getColumnAttrs() {
                ArrayList<ColumnAttribute> columnAttributes = new ArrayList<>();
                for (int i = 0; i < tableModel.getColumns().size(); i++) {
                    Column column = tableModel.getColumns().get(i);
                    // если это колона та которая выделена то не добавляем
//                    if (column == tableModel.getColumns().get(indexColumns.get(0)))
//                        continue;

                    ColumnAttribute attr = new ColumnAttribute(column.text,
                            column.getNameIdColumn(),
                            column.getInputType());
                    columnAttributes.add(attr);
                }
                return columnAttributes;
            }
        };

        // тут нужно для опредления какого типа данные для настройки отправляются в окно
        CellAbstract cellAbstract;
        if (selectMode == SelectMode.column) {
            cellAbstract = tableModel.getColumns().get(indexColumns.get(0));
            prefCellsListener.type = ((Column) cellAbstract).getInputType();
            prefCellsListener.formula = ((Column) cellAbstract).getFormula();
        } else if (selectMode == SelectMode.row) {
            cellAbstract = tableModel.getRows().get(indexHeaderPrefs.get(0));
        } else {
            cellAbstract = tableModel.getRows().get(indexCells.get(0)[0]).getCellAtIndex(indexCells.get(0)[1]);
        }

        prefCellsListener.mode = selectMode;
        prefCellsListener.pref.copyPref(cellAbstract.pref);
        prefCellsListener.dateVariable = tableModel.getDateType();

        activityInterface.showSettingCell(prefCellsListener);
    }

    public boolean deleteColumns() {

        if (columnBuilder.selectColumnCount >= tableModel.getColumns().size())
            return false;

        List<Column> columnSelect = new ArrayList<>();
        final List<Integer> indexes = new ArrayList<>();
        for (int i = 0; i < tableModel.getColumns().size(); i++) {
            Column column = tableModel.getColumns().get(i);
            if (column.isTouch) {
                columnSelect.add(column);
                indexes.add(i);
            }
        }
        //добавление в историю
        final DeleteColumn deleteColumn = history.deleteColumn(ArrayUtils.toPrimitiveArray(indexes), columnSelect, tableModel);
        deleteColumn.setOnHistoryListener(this);
        // и потом само удаление
        for (Column column : columnSelect) {
            tableModel.deleteColumn(column);
            // надо заново пересчитать их чтоб column.index колон обновились, при удалении ячеек используется индекс который в колоне
            columnBuilder.init(tableModel);
            activityInterface.invalidate();
        }
        columnBuilder.updateHeightColumn(tableModel);
        for (int i = 0; i < tableModel.getRows().size(); i++) {
            cellBuilder.updateHeightStroke(tableModel, i);
        }
//        columnBuilder.init(tableModel);
        defaultState();
        if (tableModel.isLockAlwaysFitToScreen())
            fitToScreen();
        scrollToInTable();

        return true;

    }

    public boolean deleteStroke() {
        // возвращает фалс если выделены все ячейки
        if (rowBuilder.selectRowCount >= tableModel.getRows().size())
            return false;

        //добавление в историю
        final List<Integer> indexes = new ArrayList<>();
        for (int i = 0; i < tableModel.getRows().size(); i++) {
            Row stroke = tableModel.getRows().get(i);
            if (stroke.isTouch) {
                indexes.add(i);
            }
        }
        final DeleteStroke deleteStroke = history.deleteStroke(ArrayUtils.toPrimitiveArray(indexes), tableModel);
        deleteStroke.setOnHistoryListener(this);

        rowBuilder.deleteRows(tableModel);
//        rowBuilder.init(tableModel);

        defaultState();
        scrollToInTable();
        return true;
    }

    public void undo() {
        if (history.getUndoCommands().size() > 0) {
            defaultState();
            history.undo(tableModel);
        }
    }

    public void redo() {
        if (history.getRedoCommands().size() > 0) {
            defaultState();
            history.redo(tableModel);
        }
    }

    @Override
    public void cellEdit(String oldText, String newText, long oldDate, long newDate, int[] index) {
        if (!oldText.equals(newText) || oldDate != newDate) {
            EditCell editCell = new EditCell(index);
            editCell.setOldCells(oldText, oldDate);
            editCell.setNewCells(newText, newDate);
            editCell.setOnHistoryListener(this);
            history.editCell(editCell);
        }
    }

    @Override
    public void renameColumn(String oldName, String newName, int index) {
        if (!oldName.equals(newName)) {
            RenameColumn renameColumn = new RenameColumn(index);
            renameColumn.setOldNameColumn(oldName);
            renameColumn.setNewNameColumn(newName);
            renameColumn.setOnHistoryListener(this);
            history.renameColumn(renameColumn);
        }
    }

    public void getDatePick() {
        Cell cell = cellBuilder.getSelectCell(tableModel);
        activityInterface.showDatePicker(cell.date);
    }

    public void setDate(long date) {
        Cell selectCell = cellBuilder.getSelectCell(tableModel);
        selectCell.date = date;
        rowBuilder.updateRow(tableModel, selectCell.indexRow);
        cellBuilder.updateHeightStroke(tableModel, selectCell.indexRow);
        activityInterface.showEditCellWindow(selectCell.inputType, selectCell.text);
        updateSelectionCoordinate();
        invalidate();
    }

    public void clearText() {
        if (selectMode != SelectMode.cell) {
            return;
        }
        cellBuilder.clearText(tableModel);
        Cell selectCell = cellBuilder.getSelectCell(tableModel);
        cellBuilder.updateHeightStroke(tableModel, selectCell.indexRow);
        invalidate();
//        cellBuilder.init(tableModel);
//        activityInterface.showEditCellWindow(selectCell.inputType, selectCell.text);
        updateSelectionCoordinate();
    }

    @Override
    public void undo(Command command) {

        if (command instanceof DeleteColumn) {
            selectMode = SelectMode.column;
            DeleteColumn deleteColumn = (DeleteColumn) command;
            for (int i = 0; i < deleteColumn.index.length; i++) {
                int index = deleteColumn.index[i];
                Column column = tableModel.getColumns().get(index);
                column.select(tableModel);
            }
            Coordinate coordinate = columnBuilder.getSelectedCellCoordinate(tableModel);
            columnBuilder.selectCellsOfSelector(coordinate.startX, coordinate.endX, coordinate.startY, coordinate.endY, tableModel);
            selection.setSelectionCoordinate(coordinate);
            int selectColumnCount = columnBuilder.selectColumnCount;
            activityInterface.showMenuOfColumns(true, selectColumnCount);


        } else if (command instanceof DeleteStroke) {
            DeleteStroke deleteStroke = (DeleteStroke) command;
            for (int i = 0; i < deleteStroke.index.length; i++) {
                int index = deleteStroke.index[i];
                Row row = tableModel.getRows().get(index);
                row.select();
            }
            selectMode = SelectMode.row;
            Coordinate coordinate = rowBuilder.getSelectedCellCoordinate(tableModel);
            selection.setSelectionCoordinate(coordinate);
            activityInterface.showMenuOfHeaders();

        } else if (command instanceof EditCell) {
            EditCell editCell1 = (EditCell) command;
            int[] index = editCell1.index;
            Coordinate coordinate = tableModel.getCoordinateCell(index[0], index[1]);
            int x = (int) (coordinate.startX + 1);
            int y = (int) (coordinate.startY + 1);
            cellClick(x + tableModel.widthRows, y + tableModel.heightColumns);
        } else if (command instanceof RenameColumn) {
            int index = ((RenameColumn) command).index;
            float x = tableModel.getColumns().get(index).startX + 1;
            columnClick(x + tableModel.widthRows);
        }
    }

    @Override
    public void redo(Command command) { // setWidth + setHeight


        if (command instanceof AddColumn) {                   //выделяем столбец который был возвращен
            AddColumn addColumn = (AddColumn) command;
            Coordinate coordinateCell = tableModel.getCoordinateCell(0, addColumn.index);
            float x = coordinateCell.startX + 1;
            columnClick(x + tableModel.widthRows);
        } else if (command instanceof AddStroke) {
            AddStroke addStroke = (AddStroke) command;
            Coordinate row = tableModel.getCoordinateCell(addStroke.index, 0);
            float y = row.startY + 1;
            rowClick(y + tableModel.heightColumns);
        } else if (command instanceof EditCell) {
            EditCell editCell1 = (EditCell) command;
            int[] index = editCell1.index;
            Coordinate coordinateCell = tableModel.getCoordinateCell(index[0], index[1]);
            int x = (int) (coordinateCell.startX + 1);
            int y = (int) (coordinateCell.startY + 1);
            cellClick(x + tableModel.widthRows, y + tableModel.heightColumns);
        } else if (command instanceof RenameColumn) {
            int index = ((RenameColumn) command).index;
            Coordinate column = tableModel.getCoordinateCell(0, index);
            float x = column.startX + 1;
            columnClick(x + tableModel.widthRows);
        }
    }

    public void initAndUpdate() {
//        defaultState();
//        long start = System.currentTimeMillis();
//        cellBuilder.init(tableModel);
//        Log.d(TAG, "initAndUpdate: " + (System.currentTimeMillis() - start));
        columnBuilder.init(tableModel);
        invalidate();
        columnBuilder.updateHeightColumn(tableModel);
        invalidate();
        for (int i = 0; i < tableModel.getRows().size(); i++) {
            rowBuilder.updateRow(tableModel, i);
            invalidate();
            cellBuilder.updateHeightStroke(tableModel, i);
            invalidate();
        }
        amountBuilder.initTotalAmount(tableModel);
        activityInterface.tableOpened();

//        AppEvents appEvents = AppEvents.get();
//        StaticMethods.getBackTask(new TaskListener() {
//
//            @Override
//            public void preExecute() {
//                appEvents.startOpenTable();
//            }
//
//            @Override
//            public void doOnBackground() {
//            }
//
//            @Override
//            public void main() {
//                invalidate();
//                scrollToInTable();
//            }
//        });
    }

    private void invalidate() {
        if (activityInterface != null)
            activityInterface.invalidate();
    }

    public void renameTable() {
        String openName = tableModel.openName;
        File tableFile = new File(openName);
        DialogNameTable.OnButtonClick buttonClick = (String newName) -> {
            tableModel.openName = TableFileHelper.renameFile(tableFile, newName + TableFileHelper.TBL);
            tableModel.setNameTable(newName);
            activityInterface.updateTableForToolbar(newName);
        };

        activityInterface.showRenameDialog(tableModel.getNameTable(), buttonClick);
    }

    public void sendTable() {

        SendTableGetter sendTableGetter = new SendTableGetter() {
            @Override
            public File getTableSource() {
                return new File(tableModel.openName);
            }

            @Override
            public File getTablePicture() {
                File pic = new File(TableFileHelper.getTableFolder(tableModel.openName) + tableModel.getNameTable() + ".png");
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

                try {
                    try (FileOutputStream fos = new FileOutputStream(pic)) {
                        bitmap.compress(Bitmap.CompressFormat.JPEG, 97, fos);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
                return pic;
            }

            @Override
            public File getTableExcel() {
                File excel = new File(TableFileHelper.getTableFolder(tableModel.openName) + tableModel.getNameTable() + ".xls");
                Workbook workbook = StaticMethods.getTableExcel(tableModel);
                try {
                    workbook.write(new FileOutputStream(excel));
                } catch (IOException e) {
                    e.printStackTrace();
                }
                return excel;
            }
        };
        activityInterface.showSendDialog(sendTableGetter);


    }

    public void visibleTotalAmount(boolean visible) {
        tableModel.setVisibleTotalAmount(visible);
        Row totalAmount = tableModel.totalAmount;
        int totalAmountMaxHeight = (int) amountBuilder.getTotalAmountMaxHeight(totalAmount);
        totalAmount.heightStroke = visible ? totalAmountMaxHeight : 0;
    }

    public boolean tableIsNull() {

        return tableModel == null;
    }

    public void setTableModel(TableModel table) {
        this.tableModel = table;
        tableModel.totalAmount.height = 70;
        tableModel.widthRows = (int) rowBuilder.widthHeaders;
//        initAndUpdate();
        //
    }

    public StringBuilder getNameTable() {
        StringBuilder stringBuilder = new StringBuilder();
        if (tableModel != null)
            stringBuilder.append(tableModel.getNameTable());
        return stringBuilder;
    }

    public enum SelectMode {
        cell,
        row,
        column,
        disable
    }
}
