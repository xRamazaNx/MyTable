package ru.developer.press.mytable;

import android.annotation.SuppressLint;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Environment;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.PopupMenu;
import android.widget.TextView;

//import com.squareup.picasso.Transformation;

import com.bumptech.glide.Glide;
import com.bumptech.glide.GlideBuilder;
import com.bumptech.glide.load.engine.bitmap_recycle.BitmapPool;

import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import ru.developer.press.mytable.database.Database;
import ru.developer.press.mytable.database.SchemaDB;
import ru.developer.press.mytable.dialogs.DialogNameTable;
import ru.developer.press.mytable.model.ColumnPref;
import ru.developer.press.mytable.model.TableLab;
import ru.developer.press.mytable.model.TableModel;
import ru.developer.press.mytable.views.ScrollMain;
import ru.developer.press.myTable.R;
import uk.co.markormesher.android_fab.SpeedDialMenuAdapter;
import uk.co.markormesher.android_fab.SpeedDialMenuCloseListener;
import uk.co.markormesher.android_fab.SpeedDialMenuItem;
import uk.co.markormesher.android_fab.SpeedDialMenuOpenListener;

public class MainActivity extends AppCompatActivity {
    public static String TABLEACTIVITYKEY = "tableActivity";
    private ScrollMain scrollView;
    private uk.co.markormesher.android_fab.FloatingActionButton addButtonLib;

    private Toolbar toolbar;
    private LinearLayout containerCards;
    private List<TableModel> tableModels;

//    private RapidFloatingActionLayout rfaLayout;
//    private RapidFloatingActionButton rfaBtn;
//    private RapidFloatingActionHelper rfabHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        addButtonLib = findViewById(R.id.floatingActionButton);
        setFButtonSetting();
        scrollView = findViewById(R.id.scroll_view_main);
        scrollView.setAddButton(addButtonLib);
        toolbar = findViewById(R.id.toolbar);
//        toolbar.setTitle(getString(R.string.Active));
        setSupportActionBar(toolbar);

        containerCards = findViewById(R.id.container_cards);

        // реклама будет
//        MobileAds.initialize(this, "ca-app-pub-3940256099942544~3347511713");
//        adView = findViewById(R.id.adView);
//        AdRequest adRequest = new AdRequest.Builder().build();
//        adView.loadAd(adRequest);

        addButtonLib.setOnSpeedDialMenuOpenListener(floatingActionButton -> floatingActionButton.getCardView().animate().rotation(45));
        addButtonLib.setOnSpeedDialMenuCloseListener(floatingActionButton -> floatingActionButton.getCardView().animate().rotation(0));

    }

    private void setFButtonSetting() {
        MenuFAB menuFAB = new MenuFAB();
        addButtonLib.setSpeedDialMenuAdapter(menuFAB);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        return true;
    }

    private void startTable(String s) {
        Intent intent = new Intent(MainActivity.this, TableActivity.class);
        intent.putExtra(TABLEACTIVITYKEY, s);
        startActivity(intent);
    }

    @SuppressLint("StaticFieldLeak")
    @Override
    protected void onResume() {
        super.onResume();
        new AsyncTask<Void, Void, Void>() {
            @Override
            protected Void doInBackground(Void... voids) {
                tableModels = TableLab.get(MainActivity.this).getTableList();
                return null;
            }

            @Override
            protected void onPostExecute(Void aVoid) {
                updateCards();
            }
        }.execute();
    }

    @SuppressLint("CheckResult")
    public void updateCards() {
        containerCards.removeAllViews();
//        Observable.fromArray(tableModels).
//                subscribeOn(Schedulers.computation()).
//                observeOn(AndroidSchedulers.mainThread()).
//                subscribe(new Consumer<List<TableModel>>() {
//                    @Override
//                    public void accept(List<TableModel> tableModels) throws Exception {
//                        for (final TableModel table : tableModels) {
//                            // карточка таблицы
//                            LinearLayout l = (LinearLayout) getLayoutInflater().inflate(R.layout.table_card, null);
//                            // имя карточки= System.currentTimeMillis();
//                            TextView name = l.findViewById(R.id.name_card);
//                            name.setText(table.getNameTable());
//                            //клик по карточке
//                            View.OnClickListener onClick = new View.OnClickListener() {
//                                @Override
//                                public void onClick(View v) {
//                                    startTable(table.getNameId());
//                                }
//                            };
//
//                            l.setOnClickListener(onClick);
//                            l.findViewById(R.id.container_fullinfo_card).setOnClickListener(onClick);// скролвиев обрабатывает только свои дочерние элементы, пришлось и на его лайот навешать.
//                            containerCards.addView(l);
//                        }
//                    }
//                });

        for (final TableModel table : tableModels) {
            // карточка таблицы
            LinearLayout l = (LinearLayout) getLayoutInflater().inflate(R.layout.table_card, null);
            // имя карточки
            TextView name = l.findViewById(R.id.name_card);
            name.setText(table.getNameTable());
            //клик по карточке
            View.OnClickListener onClick = v -> {
                switch (v.getId()) {
                    case R.id.menu_tablecard_imButton:
                        getPopupMenu(table, v);
                        break;
                    default:
                        startTable(table.getNameId());
                        break;
                }
            };

            l.setOnClickListener(onClick);
            l.findViewById(R.id.menu_tablecard_imButton).setOnClickListener(onClick);// скролвиев обрабатывает только свои дочерние элементы, пришлось и на его лайот навешать.

//            ImageView imageColumn = l.findViewById(R.id.image_column_card);
//            getTableToImage(table, imageColumn);
            containerCards.addView(l);

        }
    }
