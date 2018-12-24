package ru.developer.press.mytable.helpers.setting_table;

public class MigrationHelper {
    public static final int VERSION = 0;
    private int openTableVersion = 0;

    public MigrationHelper (int openTableVersion){
        this.openTableVersion = openTableVersion;
    }

}
