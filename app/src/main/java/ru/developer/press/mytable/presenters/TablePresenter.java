package ru.developer.press.mytable.presenters;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.view.View;


import com.google.android.gms.common.util.ArrayUtils;

import java.util.ArrayList;
import java.util.List;

import ru.developer.press.mytable.StaticMetods;
import ru.developer.press.mytable.history.HistoryManager;
import ru.developer.press.mytable.history.comands.AddColumn;
import ru.developer.press.mytable.history.comands.AddStroke;
import ru.developer.press.mytable.history.comands.DeleteColumn;
import ru.developer.press.mytable.history.comands.DeleteStroke;
import ru.developer.press.mytable.history.comands.EditCell;
import ru.developer.press.mytable.history.comands.EditColumn;
import ru.developer.press.mytable.history.comands.HeightStroke;
import ru.developer.press.mytable.history.comands.RenameColumn;
import ru.developer.press.mytable.history.comands.WidthColumn;
import ru.developer.press.mytable.interfaces.SettColumnsListener;
import ru.developer.press.mytable.interfaces.UpdateHeight;
import ru.developer.press.mytable.interfaces.UpdateWidth;
import ru.developer.press.mytable.interfaces.CellEditListener;
import ru.developer.press.mytable.interfaces.HistoryUpdateListener;
import ru.developer.press.mytable.interfaces.RenameColumnListener;
import ru.developer.press.mytable.interfaces.SettingTableListener;
import ru.developer.press.mytable.model.CellAbstract;
import ru.developer.press.mytable.views.ControllerBottomMenu;
import ru.developer.press.mytable.interfaces.BottomMenuClick;
import ru.developer.press.mytable.interfaces.ColumnViewListener;
import ru.developer.press.mytable.interfaces.HeaderViewListener;
import ru.developer.press.mytable.interfaces.ScrollerTo;
import ru.developer.press.mytable.interfaces.TableViewListener;
import ru.developer.press.mytable.interfaces.TableActivityInterface;
import ru.developer.press.mytable.model.Cell;
import ru.developer.press.mytable.model.ColumnPref;
import ru.developer.press.mytable.model.TableLab;
import ru.developer.press.mytable.model.TableModel;
import ru.developer.press.mytable.table.builders.CellBuilder;
import ru.developer.press.mytable.table.builders.ColumnBuilder;
import ru.developer.press.mytable.table.builders.HeaderBuilder;

import static ru.developer.press.mytable.views.ControllerBottomMenu.AddButtonEnum.ADD_LEFT_COLUMN;
import static ru.developer.press.mytable.views.ControllerBottomMenu.AddButtonEnum.ADD_UP_STROKE;

public class TablePresenter implements TableViewListener, HeaderViewListener, ColumnViewListener, BottomMenuClick, CellEditListener, RenameColumnListener {

    private static TablePresenter tablePresenter;
    private final TableModel tableModel;
    private HistoryManager history;

    private TableActivityInterface activityInterface;
    private CellBuilder cellBuilder;
    private HeaderBuilder headerBuilder;

    private boolean cellMode = false;
    private boolean headerMode = false;
    private boolean columnMode = false;
    private ColumnBuilder columnBuilder;
    private ScrollerTo scrollerTo;
//    private Bitmap mapTemp;



    private TablePresenter(Context context, final TableModel tableModel) {
        this.tableModel = tableModel;
        history = new HistoryManager();
        columnBuilder = new ColumnBuilder(context, this);
        cellBuilder = new CellBuilder(context, this);
        headerBuilder = new HeaderBuilder(context);

        columnBuilder.init(tableModel);
        cellBuilder.init(tableModel);
        headerBuilder.init(tableModel);

//        mapTemp = StaticMetods.getScreenTable(tableModel, context);
    }

    public static TablePresenter get(Context context, String nameId) {
        if (tablePresenter == null)
            tablePresenter = new TablePresenter(context,
                    TableLab.get(context).getTableForNameId(nameId));
        return tablePresenter;
    }

