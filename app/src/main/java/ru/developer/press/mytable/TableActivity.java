package ru.developer.press.mytable;

import android.app.Activity;
import android.app.DatePickerDialog;
import android.os.Bundle;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import android.text.Editable;
import android.text.InputType;
import android.util.DisplayMetrics;
import android.util.Log;
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

import java.util.Calendar;

import ru.developer.press.myTable.R;
import ru.developer.press.mytable.dialogs.DateVariableDialog;
import ru.developer.press.mytable.dialogs.DialogNameTable;
import ru.developer.press.mytable.dialogs.FormulaDialog;
import ru.developer.press.mytable.dialogs.SendTableDialog;
import ru.developer.press.mytable.helpers.AppEvents;
import ru.developer.press.mytable.helpers.BottomMenuControl;
import ru.developer.press.mytable.helpers.Coordinate;
import ru.developer.press.mytable.helpers.PrefsCellLayoutSetting;
import ru.developer.press.mytable.helpers.StaticMethods;
import ru.developer.press.mytable.helpers.TableLab;
import ru.developer.press.mytable.interfaces.SendTableGetter;
import ru.developer.press.mytable.interfaces.TaskListener;
import ru.developer.press.mytable.interfaces.table.TableActivityInterface;
import ru.developer.press.mytable.interfaces.table.TableScroller;
import ru.developer.press.mytable.interfaces.table.callback.DateVariableChangeListener;
import ru.developer.press.mytable.interfaces.table.callback.DialogShowListener;
import ru.developer.press.mytable.interfaces.table.callback.FormulaMakeListener;
import ru.developer.press.mytable.interfaces.table.callback.PrefCellsListener;
import ru.developer.press.mytable.interfaces.table.callback.SettingTableListener;
import ru.developer.press.mytable.interfaces.table.callback.UpdateHeight;
import ru.developer.press.mytable.interfaces.table.callback.WidthListener;
import ru.developer.press.mytable.model.TableModel;
import ru.developer.press.mytable.table.TablePresenter;
import ru.developer.press.mytable.table.views.TableView;

import static android.view.View.GONE;
import static android.view.View.VISIBLE;

public class TableActivity extends AppCompatActivity implements TableActivityInterface, View.OnClickListener, TableScroller {

    private static final String TAG = "test";
    private long delay_scroll = 150;
    private TablePresenter tablePresenter;
    private Toolbar toolbar;
    private Menu mMenu;
    private TextView nameTableToolBar;

    private TableView tableView;

    private LinearLayout editCellWindow;
    private EditText editText;
    private TextView dateTextView;

