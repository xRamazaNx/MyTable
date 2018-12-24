package ru.developer.press.mytable.table;

import android.content.Context;
import android.graphics.Canvas;
import android.view.View;

import com.google.android.gms.common.util.ArrayUtils;

import java.util.ArrayList;
import java.util.List;

import ru.developer.press.mytable.helpers.BottomMenuControl;
import ru.developer.press.mytable.helpers.Coordinate;
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
import ru.developer.press.mytable.interfaces.BottomMenuClick;
import ru.developer.press.mytable.interfaces.CellEditListener;
import ru.developer.press.mytable.interfaces.HistoryUpdateListener;
import ru.developer.press.mytable.interfaces.PrefCellsListener;
import ru.developer.press.mytable.interfaces.RenameColumnListener;
import ru.developer.press.mytable.interfaces.SelectorListener;
import ru.developer.press.mytable.interfaces.SettingTableListener;
import ru.developer.press.mytable.interfaces.TableActivityInterface;
import ru.developer.press.mytable.interfaces.TableScroller;
import ru.developer.press.mytable.interfaces.TableViewListener;
import ru.developer.press.mytable.interfaces.UpdateHeight;
import ru.developer.press.mytable.interfaces.UpdateWidth;
import ru.developer.press.mytable.table.builders.CellBuilder;
import ru.developer.press.mytable.table.builders.ColumnBuilder;
import ru.developer.press.mytable.table.builders.HeaderBuilder;
import ru.developer.press.mytable.table.builders.LineBuilder;
import ru.developer.press.mytable.table.model.Cell;
import ru.developer.press.mytable.table.model.CellAbstract;
import ru.developer.press.mytable.table.model.Column;
import ru.developer.press.mytable.table.model.Header;
import ru.developer.press.mytable.table.model.TableModel;

import static ru.developer.press.mytable.helpers.BottomMenuControl.AddButtonEnum.ADD_LEFT_COLUMN;
import static ru.developer.press.mytable.helpers.BottomMenuControl.AddButtonEnum.ADD_UP_STROKE;

public class TablePresenter implements TableViewListener, BottomMenuClick, CellEditListener, RenameColumnListener, HistoryUpdateListener {

    private static TablePresenter tablePresenter;
    private final TableModel tableModel;
    private HistoryManager history;

    private TableActivityInterface activityInterface;
    private CellBuilder cellBuilder;
    private HeaderBuilder headerBuilder;
    private ColumnBuilder columnBuilder;
    private LineBuilder lineBuilder;

    private boolean cellMode = false;
    private boolean headerMode = false;
    private boolean columnMode = false;
    private TableScroller scroller;
    private Selection selection;


    private TablePresenter(Context context, final TableModel tableModel) {
        this.tableModel = tableModel;

        history = new HistoryManager();
        columnBuilder = new ColumnBuilder(context, this);
        cellBuilder = new CellBuilder(context, this);
        headerBuilder = new HeaderBuilder(context);
        lineBuilder = new LineBuilder(context);

        cellBuilder.init(tableModel);
        for (int i = 0; i < tableModel.getHeaders().size(); i++) {
            cellBuilder.updateHeightStroke(tableModel, i);
        }
        cellBuilder.updateHeightColumn(tableModel);
        cellBuilder.init(tableModel);

        tableModel.widthHeaders = (int) headerBuilder.widthHeaders;
        selection = new Selection(context, new SelectorListener() {
            @Override
            public void selectZone(float startX, float endX, float startY, float endY) {
                Coordinate selectZone = null;
                if (cellMode) {
                    selectZone = cellBuilder.selectCellsOfSelector(startX, endX, startY, endY, tableModel);
                    if (cellBuilder.getSelectCellSize() == 1) {
                        Cell selectCell = cellBuilder.getSelectCell(tableModel);
                        activityInterface.showEditCellWindow(tableModel.getColumns().get(selectCell.columnIndex).getInputType(), selectCell.text);
                    } else {
                        activityInterface.hideEditCellWindow();
                    }
                } else if (headerMode)
                    selectZone = headerBuilder.selectCellsOfSelector(startX, endX, startY, endY, tableModel);
                else if (columnMode) {
                    selectZone = columnBuilder.selectCellsOfSelector(startX, endX, startY, endY, tableModel);
                    int columnCount = columnBuilder.selectColumnCount;
                    // сначало показываем меню если выделелили первую (потом будет false)
                    activityInterface.showMenuOfColumns(false, columnCount);
                }
                selection.setSelectionCoordinate(selectZone);
            }

            @Override
            public void scrollToSelectorOffside(Coordinate coordinate) {
                scroller.scrollToCell(coordinate, 0, 0);
            }
        });
    }