    @Override
    public void cellClick(float x, float y) {
        cellBuilder.unSelectCell(tableModel);
        if (cellBuilder.selectCell(x, y, tableModel)) {
            // отменяем все другие выделения если имеются
            {
                if (headerMode) {
                    headerMode = false;
                    headerBuilder.unSelectAll(tableModel);
                    activityInterface.showDefaultMenu();

                } else if (columnMode) {
                    columnMode = false;
                    columnBuilder.unSelectAll(tableModel);
                    activityInterface.showDefaultMenu();
                }
            }
            //обновляем
            activityInterface.invalidate();

            Cell selectCell = cellBuilder.getSelectCell(tableModel);
            activityInterface.showEditCellWindow(selectCell.type, selectCell.text);
//            activityInterface.setText(selectCell.text);

            scrollerTo.scrollToCell(selectCell);
            cellMode = true;
        } else defaultState();
    }

    @Override
    public void headerClick(float y) {
        int index = -1;
        for (int i = 0; i < tableModel.getHeaders().size(); i++) {
            int strokeEnd = (int) tableModel.getEntries().get(i).get(0).endY;
            if (strokeEnd > y) {
                index = i;
                break;
            }
        }
        if (index >= tableModel.getHeaders().size() || index < 0)
            return;

        {
            if (cellMode) {
                cellMode = false;
                cellBuilder.unSelectCell(tableModel);
                activityInterface.hideEditCellWindow();
            } else if (columnMode) {
                columnMode = false;
                columnBuilder.unSelectAll(tableModel);
            }
        }
        // если не было выделено то поставь меню для строк
        activityInterface.showMenuOfHeaders(!headerMode); // до того как режим изменится мы ставим меню или нет, это важно для того что бы пвторно постоянно не ставить меню
        // выделение строки
        headerBuilder.selectHeader(index, tableModel);
        // если выделена хоть одна строка то режим строк активен
        headerMode = headerBuilder.selectHeaderCount > 0;

        // если оказалось, что путем выделения мы сняли все выделения то стандартное меню
        if (!headerMode) { // после изменения режима (headerMode) смотрим не сняли ли мы все выделения
            activityInterface.showDefaultMenu();
        }
        activityInterface.invalidate();

        scrollerTo.scrollToStroke(tableModel.getHeaders().get(index));

    }

    @Override
    public void columnClick(float x) {
        // если нажали дальше чем есть колоны то ничего не происходит
        if (x > tableModel.getColumnsPref().get(tableModel.getColumnsPref().size() - 1).endX)
            return;

        // вычесляем индекс
        int index = 0;
        for (int i = 0; i < tableModel.getColumnsPref().size(); i++) {
            ColumnPref column = tableModel.getColumnsPref().get(i);
            if (x <= column.endX) {
                index = i;
                break;
            }
        }
        if (cellMode) {
            cellMode = false;
            cellBuilder.unSelectCell(tableModel);
            activityInterface.hideEditCellWindow();

        } else if (headerMode) {
            headerMode = false;
            headerBuilder.unSelectAll(tableModel);

        }
        // выделяем ну или снимаем смотря было выделено или нет
        columnBuilder.selectColumn(index, tableModel);
        // последовательность важна
        int columnCount = columnBuilder.selectColumnCount;
        // сначало показываем меню если выделелили первую (потом будет false)
        activityInterface.showMenuOfColumns(!columnMode, columnCount);
        // проверяем если выделено хоть один столбец то режим столбцов true
        columnMode = columnCount > 0;
        // если оказалось что нажатие сняло выделение с единственно выделенного столбца то возвращаем стандартное меню
        if (!columnMode)
            activityInterface.showDefaultMenu();

        activityInterface.invalidate();
        scrollerTo.scrollToColumn(tableModel.getColumnsPref().get(index));
    }

    public void setInterfaces(Context context) {
        activityInterface = (TableActivityInterface) context;
        scrollerTo = (ScrollerTo) context;
    }

    @Override
    public void drawCells(Canvas canvas, CellAbstract coordinate) {
//        canvas.drawBitmap(mapTemp, 0, 0, new Paint());
        cellBuilder.drawCells(canvas, coordinate, tableModel);
    }

