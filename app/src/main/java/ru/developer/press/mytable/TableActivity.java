package ru.developer.press.mytable;

import android.Manifest;
import android.app.Activity;
import android.app.DatePickerDialog;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.InputType;
import android.text.TextWatcher;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.inputmethod.InputMethodManager;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.SeekBar;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import net.yslibrary.android.keyboardvisibilityevent.KeyboardVisibilityEvent;

import java.io.File;
import java.util.Calendar;

import ru.developer.press.mytable.dialogs.DateVariableDialog;
import ru.developer.press.mytable.helpers.Coordinate;
import ru.developer.press.mytable.helpers.StaticMethods;
import ru.developer.press.mytable.helpers.TableViewCoordinate;
import ru.developer.press.mytable.interfaces.PrefCellsListener;
import ru.developer.press.mytable.interfaces.SettingTableListener;
import ru.developer.press.mytable.interfaces.TableActivityInterface;
import ru.developer.press.mytable.interfaces.UpdateHeight;
import ru.developer.press.mytable.interfaces.UpdateWidth;
import ru.developer.press.mytable.table.model.Column;
import ru.developer.press.mytable.helpers.TableLab;
import ru.developer.press.mytable.table.model.TableModel;
import ru.developer.press.mytable.table.TablePresenter;
import ru.developer.press.mytable.table.views.TableView;
import ru.developer.press.myTable.R;
import ru.developer.press.mytable.interfaces.TableScroller;
import ru.developer.press.mytable.helpers.BottomMenuControl;
import ru.developer.press.mytable.helpers.PrefsCellLayoutSetting;

import static android.view.View.GONE;
import static android.view.View.VISIBLE;

public class TableActivity extends AppCompatActivity implements TableActivityInterface, View.OnClickListener, TableScroller {

    private static final String TAG = "test";
    private long deley_scroll = 150;
    private TablePresenter tablePresenter;
    private Toolbar toolbar;
    private Menu mMenu;
    private TextView nameTableToolBar;

    private TableView tableView;

    private LinearLayout editCellWindow;
    private FrameLayout ok;
    private FrameLayout clearText;
    private EditText editText;
    private TextView dateTextView;

    private LinearLayout bottomMenu;
    private BottomMenuControl bottomMenuControlBM;
    private boolean isOpenKeyboard;
    private Animation animClose;
    private Animation animOpen;
    private boolean isRenameButtonAdded = true;
    private boolean isAnim;
    private int offsetScroll;

//    public int getStatusBarHeight() {
//        int resourceId = getResources().getIdentifier("status_bar_height", "dimen", "android");
//        if (resourceId > 0) {
//            return getResources().getDimensionPixelSize(resourceId);
//        }
//        return 0;
//    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_table);
        offsetScroll = StaticMethods.convertDpToPixels(4, this);

        animOpen = AnimationUtils.loadAnimation(this, R.anim.edit_cell_show);
        animClose = AnimationUtils.loadAnimation(this, R.anim.edit_cell_hide);
        animOpen.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {
                // тут наоборот так как для закрытия окна проверяется есть ли анимвция,
                // если окно анимируется для открытия и мы хотим в этот момент закрыть окно то надо чтоб isAnim был false
                isAnim = false;
            }

