package ru.developer.press.mytable;

import android.Manifest;
import android.app.Activity;
import android.app.DatePickerDialog;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.support.annotation.Nullable;
import android.support.annotation.RequiresApi;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.InputType;
import android.text.TextWatcher;
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
import android.widget.CompoundButton;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import net.yslibrary.android.keyboardvisibilityevent.KeyboardVisibilityEvent;
import net.yslibrary.android.keyboardvisibilityevent.KeyboardVisibilityEventListener;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Calendar;

import ru.developer.press.mytable.interfaces.SettColumnsListener;
import ru.developer.press.mytable.interfaces.SettingTableListener;
import ru.developer.press.mytable.interfaces.TableActivityInterface;
import ru.developer.press.mytable.interfaces.UpdateHeight;
import ru.developer.press.mytable.interfaces.UpdateWidth;
import ru.developer.press.mytable.model.Cell;
import ru.developer.press.mytable.model.ColumnPref;
import ru.developer.press.mytable.model.TableLab;
import ru.developer.press.mytable.presenters.TablePresenter;
import ru.developer.press.mytable.table.ColumnView;
import ru.developer.press.mytable.table.HeaderView;
import ru.developer.press.mytable.table.TableView;
import ru.developer.press.myTable.R;
import ru.developer.press.mytable.interfaces.ScrollerTo;
import ru.developer.press.mytable.views.ControllerBottomMenu;
import ru.developer.press.mytable.views.SettingButtonView;

import static android.view.View.GONE;
import static android.view.View.VISIBLE;

public class TableActivity extends AppCompatActivity implements TableActivityInterface, View.OnClickListener, ScrollerTo {

    private static final String TAG = "test";
    private long deley_scroll = 150;
    private TablePresenter tablePresenter;
    private Toolbar toolbar;
    private Menu mMenu;
    private TextView nameTableToolBar;

    private TableView tableView;
    private ColumnView columnView;
    private HeaderView headerView;

    private LinearLayout editCellWindow;
    private FrameLayout ok;
    private FrameLayout clearText;
    private EditText editText;
    private TextView dateTextView;