    public static TablePresenter get(Context context, TableModel table) {
        if (tablePresenter == null)
            tablePresenter = new TablePresenter(context, table);
        return tablePresenter;
    }

    @Override
    public void click(float x, float y, Coordinate coordinate) {
        float startX = tableModel.widthHeaders + coordinate.startX;
        float startY = tableModel.heightColumns + coordinate.startY;
        if (x > startX && y > startY)
            cellClick(x, y);
        else if (x < startX && y > startY)
            headerClick(y);
        else if (y < startY && x > startX)
            columnClick(x);
    }

    @Override
    public void moveSelector(float x, float y, Coordinate coordinate) {

        if (cellMode)
            selection.moveSelector(x, y);
        else if (headerMode)
            selection.moveSelector(0, y);
        else if (columnMode)
            selection.moveSelector(x, 0);
        activityInterface.invalidate();
//        selection.scrollToMoveCoordinate(coordinate, tableModel.widthHeaders, tableModel.heightColumns);
    }

    @Override
    public int getTableWidth() {
        return tableModel.widthTable + tableModel.widthHeaders;
    }

    @Override
    public int getTableHeight() {
        return tableModel.heightTable + tableModel.heightColumns;
    }

    private void cellClick(float x, float y) {
        int stroke = -1;
        for (int i = 0; i < tableModel.getHeaders().size(); i++) {
            int strokeEnd = (int) tableModel.getHeaders().get(i).endY;
            if (strokeEnd > y - tableModel.heightColumns) {
                stroke = i;
                break;
            }
        }
        int column = -1;
        for (int i = 0; i < tableModel.getColumns().size(); i++) {
            int columnEnd = (int) tableModel.getColumns().get(i).endX;
            if (columnEnd > x - tableModel.widthHeaders) {
                column = i;
                break;
            }
        }

        if (stroke > tableModel.getHeaders().size() - 1
                || stroke < 0
                || column > tableModel.getColumns().size() - 1
                || column < 0)
            return;

        if (cellBuilder.selectCell(stroke, column, tableModel)) {
            // отменяем все другие выделения если имеются
            if (headerMode) {
                headerMode = false;
                headerBuilder.unSelectAll(tableModel);

            } else if (columnMode) {
                columnMode = false;
                columnBuilder.unSelectAll(tableModel);
            }
            if (!cellMode)
                activityInterface.showMenuOfCells();
            cellMode = true;
            //обновляем
            activityInterface.invalidate();

            Cell selectCell = cellBuilder.getSelectCell(tableModel);
            selection.setCoordinateForSelectCell(
                    selectCell.startX + tableModel.widthHeaders,
                    selectCell.endX + tableModel.widthHeaders,
                    selectCell.startY + tableModel.heightColumns,
                    selectCell.endY + tableModel.heightColumns
            );
            scroller.scrollToCell(selection.getCoordinateScroll(), tableModel.widthHeaders, tableModel.heightColumns);
            activityInterface.showEditCellWindow(tableModel.getColumns().get(selectCell.columnIndex).getInputType(), selectCell.text);
            // задаем первоначальные координаты
        } else { // если нажали на выделенную область
            if (cellMode) {

            } else if (headerMode) {

            } else if (columnMode) {

            }
            // show menu to pref_cells
//            activityInterface.showMenuCell(x, y, iterface);
        }
        /*
        при клике или добавлении строки, столба - изменяется высота ячеек
        при клике выделяются все, надо делать если нажато на не выделенный участок то убрать выделения перед выделением
        да и вообще работы еще много
         */
    }

    private void headerClick(float y) {
        int heightColumns = tableModel.heightColumns;
        int index = -1;
        for (int i = 0; i < tableModel.getHeaders().size(); i++) {
            float strokeEnd = tableModel.getHeaders().get(i).endY;
            float v = y - heightColumns;
            if (strokeEnd > v) {
                index = i;
                break;
            }
        }
        if (index >= tableModel.getHeaders().size() || index < 0)
            return;

        if (cellMode) {
            cellMode = false;
            cellBuilder.unSelectAll(tableModel);
            activityInterface.hideEditCellWindow();

        } else if (columnMode) {
            columnMode = false;
            columnBuilder.unSelectAll(tableModel);
        }
        if (headerBuilder.selectHeader(index, tableModel)) {

            activityInterface.invalidate();
            headerMode = true;

            Coordinate coordinate = headerBuilder.getSelectedCellCoordinate(tableModel);
            selection.setSelectionCoordinate(coordinate);

            scroller.scrollToStroke(headerBuilder.getSelectedCellCoordinate(tableModel));
            activityInterface.showMenuOfHeaders(headerMode); // до того как режим изменится мы ставим меню или нет, это важно для того что бы пвторно постоянно не ставить меню
            activityInterface.invalidate();
        } else {

        }

    }