            @Override
            public void onAnimationEnd(Animation animation) {
            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });
        animClose.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {
                isAnim = true;
            }

            @Override
            public void onAnimationEnd(Animation animation) {
                editText.clearFocus();
                editCellWindow.setVisibility(GONE);
                isAnim = false;
            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });
        init();

        String path = getIntent().getStringExtra(MainActivity.TABLE_ACTIVITY_KEY);
        TableModel table = TableLab.get(this).getTableForFile(new File(path));
        table.openName = path;

        String nameTable = table.getNameTable();

        tablePresenter = TablePresenter.get(this, table);
        nameTableToolBar.setText(nameTable);


        KeyboardVisibilityEvent.setEventListener(this,
                isOpen -> {
                    isOpenKeyboard = isOpen;
                    tablePresenter.openKeyboardEvent(isOpenKeyboard);
                    if (isOpen) {
                        hideToolbar();
                        bottomMenu.setVisibility(GONE);
                    } else {
                        showToolbar();
                        editText.clearFocus();
                        bottomMenu.setVisibility(VISIBLE);
                    }
                });
    }

    private boolean permissionCheck() {
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
            int perm = checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE);
            if (perm != PackageManager.PERMISSION_GRANTED) {
                if (shouldShowRequestPermissionRationale(Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
                    Toast.makeText(getApplicationContext(), "Включите разрешение памяти в настройках приложения"
                            , Toast.LENGTH_SHORT).show();
                    return false;
                } else {
                    requestPermissions(new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 416);
                    return false;
                }
            }
        }
        return true;
    }

    @Override
    protected void onStart() {
        super.onStart();
        tablePresenter.setInterfaces(this);
        tableView.setTableListener(tablePresenter);
        bottomMenuControlBM.setAddButtonListener(tablePresenter);
        // проверка находились ли мы в каком ни будь режиме или нет
        tableView.postDelayed(() -> tablePresenter.resume(), 100);
        invalidate();
    }


    @Override
    protected void onStop() {
        super.onStop();

        tablePresenter.updateTable(this);

        tableView.setTableListener(null);
        //поставил по очереди последним потому что остальные листенеры зависят от него, он их листенер
        tablePresenter.setInterfaces(null);
        bottomMenuControlBM.setAddButtonListener(null);
    }

    @Override
    public void finish() {
        tablePresenter.destroyed(this);
        super.finish();
    }

    @Override
    public void onBackPressed() {
        if (!tablePresenter.defaultState())
            super.onBackPressed();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        showDefaultMenu();
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                onBackPressed();
                break;
            case R.id.rename_column:
                tablePresenter.getColumnRenameWin();
                break;
            case R.id.pref_cells:
                tablePresenter.prefCells();
                break;
            case R.id.delete_column:
                if (!tablePresenter.deleteColumns())
                    Toast.makeText(this, R.string.not_delete_all_cells, Toast.LENGTH_SHORT).show();
                break;
            case R.id.delete_header:
                if (!tablePresenter.deleteStroke())
                    Toast.makeText(this, R.string.not_delete_all_cells, Toast.LENGTH_SHORT).show();
                break;
            case R.id.undo:
                tablePresenter.undo();
                break;
            case R.id.redo:
                tablePresenter.redo();
                break;
        }
        return true;
    }

    private void init() {
        bottomMenuControlBM = new BottomMenuControl();
        bottomMenuControlBM.initView(this);


        toolbar = findViewById(R.id.toolbar_table);
        mMenu = toolbar.getMenu();
        nameTableToolBar = findViewById(R.id.name_table_in_toolbar);

        tableView = findViewById(R.id.table_view);

        editCellWindow = findViewById(R.id.include_edit_cell);
        ok = findViewById(R.id.ok_edit_cell_button_frame);
        clearText = findViewById(R.id.clear_edit_cell_button_frame);
        editText = findViewById(R.id.edit_cell_in_table);
        dateTextView = findViewById(R.id.textview_cell_in_table);
        bottomMenu = findViewById(R.id.bottom_menu);

        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
            getSupportActionBar().setHomeAsUpIndicator(R.drawable.ic_back_button);

        }

        ok.setOnClickListener(this);
        dateTextView.setOnClickListener(this);
        clearText.setOnClickListener(this);
        editText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                tablePresenter.setText(s.toString());
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });

    }

    @Override
    public void showEditCellWindow(final int typeCell, final String text) {
        final boolean isDate = typeCell == 2;
        tableView.postDelayed(() -> {
            // показывать окно
            if (!isVisibleEditWindow()) {
                // чтоб скролилось после появления окна
                deley_scroll = animOpen.getDuration() + 50;
                editCellWindow.setVisibility(VISIBLE);
                editCellWindow.startAnimation(animOpen);
            }
            if (isDate) {
                hideKeyboard();
                dateTextView.setVisibility(VISIBLE);
                dateTextView.setText(text);
                editText.setVisibility(GONE);
            } else {
                editText.setVisibility(VISIBLE);
                editText.setText(text);
                dateTextView.setVisibility(GONE);

                if (typeCell == 1) {
                    editText.setInputType(InputType.TYPE_CLASS_NUMBER);
                } else {
                    editText.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_FLAG_MULTI_LINE);
                    editText.setSelection(text.length());
                }
            }
        }, 10); // специально сделали через пост на 10 мл чтоб анимация инициализировалась и isAnim задался значением нужным для проверок
    }

    @Override
    public void hideEditCellWindow() {

        if (isVisibleEditWindow()) {
            deley_scroll = animClose.getDuration() + 50;
            editCellWindow.startAnimation(animClose);
            hideKeyboard();
        }
    }

    @Override
    public void showRenameColumnWin(final Column column) {
        showEditCellWindow(0, column.text);
//        setText(column.getName());

    }

    @Override
    public void showEditHeightCellsWin(View view, int heightCells, final UpdateHeight updateHeight) {
        FrameLayout frameLayout = (FrameLayout) LayoutInflater.from(this).inflate(R.layout.heigh_seek, null);
        SeekBar seekBar = frameLayout.findViewById(R.id.seekBar_height_cell);
        seekBar.setProgress(heightCells - 10);
        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {

            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                updateHeight.setHeight(seekBar.getProgress() + 10);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                updateHeight.stopTracking();
            }
        });

        final PopupWindow heightWindow = StaticMethods.createPopupWindow(frameLayout, FrameLayout.LayoutParams.WRAP_CONTENT, FrameLayout.LayoutParams.WRAP_CONTENT);

        heightWindow.showAtLocation(view, Gravity.START | Gravity.BOTTOM, 40, view.getHeight());
        heightWindow.setOnDismissListener(new PopupWindow.OnDismissListener() {
            @Override
            public void onDismiss() {
                updateHeight.onDismiss();
            }
        });
    }

    @Override
    public void showEditWidthCellsWin(View view, final UpdateWidth updateWidth) {
        FrameLayout frameLayout = (FrameLayout) LayoutInflater.from(this).inflate(R.layout.width_seek, null);
        final SeekBar seekBar = frameLayout.findViewById(R.id.seekBar_height_cell);
        final ImageButton widthForScreen = frameLayout.findViewById(R.id.width_for_screen_button);
        widthForScreen.setOnClickListener(v -> {
            updateWidth.setWidthForScreen(tableView.getWidth());
            seekBar.setProgress(updateWidth.getGeneralWidth() - 10);
            widthForScreen.startAnimation(AnimationUtils.loadAnimation(TableActivity.this, R.anim.click_anim));
        });
        seekBar.setMax(tableView.getWidth());
        seekBar.setProgress(updateWidth.getGeneralWidth() - 10);
        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {

            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                updateWidth.setWidthCell(progress - updateWidth.getGeneralWidth() + 10); // на сколько сдвинулось
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                updateWidth.stopTracking();
            }
        });

        final PopupWindow widthWindow = StaticMethods.createPopupWindow(frameLayout, FrameLayout.LayoutParams.WRAP_CONTENT, FrameLayout.LayoutParams.WRAP_CONTENT);

        widthWindow.showAtLocation(view, Gravity.CENTER | Gravity.BOTTOM, 0, view.getHeight());

        widthWindow.setOnDismissListener(updateWidth::onDismiss);
    }

    @Override
    public void showSettingCellWin(final PrefCellsListener prefCells) {
        LinearLayout layout = (LinearLayout) LayoutInflater.from(this).inflate(R.layout.cell_prefs_layout, null);
        // тут настраиваются все кнопки в окне настройки столбцов
        new PrefsCellLayoutSetting(layout, prefCells);
        PopupWindow popupWindow = StaticMethods.createPopupWindow(layout, ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        popupWindow.showAtLocation(layout, Gravity.BOTTOM, 0, 0);
        // если нажали в не окна
        popupWindow.setOnDismissListener(prefCells::closeWindow);
    }

    @Override
    public void showDatePickerWin(long date) {
        final Calendar calendar = Calendar.getInstance();
        if (date > 0)
            calendar.setTimeInMillis(date);

        DatePickerDialog datePickerDialog = new DatePickerDialog(TableActivity.this, new DatePickerDialog.OnDateSetListener() {
            @Override
            public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
                calendar.set(year, month, dayOfMonth);
                long dateMillis = calendar.getTimeInMillis();
                tablePresenter.setDate(dateMillis);
//                dateTextView.setText(StaticMethods.getDateOfMillis(dateMillis));
            }
        }, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH));
        datePickerDialog.show();
    }

    @Override
    public void showSettingTableWin(final SettingTableListener setting) {
        LinearLayout layout = (LinearLayout) LayoutInflater.from(this).inflate(R.layout.setting_table, null);
        Switch lockHeight = layout.findViewById(R.id.fixed_height_switch);
        TextView dateVar = layout.findViewById(R.id.date_variable);
        dateVar.setOnClickListener(v -> {
            DateVariableDialog dateDialog = DateVariableDialog.get(setting.variable, setting::setDateVariable);
            dateDialog.show(getSupportFragmentManager(), "dateVariable");
        });
        lockHeight.setChecked(setting.lockHeight);
        lockHeight.setOnCheckedChangeListener((buttonView, isChecked) -> setting.setLockHeight(isChecked));

        PopupWindow popupWindow = StaticMethods.createPopupWindow(layout, ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        popupWindow.showAtLocation(layout, Gravity.BOTTOM, 0, 0);
        // если нажали в не окна
        popupWindow.setOnDismissListener(() -> {

        });
    }

    @Override
    public void scrollToSelector(float xTouch, float yTouch, float widthTable, float heightTable, int widthHeader, int heightColumn) {

        int x = (int) xTouch - widthHeader;
        int y = (int) yTouch - heightColumn;
        int scrollValue = offsetScroll * 2;

        int endScreenX = tableView.getScrollX() + tableView.getWidth();
        int endScreenY = tableView.getScrollY() + tableView.getHeight();

        //проверяем точка касания находится в левом краю таблицы
        if (x < tableView.getScrollX() + scrollValue)
            x = -scrollValue;
            // проверяем точка касания находится в правом краю экрана
            // добавляем ранее отнятую величину ширины хеадеров
        else if (x + widthHeader > endScreenX - scrollValue) {
            x = scrollValue;
        } else x = 0;

        // проверяем точка касания в верху таблицы (под заголовками)
        if (y < tableView.getScrollY() + scrollValue)
            y = -scrollValue;
            // проверяем точка касания находится в нижней части таблицы
            // возвращаем ранее отнятую величину высоты заголовков колон
        else if (y + heightColumn > endScreenY - scrollValue)
            y = scrollValue;
        else y = 0;

        // если в итоге прибавленная величина к скролингу приводит к смещению таблицы за границы то х = 0
        if (endScreenX + scrollValue > widthTable
                || xTouch - scrollValue - widthHeader < 0)
            x = 0;

        // тут та же беда
        if (endScreenY + scrollValue > heightTable
                || yTouch - scrollValue - heightColumn < 0)
            y = 0;

        tableView.scrollBy(x, y);
    }

    @Override
    public void showMenuOfCells() {
        showDefaultMenu();
        MenuItem prefCell = mMenu.findItem(R.id.pref_cells);
        prefCell.setVisible(true);


    }

    @Override
    public TableViewCoordinate getTableViewCoordinate() {
        TableViewCoordinate viewCoordinate = new TableViewCoordinate();
        viewCoordinate.scrollX = tableView.getScrollX();
        viewCoordinate.scrollY = tableView.getScrollY();
        viewCoordinate.width = tableView.getWidth();
        viewCoordinate.height = tableView.getHeight();
        return viewCoordinate;
    }

    private boolean isVisibleEditWindow() {

        return editCellWindow.getVisibility() != GONE && !isAnim;

    }

    private void hideKeyboard() {
        if (!isOpenKeyboard) return;

        InputMethodManager inputMethodManager = (InputMethodManager) getSystemService(Activity.INPUT_METHOD_SERVICE);
        assert inputMethodManager != null;
        inputMethodManager.hideSoftInputFromWindow(editText.getWindowToken(), 0);
    }

    private void showToolbar() {
        toolbar.setAnimation(AnimationUtils.loadAnimation(this, R.anim.toolbar_show));
        toolbar.setVisibility(VISIBLE);
    }

    private void hideToolbar() {
//        toolbar.setAnimation(AnimationUtils.loadAnimation(this, R.anim.toolbar_hide));
        toolbar.setVisibility(GONE);

    }

    @Override
    public void showMenuOfColumns(boolean isEditColumn, int columnCount) {
        if (columnCount == 0) return;

        if (isEditColumn) {
            showToolbar();
            mMenu.clear();
            getMenuInflater().inflate(R.menu.column_menu, mMenu);
            getSupportActionBar().setHomeAsUpIndicator(R.drawable.ic_galochka_norm);
            bottomMenuControlBM.showAddButtonOfColumn();

            hideKeyboard();
        }
        MenuItem rename = mMenu.findItem(R.id.rename_column);
        if (columnCount == 1) {
            if (!isRenameButtonAdded) {
                rename.setVisible(true);
                isRenameButtonAdded = true;
            }
        } else {
            rename.setVisible(false);
            isRenameButtonAdded = false;
            hideEditCellWindow();
        }

    }

    @Override
    public void showMenuOfHeaders(boolean isEditHeader) {
        if (isEditHeader) {
            showToolbar();
            mMenu.clear();
            getMenuInflater().inflate(R.menu.header_menu, mMenu);
            getSupportActionBar().setHomeAsUpIndicator(R.drawable.ic_galochka_norm);
            bottomMenuControlBM.showAddButtonOfStroke();

            hideKeyboard();
        }
    }

    @Override
    public void showDefaultMenu() {
        showToolbar();
        mMenu.clear();
        getMenuInflater().inflate(R.menu.table_menu, mMenu);
        MenuItem prefCell = mMenu.findItem(R.id.pref_cells);
        prefCell.setVisible(false);
        hideEditCellWindow();
        getSupportActionBar().setHomeAsUpIndicator(R.drawable.ic_back_button);
        bottomMenuControlBM.showStandartAddButton();
    }


    @Override
    public void invalidate() {
        tableView.invalidate();
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.ok_edit_cell_button_frame:
                onBackPressed();
                break;
            case R.id.clear_edit_cell_button_frame:
                tablePresenter.clearText();
                break;
            case R.id.textview_cell_in_table:
                tablePresenter.getDatePick();
                break;
            /*

             */
        }
    }

    //методы скроллинга
    @Override
    public void scrollTableBy(float distanceX, float distanceY, int tableWidth, int tableHeight) {

        int x = (int) distanceX;
        int y = (int) distanceY;

        int xOff = -tableView.getScrollX(), // точка отчета координат таблицы
                yOff = -tableView.getScrollY();

        if (xOff <= 0) { // если по горизонтали таблица ушла влево
            int ostatok = tableWidth - -xOff;
            // это остаток таблицы с правой стороны за границой
            int posleOstatka = ostatok - tableView.getWidth();

            if (x > posleOstatka) { // если рывок по х будет больше чем остаток таблицы за гранями
                x = posleOstatka;
            }
            if (x < xOff) x = xOff; // если рывок меньще чем координаты начала таблицы

        } else {
            x = 0;
        }

        // тут аналогично
        if (yOff <= 0) {
            int ostatok = tableHeight - -yOff;
            int posleOstatka = ostatok - tableView.getHeight();

            if (y > posleOstatka) {
                y = posleOstatka;
            }
            if (y < yOff) y = yOff;
        } else y = 0;

        tableView.scrollBy(x, y);
    }

    @Override
    public void scrollTableTo(int x, int y) {
        tableView.scrollTo(x, y);
    }

    @Override
    public void scrollToCell(final Coordinate coordinate, int widthHeaders, int heightColumns) {
        tableView.postDelayed(() -> {
            int scrollX = tableView.getScrollX();
            int scrollY = tableView.getScrollY();

            int xPosition = (int) (coordinate.startX - widthHeaders - scrollX); //позиция старт х у ячейки относительно экрана (если вышла за экран в лево значит придет на место

            if (xPosition > 0) {// если позиция стартХ не вышла с левой стороны
                int endX = (int) (coordinate.endX - scrollX - tableView.getWidth()); // то работаем с позицией ендХ
                if (endX > 0) { // если позиция ендХ вышла за предела экрана с правой стороны
                    xPosition = endX; // присваимаев это значение для перехода на место
                } else
                    xPosition = 0; // если ни позиция стартХ и ендХ не вышли за свои края, то ни куда не скролимся
            }

            // по у суета
            int yPosition = (int) (coordinate.endY - tableView.getHeight() - scrollY); // значение y для скролинга

            if (yPosition < 0) {// если нижняя часть ячейки выше нижней части экрана
                if (coordinate.startY - heightColumns - scrollY < 0) {// проверяем стартУ не ушел за экран вверх
                    yPosition = (int) (coordinate.startY - heightColumns - scrollY) - 2;// если так то позиция скроллинга меняется чтоб вернуть из за экрана вниз
                } else // если все эти условия не выполняются значит ячейка по У не вышла за границы
                    yPosition = 0;
            }

            // манипуляции для правильного отображения синей полоски (наверно)
            ;
            if (coordinate.startX - widthHeaders > 1)
                if (xPosition < 0) {
                    xPosition -= offsetScroll;
                } else if (xPosition > 0) {
                    xPosition += offsetScroll;
                }

            if (coordinate.startY - heightColumns > 1) {
                if (yPosition < 0)
                    yPosition -= offsetScroll;
                else if (yPosition > 0)
                    yPosition += offsetScroll;
            }

            tableView.scroller.startScroll(scrollX, scrollY, xPosition, yPosition, 350);
            invalidate();
            deley_scroll = 150; // задержку меняю для того что бы подождать пока закроется окно ввода
        }, deley_scroll);
    }

    @Override
    public void scrollToColumn(Coordinate coordinate) {
        tableView.postDelayed(() -> {

            float startX = coordinate.startX;
            int scrollX = tableView.getScrollX();
            float endX = coordinate.endX;

            int scrollStartX = (int) (startX - scrollX); //позиция старт х у ячейки относительно экрана (если вышла за экран в лево значит придет на место
            if (scrollStartX > 0) {// если позиция стартХ не вышла с левой стороны
                int scrollEndX = (int) (endX - scrollX - tableView.getWidth()); // то работаем с позицией ендХ
                if (scrollEndX > 0) { // если позиция ендХ вышла за предела экрана с правой стороны
                    scrollStartX = scrollEndX; // присваимаев это значение для перехода на место
                } else
                    scrollStartX = 0; // если ни позиция стартХ и ендХ не вышли за свои края, то ни куда не скролимся
            }
            if (startX > 0 && scrollStartX < 0)
                scrollStartX--;
            tableView.scroller.startScroll(scrollX, tableView.getScrollY(), scrollStartX, 0, 350);
            invalidate();
            deley_scroll = 150; // задержку меняю для того что бы подождать пока закроется окно ввода
        }, deley_scroll);
    }

    @Override
    public void scrollToStroke(final Coordinate header) {
        tableView.postDelayed(() -> {

            float endY = header.endY;
            int scrollY = tableView.getScrollY();
            float startY = header.startY;

            int yPos = (int) (endY - tableView.getHeight() - scrollY); // значение для скролинга

            if (yPos < 0) {// если позиция ендУ ячейки выше чем нижняя видимая чвсть
                if (startY - scrollY < 0) {// проверяем стартУ не ушел за экран вверх
                    yPos = (int) (startY - scrollY) - 2;// если так то позиция скроллинга меняется чтоб вернуть из за экрана вниз
                } else // если все эти условия не выполняются значит ячейка по У не вышла за границы
                    yPos = 0;
            }

            if (yPos != 0) yPos += 1;

            tableView.scroller.startScroll(tableView.getScrollX(), scrollY, 0, yPos, 350);
            invalidate();
            deley_scroll = 150; // задержку меняю для того что бы подождать пока закроется окно ввода
        }, deley_scroll);
    }

    @Override
    public void scrollToEndIfOutside(final int widthTable, final int heightTable) {
        final boolean visibleBottomMenu = bottomMenu.getVisibility() == VISIBLE;
        tableView.postDelayed(() -> {

            int xPosition = widthTable - tableView.getWidth() - tableView.getScrollX();
            int yPosition = heightTable - tableView.getHeight() - tableView.getScrollY(); // - cellEditWindowHeight;

            if (editCellWindow.getVisibility() == VISIBLE) {
                if (visibleBottomMenu)
                    yPosition -= editCellWindow.getHeight();
            }


            if (xPosition > 0) xPosition = 0;
            if (widthTable < tableView.getWidth()) xPosition = -tableView.getScrollX();
            if (yPosition > 0) yPosition = 0;
            if (heightTable < tableView.getHeight()) yPosition = -tableView.getScrollY();

            if (yPosition < 0 || xPosition < 0) {
                tableView.scroller.startScroll(tableView.getScrollX(), tableView.getScrollY(), xPosition, yPosition, 350);
                invalidate();
            }
            deley_scroll = 150; // задержку меняю для того что бы подождать пока закроется окно ввода
        }, deley_scroll);
    }

}