    private LinearLayout bottomMenu;
    private ControllerBottomMenu controllerBottomMenuBM;
    private boolean isOpenKeyboard;
    private Animation animClose;
    private Animation animOpen;
    private boolean isRenameButtonAdded = true;
    private boolean isAnim;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
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
            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });
        init();
        String nameId = getIntent().getStringExtra(MainActivity.TABLEACTIVITYKEY);
        String nameTable = TableLab.get(this).getTableForNameId(nameId).getNameTable();

        tablePresenter = TablePresenter.get(this, nameId);
        nameTableToolBar.setText(nameTable);

        KeyboardVisibilityEvent.setEventListener(this,
                new KeyboardVisibilityEventListener() {
                    @Override
                    public void onVisibilityChanged(boolean isOpen) {
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
        headerView.setHeaderListener(tablePresenter);
        columnView.setColumnViewListener(tablePresenter);
        controllerBottomMenuBM.initView(this);
        controllerBottomMenuBM.setAddButtonListener(tablePresenter);

        updateColumnHeight(tablePresenter.getColumnsHeight());
        // проверка находились ли мы в каком ни будь режиме или нет
        tableView.postDelayed(() -> tablePresenter.modeCheck(), 100);
        invalidate();
    }

    @Override
    protected void onStop() {
        super.onStop();
        tablePresenter.updateTable(this);

        tableView.setTableListener(null);
        headerView.setHeaderListener(null);
        columnView.setColumnViewListener(null);
        //поставил по очереди последним потому что остальные листенеры зависят от него, он их листенер
        tablePresenter.setInterfaces(null);
        controllerBottomMenuBM.setAddButtonListener(null);
    }

    @Override
    public void finish() {
        tablePresenter.destroyed();
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
            case R.id.setting_column:
                tablePresenter.settingColumns();
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
        controllerBottomMenuBM = new ControllerBottomMenu();

        toolbar = findViewById(R.id.toolbar_table);
        mMenu = toolbar.getMenu();
        nameTableToolBar = findViewById(R.id.name_table_in_toolbar);

        tableView = findViewById(R.id.table_view);
        columnView = findViewById(R.id.column_view);
        headerView = findViewById(R.id.header_view);

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
                if (s.length() > 0)
                    if (s.charAt(s.length() - 1) == '\n') {
                        tablePresenter.enterPressed();
                    }
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

    }

    @Override
    public void showEditCellWindow(final int typeCell, final String text) {
        final boolean isDate = typeCell == 2;
        tableView.postDelayed(new Runnable() {
            @Override
            public void run() {
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
    public void updateColumnHeight(float height) {
        RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) columnView.getLayoutParams();
        params.height = (int) height;
        columnView.setLayoutParams(params);
    }

    @Override
    public void showRenameColumnWin(final ColumnPref columnPref) {
        showEditCellWindow(0, columnPref.getName());
//        setText(columnPref.getName());
    }

    @Override
    public void showEditHeightCellsWin(View view, int heightCells, final UpdateHeight updateHeight) {
        FrameLayout frameLayout = (FrameLayout) LayoutInflater.from(this).inflate(R.layout.heigh_seek_layout, null);
        SeekBar seekBar = frameLayout.findViewById(R.id.seekBar_height_cell);
        seekBar.setProgress(heightCells - 10);
        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {

            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                updateHeight.setHeightCell(seekBar.getProgress() + 10);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });

        final PopupWindow heightWindow = StaticMetods.sreatePopupWindow(frameLayout, FrameLayout.LayoutParams.WRAP_CONTENT, FrameLayout.LayoutParams.WRAP_CONTENT);

        heightWindow.showAtLocation(view, Gravity.START | Gravity.BOTTOM, headerView.getWidth(), view.getHeight());
        heightWindow.setOnDismissListener(new PopupWindow.OnDismissListener() {
            @Override
            public void onDismiss() {
                updateHeight.onDismiss();
            }
        });
    }

    @Override
    public void showEditWidthCellsWin(View view, final UpdateWidth updateWidth) {
        FrameLayout frameLayout = (FrameLayout) LayoutInflater.from(this).inflate(R.layout.width_seek_layout, null);
        final SeekBar seekBar = frameLayout.findViewById(R.id.seekBar_height_cell);
        final ImageButton widthForScreen = frameLayout.findViewById(R.id.width_for_screen_button);
        widthForScreen.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                updateWidth.setWidthForScreen(tableView.getWidth());
                seekBar.setProgress(updateWidth.getGeneralWidth() - 10);
                widthForScreen.startAnimation(AnimationUtils.loadAnimation(TableActivity.this, R.anim.click_anim));
            }
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

            }
        });

        final PopupWindow widthWindow = StaticMetods.sreatePopupWindow(frameLayout, FrameLayout.LayoutParams.WRAP_CONTENT, FrameLayout.LayoutParams.WRAP_CONTENT);

        widthWindow.showAtLocation(view, Gravity.CENTER | Gravity.BOTTOM, 0, view.getHeight());
        widthWindow.setOnDismissListener(new PopupWindow.OnDismissListener() {
            @Override
            public void onDismiss() {
                updateWidth.onDismiss();
            }
        });
    }

    @Override
    public void showSettingColumnWin(final SettColumnsListener settColumns) {
        LinearLayout layout = (LinearLayout) LayoutInflater.from(this).inflate(R.layout.setting_column_layout, null);
        // тут настраиваются все кнопки в окне настройки столбцов
        new SettingButtonView(layout, settColumns);

        PopupWindow popupWindow = StaticMetods.sreatePopupWindow(layout, ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        popupWindow.showAtLocation(layout, Gravity.BOTTOM, 0, 0);
        // если нажали в не окна
        popupWindow.setOnDismissListener(new PopupWindow.OnDismissListener() {
            @Override
            public void onDismiss() {
                settColumns.closeWindow();
            }
        });
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
                dateTextView.setText(StaticMetods.getDateOfMillis(dateMillis));
            }
        }, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH));
        datePickerDialog.show();
    }

    @Override
    public void showSettingTableWin(final SettingTableListener setting) {
        LinearLayout layout = (LinearLayout) LayoutInflater.from(this).inflate(R.layout.setting_table_layout, null);
        Switch lockHeight = layout.findViewById(R.id.fixed_height_switch);
        lockHeight.setChecked(setting.lockHeight == 1);
        lockHeight.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                int lock = isChecked ? 1 : 0;
                setting.setLockHeight(lock);
            }
        });

        PopupWindow popupWindow = StaticMetods.sreatePopupWindow(layout, ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        popupWindow.showAtLocation(layout, Gravity.BOTTOM, 0, 0);
        // если нажали в не окна
        popupWindow.setOnDismissListener(new PopupWindow.OnDismissListener() {
            @Override
            public void onDismiss() {
            }
        });
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
            controllerBottomMenuBM.showAddButtonOfColumn();

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
            controllerBottomMenuBM.showAddButtonOfStroke();

            hideKeyboard();
        }
    }

    @Override
    public void showDefaultMenu() {
        showToolbar();
        mMenu.clear();
        getMenuInflater().inflate(R.menu.table_menu, mMenu);
        getSupportActionBar().setHomeAsUpIndicator(R.drawable.ic_back_button);
        controllerBottomMenuBM.showStandartAddButton();
    }


    @Override
    public void invalidate() {
        tableView.invalidate();
        headerView.invalidate();
        columnView.invalidate();
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
    public void scrollTableBy(int x, int y) {
        tableView.scrollBy(x, y);
        headerView.scrollBy(0, y);
        columnView.scrollBy(x, 0);

    }

    @Override
    public void scrollTableTo(int x, int y) {
        tableView.scrollTo(x, y);
        headerView.scrollTo(0, y);
        columnView.scrollTo(x, 0);

    }

    @Override
    public void scrollToCell(final Cell cell) {
        tableView.postDelayed(new Runnable() {
            @Override
            public void run() {

                int xPosition = (int) (cell.startX - tableView.getScrollX()); //позиция старт х у ячейки относительно экрана (если вышла за экран в лево значит придет на место

                if (xPosition > 0) {// если позиция стартХ не вышла с левой стороны
                    int endX = (int) (cell.endX - tableView.getScrollX() - tableView.getWidth()); // то работаем с позицией ендХ
                    if (endX > 0) { // если позиция ендХ вышла за предела экрана с правой стороны
                        xPosition = endX; // присваимаев это значение для перехода на место
                    } else
                        xPosition = 0; // если ни позиция стартХ и ендХ не вышли за свои края, то ни куда не скролимся
                }

                // по у суета
                int yPosition = (int) (cell.endY - tableView.getHeight() - tableView.getScrollY()); // значение y для скролинга

                if (yPosition < 0) {// если нижняя часть ячейки выше нижней части экрана
                    if (cell.startY - tableView.getScrollY() < 0) {// проверяем стартУ не ушел за экран вверх
                        yPosition = (int) (cell.startY - tableView.getScrollY()) - 2;// если так то позиция скроллинга меняется чтоб вернуть из за экрана вниз
                    } else // если все эти условия не выполняются значит ячейка по У не вышла за границы
                        yPosition = 0;
                }

                // манипуляции для правильного отображения синей полоски (наверно)
                if (cell.startX > 0 && xPosition < 0)
                    xPosition--;
                if (yPosition != 0) yPosition += 1;

                tableView.scroller.startScroll(tableView.getScrollX(), tableView.getScrollY(), xPosition, yPosition, 350);
                invalidate();
                deley_scroll = 150; // задержку меняю для того что бы подождать пока закроется окно ввода
            }
        }, deley_scroll);
    }

    @Override
    public void scrollToColumn(final ColumnPref columnPref) {
        tableView.postDelayed(new Runnable() {
            @Override
            public void run() {

                int cellStartX = (int) (columnPref.startX - tableView.getScrollX()); //позиция старт х у ячейки относительно экрана (если вышла за экран в лево значит придет на место
                if (cellStartX > 0) {// если позиция стартХ не вышла с левой стороны
                    int endX = (int) (columnPref.endX - tableView.getScrollX() - tableView.getWidth()); // то работаем с позицией ендХ
                    if (endX > 0) { // если позиция ендХ вышла за предела экрана с правой стороны
                        cellStartX = endX; // присваимаев это значение для перехода на место
                    } else
                        cellStartX = 0; // если ни позиция стартХ и ендХ не вышли за свои края, то ни куда не скролимся
                }
                if (columnPref.startX > 0 && cellStartX < 0)
                    cellStartX--;
                tableView.scroller.startScroll(tableView.getScrollX(), tableView.getScrollY(), cellStartX, 0, 350);
                invalidate();
                deley_scroll = 150; // задержку меняю для того что бы подождать пока закроется окно ввода
            }
        }, deley_scroll);
    }

    @Override
    public void scrollToStroke(final Cell header) {
        tableView.postDelayed(new Runnable() {
            @Override
            public void run() {

                int yPos = (int) (header.endY - tableView.getHeight() - tableView.getScrollY()); // значение для скролинга

                if (yPos < 0) {// если позиция ендУ ячейки выше чем нижняя видимая чвсть
                    if (header.startY - tableView.getScrollY() < 0) {// проверяем стартУ не ушел за экран вверх
                        yPos = (int) (header.startY - tableView.getScrollY()) - 2;// если так то позиция скроллинга меняется чтоб вернуть из за экрана вниз
                    } else // если все эти условия не выполняются значит ячейка по У не вышла за границы
                        yPos = 0;
                }

                if (yPos != 0) yPos += 1;

                tableView.scroller.startScroll(tableView.getScrollX(), tableView.getScrollY(), 0, yPos, 350);
                invalidate();
                deley_scroll = 150; // задержку меняю для того что бы подождать пока закроется окно ввода
            }
        }, deley_scroll);
    }

    @Override
    public void scrollToEndIfOutside(final int widthTable, final int heightTable) {
        final boolean visibleBottomMenu = bottomMenu.getVisibility() == VISIBLE;
        tableView.postDelayed(new Runnable() {
            @Override
            public void run() {

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
            }
        }, deley_scroll);
    }

}