    private LinearLayout bottomMenu;
    private BottomMenuControl bottomMenuControlBM;
    private boolean isOpenKeyboard;
    private Animation animClose;
    private Animation animOpen;
    private boolean isRenameButtonAdded = true;
    private boolean isAnim;
    private int dpToPixels;
    private boolean blockEditText;

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
        dpToPixels = StaticMethods.convertDpToPixels(10, this);
        setContentView(R.layout.activity_table);

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
                // показываем строку тоталамонт после закрытия окна ввода
                tablePresenter.visibleTotalAmount(true);
            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });

        KeyboardVisibilityEvent.setEventListener(this,
                isOpen -> {
                    isOpenKeyboard = isOpen;
                    tablePresenter.openKeyboardEvent(isOpen);
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

    @Override
    protected void onStart() {
        tablePresenter = TablePresenter.get(this);
        init();

        DisplayMetrics displaymetrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(displaymetrics);

        String path = getIntent().getStringExtra(MainActivity.TABLE_ACTIVITY_KEY);
        StringBuilder nameTable = tablePresenter.getNameTable();
        StaticMethods.getBackTask(new TaskListener() {
//            AlertDialog builder = new AlertDialog.Builder(TableActivity.this).create();

            @Override
            public void preExecute() {
                AppEvents.get().startOpenTable();
//                if (tablePresenter.tableIsNull()) {
//                    builder.setTitle(" загрузка идет если что");
//                    builder.show();
//                }
            }

            @Override
            public void doOnBackground() {
                if (tablePresenter.tableIsNull()) {
                    TableModel table = TableLab.get(TableActivity.this).getTableForFile(path);
                    table.openName = path;
                    table.setWidthView(displaymetrics.widthPixels);
                    nameTable.append(table.getNameTable());
                    tablePresenter.setTableModel(table);
                }
                if (tablePresenter == null)
                    return;
                tableView.setTableListener(tablePresenter);
                tablePresenter.setInterfaces(TableActivity.this);
                tablePresenter.initAndUpdate();

            }

            @Override
            public void main() {
                // проверка находились ли мы в каком ни будь режиме или нет
                tableView.postDelayed(() -> tablePresenter.resume(), 70);
//                if (builder.isShowing())
//                    builder.dismiss();
                nameTableToolBar.setText(nameTable.toString());
                invalidate();

            }
        });
//        invalidate();
        super.onStart();
    }

    @Override
    protected void onPause() {
        AppEvents.get().openTableFinish();
        AppEvents appEvents = AppEvents.get();
        if (!appEvents.isSaveStart())
            tablePresenter.updateTable(TableActivity.this);
        super.onPause();
    }

    @Override
    protected void onStop() {


        tableView.setTableListener(null);
        //поставил по очереди последним потому что остальные листенеры зависят от него, он их листенер
        tablePresenter.setInterfaces(null);
        bottomMenuControlBM.setAddButtonListener(null);
        super.onStop();
    }

    @Override
    public void finish() {
        tablePresenter.destroyed();
        super.finish();
    }

    @Override
    public void onBackPressed() {
        if (!tablePresenter.tableIsNull()) {
            if (!tablePresenter.defaultState()) {
//                AppEvents appEvents = AppEvents.get();
//                if (!appEvents.isStartOpenTable()) {
//                    tablePresenter.updateTable(TableActivity.this);
//                }
                super.onBackPressed();
            }
        }
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
            case R.id.send:
                tablePresenter.sendTable();
                break;
            case R.id.rename:
                tablePresenter.renameTable();
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
        FrameLayout ok = findViewById(R.id.ok_edit_cell_button_frame);
        FrameLayout clearText = findViewById(R.id.clear_edit_cell_button_frame);
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

        TextWatcher textWatcher = new TextWatcher();
        editText.addTextChangedListener(textWatcher);

    }

    @Override
    public void showEditCellWindow(final int typeCell, final String text) {
        if (typeCell == 2) {
            hideKeyboard();
            dateTextView.setVisibility(VISIBLE);
            dateTextView.setText(text);
            editText.setVisibility(GONE);
        } else {
            long start = System.currentTimeMillis();
            blockEditText = true; // блокировать отправку данных из едит текст обратно в презентер
            editText.setText(text);
            blockEditText = false;
            editText.setVisibility(VISIBLE);
            dateTextView.setVisibility(GONE);
            editText.setFocusableInTouchMode(true);
            editText.setSelection(text.length());
            Log.d(TAG, "selectZone: " + (System.currentTimeMillis() - start));
            if (typeCell == 1 || typeCell == 3) {
                editText.setInputType(InputType.TYPE_CLASS_PHONE);
            } else
                editText.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_FLAG_MULTI_LINE);

        }
        tableView.postDelayed(() -> {
            // показывать окно
            if (!isVisibleEditWindow()) {
                // чтоб скролилось после появления окна
                delay_scroll = animOpen.getDuration() + 50;
                editCellWindow.setVisibility(VISIBLE);
                editCellWindow.startAnimation(animOpen);
                //скрываем тоталамонт при старте анимации окна ввода
                tablePresenter.visibleTotalAmount(false);
            }
        }, 10); // специально сделали через пост на 10 мл чтоб анимация инициализировалась и isAnim задался значением нужным для проверок
    }

    @Override
    public void hideEditCellWindow() {

        if (isVisibleEditWindow()) {
            delay_scroll = animClose.getDuration() + 50;
            editCellWindow.startAnimation(animClose);
            hideKeyboard();
            //
        }
    }

    @Override
    public void showEditHeightCells(int heightCells, final UpdateHeight updateHeight) {
        FrameLayout frameLayout = (FrameLayout) LayoutInflater.from(this).inflate(R.layout.heigh_seek, null);
        SeekBar seekBar = frameLayout.findViewById(R.id.seekBar_height_cell);

        final ImageButton up = frameLayout.findViewById(R.id.up_height);
        TaskListener taskUpHeight = new TaskListener() {
            @Override
            public void preExecute() {

            }

            @Override
            public void doOnBackground() {
            }

            @Override
            public void main() {
                seekBar.setProgress(seekBar.getProgress() - 1);
                updateHeight.stopTracking();

            }
        };
        up.setOnClickListener(v -> StaticMethods.getBackTask(taskUpHeight));

        final ImageButton down = frameLayout.findViewById(R.id.down_height);
        TaskListener taskDownHeight = new TaskListener() {
            @Override
            public void preExecute() {

            }

            @Override
            public void doOnBackground() {
            }

            @Override
            public void main() {
                seekBar.setProgress(seekBar.getProgress() + 1);
                updateHeight.stopTracking();
            }
        };
        down.setOnClickListener(v -> StaticMethods.getBackTask(taskDownHeight));

        seekBar.setProgress(heightCells - dpToPixels);
        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {

            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                updateHeight.setHeight(progress + dpToPixels);
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

        heightWindow.showAtLocation(bottomMenu, Gravity.START | Gravity.BOTTOM,
                0, bottomMenu.getHeight());
//        heightWindow.showAsDropDown(bottomMenu);
        heightWindow.setOnDismissListener(new PopupWindow.OnDismissListener() {
            @Override
            public void onDismiss() {
                updateHeight.onDismiss();
            }
        });
    }

    @Override
    public void showEditWidthCells(int widthHeader, final WidthListener widthListener) {
        FrameLayout frameLayout = (FrameLayout) LayoutInflater.from(this).inflate(R.layout.width_seek, null);
        final PopupWindow widthWindow = StaticMethods.createPopupWindow(frameLayout, FrameLayout.LayoutParams.WRAP_CONTENT, FrameLayout.LayoutParams.WRAP_CONTENT);

        final SeekBar seekBar = frameLayout.findViewById(R.id.seekBar_height_cell);
        final ImageButton widthForScreen = frameLayout.findViewById(R.id.width_for_screen_button);
        widthForScreen.setOnClickListener(v -> {
            widthListener.setWidthForScreen();
            widthForScreen.startAnimation(AnimationUtils.loadAnimation(TableActivity.this, R.anim.click_anim));
            widthWindow.dismiss();
//            seekBar.setProgress(widthListener.getGeneralWidth() - dpToPixels);
        });

        final ImageButton right = frameLayout.findViewById(R.id.right_width);
        TaskListener taskUpWidth = new TaskListener() {
            @Override
            public void preExecute() {
            }

            @Override
            public void doOnBackground() {
                seekBar.setProgress(seekBar.getProgress() + 1);
                widthListener.stopTracking();

            }

            @Override
            public void main() {
            }
        };
        right.setOnClickListener(v -> StaticMethods.getBackTask(taskUpWidth));

        final ImageButton left = frameLayout.findViewById(R.id.left_width);
        TaskListener taskDownWidth = new TaskListener() {
            @Override
            public void preExecute() {
            }

            @Override
            public void doOnBackground() {
                seekBar.setProgress(seekBar.getProgress() - 1);
                widthListener.stopTracking();

            }

            @Override
            public void main() {
            }
        };
        left.setOnClickListener(v -> StaticMethods.getBackTask(taskDownWidth));

        seekBar.setMax(tableView.getWidth());
        seekBar.setProgress(widthListener.getGeneralWidth() - dpToPixels);
        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {

            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                widthListener.setWidthCell(progress - widthListener.getGeneralWidth() + dpToPixels); // на сколько сдвинулось
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                widthListener.stopTracking();
            }
        });


        widthWindow.showAtLocation(bottomMenu, Gravity.START | Gravity.BOTTOM,
                widthHeader, bottomMenu.getHeight());
        widthWindow.setOnDismissListener(widthListener::onDismiss);

    }

    @Override
    public void showSettingCell(final PrefCellsListener prefCells) {
        LinearLayout layout = (LinearLayout) LayoutInflater.from(this).inflate(R.layout.cell_prefs_layout, null);
        // тут настраиваются все кнопки в окне настройки столбцов
        new PrefsCellLayoutSetting(layout, prefCells, new DialogShowListener() {
            @Override
            public void showFormulaDialog(FormulaMakeListener formulaMakeListener) {
                FormulaDialog formulaDialog = FormulaDialog.get(prefCells.formula, formulaMakeListener);
                formulaDialog.show(TableActivity.this.getSupportFragmentManager(), "formulaDialog");
            }

            @Override
            public void showDateCheckDialog(int variable, DateVariableChangeListener dateVariableChangeListener) {
                DateVariableDialog dateDialog = DateVariableDialog.get(variable, dateVariableChangeListener);
                dateDialog.show(getSupportFragmentManager(), "dateVariable");
            }
        });
        PopupWindow popupWindow = StaticMethods.createPopupWindow(layout, ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        popupWindow.showAtLocation(layout, Gravity.BOTTOM, 0, 0);
        // если нажали в не окна
        popupWindow.setOnDismissListener(prefCells::closeWindow);
    }

    @Override
    public void showDatePicker(long date) {
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
    public void showSettingTable(final SettingTableListener setting) {
        LinearLayout layout = (LinearLayout) LayoutInflater.from(this).inflate(R.layout.setting_table, null);
        Switch lockHeight = layout.findViewById(R.id.fixed_height_switch);
        lockHeight.setChecked(setting.lockHeight);
        lockHeight.setOnCheckedChangeListener((buttonView, isChecked) -> setting.setLockHeight(isChecked));
        Switch lockWidthFitScreen = layout.findViewById(R.id.always_fit_to_screen_width);
        lockWidthFitScreen.setChecked(setting.lockAlwaysFitToScreen);
        lockWidthFitScreen.setOnCheckedChangeListener((buttonView, isChecked) -> setting.setLockAlwaysFitToScreen(isChecked));

        Switch totalAmount = layout.findViewById(R.id.total_amount);
        totalAmount.setChecked(setting.totalAmountEnable);
        totalAmount.setOnCheckedChangeListener((buttonView, isChecked) -> setting.setTotalAmountEnable(isChecked));

        PopupWindow popupWindow = StaticMethods.createPopupWindow(layout, ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        popupWindow.showAtLocation(layout, Gravity.BOTTOM, 0, 0);
        // если нажали в не окна
        popupWindow.setOnDismissListener(() -> {
            // ну может надо будет
        });
    }

    @Override
    public void showMenuOfCells() {
        showDefaultMenu();
        MenuItem prefCell = mMenu.findItem(R.id.pref_cells);
        prefCell.setVisible(true);


    }
//
//    @Override
//    public int getTableWidth() {
//        return tableView.getWidth();
//    }

    @Override
    public void updateTableForToolbar(String newName) {
        nameTableToolBar.setText(newName);
    }

    @Override
    public void showRenameDialog(String name, DialogNameTable.OnButtonClick buttonClick) {
        DialogNameTable nameTable = DialogNameTable.getDialog(name, buttonClick);
        nameTable.showNow(getSupportFragmentManager(), "name_table");
    }

    @Override
    public void showSendDialog(SendTableGetter sendTableGetter) {
        SendTableDialog sendTableDialog = SendTableDialog.get(sendTableGetter);
        sendTableDialog.show(getSupportFragmentManager(), "send");
    }

    @Override
    public void tableOpened() {
        // пусть кнопки работают тогда когда таблица открыта
        bottomMenuControlBM.setAddButtonListener(tablePresenter);
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
//        toolbar.setAnimation(AnimationUtils.loadAnimation(this, R.anim.toolbar_show));
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
    public void showMenuOfHeaders() {
        showToolbar();
        mMenu.clear();
        getMenuInflater().inflate(R.menu.header_menu, mMenu);
        getSupportActionBar().setHomeAsUpIndicator(R.drawable.ic_galochka_norm);
        bottomMenuControlBM.showAddButtonOfStroke();

        hideKeyboard();
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
                blockEditText = true;
                editText.setText("");
                blockEditText = false;
                break;
            case R.id.textview_cell_in_table:
                tablePresenter.getDatePick();
                break;
            /*

             */
        }
    }

    @Override
    public void scrollToCell(final Coordinate cellCoordinate) {
        tableView.postDelayed(() -> {
//            float scrollX = tableView.getScrollX();
//            float scrollY = tableView.getScrollY();
//            float scale = tableView.mScaleFactor;
//
//            int xPosition = (int) (cellCoordinate.startX * scale - widthHeaders * scale - scrollX); //позиция старт х у ячейки относительно экрана (если вышла за экран в лево значит придет на место
//
//            if (xPosition > 0) {// если позиция стартХ не вышла с левой стороны
//                int endX = (int) (cellCoordinate.endX * scale - scrollX - tableView.getWidth()); // то работаем с позицией ендХ
//                if (endX > 0) { // если позиция ендХ вышла за предела экрана с правой стороны
//                    xPosition = endX; // присваимаев это значение для перехода на место
//                } else
//                    xPosition = 0; // если ни позиция стартХ и ендХ не вышли за свои края, то ни куда не скролимся
//            }
//
//            // по у суета
//            int yPosition = (int) (cellCoordinate.endY * scale - tableView.getHeight() - scrollY); // значение y для скролинга
//
//            if (yPosition < 0) {// если нижняя часть ячейки выше нижней части экрана
//                if (cellCoordinate.startY * scale - heightColumns * scale - scrollY < 0) {// проверяем стартУ не ушел за экран вверх
//                    yPosition = (int) (cellCoordinate.startY * scale - heightColumns * scale - scrollY) - 2;// если так то позиция скроллинга меняется чтоб вернуть из за экрана вниз
//                } else // если все эти условия не выполняются значит ячейка по У не вышла за границы
//                    yPosition = 0;
//            }
//
//            // манипуляции для правильного отображения синей полоски (наверно)
//            ;
//            if (cellCoordinate.startX * scale - widthHeaders * scale > 1)
//                if (xPosition < 0) {
//                    xPosition -= offsetScroll;
//                } else if (xPosition > 0) {
//                    xPosition += offsetScroll;
//                }
//
//            if (cellCoordinate.startY * scale - heightColumns * scale > 1) {
//                if (yPosition < 0)
//                    yPosition -= offsetScroll;
//                else if (yPosition > 0)
//                    yPosition += offsetScroll;
//            }
            Coordinate coordinate = tableView.getCoordinate();
            float endX = coordinate.endX;
            float startX = coordinate.startX;
            float endXCol = cellCoordinate.endX;
            float startXCol = cellCoordinate.startX;
            // надо вверх скролить
            int xPosition = endXCol > endX ? (int) (endXCol - endX) : 0; // значение для скролинга
            // если надо вниз скролить
            if (xPosition == 0)
                xPosition = startX > startXCol ? (int) -(startX - startXCol) : 0;

            float endY = coordinate.endY;
            float startY = coordinate.startY;
            float endYRow = cellCoordinate.endY;
            float startYRow = cellCoordinate.startY;
            // надо вверх скролить
            int yPosition = endYRow > endY ? (int) (endYRow - endY) : 0; // значение для скролинга
            // если надо вниз скролить
            if (yPosition == 0)
                yPosition = startY > startYRow ? (int) -(startY - startYRow) : 0;

            float mScaleFactor = tableView.mScaleFactor;
            tableView.scroller.startScroll(tableView.getScrollX(), tableView.getScrollY(), (int) (xPosition * mScaleFactor), (int) (yPosition * mScaleFactor), 350);
            invalidate();
            delay_scroll = 150; // задержку меняю для того что бы подождать пока закроется окно ввода
        }, delay_scroll);
    }

    @Override
    public void scrollToColumn(Coordinate column) {
        tableView.postDelayed(() -> {
//            float scale = tableView.mScaleFactor;
//            float startX = column.startX * scale;
//            int scrollX = tableView.getScrollX();
//            float endX = column.endX * scale;
//
//
//            int scrollStartX = (int) (startX - scrollX); //позиция старт х у ячейки относительно экрана (если вышла за экран в лево значит придет на место
//            if (scrollStartX > 0) {// если позиция стартХ не вышла с левой стороны
//                int scrollEndX = (int) (endX - scrollX - tableView.getWidth()); // то работаем с позицией ендХ
//                if (scrollEndX > 0) { // если позиция ендХ вышла за предела экрана с правой стороны
//                    scrollStartX = scrollEndX; // присваимаев это значение для перехода на место
//                } else
//                    scrollStartX = 0; // если ни позиция стартХ и ендХ не вышли за свои края, то ни куда не скролимся
//            }
//            if (startX > 0 && scrollStartX < 0)
//                scrollStartX--;
            Coordinate coordinate = tableView.getCoordinate();
            float endX = coordinate.endX;
            float startX = coordinate.startX;
            float endXCol = column.endX;
            float startXCol = column.startX;
            // надо вверх скролить
            int xPos = endXCol > endX ? (int) (endXCol - endX) : 0; // значение для скролинга
            // если надо вниз скролить
            if (xPos == 0)
                xPos = startX > startXCol ? (int) -(startX - startXCol) : 0;

            float mScaleFactor = tableView.mScaleFactor;
            tableView.scroller.startScroll(tableView.getScrollX(), tableView.getScrollY(), (int) (xPos * mScaleFactor), 0, 350);
            invalidate();
            delay_scroll = 150; // задержку меняю для того что бы подождать пока закроется окно ввода
        }, delay_scroll);
    }

    @Override
    public void scrollToStroke(final Coordinate row) {
        tableView.postDelayed(() -> {

//            float scale = tableView.mScaleFactor;
            Coordinate coordinate = tableView.getCoordinate();
            float endY = coordinate.endY;
            float startY = coordinate.startY;
            float endYRow = row.endY;
            float startYRow = row.startY;
            // надо вверх скролить
            int yPos = endYRow > endY ? (int) (endYRow - endY) : 0; // значение для скролинга
            // если надо вниз скролить
            if (yPos == 0)
                yPos = startY > startYRow ? (int) -(startY - startYRow) : 0;
//            int scrollY = tableView.getScrollY();
//            float startY = row.startY * scale;
//
//            int yPos = (int) (endY - tableView.getHeight() - scrollY); // значение для скролинга
//
//            if (yPos < 0) {// если позиция ендУ ячейки выше чем нижняя видимая чвсть
//                if (startY - scrollY < 0) {// проверяем стартУ не ушел за экран вверх
//                    yPos = (int) (startY - scrollY) - 2;// если так то позиция скроллинга меняется чтоб вернуть из за экрана вниз
//                } else // если все эти условия не выполняются значит ячейка по У не вышла за границы
//                    yPos = 0;
//            }

//            if (yPos != 0) yPos += 1;

            float mScaleFactor = tableView.mScaleFactor;
            tableView.scroller.startScroll(tableView.getScrollX(), tableView.getScrollY(), 0, (int) (yPos * mScaleFactor), 350);
            invalidate();
            delay_scroll = 150; // задержку меняю для того что бы подождать пока закроется окно ввода
        }, delay_scroll);
    }

    @Override
    public void scrollToEndIfOutside(final int widthTable, final int heightTable) {
        delay_scroll = 400;
//        final boolean visibleBottomMenu = bottomMenu.getVisibility() == VISIBLE;
        tableView.postDelayed(() -> {
//            float scale = tableView.mScaleFactor;

//            int xPosition = (int) (widthTable * scale - tableView.getWidth() - tableView.getScrollX());
//            int yPosition = (int) (heightTable * scale - tableView.getHeight() - tableView.getScrollY()); // - cellEditWindowHeight;
            /*
            ну зачем мне это говно нужно было (((
             */
            // когда при открытой клаве нажимают назад ячейку не будет видно поэтому...
//            if (editCellWindow.getVisibility() == VISIBLE
//                    && visibleBottomMenu
//                    && isHideKeyboardEvent) {
//                yPosition += editCellWindow.getHeight();
//                isHideKeyboardEvent = false;
//            }

//            if (xPosition > 0) xPosition = 0;
//            if (widthTable * scale < tableView.getWidth()) xPosition = -tableView.getScrollX();
//            if (yPosition > 0) yPosition = 0;
//            if (heightTable * scale < tableView.getHeight()) yPosition = -tableView.getScrollY();

            Coordinate coordinate = tableView.getCoordinate();
            int xPosition = coordinate.endX > widthTable ? (int) -(coordinate.endX - widthTable) : 0;
            int yPosition = coordinate.endY > heightTable ? (int) -(coordinate.endY - heightTable) : 0; // - cellEditWindowHeight;

            if (widthTable < coordinate.width) {
                xPosition = 0;
                if (coordinate.startX > 0)
                    xPosition = (int) -coordinate.startX;
            }

            if (heightTable < coordinate.height) {
                yPosition = 0;
                if (coordinate.startY > 0)
                    yPosition = (int) -coordinate.startY;
            }

            if (yPosition < 0 || xPosition < 0) {
                tableView.scroller.startScroll(tableView.getScrollX(), tableView.getScrollY(), xPosition, yPosition, 350);
                invalidate();
            }
            delay_scroll = 150; // задержку меняю для того что бы подождать пока закроется окно ввода
        }, delay_scroll);
    }

    private class TextWatcher implements android.text.TextWatcher {
        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {

        }

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {
            if (!blockEditText && isVisibleEditWindow())
                tablePresenter.setText(s.toString());
        }

        @Override
        public void afterTextChanged(Editable s) {
        }
    }

}
