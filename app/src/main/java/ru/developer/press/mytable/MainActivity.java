package ru.developer.press.mytable;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.Toast;

import org.apache.poi.ss.usermodel.Workbook;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import ru.developer.press.myTable.R;
import ru.developer.press.mytable.dialogs.DialogNameTable;
import ru.developer.press.mytable.dialogs.SendTableDialog;
import ru.developer.press.mytable.helpers.AppEvents;
import ru.developer.press.mytable.helpers.StaticMethods;
import ru.developer.press.mytable.helpers.TableFileHelper;
import ru.developer.press.mytable.helpers.TableLab;
import ru.developer.press.mytable.interfaces.SendTableGetter;
import ru.developer.press.mytable.model.Column;
import ru.developer.press.mytable.model.TableModel;
import uk.co.markormesher.android_fab.SpeedDialMenuAdapter;
import uk.co.markormesher.android_fab.SpeedDialMenuItem;

//import com.squareup.picasso.Transformation;

public class MainActivity extends AppCompatActivity {
    public static String TABLE_ACTIVITY_KEY = "tableActivity";
    private final MainActivity context;
    private uk.co.markormesher.android_fab.FloatingActionButton addButton;

    private LinearLayout containerCards;

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

        addButton = findViewById(R.id.floatingActionButton);
        setFButtonSetting();
        ScrollMain scrollView = findViewById(R.id.scroll_view_main);

        scrollView.setAddButton(addButton);
        Toolbar toolbar = findViewById(R.id.toolbar);
//        toolbar.setTitle(getString(R.string.Active));
        setSupportActionBar(toolbar);

        containerCards = findViewById(R.id.container_cards);

        // реклама будет
//        MobileAds.initialize(this, "ca-app-pub-3940256099942544~3347511713");
//        adView = findViewById(R.id.adView);
//        AdRequest adRequest = new AdRequest.Builder().build();
//        adView.loadAd(adRequest);

