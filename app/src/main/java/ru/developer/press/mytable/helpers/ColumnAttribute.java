package ru.developer.press.mytable.helpers;

import com.google.gson.annotations.SerializedName;

// аттрибуты колон для формул
public class ColumnAttribute {
    // такой бывает ид если это не колона а customVal
    public static final String VALUE = "value";
    @SerializedName("n")
    private String name;
    @SerializedName("nID")
    private final String nameId;
    @SerializedName("t")
    private final int type;


    public ColumnAttribute(String name, String nameId, int type) {
        this.name = name;
        this.nameId = nameId;
        this.type = type;
    }

    public ColumnAttribute() {
        type = -1;
        nameId = VALUE;
        name = "";
    }

    ColumnAttribute(ColumnAttribute columnAttr) {
        name = columnAttr.name;
        nameId = columnAttr.nameId;
        type = columnAttr.type;
    }

    public String getName() {
        return name;
    }

    public String getNameId() {
        return nameId;
    }

    public int getType() {
        return type;
    }

    public void setName(String string) {
        name = string;
    }
}
