package ru.developer.press.mytable.helpers;

import java.io.File;
import java.io.FilenameFilter;
import java.util.ArrayList;

import android.os.Bundle;
import android.os.Environment;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import ru.developer.press.myTable.R;

public class FileExplore extends AppCompatActivity {
    private RecyclerView recyclerView;
    RecyclerView.Adapter adapter;

    // Stores names of traversed directories
    ArrayList<String> str = new ArrayList<>();

    // Check if the first level of the directory structure is the one showing
    private Boolean firstLvl = true;

    private static final String TAG = "F_PATH";

    private Item[] fileList;
    private File path = new File(Environment.getExternalStorageDirectory().getPath()+"/MyTables");
    private String chosenFile;
    private static final int DIALOG_LOAD_FILE = 1000;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_explorer);
        recyclerView = findViewById(R.id.recycler_files);
        recyclerView.setLayoutManager(new GridLayoutManager(this, 3));
        loadFileList();
        showFiles();
    }

    private void loadFileList() {
        try {
            path.mkdirs();
        } catch (SecurityException e) {
            Log.e(TAG, "unable to write on the sd card ");
        }

        // Checks whether path exists
        if (path.exists()) {
            FilenameFilter filter = (dir, filename) -> {
                File sel = new File(dir, filename);
                // Filters based on whether the file is hidden or not
                String extension = getFileExtension(sel);
                boolean isExel = extension.equals("xls") || extension.equals("xlsx");
                Log.d(TAG, "loadFileList: " + isExel+"  "+extension);
                return (sel.isFile() && isExel) || sel.isDirectory()
                        && !sel.isHidden();

            };

            String[] fList = path.list(filter);
            fileList = new Item[fList.length];
            for (int i = 0; i < fList.length; i++) {
                fileList[i] = new Item(fList[i], R.mipmap.ic_exel);

                // Convert into file path
                File sel = new File(path, fList[i]);

                // Set drawables
                if (sel.isDirectory()) {
                    fileList[i].icon = R.mipmap.ic_directory;
                    Log.d("DIRECTORY", "" + fileList.length);
                } else {

                }
            }

            if (!firstLvl) {
                Item temp[] = new Item[fileList.length + 1];
                System.arraycopy(fileList, 0, temp, 1, fileList.length);
                temp[0] = new Item("Up", R.drawable.directory_up);
                fileList = temp;
            }
        } else {
            Log.e(TAG, "path does not exist");
        }

        adapter = new FilesAdapter();

    }

    //метод определения расширения файла
    private static String getFileExtension(File file) {
        String fileName = file.getName();
        // если в имени файла есть точка и она не является первым символом в названии файла
        if (fileName.lastIndexOf(".") != -1 && fileName.lastIndexOf(".") != 0)
            // то вырезаем все знаки после последней точки в названии файла, то есть ХХХХХ.txt -> txt
            return fileName.substring(fileName.lastIndexOf(".") + 1);
            // в противном случае возвращаем заглушку, то есть расширение не найдено
        else return "";
    }

    private class Item {
        public String file;
        public int icon;

        public Item(String file, Integer icon) {
            this.file = file;
            this.icon = icon;
        }

        @Override
        public String toString() {
            return file;
        }
    }

    protected void showFiles() {
        if (fileList == null) {
            path = new File(Environment.getExternalStorageDirectory().getPath());
            loadFileList();
            showFiles();
            Log.e(TAG, "No files loaded");
        } else {
            Log.d(TAG, "showFiles: ");
            recyclerView.setAdapter(adapter);
        }
    }


    private class FilesAdapter extends RecyclerView.Adapter<FileHolder> {

        @NonNull
        @Override
        public FileHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
            LinearLayout view = (LinearLayout) LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.table_file_card, null);
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,ViewGroup.LayoutParams.WRAP_CONTENT);
            view.setLayoutParams(params);
            view.setGravity(Gravity.CENTER);
            view.setPadding(20,20,20,20);

            return new FileHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull FileHolder fileHolder, int i) {
            fileHolder.bind(i);
        }

        @Override
        public int getItemCount() {
            return fileList.length;
        }
    }

    class FileHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        private final TextView name;
        private ImageButton icon;

        FileHolder(@NonNull View itemView) {
            super(itemView);
            icon = itemView.findViewById(R.id.icon);
            name = itemView.findViewById(R.id.name_file);
            itemView.setOnClickListener(this);
            icon.setOnClickListener(this);
            name.setOnClickListener(this);
        }

        void bind(int i) {
            name.setText(new File(fileList[i].file).getName());
            icon.setImageResource(fileList[i].icon);
        }

        @Override
        public void onClick(View v) {
            chosenFile = fileList[getAdapterPosition()].file;
            File sel = new File(path + "/" + chosenFile);
            if (sel.isDirectory()) {
                firstLvl = false;

                // Adds chosen directory to list
                str.add(chosenFile);
                fileList = null;
                path = new File(sel + "");

                loadFileList();
                showFiles();
            }

            // это вообще про нажатие кнопки вверх
//                // Checks if 'up' was clicked
//                else if (chosenFile.equalsIgnoreCase("up") && !sel.exists()) {
//
//                    // present directory removed from list
//                    String s = str.remove(str.size() - 1);
//
//                    // path modified to exclude present directory
//                    path = new File(path.toString().substring(0,
//                            path.toString().lastIndexOf(s)));
//                    fileList = null;
//
//                    // if there are no more directories in the list, then
//                    // its the first level
//                    if (str.isEmpty()) {
//                        firstLvl = true;
//                    }
//                    loadFileList();
//
//                }
            // File picked
            else {
                Toast.makeText(FileExplore.this, "ураааааааааа", Toast.LENGTH_SHORT).show();
                // Perform action with file picked
            }

        }
    }

}