        addButton.setOnSpeedDialMenuOpenListener(floatingActionButton -> floatingActionButton.getCardView().animate().rotation(45));
        addButton.setOnSpeedDialMenuCloseListener(floatingActionButton -> floatingActionButton.getCardView().animate().rotation(0));

    }

    private void setFButtonSetting() {
        MenuFAB menuFAB = new MenuFAB();
        addButton.setSpeedDialMenuAdapter(menuFAB);
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

    @Override
    protected void onResume() {
        super.onResume();
        if (permissionCheck())
            updateCards();
//            containerCards.postDelayed(this::updateCards, 500);
    }

    public void updateCards() {
        containerCards.removeAllViews();
        List<File> tableFiles = TableLab.get(this).getTableFiles();
        AppEvents appEvents = AppEvents.get();
        String openName = appEvents.getFileName();
        for (final File tableFile : tableFiles) {
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
            // карточка таблицы
            LinearLayout l = (LinearLayout) getLayoutInflater().inflate(R.layout.table_card, null);
            // имя карточки
            TextView nameTable = l.findViewById(R.id.name_card);
            if (appEvents.isSaveStart() && tableFile.getPath().equals(openName)) {
                nameTable.setTextColor(Color.LTGRAY);
                appEvents.setSaveEventListener(() -> {
                    nameTable.setTextColor(Color.BLACK);
                    l.setOnClickListener(onClick);
                    l.findViewById(R.id.menu_tablecard_imButton).setOnClickListener(onClick);// скролвиев обрабатывает только свои дочерние элементы, пришлось и на его лайот навешать.
                    AppEvents.destroy();
//                    updateCards();
                });
            } else {
                l.setOnClickListener(onClick);
                l.findViewById(R.id.menu_tablecard_imButton).setOnClickListener(onClick);// скролвиев обрабатывает только свои дочерние элементы, пришлось и на его лайот навешать.
            }
            String name = tableFile.getName();
            nameTable.setText(name.substring(0, name.lastIndexOf(".")));
            //клик по карточке

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
            String fileName = tableFile.getName();
            final String tableName = fileName.substring(0, fileName.lastIndexOf("."));
            switch (menuItem.getItemId()) {
                case R.id.rename:

                    DialogNameTable.OnButtonClick buttonClick = (String newName) -> {
                        String newNameTableFile = newName + TableFileHelper.TBL;
                        TableFileHelper.renameFile(tableFile, newNameTableFile); //  добавляет расширение
                        updateCards();
                    };
                    DialogNameTable nameTable = DialogNameTable.getDialog(tableName, buttonClick);
                    nameTable.showNow(getSupportFragmentManager(), "name_table");
                    break;
                case R.id.send:
                    MainActivity context = MainActivity.this;

                    SendTableGetter sendTableGetter = new SendTableGetter() {
                        @Override
                        public File getTableSource() {
                            return tableFile;
                        }

                        @Override
                        public File getTablePicture() {
                            File pic = new File(TableFileHelper.getTableFolder(tableFile.getPath()) + tableName.substring(tableName.length() - 4) + ".png");

                            try {
                                try (FileOutputStream fos = new FileOutputStream(pic)) {
                                    Bitmap bitmap = StaticMethods.getScreenTable(TableLab.get(context).getTableForFile(tableFile.getPath()), context);
                                    bitmap.compress(Bitmap.CompressFormat.JPEG, 97, fos);
                                }
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                            return pic;
                        }

                        @Override
                        public File getTableExcel() {
                            File excel = new File(TableFileHelper.getTableFolder(tableFile.getPath()) + tableName.substring(0, tableName.length() - 4) + ".xls");
                            Workbook workbook = StaticMethods.getTableExcel(TableLab.get(context).getTableForFile(tableFile.getPath()));
                            try {
                                workbook.write(new FileOutputStream(excel));
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                            return excel;
                        }
                    };
                    SendTableDialog sendTableDialog = SendTableDialog.get(sendTableGetter);
                    sendTableDialog.show(getSupportFragmentManager(), "send");
                    break;
                case R.id.delete:

                    TableLab.get(this.context).deleteTable(tableFile);
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

    private TableModel addTable(int variation) {

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
                tm.addNewColumn(column);
        }

        tm.setNameTable(nameTable);
        tm.addStroke(-1, -1); //  -1 типа просто добавить


        return tm;
    }

    private void addColumnTableContact(TableModel table) {
        Column colFullName = new Column();
        Column colNumberPhone = new Column();
        Column colMail = new Column();
        Column colBusiness = new Column();

        //name
        colFullName.text = getString(R.string.full_name);
        colFullName.setWidth(350);
        table.addNewColumn(colFullName);
        // number
        colNumberPhone.text = getString(R.string.phone);
        colNumberPhone.setWidth(300);
        colNumberPhone.setInputType(1);
        table.addNewColumn(colNumberPhone);
        //mail
        colMail.text = getString(R.string.mail);
        colMail.setWidth(300);
        table.addNewColumn(colMail);
        //business
        colBusiness.text = getString(R.string.business);
        colBusiness.setWidth(400);
        table.addNewColumn(colBusiness);
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

    private boolean permissionCheck() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            int perm = checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE);
            if (perm != PackageManager.PERMISSION_GRANTED) {
                if (shouldShowRequestPermissionRationale(Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
                    Toast.makeText(getApplicationContext(), "Включите разрешение памяти в настройках приложения"
                            , Toast.LENGTH_SHORT).show();
                    return false;
                } else {
                    requestPermissions(new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 777);
                    return false;
                }
            }
        }
        return true;
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

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == 777) {
            if (grantResults.length == 0 || grantResults[0] != PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(getApplicationContext(), "Отказано в доступе!", Toast.LENGTH_SHORT).show();
            } else {
                for (int grantResult : grantResults) {
                    if (grantResult == PackageManager.PERMISSION_GRANTED) {
                        Toast.makeText(getApplicationContext(), "Доступ разрешен!", Toast.LENGTH_SHORT).show();
                    }
                }
            }
        }
    }

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
            new Handler().postDelayed(() -> {
                if (permissionCheck()) {
                    TableModel tableModel = addTable(position);
                    DialogNameTable.OnButtonClick buttonClick = (String newName) -> {

                        tableModel.setNameTable(newName);
                        TableLab.get(MainActivity.this).createTable(tableModel);

                        String newNameTableFile = newName + TableFileHelper.TBL;
                        tableModel.openName = TableFileHelper.renameFile(new File(tableModel.openName), newNameTableFile); //  добавляет расширение

                        updateCards();
                        startTable(tableModel.openName);
                    };
                    DialogNameTable nameTable = DialogNameTable.getDialog(tableModel.getNameTable(), buttonClick);
                    nameTable.showNow(getSupportFragmentManager(), "name_table");
                }
            }, 150);

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
}