    private void columnClick(float x) {
        // если нажали дальше чем есть колоны то ничего не происходит
        if (x > tableModel.getColumns().get(tableModel.getColumns().size() - 1).endX + tableModel.widthHeaders)
            return;

        if (cellMode) {
            cellMode = false;
            cellBuilder.unSelectAll(tableModel);
            activityInterface.hideEditCellWindow();
        } else if (headerMode) {
            headerMode = false;
            headerBuilder.unSelectAll(tableModel);
        }
        if (columnBuilder.selectColumn(x, tableModel)) {
            // последовательность важна
            int columnCount = columnBuilder.selectColumnCount;
            // сначало показываем меню если выделелили первую (потом будет false)
            activityInterface.showMenuOfColumns(!columnMode, columnCount);
            // проверяем если выделено хоть один столбец то режим столбцов true
            columnMode = true;
            // если оказалось что нажатие сняло выделение с единственно выделенного столбца то возвращаем стандартное меню
            activityInterface.invalidate();
            Coordinate coordinate = columnBuilder.getSelectedCellCoordinate(tableModel);
            selection.setSelectionCoordinate(coordinate);
            scroller.scrollToColumn(coordinate);
        }
    }

    public void setInterfaces(Context context) {
        activityInterface = (TableActivityInterface) context;
        scroller = (TableScroller) context;
    }

    @Override
    public void draw(Canvas canvas, Coordinate coordinate) {
        if (tableModel.getHeaders().size() > 0) {
            cellBuilder.draw(canvas, coordinate, tableModel);
            headerBuilder.draw(canvas, coordinate, tableModel);
            columnBuilder.draw(canvas, coordinate, tableModel);
            lineBuilder.draw(canvas, coordinate, tableModel);
            if (cellMode)
                selection.draw(canvas, coordinate, tableModel.widthHeaders, tableModel.heightColumns, SelectMode.cell);
            else if (headerMode)
                selection.draw(canvas, coordinate, tableModel.widthHeaders, tableModel.heightColumns, SelectMode.header);
            else if (columnMode)
                selection.draw(canvas, coordinate, tableModel.widthHeaders, tableModel.heightColumns, SelectMode.column);


        }
    }

    @Override
    public boolean touchCoordinate(float x, float y) {
        SelectMode selectMode = null;
        if (cellMode) selectMode = SelectMode.cell;
        else if (headerMode) selectMode = SelectMode.header;
        else if (columnMode) selectMode = SelectMode.column;

        if (cellMode || headerMode || columnMode)
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
        selection.moveCoordinateToSelect();
        if (cellMode) {
            if (cellBuilder.getSelectCellSize() == 0) {
                defaultState();
            }
        }
        activityInterface.invalidate();
    }

    @Override
    public void scrollBy(float distanceX, float distanceY) {
        activityInterface.scrollTableBy(distanceX, distanceY,
                tableModel.widthTable + tableModel.widthHeaders,
                tableModel.heightTable + tableModel.heightColumns);
    }

    @Override
    public void scrollTo(int x, int y) {
        activityInterface.scrollTableTo(x, y);
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
            if (columnMode) { // если мы в режиме выделения столбцов то ищем какой выделен последний
                addColumnIndex = endSelect + 1;
                copyPrefsColumn = endSelect;
            } else {
                copyPrefsColumn = addColumnIndex - 1; // если просто добавляем то последний столбец берется как настраиваемый по ширине
            }
        }

        // убираем выделения
        columnBuilder.unSelectAll(tableModel);

