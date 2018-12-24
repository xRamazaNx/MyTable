package ru.developer.press.mytable.helpers.setting_table;

import android.content.Context;
import android.os.Environment;
import android.util.Log;

import com.google.gson.Gson;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.Writer;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

import ru.developer.press.mytable.table.model.TableModel;
import ru.developer.press.mytable.table.model.TablePrefs;

public class TableFileHelper {
    public static final String SETTING_TABLE_JSON = "setting_table.json";
    public static final String TABLE_DB = "table.db";
    public static final String TBL = ".tbl";
    private String mMyTableFolder;
    private String mCacheFolder;
    //    private String mSharedPrefFolder;
    private File mFileList;

    public static void renameFile(File tableFile, String newName) { // имя должно быть с расширением
        String path = tableFile.getPath();
        String folder = path.substring(0, path.lastIndexOf("/") + 1);
        File renameFile = new File(folder + newName);
        tableFile.renameTo(renameFile);
    }

    public String getmMyTableFolder() {
        return mMyTableFolder;
    }

    public String getmCacheFolder() {
        return mCacheFolder;
    }

//    public String getmSharedPrefFolder() {
//        return mSharedPrefFolder;
//    }

    public TableFileHelper(Context context) {
        String s =
                context.getFilesDir().getPath();
        mCacheFolder = s.substring(0, s.lastIndexOf("/") + 1) + "cache/";
//        mSharedPrefFolder = s.substring(0, s.lastIndexOf("/") + 1) + "shared_prefs/";
        // на всякий пожарный если нет папки то создаем
        new File(mCacheFolder).mkdir();
        // путь папки в телефоне
        mMyTableFolder = Environment.getExternalStoragePublicDirectory("MyTable").getPath() + "/";
        // лист файлов таблиц
        mFileList = new File(mMyTableFolder);
        mFileList.mkdir();
    }

    // этот процесс должен контролировать лаб
    public void saveTable(String fullName) {

//        String nameTable = table.getNameTable();
//        String nameIdTable = table.getNameId();
        File tableFile = new File(fullName);
//        File versionFile = new File(mCacheFolder + VERSION_NAME_FILE);
        try {
            // create files of table
            FileOutputStream outputStreamTable = new FileOutputStream(tableFile);
//            FileOutputStream outputStreamVersion = new FileOutputStream(versionFile);
//            // create obj file
//            ObjectOutputStream tableObjOutput = new ObjectOutputStream(outputStreamTable);
//            ObjectOutputStream versionObjOutput = new ObjectOutputStream(outputStreamVersion);
//            // write objects to files
//            tableObjOutput.writeObject(table);
//            versionObjOutput.writeObject(MigrationHelper.VERSION);
//
//            // delete old tableFile
//            File oldTableFile = new File(mMyTableFolder + nameTable);
//            oldTableFile.delete();
//
//            // zipping all created files
//            FileOutputStream outputTableFile = new FileOutputStream(new File(mMyTableFolder + nameTable));
            ZipOutputStream zipOutputStream = new ZipOutputStream(outputStreamTable);
//
            ZipEntry tableZipEntry = new ZipEntry(TABLE_DB);
            ZipEntry prefsZipEntry = new ZipEntry(SETTING_TABLE_JSON);
//
            zipOutputStream.putNextEntry(tableZipEntry);
            writeEntry(mCacheFolder + TABLE_DB, zipOutputStream);
            zipOutputStream.closeEntry();

            zipOutputStream.putNextEntry(prefsZipEntry);
            writeEntry(mCacheFolder + SETTING_TABLE_JSON, zipOutputStream);
            zipOutputStream.closeEntry();
//
//            // close all
            zipOutputStream.close();
            outputStreamTable.close();
//            outputStreamVersion.close();
//            outputTableFile.close();
//            tableObjOutput.close();
//            versionObjOutput.close();


        } catch (IOException e) {
            e.printStackTrace();
            Log.d("ошибка", "saveTable: ");
        }

    }

    private void writeEntry(String inputFileName, ZipOutputStream zipOutputStream) throws IOException {
        FileInputStream inputStream = new FileInputStream(inputFileName);
        byte[] buffer = new byte[1024];
        int len;
        while ((len = inputStream.read(buffer)) > 0) {
            zipOutputStream.write(buffer, 0, len);
        }
    }

    // наверно надо выполнять при закрытии активити табл в презентере
    public void deleteCacheFiles() {
//        File table = new File(mCacheFolder + TABLE_DB);
//        File setting = new File(mCacheFolder + SETTING_TABLE_JSON + JSON);
//        File setting = new File(mSharedPrefFolder + SETTING_TABLE_JSON + JSON);
//        if (table.exists())
//            table.delete();
//        if (setting.exists())
//            setting.delete();
        File cache = new File(mCacheFolder);
        for (File file :
                cache.listFiles()) {
            file.delete();
        }
    }

    private void unzipEntry(ZipEntry zipEntry, InputStream zipInput) {
        String nameFile = zipEntry.getName();
        File file = new File(mCacheFolder + nameFile);

        try {
            FileOutputStream out = new FileOutputStream(file);
            byte[] buffer = new byte[1024];
            int len;
            while ((len = zipInput.read(buffer)) > 0) {
                out.write(buffer, 0, len);
            }
            out.close();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public List<File> getTableFiles() {
        List<File> files = new ArrayList<>();

        if (mFileList.listFiles() != null && mFileList.listFiles().length > 0)
            for (File file : mFileList.listFiles()) {
                String name = file.getName();
                String type = name.substring(name.lastIndexOf(".") + 1);
                if (type.equals("tbl"))
                    files.add(file);
            }
        return files;
    }

    public void openTableFile(File file) {
        // вверху проверка на то есть ли файл с таким именем
        deleteCacheFiles();
        if (file != null)
            try {
                FileInputStream inputTableFile = new FileInputStream(file);
                ZipInputStream zipInputStream = new ZipInputStream(inputTableFile);
                ZipEntry zipEntry;
                while ((zipEntry = zipInputStream.getNextEntry()) != null) {
                    unzipEntry(zipEntry, zipInputStream);
                    zipInputStream.closeEntry();
                }
                inputTableFile.close();
                zipInputStream.close();

            } catch (IOException e) {
                e.printStackTrace();
            }
    }

    public void createOrUpdatePrefs(TableModel tm) {
        TablePrefs tablePrefs = tm.getPrefs();

        Gson gson = new Gson();

        File file = new File(getmCacheFolder() + SETTING_TABLE_JSON);
        if (file.exists()) file.delete();
        try {

            Writer output = new BufferedWriter(new FileWriter(file));
            gson.toJson(tablePrefs, TablePrefs.class, output);
            output.close();

        } catch (Exception ignored) {
        }

    }

    public TableModel getTableFromCache() {
        Gson gson = new Gson();

        TablePrefs tablePrefs = null;
        try (FileInputStream inputStream = new FileInputStream(mCacheFolder + SETTING_TABLE_JSON)) {
            String jsonString = readStream(inputStream);
            tablePrefs = gson.fromJson(jsonString, TablePrefs.class);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        assert tablePrefs != null;
        return new TableModel(tablePrefs);
    }

    public static String readStream(InputStream is) {
        StringBuilder sb = new StringBuilder(512);
        try {
            Reader r = new InputStreamReader(is, "UTF-8");
            int c;
            while ((c = r.read()) != -1) {
                sb.append((char) c);
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return sb.toString();
    }

}
