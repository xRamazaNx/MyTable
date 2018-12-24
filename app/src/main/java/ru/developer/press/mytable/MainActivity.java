package ru.developer.press.mytable;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
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

import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import ru.developer.press.mytable.dialogs.DialogNameTable;
import ru.developer.press.mytable.helpers.StaticMethods;
import ru.developer.press.mytable.helpers.setting_table.TableFileHelper;
import ru.developer.press.mytable.table.model.Column;
import ru.developer.press.mytable.helpers.TableLab;
import ru.developer.press.mytable.table.model.TableModel;
import ru.developer.press.myTable.R;
import uk.co.markormesher.android_fab.SpeedDialMenuAdapter;
import uk.co.markormesher.android_fab.SpeedDialMenuItem;

public class MainActivity extends AppCompatActivity {
    public static String TABLE_ACTIVITY_KEY = "tableActivity";
    private final MainActivity context;
    private ScrollMain scrollView;
    private uk.co.markormesher.android_fab.FloatingActionButton addButtonLib;

    private Toolbar toolbar;
    private LinearLayout containerCards;
    private List<File> tableFiles;

    public MainActivity() {
        context = MainActivity.this;
    }

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
        Intent intent = new Intent(context, TableActivity.class);
        intent.putExtra(TABLE_ACTIVITY_KEY, s);
        startActivity(intent);
    }

    @SuppressLint("StaticFieldLeak")
    @Override
    protected void onResume() {
        super.onResume();
        updateCards();
    }

    @SuppressLint("CheckResult")
    public void updateCards() {
        containerCards.removeAllViews();
        tableFiles = TableLab.get(this).getTableFiles();

        for (final File tableFile : tableFiles) {
            // карточка таблицы
            LinearLayout l = (LinearLayout) getLayoutInflater().inflate(R.layout.table_card, null);
            // имя карточки
            TextView nameTable = l.findViewById(R.id.name_card);
            String name = tableFile.getName();
            nameTable.setText(name.substring(0, name.lastIndexOf(".")));
            //клик по карточке
            View.OnClickListener onClick = v -> {
                switch (v.getId()) {
                    case R.id.menu_tablecard_imButton:
                        getPopupMenu(tableFile, v);
                        break;
                    default:
                        startTable(tableFile.getPath());
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
//        columnBuilder.draw(canvasColumn,table);
//
//        imageColumn.setImageBitmap(bitmapColumn);
//    }

    private void getPopupMenu(File tableFile, View view) {
        PopupMenu popupMenu = new PopupMenu(this, view);
        popupMenu.getMenuInflater().inflate(R.menu.table_context_menu, popupMenu.getMenu());
        popupMenu.setOnMenuItemClickListener(menuItem -> {
            switch (menuItem.getItemId()) {
                case R.id.rename:

                    String name = tableFile.getName();
                    DialogNameTable nameTable = DialogNameTable.getDialog(name.substring(0, name.lastIndexOf(".")));
                    DialogNameTable.OnButtonClick buttonClick = (String newName) -> {

                        TableFileHelper.renameFile(tableFile, newName + TableFileHelper.TBL); //  добавляет расширение
                        updateCards();
                    };
                    nameTable.setClickInterface(buttonClick);
                    nameTable.showNow(getSupportFragmentManager(), "name_table");
                    break;
                case R.id.save:
//                    saveTable(name);
                    break;
                case R.id.delete:

                    TableLab.get(context).deleteTableOfDB(tableFile);
                    updateCards();
            }
            return true;
        });
        popupMenu.show();
    }

//    private void saveTable(TableModel table) {
//        Bitmap bitmap = StaticMethods.getScreenTable(table, MainActivity.this);
//        String dir = Environment.getExternalStorageDirectory().getPath();
//        File imageDir = new File(dir + "/tableSave/");
//        imageDir.mkdir();
//        File file = new File(imageDir.getPath() + "table_" + table.getNameTable() + ".png");
//
//        try {
//            FileOutputStream outputStream = new FileOutputStream(file);
//            bitmap.compress(Bitmap.CompressFormat.PNG, 100, outputStream);
//            outputStream.flush();
//            outputStream.close();
//        } catch (FileNotFoundException e) {
//            e.printStackTrace();
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
//    }

    private String addTable(int variation) {

        TableModel tm = new TableModel();
        // имя таблицы (ид таблицы, ид таблицы настроек в конструкторе заданы)
        String nameTable = "";

        String date = StaticMethods.getDateOfMillis(Calendar.getInstance().getTimeInMillis(), tm.getDateType());
        switch (variation) {
            case 0:
                nameTable = getString(R.string.contacts) + date;
                addColumnTableContact(tm);
                break;
            case 1:

                nameTable = getString(R.string.empty_table) + date;
                Column column = new Column();
                column.text = getString(R.string.column) + " " + tm.getColumns().size();
                tm.getColumns().add(column);
        }

        tm.setNameTable(nameTable);
        tm.addNewStroke(-1, -1); //  -1 типа просто добавить
        TableLab.get(this).createTable(tm);

        return tm.openName;
    }

    private void addColumnTableContact(TableModel table) {
        List<Column> columns = table.getColumns();
        Column colFullName = new Column();
        Column colNumberPhone = new Column();
        Column colMail = new Column();
        Column colBusiness = new Column();

        //name
        colFullName.text = getString(R.string.full_name);
        colFullName.width = 350;
        columns.add(colFullName);
        // number
        colNumberPhone.text = getString(R.string.phone);
        colNumberPhone.width = 300;
        colNumberPhone.setInputType(1);
        columns.add(colNumberPhone);
        //mail
        colMail.text = getString(R.string.mail);
        colMail.width = 300;
        columns.add(colMail);
        //business
        colBusiness.text = getString(R.string.business);
        colBusiness.width = 400;
        columns.add(colBusiness);
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
        public SpeedDialMenuItem getMenuItem(@NonNull Context context, int i) {
            ItemInfo itemInfo = itemSettingList.get(i);
            return new SpeedDialMenuItem(context, itemInfo.ic_id, itemInfo.label);
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
        public void onPrepareItemCard(@NonNull Context context, int position, @NonNull View card) {
//            card.setBackgroundColor(ContextCompat.getColor(MainActivity.this, R.color.gray));
            card.setBackgroundResource(R.drawable.contur_oval);
            super.onPrepareItemCard(context, position, card);
        }

        @Override
        public void onPrepareItemIconWrapper(@NonNull Context context, int position, @NonNull LinearLayout label) {
//            label.setBackgroundColor(ContextCompat.getColor(MainActivity.this, R.color.color_normal));
            super.onPrepareItemIconWrapper(context, position, label);
        }

        @Override
        public void onPrepareItemLabel(@NonNull Context context, int position, @NonNull TextView label) {
            int padding = StaticMethods.convertDpToPixels(5, context);
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