//
//    private void getTableToImage(TableModel table,ImageView imageColumn) {
//        ColumnBuilder columnBuilder = new ColumnBuilder(this, null);
//        columnBuilder.init(table);
//        Bitmap bitmapColumn = Bitmap.createBitmap(containerCards.getWidth(), (int) columnBuilder.heightColumns, Bitmap.Config.RGB_565);
//        bitmapColumn.eraseColor(ContextCompat.getColor(this, R.color.gray_light));
//
//        Canvas canvasColumn = new Canvas(bitmapColumn);
//
//        columnBuilder.drawColumns(canvasColumn,table);
//
//        imageColumn.setImageBitmap(bitmapColumn);
//    }

    private void getPopupMenu(final TableModel table, View view) {
        PopupMenu popupMenu = new PopupMenu(this, view);
        popupMenu.getMenuInflater().inflate(R.menu.table_context_menu, popupMenu.getMenu());
        popupMenu.setOnMenuItemClickListener(menuItem -> {
            switch (menuItem.getItemId()) {
                case R.id.rename:
                    DialogNameTable nameTable = DialogNameTable.getDialog(table.getNameTable());
                    DialogNameTable.OnButtonClick buttonClick = name -> {
                        table.setNameTable(name);
                        updateCards();
                        TableLab.get(MainActivity.this).updateTableOfDB(MainActivity.this, table);
                    };
                    nameTable.setClickInterface(buttonClick);
                    nameTable.showNow(getSupportFragmentManager(), "name_table");
                    break;
                case R.id.send:
                    saveTable(table);
                    break;
                case R.id.delete:
                    deleteteTable(table);
                    updateCards();
                    TableLab.get(MainActivity.this).deleteTableOfDB(MainActivity.this, table);
            }
            return true;
        });
        popupMenu.show();
    }

    private void saveTable(TableModel table) {
        Bitmap bitmap = StaticMetods.getScreenTable(table,MainActivity.this);
        String dir = Environment.getExternalStorageDirectory().getPath();
        File imageDir = new File(dir+"/tableSave/");
        imageDir.mkdir();
        File file = new File(imageDir.getPath()+"table_"+table.getNameTable()+".png");

        try {
            FileOutputStream outputStream = new FileOutputStream(file);
            bitmap.compress(Bitmap.CompressFormat.PNG,100, outputStream);
            outputStream.flush();
            outputStream.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void deleteteTable(TableModel table) {
        TableLab.get(this).getTableList().remove(table);
    }

    private String addTable(int variation) {

        TableModel tm = new TableModel();

        Date time = Calendar.getInstance().getTime();
        // имя таблицы (ид таблицы, ид таблицы настроек в конструкторе заданы)
        String nameTable = "";

        switch (variation) {
            case 0:
                nameTable = getString(R.string.contacts);
                addColumnTableContact(tm);
                break;
            case 1:
                nameTable = getString(R.string.empty_table);
                tm.getColumnsPref().add(new ColumnPref().setName(getString(R.string.column) + " " + tm.getColumnsPref().size()));
        }

        tm.setNameTable(nameTable);


        SQLiteDatabase db = new Database(this).getWritableDatabase();
        db.beginTransaction();
        //создание таблицы настроек для этой таблицы
        db.execSQL(SchemaDB.createTableSetting(tm.getNameSettingId()));
        //заполнение таблицы настроек
        for (ColumnPref columnPref : tm.getColumnsPref()) {

            ContentValues value = new ContentValues();
            value.put(SchemaDB.Table.Cols.ID_FOR_COLUMN, columnPref.getNameIdColumn());
            value.put(SchemaDB.Table.Cols.NAME_FOR_COLUMN, columnPref.getName());
            value.put(SchemaDB.Table.Cols.INPUTTYPE, columnPref.getInputType());
            value.put(SchemaDB.Table.Cols.FUNCTION, columnPref.getFunction());

//                    if (lastTableSetting(db, value)){
//                        continue;
//                    }
            value.put(SchemaDB.Table.Cols.WIDTH, columnPref.getWidthColumn());

            value.put(SchemaDB.Table.Cols.SIZETEXT_COL, columnPref.getTextSizeTitle());
            value.put(SchemaDB.Table.Cols.STYLE_COL, columnPref.getTextStyleTitle());
            value.put(SchemaDB.Table.Cols.COLOR_COL, columnPref.getTextColorTitle());

            value.put(SchemaDB.Table.Cols.SIZETEXT_ITEM, columnPref.getTextSizeCell());
            value.put(SchemaDB.Table.Cols.STYLE_ITEM, columnPref.getTextStyleCell());
            value.put(SchemaDB.Table.Cols.COLOR_ITEM, columnPref.getTextColorCell());


            db.insert(tm.getNameSettingId(), null, value);
        }

        //создание обычной таблицы с данными
        db.execSQL(SchemaDB.createTable(tm.getNameId()));
        //добавление колонок в обычную таблицу
        ContentValues contenEntry = new ContentValues();
        for (ColumnPref columnPref : tm.getColumnsPref()) {
            String idColumn = columnPref.getNameIdColumn();
            db.execSQL(SchemaDB.addColumn(tm.getNameId(), idColumn));
            contenEntry.put(idColumn, "");
        }
        //добавление одной записи
        db.insert(tm.getNameId(), null, contenEntry);

        // добавление данных о таблице
        ContentValues value = new ContentValues();

        value.put(SchemaDB.Table.Cols.IS_ARHIVE, tm.getIsArhive());
        value.put(SchemaDB.Table.Cols.IS_FILL, tm.getIsFill());
        value.put(SchemaDB.Table.Cols.ID_FOR_TABLE, tm.getNameId());
        value.put(SchemaDB.Table.Cols.ID_FOR_TABLE_SETTING, tm.getNameSettingId());
        value.put(SchemaDB.Table.Cols.NAME_FOR_TABLE, tm.getNameTable());
        value.put(SchemaDB.Table.Cols.HEIGHT, tm.getHeightCells());
        value.put(SchemaDB.Table.Cols.DATE_CREATE_TABLE, tm.getDateCreated());
        value.put(SchemaDB.Table.Cols.DATE_MODIFY_TABLE, tm.getDateModify());

        db.insert(SchemaDB.Table.NAME_ACTIVE, null, value);

        db.setTransactionSuccessful();
        db.endTransaction();
        db.close();

        TableLab.get(this).upddateTables(this);

        return tm.getNameId();
    }

    private void addColumnTableContact(TableModel table) {
        table.setHeightCells(60);
        List<ColumnPref> columnPrefs = table.getColumnsPref();
        ColumnPref colFullName = new ColumnPref();
        ColumnPref colNumberPhone = new ColumnPref();
        ColumnPref colMail = new ColumnPref();
        ColumnPref colBusiness = new ColumnPref();

        //name
        colFullName.setName(getString(R.string.full_name));
        colFullName.setWidthColumn(350);
        columnPrefs.add(colFullName);
        // number
        colNumberPhone.setName(getString(R.string.phone));
        colNumberPhone.setWidthColumn(300);
        colNumberPhone.setInputType(1);
        columnPrefs.add(colNumberPhone);
        //mail
        colMail.setName(getString(R.string.mail));
        colMail.setWidthColumn(300);
        columnPrefs.add(colMail);
        //business
        colBusiness.setName(getString(R.string.business));
        colBusiness.setWidthColumn(400);
        columnPrefs.add(colBusiness);
    }

//    @Override
//    public void onRFACItemLabelClick(int position, RFACLabelItem item) {
//        rfabHelper.toggleContent();
//        changeItemFB(position);
//    }
//
//    @Override
//    public void onRFACItemIconClick(int position, RFACLabelItem item) {
//        rfabHelper.toggleContent();
//        changeItemFB(position);
//    }

//    private void changeItemFB(int position){
//        switch (position){
//            case 1:
//                addTable();
//                break;
//        }
//    }

    private class MenuFAB extends SpeedDialMenuAdapter {
        private ArrayList<ItemInfo> itemSettingList;

        MenuFAB() {
            itemSettingList = new ArrayList<>();
            //2
            ItemInfo contacts = new ItemInfo();
            contacts.ic_id = R.drawable.ic_contacts_variation;
            contacts.label = getString(R.string.contacts);
            itemSettingList.add(contacts);
            //1
            ItemInfo empty = new ItemInfo();
            itemSettingList.add(empty);

        }

        @Override
        public int getCount() {
            return 2;
        }

        @NotNull
        @Override
        public SpeedDialMenuItem getMenuItem(Context context, int i) {
            ItemInfo itemInfo = itemSettingList.get(i);
            return new SpeedDialMenuItem(MainActivity.this, itemInfo.ic_id, itemInfo.label);
        }

        @Override
        public boolean onMenuItemClick(final int position) {
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    startTable(addTable(position));
                }
            }, 250);

            return true;
        }

        @Override
        public void onPrepareItemCard(Context context, int position, View card) {
//            card.setBackgroundColor(ContextCompat.getColor(MainActivity.this, R.color.gray));
            card.setBackgroundResource(R.drawable.contur_oval);
            super.onPrepareItemCard(context, position, card);
        }

        @Override
        public void onPrepareItemIconWrapper(Context context, int position, LinearLayout label) {
//            label.setBackgroundColor(ContextCompat.getColor(MainActivity.this, R.color.color_normal));
            super.onPrepareItemIconWrapper(context, position, label);
        }

        @Override
        public void onPrepareItemLabel(Context context, int position, TextView label) {
            int padding = StaticMetods.convertDpToPixels(5, context);
            label.setBackgroundResource(R.drawable.contur);
            label.setPadding(padding * 3, padding, padding * 3, padding);
//            label.setTextSize(16);
            label.setTextColor(Color.WHITE);
            super.onPrepareItemLabel(context, position, label);
        }

        private class ItemInfo {
            int ic_id;
            String label;

            ItemInfo() {
                ic_id = R.drawable.ic_empty_table;
                label = getString(R.string.empty_table);
            }
        }
    }

//    private class TransformImage implements Transformation{
//
//        @Override
//        public Bitmap transform(Bitmap source) {
//            return null;
//        }
//
//        @Override
//        public String key() {
//            return null;
//        }
//    }
}