        // добавляем и возвращаем добавленный
        Column column = tableModel.addColumn(addColumnIndex, copyPrefsColumn);
        // нормализация высоты столбцов после инициализации
        cellBuilder.updateHeightColumn(tableModel);
        // инициализация
        columnBuilder.init(tableModel);
        //выделяем
        columnClick(column.endX + tableModel.widthHeaders);
//        скролим (по клику скролит)
//        scroller.scrollToColumn(column);
//
        // добавление в историю
        final AddColumn addColumn = history.addColumn(addColumnIndex, tableModel);
        addColumn.setOnHistoryListener(this);
    }

    // тут идет логика добавления строки, смотря куда добавить, вниз или вверх
    @Override
    public void addStroke(BottomMenuControl.AddButtonEnum addButtonEnum) {
        ArrayList<Header> headers = tableModel.getHeaders();

        int upSelect = 0;
        int downSelect = 0;
        // на конец если мы не в режиме выделения строк и не добавляем верх
        int addStrokeIndex = headers.size(); // от какой cтроки мы будем добавляться верхнего или самого нижнего

        // узнаем первую и последнюю выделенную ячейку
        boolean start = true;
        for (int i = 0; i < headers.size(); i++) {
            Header header = headers.get(i);
            if (header.isTouch) { // не забываем что у строк используется isTouch
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
            if (headerMode) { // если мы в режиме выделения столбцов то ищем какой выделен последний
                // это нужно для того что бы добавлять строку после выделенной ячейки а не на ее место
                addStrokeIndex = downSelect + 1;
                copyPrefsStrokeIndex = downSelect;
            }
        }
        if (headerMode)
            headerBuilder.unSelectAll(tableModel);

        tableModel.addNewStroke(addStrokeIndex, copyPrefsStrokeIndex);
        // инициализация всего
        cellBuilder.updateHeightStroke(tableModel, addStrokeIndex);
        headerBuilder.init(tableModel);
        // выделить строку
        headerClick(headers.get(addStrokeIndex).startY + 1 + tableModel.heightColumns);
//        scroller.scrollToStroke(headers.get(addStrokeIndex));

        // добавление в историю
        final AddStroke addStrokeHistory = history.addStroke(addStrokeIndex, tableModel);
        addStrokeHistory.setOnHistoryListener(this);
    }

    @Override
    public void setWidth(View view) { // view is'nt by here
        final ArrayList<Integer> selectIndex = new ArrayList<>();
        if (columnMode) {
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
        UpdateWidth updateWidth = new UpdateWidth() {

            @Override // тут задается смещение на которое сместился ползунок
            public void setWidthCell(int dx) {
                ArrayList<Column> columnsPref = tableModel.getColumns();
                for (int i = 0; i < selectIndex.size(); i++) {
                    Column column = columnsPref.get(selectIndex.get(i));
                    int width = (int) (column.width + dx);
                    int paddingSize = column.rightPadding + column.leftPadding;
                    if (width < paddingSize) width = paddingSize + 2;
                    column.width = width;
                }
                update();
            }

            private void update() {
                cellBuilder.init(tableModel);
                resume();
                activityInterface.invalidate();
                scroller.scrollToEndIfOutside(tableModel.widthTable, tableModel.heightTable);
            }

            @Override // кнопка для того чтобы настроить ширину всех столбцов под экран
            public void setWidthForScreen(int widthTableView) {
                // длина таблицы без хеадеров
                int tableWidth = widthTableView - tableModel.widthHeaders;
                int width = tableModel.widthTable;
                // если ширина таблицы больше чем экран
                while (width > tableWidth) {
                    width = 0;
                    // проходимся по каждой колоне и минисуем на 1 пока ширина таблицы не получится меньше чем экран
                    for (Column column : tableModel.getColumns()) {
                        int newWidth = (int) (column.width - 1);
                        int widthColumn = newWidth;
                        int paddingSize = column.rightPadding + column.leftPadding;
                        if (widthColumn < paddingSize) widthColumn = paddingSize + 2;

                        column.width = widthColumn;
                        width += newWidth;
                    }
                }
                // если ширина таблицы меньше чем экран
                // +1 для сдвига на 1 пиксель в лево от правой части экрана
                while (width + 1 < tableWidth) {
                    // проходим по каждому столбцу плюсуя на 1 попутно проверяя после каждого измененного столбца
                    for (Column column : tableModel.getColumns()) {
                        int widthColumn = (int) (column.width + 1);
                        int paddingSize = column.rightPadding + column.leftPadding;
                        if (widthColumn < paddingSize) widthColumn = paddingSize + 2;

                        column.width = widthColumn;
                        width++;
                        if (width == tableWidth) break;
                    }
                }
                stopTracking();
            }

            // этот метод служит для того что бы всегда получать усредненную ширину всех столбцов
            @Override
            public int getGeneralWidth() {
                int width = 0;
                if (columnMode) {
                    for (int i = 0; i < selectIndex.size(); i++) {
                        Column column = tableModel.getColumns().get(selectIndex.get(i));
                        width += column.width;
                    }
                    width /= selectIndex.size();
                } else {

                    for (Column col : tableModel.getColumns()) {
                        width += col.width;
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
                for (int i = 0; i < tableModel.getHeaders().size(); i++) {
                    cellBuilder.updateHeightStroke(tableModel, i);
                }
                cellBuilder.updateHeightColumn(tableModel);
            }
        };
        activityInterface.showEditWidthCellsWin(view, updateWidth);
    }

    @Override
    public void setHeightCells(View view) {
        final ArrayList<Integer> selectIndex = new ArrayList<>();
        if (headerMode) {
            for (int i = 0; i < tableModel.getHeaders().size(); i++) {
                if (tableModel.getHeaders().get(i).isTouch)
                    selectIndex.add(i);
            }
        } else {
            for (int i = 0; i < tableModel.getHeaders().size(); i++) {
                selectIndex.add(i);
            }
        }
        int genHeight = 0;
        for (int index :
                selectIndex) {
            genHeight += tableModel.getHeaders().get(index).height;
        }
        genHeight = genHeight / selectIndex.size();


        ArrayList<Header> headerPrefs = tableModel.getHeaders();
        int[] oldHeight = new int[headerPrefs.size()];
        for (int i = 0; i < headerPrefs.size(); i++) {
            Header header = headerPrefs.get(i);
            oldHeight[i] = (int) header.height;
        }

        UpdateHeight updateHeight = new UpdateHeight() {
            @Override
            public void setHeight(int height) {

                for (int index :
                        selectIndex) {
                    Header headerPref = tableModel.getHeaders().get(index);
                    int paddingSize = headerPref.downPadding + headerPref.upPadding;
                    if (height < paddingSize) height = paddingSize + 2;
                    headerPref.height = height;
                    headerPref.heightStroke = height;
                }
                // просто рисуем какую высоту мы задали
                update();
            }

            private void update() {
                cellBuilder.init(tableModel);
                resume();
                activityInterface.invalidate();
                scroller.scrollToEndIfOutside(tableModel.widthTable, tableModel.heightTable);
            }

            @Override
            public void onDismiss() {

                int[] newHeight = new int[headerPrefs.size()];
                for (int i = 0; i < headerPrefs.size(); i++) {
                    Header header = headerPrefs.get(i);
                    newHeight[i] = (int) header.height;
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
//                    Header headerPref = tableModel.getHeaders().get(index);
//                    headerPref.height = headerPref.heightStroke;
                    cellBuilder.updateHeightStroke(tableModel, index);
                }
                update();
            }
        };
        activityInterface.showEditHeightCellsWin(view, genHeight, updateHeight);
    }

    @Override
    public void settingTable() {
        SettingTableListener setting = new SettingTableListener(tableModel) {
            @Override
            public void setLockHeight(boolean lockHeight) {
                tableModel.setLockHeightCells(lockHeight);

                cellBuilder.init(tableModel);
                selection.updateCoordinateDraw(cellBuilder.getSelectedCellCoordinate(tableModel));
                activityInterface.invalidate();
                scroller.scrollToEndIfOutside(tableModel.widthTable, tableModel.heightTable);
            }

            @Override
            public void setDateVariable(int variable) {
                tableModel.setDateType(variable);
                for (int i = 0; i < tableModel.getHeaders().size(); i++) {
                    cellBuilder.updateHeightStroke(tableModel, i);
                }
                cellBuilder.init(tableModel);
                activityInterface.invalidate();
            }
        };
        activityInterface.showSettingTableWin(setting);
    }

    public void updateTable(Context context) {
        TableLab.get(context).saveTable(tableModel);
    }

    public void setText(String s) {

        if (cellMode) {
            Cell cell = cellBuilder.getSelectCell(tableModel);
            if (!s.equals(cell.text)) {
                cell.text = s;
                int oldHeight = (int) cell.height;
                cellBuilder.updateHeightStroke(tableModel, cell.strokeIndex);
                cellBuilder.init(tableModel);
                selection.updateCoordinateDraw(cellBuilder.getSelectedCellCoordinate(tableModel));
                if (oldHeight != cell.height
                        || s.length() > 0 && s.charAt(s.length() - 1) == '\n')
                    scroller.scrollToCell(selection.getCoordinateScroll(), tableModel.widthHeaders, tableModel.heightColumns);

            }
        } else if (columnMode) {

            Column renameColumn = columnBuilder.getSelectColumn(tableModel);
            if (!s.equals(renameColumn.text)) {
                renameColumn.text = s;
                // обновляем высоту у колумнвью
                cellBuilder.updateHeightColumn(tableModel);
                //надо обновить чтоб высота была что нужно
                columnBuilder.init(tableModel);
            }
        }

        activityInterface.invalidate();
    }

    public void openKeyboardEvent(boolean isOpen) {
//        if (cellMode)
        if (isOpen) {
            if (cellMode)
                scroller.scrollToCell(selection.getCoordinateScroll(), tableModel.widthHeaders, tableModel.heightColumns);
        } else
            scroller.scrollToEndIfOutside(tableModel.widthTable + tableModel.widthHeaders,
                    tableModel.heightTable + tableModel.heightColumns);
    }

    public void destroyed(Context context) {
        TableLab.get(context).deleteCache();
        tablePresenter = null;
    }

    public void resume() {
        Coordinate coordinate = null;
        if (cellMode) {
            coordinate = cellBuilder.getSelectedCellCoordinate(tableModel);
        } else if (headerMode) {
            coordinate = headerBuilder.getSelectedCellCoordinate(tableModel);
        } else if (columnMode) {
            coordinate = columnBuilder.getSelectedCellCoordinate(tableModel);
        }
        if (coordinate != null) {
            selection.setSelectionCoordinate(coordinate);
        }
        activityInterface.invalidate();
    }

    public boolean defaultState() {
        boolean toReturn = true;

        if (cellMode) {
            cellMode = false;
            cellBuilder.unSelectAll(tableModel);
            activityInterface.hideEditCellWindow();
            scroller.scrollToEndIfOutside(tableModel.widthTable + tableModel.widthHeaders, tableModel.heightTable + tableModel.heightColumns);
        } else if (headerMode) {
            headerMode = false;
            headerBuilder.unSelectAll(tableModel);

        } else if (columnMode) {
            columnMode = false;
            columnBuilder.unSelectAll(tableModel);
            activityInterface.hideEditCellWindow();

        } else {
            toReturn = false;
        }

        if (toReturn)
            activityInterface.showDefaultMenu();
        activityInterface.invalidate();
        return toReturn;
    }

    public void getColumnRenameWin() {
        Column column = columnBuilder.getSelectColumn(tableModel);
        if (column != null)
            activityInterface.showRenameColumnWin(column);
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
        for (int i = 0; i < tableModel.getHeaders().size(); i++) {
            if (tableModel.getHeaders().get(i).isTouch)
                indexHeaderPrefs.add(i);
        }
        for (int i = 0; i < tableModel.getHeaders().size(); i++) {
            for (int j = 0; j < tableModel.getColumns().size(); j++) {
                Cell cell = tableModel.getHeaders().get(i).getCell(j);
                if (cell.isTouch) {
                    int[] index = new int[2];
                    index[0] = i;
                    index[1] = j;
                    indexCells.add(index);

                }
            }
        }

//
        final EditPrefs editColumn = new EditPrefs(indexColumns, indexHeaderPrefs, indexCells);
        editColumn.setOnHistoryListener(this);
        editColumn.setOldPrefs(tableModel);
//
        PrefCellsListener prefCellsListener = new PrefCellsListener() {

            private void update() {
                Coordinate coordinate = null;
                if (cellMode) {
                    int index = -1;
                    for (int i = 0; i < indexCells.size(); i++) {
                        int in = indexCells.get(i)[0];
                        //типа опимизация
                        if (in != index)
                            cellBuilder.updateHeightStroke(tableModel, in);
                        index = in;
                    }
                    coordinate = cellBuilder.getSelectedCellCoordinate(tableModel);
                } else if (headerMode) {
                    for (int i = 0; i < indexHeaderPrefs.size(); i++) {
                        cellBuilder.updateHeightStroke(tableModel, i);
                    }
                    coordinate = headerBuilder.getSelectedCellCoordinate(tableModel);
                } else if (columnMode) {
                    for (int i = 0; i < tableModel.getHeaders().size(); i++) {
                        cellBuilder.updateHeightStroke(tableModel, i);
                    }
                    cellBuilder.updateHeightColumn(tableModel);
                    coordinate = columnBuilder.getSelectedCellCoordinate(tableModel);
                }
                cellBuilder.init(tableModel);
                if (coordinate != null)
                    selection.updateCoordinateDraw(coordinate);
                activityInterface.invalidate();
            }

            @Override
            public void setType(int type) {
                if (!columnMode)
                    return;
                this.type = type;
                for (int i = 0; i < indexColumns.size(); i++) {
                    Column column = tableModel.getColumns().get(indexColumns.get(i));
                    column.setInputType(type);
                }
                update();
            }

            @Override
            public void setFunction(String function) {
                if (!columnMode)
                    return;
                this.function = function;
                for (int i = 0; i < indexColumns.size(); i++) {
                    Column column = tableModel.getColumns().get(indexColumns.get(i));
                    column.setFunction(function);
                }
                update();
            }

            @Override
            public void setSizeText(int size) {
                this.sizeText = size;
                for (int i = 0; i < indexColumns.size(); i++) {
                    Column column = tableModel.getColumns().get(indexColumns.get(i));
                    column.sizeFont = size;
                }
                for (int index : indexHeaderPrefs) {
                    Header header = tableModel.getHeaders().get(index);
                    header.sizeFont = size;
                }
                if (isCellsEdit)
                    for (int[] index : indexCells) {
                        Cell cell = tableModel.getHeaders().get(index[0]).getCell(index[1]);
                        cell.sizeFont = size;
                    }
                update();
            }

            @Override
            public void setBold(int bold) {
                this.bold = bold;
                for (int i = 0; i < indexColumns.size(); i++) {
                    Column column = tableModel.getColumns().get(indexColumns.get(i));
                    column.bold = bold;
                }
                for (int index : indexHeaderPrefs) {
                    Header header = tableModel.getHeaders().get(index);
                    header.bold = bold;
                }
                if (isCellsEdit)
                    for (int[] index : indexCells) {
                        Cell cell = tableModel.getHeaders().get(index[0]).getCell(index[1]);
                        cell.bold = bold;
                    }
                update();
            }

            @Override
            public void setItalic(int italic) {
                this.italic = italic;
                for (int i = 0; i < indexColumns.size(); i++) {
                    Column column = tableModel.getColumns().get(indexColumns.get(i));
                    column.italic = italic;
                }
                for (int index : indexHeaderPrefs) {
                    Header header = tableModel.getHeaders().get(index);
                    header.italic = italic;
                }
                if (isCellsEdit)
                    for (int[] index : indexCells) {
                        Cell cell = tableModel.getHeaders().get(index[0]).getCell(index[1]);
                        cell.italic = italic;
                    }
                update();
//                activityInterface.updateColumnHeight(columnBuilder.heightColumns);
            }

            @Override
            public void setColorText(int color) {
                this.colorText = color;
                for (int i = 0; i < indexColumns.size(); i++) {
                    Column column = tableModel.getColumns().get(indexColumns.get(i));
                    column.colorFont = colorText;
                }
                for (int index : indexHeaderPrefs) {
                    Header header = tableModel.getHeaders().get(index);
                    header.colorFont = colorText;
                }
                if (isCellsEdit)
                    for (int[] index : indexCells) {
                        Cell cell = tableModel.getHeaders().get(index[0]).getCell(index[1]);
                        cell.colorFont = colorText;
                    }
                update();
            }

            @Override
            public void setColorBack(int color) {
                this.colorBack = color;
                for (int i = 0; i < indexColumns.size(); i++) {
                    Column column = tableModel.getColumns().get(indexColumns.get(i));
                    column.colorBack = colorBack;
                }
                for (int index : indexHeaderPrefs) {
                    Header header = tableModel.getHeaders().get(index);
                    header.colorBack = colorBack;
                }
                if (isCellsEdit)
                    for (int[] index : indexCells) {
                        Cell cell = tableModel.getHeaders().get(index[0]).getCell(index[1]);
                        cell.colorBack = colorBack;
                    }
                update();
            }

            @Override
            public void closeWindow() {
                editColumn.setNewColumns(tableModel);
                history.editColumns(editColumn);
            }
        };
        prefCellsListener.isCellsEditOnly = cellMode;
        prefCellsListener.isHeaderEdit = headerMode;

        // тут нужно для опредления какого типа данные для настройки отправляются в окно
        CellAbstract cellAbstract;
        if (columnMode) {
            cellAbstract = tableModel.getColumns().get(indexColumns.get(0));
            prefCellsListener.type = ((Column) cellAbstract).getInputType();
            prefCellsListener.function = ((Column) cellAbstract).getFunction();
        } else if (headerMode)
            cellAbstract = tableModel.getHeaders().get(indexHeaderPrefs.get(0));
        else
            cellAbstract = tableModel.getHeaders().get(indexCells.get(0)[0]).getCell(indexCells.get(0)[1]);

        prefCellsListener.sizeText = (int) cellAbstract.sizeFont;
        prefCellsListener.bold = cellAbstract.bold;
        prefCellsListener.italic = cellAbstract.italic;
        prefCellsListener.colorText = cellAbstract.colorFont;
        prefCellsListener.colorBack = cellAbstract.colorBack;

        activityInterface.showSettingCellWin(prefCellsListener);
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
        columnBuilder.deleteSelectedColumn(tableModel);
        cellBuilder.updateHeightColumn(tableModel);
        columnBuilder.init(tableModel);
        defaultState();
        scroller.scrollToEndIfOutside(tableModel.widthTable, tableModel.heightTable);

        return true;

    }

    public boolean deleteStroke() {
        // возвращает фалс если выделены все ячейки
        if (headerBuilder.selectHeaderCount >= tableModel.getHeaders().size())
            return false;

        //добавление в историю
        final List<Integer> indexes = new ArrayList<>();
        for (int i = 0; i < tableModel.getHeaders().size(); i++) {
            Header stroke = tableModel.getHeaders().get(i);
            if (stroke.isTouch) {
                indexes.add(i);
            }
        }
        final DeleteStroke deleteStroke = history.deleteStroke(ArrayUtils.toPrimitiveArray(indexes), tableModel);
        deleteStroke.setOnHistoryListener(this);

        headerBuilder.deleteHeaders(tableModel);
        headerBuilder.init(tableModel);

        defaultState();
        scroller.scrollToEndIfOutside(tableModel.widthTable, tableModel.heightTable);
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
        activityInterface.showDatePickerWin(cell.date);
    }

    public void setDate(long date) {
        Cell selectCell = cellBuilder.getSelectCell(tableModel);
        selectCell.date = date;
        cellBuilder.updateHeightStroke(tableModel, selectCell.strokeIndex);
        cellBuilder.init(tableModel); // тут это обязательно там логика для отображения даты
        selection.updateCoordinateDraw(cellBuilder.getSelectedCellCoordinate(tableModel));
        activityInterface.showEditCellWindow(tableModel.getColumns().get(selectCell.columnIndex).getInputType(), selectCell.text);
        activityInterface.invalidate();
    }

    public void clearText() {
        cellBuilder.clearText(tableModel);
        Cell selectCell = cellBuilder.getSelectCell(tableModel);
        cellBuilder.updateHeightStroke(tableModel, selectCell.strokeIndex);
        cellBuilder.init(tableModel);
        selection.updateCoordinateDraw(cellBuilder.getSelectedCellCoordinate(tableModel));
        activityInterface.showEditCellWindow(tableModel.getColumns().get(selectCell.columnIndex).getInputType(), selectCell.text);
        activityInterface.invalidate();

    }

    @Override
    public void undo(Command command) {
        initAndUpdate();

//        if (command instanceof AddColumn || command instanceof AddStroke)
//            scrollToEndIfOutside();
        if (command instanceof DeleteColumn) {
            DeleteColumn deleteColumn = (DeleteColumn) command;
            for (int i = 0; i < deleteColumn.index.length; i++) {
                int index = deleteColumn.index[i];
                Column column = tableModel.getColumns().get(index);
                column.select(tableModel);
            }
            columnMode = true;
            Coordinate coordinate = columnBuilder.getSelectedCellCoordinate(tableModel);
            selection.setSelectionCoordinate(coordinate);
        } else if (command instanceof DeleteStroke) {
            DeleteStroke deleteStroke = (DeleteStroke) command;
            for (int i = 0; i < deleteStroke.index.length; i++) {
                int index = deleteStroke.index[i];
                Header header = tableModel.getHeaders().get(index);
                header.select();
            }
            headerMode = true;
            Coordinate coordinate = headerBuilder.getSelectedCellCoordinate(tableModel);
            selection.setSelectionCoordinate(coordinate);

        } else if (command instanceof EditCell) {
            EditCell editCell1 = (EditCell) command;
            int[] index = editCell1.index;
            int x = (int) (tableModel.getHeaders().get(index[0]).getCell(index[1]).startX + 1);
            int y = (int) (tableModel.getHeaders().get(index[0]).getCell(index[1]).startY + 1);
            cellClick(x + tableModel.widthHeaders, y + tableModel.heightColumns);
        } else if (command instanceof RenameColumn) {
            int index = ((RenameColumn) command).index;
            float x = tableModel.getColumns().get(index).startX + 1;
            columnClick(x + tableModel.widthHeaders);
        }
    }

    @Override
    public void redo(Command command) { // setWidth + setHeight
        initAndUpdate();

        if (command instanceof AddColumn) {                   //выделяем столбец который был возвращен
            AddColumn addColumn = (AddColumn) command;
            float x = tableModel.getColumns().get(addColumn.index).startX + 1;
            columnClick(x + tableModel.widthHeaders);
        } else if (command instanceof AddStroke) {
            AddStroke addStroke = (AddStroke) command;
            float y = tableModel.getHeaders().get(addStroke.index).startY + 1;
            headerClick(y + tableModel.heightColumns);
        } else if (command instanceof EditCell) {
            EditCell editCell1 = (EditCell) command;
            int[] index = editCell1.index;
            int x = (int) (tableModel.getHeaders().get(index[0]).getCell(index[1]).startX + 1);
            int y = (int) (tableModel.getHeaders().get(index[0]).getCell(index[1]).startY + 1);
            cellClick(x + tableModel.widthHeaders, y + tableModel.heightColumns);
        } else if (command instanceof RenameColumn) {
            int index = ((RenameColumn) command).index;
            float x = tableModel.getColumns().get(index).startX + 1;
            columnClick(x + tableModel.widthHeaders);
        }


    }

    private void initAndUpdate() {
        defaultState();
        cellBuilder.updateHeightColumn(tableModel);
        for (int i = 0; i < tableModel.getHeaders().size(); i++) {
            cellBuilder.updateHeightStroke(tableModel, i);
        }
        cellBuilder.init(tableModel);
        activityInterface.invalidate();
        scroller.scrollToEndIfOutside(tableModel.widthTable, tableModel.heightTable);
    }
    /*

     */


    public enum SelectMode {
        cell,
        header,
        column
    }
}