    @Override
    public void drawColumns(Canvas canvas, CellAbstract coordinateDraw) {
        columnBuilder.drawColumns(canvas, coordinateDraw, tableModel);
    }

    @Override
    public void drawHeaders(Canvas canvas, CellAbstract coordinateDraw) {
        headerBuilder.drawHeaders(canvas, coordinateDraw, tableModel);
    }

    @Override
    public int getTableWidth() {
        return cellBuilder.getWidth();
    }

    @Override
    public int getTableHeight() {
        return cellBuilder.getHeight();
    }


    @Override
    public void scrollBy(int x, int y) {
        activityInterface.scrollTableBy(x, y);
    }

    @Override
    public void scrollTo(int x, int y) {
        activityInterface.scrollTableTo(x, y);
    }

    @Override
    public void addColumn(ControllerBottomMenu.AddButtonEnum addButtonEnum) {
        // новая колона
        ColumnPref columnPref = new ColumnPref();
        int startSelect = 0;
        int endSelect = 0;
        // на конец если мы не в режиме выделения стоблцов и не добавляем в левую сторону
        int addColumnIndex = tableModel.getColumnsPref().size(); // от какого заголовка мы будем добавляться самомго левого или самого правого (по умолчанию последним)
        int selectedColumn;

        // узнаем первую и последнюю выделенную ячейку
        boolean start = true;
        for (int i = 0; i < tableModel.getColumnsPref().size(); i++) {
            ColumnPref columnPrefSelect = tableModel.getColumnsPref().get(i);
            if (columnPrefSelect.isTouched) {
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
            selectedColumn = startSelect;
        } else {
            if (columnMode) { // если мы в режиме выделения столбцов то ищем какой выделен последний
                addColumnIndex = endSelect + 1;
                selectedColumn = endSelect;
            } else {
                selectedColumn = addColumnIndex - 1; // если просто добавляем то последний столбец берется как настраиваемый по ширине
            }
        }

        // соблюдать порядок

        // делаем такую же ширину как и у рядом стоящего столбца
        columnPref.setWidthColumn(tableModel.getColumnsPref().get(selectedColumn).getWidthColumn());
        // убираем выделения
        columnBuilder.unSelectAll(tableModel);
        // добавляем ячейки в столб
        for (int i = 0; i < tableModel.getEntries().size(); i++) {
            Cell cell = new Cell();
            cell.isTouchedStrCol = true;
            tableModel.getEntries().get(i).add(addColumnIndex, cell);
        }

        // добавляем
        tableModel.addColumn(addColumnIndex, columnPref);
        // инициализация
        columnBuilder.init(tableModel);
        cellBuilder.init(tableModel);
        // нормализация высоты столбцов после инициализации
        activityInterface.updateColumnHeight(columnBuilder.heightColumns);
        //выделяем
        columnClick(columnPref.endX);
//        скролим (по клику скролит)
//        scrollerTo.scrollToColumn(columnPref);
//
        // добавление в историю
        final AddColumn addColumn = history.addColumn(addColumnIndex, tableModel);
        addColumn.setOnHistoryListener(new HistoryUpdateListener() {
            @Override
            public void undo(Object argument) {
                init();
                scrollerTo.scrollToEndIfOutside(cellBuilder.getWidth(), cellBuilder.getHeight());
            }

            private void init() {
                columnBuilder.init(tableModel);
                cellBuilder.init(tableModel);
                activityInterface.updateColumnHeight(columnBuilder.heightColumns);
                columnBuilder.selectColumnCount = 0;
            }

            @Override
            public void redo(Object argument) {
                init();
                columnClick(tableModel.getColumnsPref().get(addColumn.index).startX + 1);
            }
        });
    }

    // тут идет логика добавления строки, смотря куда добавить, вниз или вверх
    @Override
    public void addStroke(ControllerBottomMenu.AddButtonEnum addButtonEnum) {
        ArrayList<Cell> headers = tableModel.getHeaders();

        int upSelect = 0;
        int downSelect = 0;
        // на конец если мы не в режиме выделения строк и не добавляем верх
        int addStrokeIndex = headers.size(); // от какой cтроки мы будем добавляться верхнего или самого нижнего

        // узнаем первую и последнюю выделенную ячейку
        boolean start = true;
        for (int i = 0; i < headers.size(); i++) {
            Cell cell = headers.get(i);
            if (cell.isTouched) { // не забываем что у строк используется isTouched
                if (start) {
                    upSelect = i;
                    start = false;
                }
                downSelect = i;
            }
        }

        if (addButtonEnum == ADD_UP_STROKE) {
            addStrokeIndex = upSelect;
        } else {
            if (headerMode) { // если мы в режиме выделения столбцов то ищем какой выделен последний
                // это нужно для того что бы добавлять строку после выделенной ячейки а не на ее место
                addStrokeIndex = downSelect + 1;
            }
        }
        if (headerMode)
            headerBuilder.unSelectAll(tableModel);
        tableModel.addStroke(addStrokeIndex);
        // инициализация всего
        cellBuilder.init(tableModel);
        headerBuilder.init(tableModel);
        // выделить строку
        headerClick(headers.get(addStrokeIndex).startY + 1);
//        scrollerTo.scrollToStroke(headers.get(addStrokeIndex));

        // добавление в историю
        final AddStroke addStrokeHistory = history.addStroke(addStrokeIndex, tableModel);
        addStrokeHistory.setOnHistoryListener(new HistoryUpdateListener() {
            @Override
            public void undo(Object argument) {
                init();
                scrollerTo.scrollToEndIfOutside(cellBuilder.getWidth(), cellBuilder.getHeight());
            }

            private void init() {
                headerBuilder.init(tableModel);
                cellBuilder.init(tableModel);
                headerBuilder.selectHeaderCount = 0;
            }

            @Override
            public void redo(Object argument) {
                // порядок важен
                init();
                headerClick(tableModel.getHeaders().get(addStrokeHistory.index).startY + 1);
            }
        });
    }

    @Override
    public void setWidth(View view) {
        final ArrayList<Integer> selectIndex = new ArrayList<>();
        if (columnMode) {
            for (int i = 0; i < tableModel.getColumnsPref().size(); i++) {
                if (tableModel.getColumnsPref().get(i).isTouched)
                    selectIndex.add(i);
            }
        } else {
            for (int i = 0; i < tableModel.getColumnsPref().size(); i++) {
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
                ArrayList<ColumnPref> columnsPref = tableModel.getColumnsPref();
                for (int i = 0; i < selectIndex.size(); i++) {
                    ColumnPref column = columnsPref.get(selectIndex.get(i));
                    int width = column.getWidthColumn() + dx;
                    if (width < 10) width = 10;
                    column.setWidthColumn(width);
                }
                update();
            }

            private void update() {
                cellBuilder.init(tableModel);
                columnBuilder.init(tableModel);
                headerBuilder.init(tableModel);

                activityInterface.updateColumnHeight((int) columnBuilder.heightColumns);
                activityInterface.invalidate();
                scrollerTo.scrollToEndIfOutside(cellBuilder.getWidth(), cellBuilder.getHeight());
            }

            @Override // кнопка для того чтобы настроить ширину всех столбцов под экран
            public void setWidthForScreen(int tableWidth) {
                int width = cellBuilder.getWidth();
                // если ширина таблицы больше чем экран
                while (width > tableWidth) {
                    width = 0;
                    // проходимся по каждой колоне и минисуем на 1 пока ширина таблицы не получится меньше чем экран
                    for (ColumnPref column : tableModel.getColumnsPref()) {
                        int newWidth = column.getWidthColumn() - 1;
                        column.setWidthColumn(newWidth);
                        width += newWidth;
                    }
                }
                // если ширина таблицы меньше чем экран
                // +1 для сдвига на 1 пиксель в лево от правой части экрана
                while (width + 1 < tableWidth) {
                    // проходим по каждому столбцу плюсуя на 1 попутно проверяя после каждого измененного столбца
                    for (ColumnPref column : tableModel.getColumnsPref()) {
                        int newWidth = column.getWidthColumn() + 1;
                        column.setWidthColumn(newWidth);
                        width++;
                        if (width == tableWidth) break;
                    }
                }
                update();

            }

            // этот метод служит для того что бы всегда получать усредненную ширину всех столбцов
            @Override
            public int getGeneralWidth() {
                int width = 0;
                if (columnMode) {
                    for (int i = 0; i < selectIndex.size(); i++) {
                        ColumnPref columnPref = tableModel.getColumnsPref().get(selectIndex.get(i));
                        width += columnPref.getWidthColumn();
                    }
                    width /= selectIndex.size();
                } else {

                    for (ColumnPref col : tableModel.getColumnsPref()) {
                        width += col.getWidthColumn();
                    }
                    width /= tableModel.getColumnsPref().size();
                }
                return width;
            }

            @Override
            public void onDismiss() {
                ArrayList<Integer> width = widthStory.getCurrentWidth(tableModel);
                widthStory.setNewWidths(ArrayUtils.toPrimitiveArray(width));
                widthStory.setOnHistoryListener(new HistoryUpdateListener() {
                    @Override
                    public void undo(Object argument) {
                        update();
                    }

                    @Override
                    public void redo(Object argument) {
                        update();
                    }
                });
                if (widthStory.isEdited())
                    history.setWidth(widthStory);

            }
        };
        activityInterface.showEditWidthCellsWin(view, updateWidth);
    }

    @Override
    public void setHeightCells(View view) {
        final int oldHeight = tableModel.getHeightCells();
        UpdateHeight updateHeight = new UpdateHeight() {
            @Override
            public void setHeightCell(int heightCell) {
                tableModel.setHeightCells(heightCell);
                update();
            }

            private void update() {
                cellBuilder.init(tableModel);
                headerBuilder.init(tableModel);

                activityInterface.invalidate();
                scrollerTo.scrollToEndIfOutside(cellBuilder.getWidth(), cellBuilder.getHeight());
            }

            @Override
            public void onDismiss() {
                HeightStroke heightStroke = new HeightStroke(oldHeight, tableModel.getHeightCells());
                heightStroke.setOnHistoryListener(new HistoryUpdateListener() {
                    @Override
                    public void undo(Object argument) {
                        update();
                    }

                    @Override
                    public void redo(Object argument) {
                        update();
                    }
                });
                if (heightStroke.isEdited())
                    history.setHeightStroke(heightStroke);
            }
        };
        activityInterface.showEditHeightCellsWin(view, oldHeight, updateHeight);
    }

    @Override
    public void settingTable() {
        SettingTableListener setting = new SettingTableListener(tableModel) {
            @Override
            public void setLockHeight(int lockHeight) {
                tableModel.setLockHeightCells(lockHeight);

                cellBuilder.init(tableModel);
                headerBuilder.init(tableModel);
                activityInterface.invalidate();
                scrollerTo.scrollToEndIfOutside(cellBuilder.getWidth(), cellBuilder.getHeight());
            }
        };
        activityInterface.showSettingTableWin(setting);
    }

    public void updateTable(Context context) {
        TableLab.get(context).updateTableOfDB(context, tableModel);
    }

    public void setText(String s) {

        if (cellMode) {
            Cell cell = cellBuilder.getSelectCell(tableModel);
            cell.text = s;
            cellBuilder.init(tableModel);
            headerBuilder.init(tableModel);
        } else if (columnMode) {
            ColumnPref renameColumn = columnBuilder.getSelectColumn(tableModel);
            renameColumn.setName(s);
            //надо обновить чтоб высота была что нужно
            columnBuilder.init(tableModel);
            // обновляем высоту у колумнвью
            activityInterface.updateColumnHeight((int) columnBuilder.heightColumns);
        }

        activityInterface.invalidate();
    }

    public void openKeyboardEvent(boolean isOpen) {
//        if (cellMode)
        if (isOpen) {
            if (cellMode)
                scrollerTo.scrollToCell(cellBuilder.getSelectCell(tableModel));
        } else
            scrollerTo.scrollToEndIfOutside(cellBuilder.getWidth(), cellBuilder.getHeight());
    }

    public void destroyed() {
        tablePresenter = null;
    }

    public void modeCheck() {
        if (cellMode) {
            cellMode = false; //  может надо будет убрать ((
            int x = (int) (cellBuilder.getSelectCell(tableModel).startX + 1); // +1 обязательно а то выделит не ту, везде так.
            int y = (int) (cellBuilder.getSelectCell(tableModel).startY + 1);
            cellClick(x, y);
        } else if (headerMode) {
            headerMode = false;
            headerBuilder.selectHeaderCount = 0;
            for (Cell header : tableModel.getHeaders()) {
                if (header.isTouched) {
                    header.isTouched = false; // нужно для того чтоб строка выделялась а не снимала выделение
                    headerClick(header.startY + 1);
                }
            }
        } else if (columnMode) {
            columnMode = false;
            columnBuilder.selectColumnCount = 0;
            for (ColumnPref column : tableModel.getColumnsPref()) {
                if (column.isTouched) {
                    column.isTouched = false;// нужно для того чтоб строка выделялась а не снимала выделение
                    columnClick(column.startX + 1);
                }
            }
        }
    }

    public boolean defaultState() {
        boolean toReturn = true;
        boolean isDefaultMenu = cellMode;

        if (cellMode) {
            cellMode = false;
            cellBuilder.unSelectCell(tableModel);
            activityInterface.hideEditCellWindow();
            scrollerTo.scrollToEndIfOutside(cellBuilder.getWidth(), cellBuilder.getHeight());

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

        if (!isDefaultMenu)
            activityInterface.showDefaultMenu();
        activityInterface.invalidate();
        return toReturn;
    }

    public int getColumnsHeight() {
        return (int) columnBuilder.heightColumns;
    }

    public void getColumnRenameWin() {
        ColumnPref columnPref = columnBuilder.getSelectColumn(tableModel);
        if (columnPref != null)
            activityInterface.showRenameColumnWin(columnPref);
    }

    public void settingColumns() {
        final ArrayList<Integer> selectIndex = new ArrayList<>();
        final ArrayList<ColumnPref> selectColumn = new ArrayList<>();
        for (int i = 0; i < tableModel.getColumnsPref().size(); i++) {
            if (tableModel.getColumnsPref().get(i).isTouched) {
                selectIndex.add(i);
                selectColumn.add(tableModel.getColumnsPref().get(i));
            }
        }

        final EditColumn editColumn = new EditColumn(ArrayUtils.toPrimitiveArray(selectIndex));
        editColumn.setOnHistoryListener(new HistoryUpdateListener() {
            @Override
            public void undo(Object argument) {
                get();
            }

            private void get() {
                columnBuilder.init(tableModel);
                cellBuilder.init(tableModel);

                activityInterface.updateColumnHeight(columnBuilder.heightColumns);
                activityInterface.invalidate();
            }

            @Override
            public void redo(Object argument) {
                get();
            }
        });
        editColumn.setOldColumnPrefs(selectColumn);

        SettColumnsListener settColumn = new SettColumnsListener() {
            private void update() {
                cellBuilder.init(tableModel);
                headerBuilder.init(tableModel);
                columnBuilder.init(tableModel);

                activityInterface.invalidate();
            }

            @Override
            public void setType(int type) {
                this.type = type;
                for (int i = 0; i < selectIndex.size(); i++) {
                    ColumnPref column = tableModel.getColumnsPref().get(selectIndex.get(i));
                    column.setInputType(type);
                }
                update();
            }

            @Override
            public void setTextSizeTitle(int size) {
                sizeTitle = size;
                for (int i = 0; i < selectIndex.size(); i++) {
                    ColumnPref column = tableModel.getColumnsPref().get(selectIndex.get(i));
                    column.setTextSizeTitle(size);
                }
                update();
                activityInterface.updateColumnHeight(columnBuilder.heightColumns);
            }

            @Override
            public void setTextSizeCell(int size) {
                sizeCell = size;
                for (int i = 0; i < selectIndex.size(); i++) {
                    ColumnPref column = tableModel.getColumnsPref().get(selectIndex.get(i));
                    column.setTextSizeCell(size);
                }
                update();
            }

            @Override
            public void setStyleTitle(int style) {
                styleTitle = style;
                for (int i = 0; i < selectIndex.size(); i++) {
                    ColumnPref column = tableModel.getColumnsPref().get(selectIndex.get(i));
                    column.setTextStyleTitle(style);
                }
                update();
                activityInterface.updateColumnHeight(columnBuilder.heightColumns);
            }

            @Override
            public void setStyleCell(int style) {
                styleCell = style;
                for (int i = 0; i < selectIndex.size(); i++) {
                    ColumnPref column = tableModel.getColumnsPref().get(selectIndex.get(i));
                    column.setTextStyleCell(style);
                }
                update();
            }

            @Override
            public void setColorTitle(int color) {
                colorTitle = color;
                for (int i = 0; i < selectIndex.size(); i++) {
                    ColumnPref column = tableModel.getColumnsPref().get(selectIndex.get(i));
                    column.setTextColorTitle(color);
                }
                update();
            }

            @Override
            public void setColorCell(int color) {
                colorCell = color;
                for (int i = 0; i < selectIndex.size(); i++) {
                    ColumnPref column = tableModel.getColumnsPref().get(selectIndex.get(i));
                    column.setTextColorCell(color);
                }
                update();
            }

            @Override
            public void closeWindow() {
                editColumn.setNewColumnPrefs(selectColumn);
                history.editColumns(editColumn);
            }
        };

        ColumnPref columnPref = tableModel.getColumnsPref().get(selectIndex.get(0));
        settColumn.type = columnPref.getInputType();
        settColumn.sizeTitle = columnPref.getTextSizeTitle();
        settColumn.sizeCell = columnPref.getTextSizeCell();
        settColumn.styleTitle = columnPref.getTextStyleTitle();
        settColumn.styleCell = columnPref.getTextStyleCell();
        settColumn.colorTitle = columnPref.getTextColorTitle();
        settColumn.colorCell = columnPref.getTextColorCell();

        activityInterface.showSettingColumnWin(settColumn);
    }

    public boolean deleteColumns() {

        if (columnBuilder.selectColumnCount >= tableModel.getColumnsPref().size())
            return false;

        List<ColumnPref> columnSelect = new ArrayList<>();
        final List<Integer> indexes = new ArrayList<>();
        for (int i = 0; i < tableModel.getColumnsPref().size(); i++) {
            ColumnPref column = tableModel.getColumnsPref().get(i);
            if (column.isTouched) {
                columnSelect.add(column);
                indexes.add(i);
            }
        }
        //добавление в историю
        final DeleteColumn deleteColumn = history.deleteColumn(ArrayUtils.toPrimitiveArray(indexes), columnSelect, tableModel);
        deleteColumn.setOnHistoryListener(new HistoryUpdateListener() {
            private void init() {
                columnBuilder.init(tableModel);
                cellBuilder.init(tableModel);
                activityInterface.updateColumnHeight(columnBuilder.heightColumns);
                columnBuilder.selectColumnCount = 0;
            }

            @Override
            public void undo(Object argument) {
                init();
                for (int i = 0; i < deleteColumn.index.length; i++) {
                    columnClick(tableModel.getColumnsPref().get(deleteColumn.index[i]).startX + 1);
                }
            }

            @Override
            public void redo(Object argument) {
                init();
                scrollerTo.scrollToEndIfOutside(cellBuilder.getWidth(), cellBuilder.getHeight());
            }
        });
        // и потом само удаление
        columnBuilder.deleteSelectedColumn(tableModel);
        columnBuilder.init(tableModel);
        cellBuilder.init(tableModel);

        activityInterface.updateColumnHeight(columnBuilder.heightColumns);
        defaultState();
        scrollerTo.scrollToEndIfOutside(cellBuilder.getWidth(), cellBuilder.getHeight());

        return true;

    }

    public boolean deleteStroke() {
        // возвращает фалс если выделены все ячейки
        if (headerBuilder.selectHeaderCount >= tableModel.getHeaders().size())
            return false;

        //добавление в историю
        final List<Integer> indexes = new ArrayList<>();
        for (int i = 0; i < tableModel.getEntries().size(); i++) {
            Cell stroke = tableModel.getHeaders().get(i);
            if (stroke.isTouched) {
                indexes.add(i);
            }
        }
        final DeleteStroke deleteStroke = history.deleteStroke(ArrayUtils.toPrimitiveArray(indexes), tableModel);
        deleteStroke.setOnHistoryListener(new HistoryUpdateListener() {
            private void init() {
                cellBuilder.init(tableModel);
                headerBuilder.init(tableModel);
                headerBuilder.selectHeaderCount = 0;
            }

            @Override
            public void undo(Object argument) {
                init();
                for (int i = 0; i < deleteStroke.index.length; i++) {
                    headerClick(tableModel.getHeaders().get(deleteStroke.index[i]).startY + 1);
                }
            }

            @Override
            public void redo(Object argument) {
                init();
                scrollerTo.scrollToEndIfOutside(cellBuilder.getWidth(), cellBuilder.getHeight());
            }
        });

        headerBuilder.deleteHeaders(tableModel);

        headerBuilder.init(tableModel);
        cellBuilder.init(tableModel);

        defaultState();
        scrollerTo.scrollToEndIfOutside(cellBuilder.getWidth(), cellBuilder.getHeight());
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
            editCell.setOnHistoryListener(new HistoryUpdateListener() {
                @Override
                public void undo(Object argument) {
                    get((int[]) argument);
                }

                private void get(int[] index) {
                    cellBuilder.init(tableModel);
                    int x = (int) (tableModel.getEntries().get(index[0]).get(index[1]).startX + 1);
                    int y = (int) (tableModel.getEntries().get(index[0]).get(index[1]).startY + 1);
                    cellClick(x, y);
                }

                @Override
                public void redo(Object argument) {
                    get((int[]) argument);
                }
            });
            history.editCell(editCell);
        }
    }

    @Override
    public void renameColumn(String oldName, String newName, int index) {
        if (!oldName.equals(newName)) {
            RenameColumn renameColumn = new RenameColumn(index);
            renameColumn.setOldNameColumn(oldName);
            renameColumn.setNewNameColumn(newName);
            renameColumn.setOnHistoryListener(new HistoryUpdateListener() {
                @Override
                public void undo(Object argument) {
                    get((int) argument);
                }

                private void get(int index) {
                    columnBuilder.init(tableModel);
                    activityInterface.updateColumnHeight(columnBuilder.heightColumns);
                    float x = tableModel.getColumnsPref().get(index).startX + 1;
                    columnClick(x);
                }

                @Override
                public void redo(Object argument) {
                    get((int) argument);
                }
            });
            history.renameColumn(renameColumn);
        }
    }

    public void getDatePick() {
        Cell cell = cellBuilder.getSelectCell(tableModel);
        activityInterface.showDatePickerWin(cell.date);
    }

    public void setDate(long date) {
        cellBuilder.getSelectCell(tableModel).date = date;
        cellBuilder.init(tableModel); // тут это обязательно там логика для отображения даты
        activityInterface.invalidate();
    }

    public void enterPressed() {
        if (cellMode) {
            cellBuilder.init(tableModel);
            headerBuilder.init(tableModel);
            scrollerTo.scrollToCell(cellBuilder.getSelectCell(tableModel));
        }
    }

    public void clearText() {
        cellBuilder.clearText(tableModel);
        cellBuilder.init(tableModel);
        headerBuilder.init(tableModel);
        activityInterface.invalidate();

    }
    /*

     */
}
