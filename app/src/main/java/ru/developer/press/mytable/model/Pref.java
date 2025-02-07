package ru.developer.press.mytable.model;

import android.graphics.Color;

import com.google.gson.annotations.SerializedName;

public class Pref {
    @SerializedName("sf")
    public int sizeFont;
    @SerializedName("cf")
    public int colorFont;
    @SerializedName("cb")
    public int colorBack;
    @SerializedName("b")
    public int bold;
    @SerializedName("i")
    public int italic;

    @SerializedName("pl")
    public int paddingLeft;
    @SerializedName("pu")
    public int paddingUp;
    @SerializedName("pr")
    public int paddingRight;
    @SerializedName("pd")
    public int paddingDown;

    public Pref() {
        sizeFont = 14;
        colorFont = Color.parseColor("#181818");
        colorBack = Color.parseColor("#f1f1f1");

        paddingLeft = 4;
        paddingUp = 4;
        paddingRight = 4;
        paddingDown = 4;
    }

    public void copyPref(Pref pref) {
        this.sizeFont = pref.sizeFont;
        this.colorFont = pref.colorFont;
        this.colorBack = pref.colorBack;
        this.bold = pref.bold;
        this.italic = pref.italic;

        paddingLeft = pref.paddingLeft;
        paddingUp = pref.paddingUp;
        paddingRight = pref.paddingRight;
        paddingDown = pref.paddingDown;
    }

    public boolean isNoEquals(Pref pref) {
        return sizeFont != pref.sizeFont ||
                colorFont != pref.colorFont ||
                colorBack != pref.colorBack ||
                bold != pref.bold ||
                italic != pref.italic ||
                paddingLeft != pref.paddingLeft ||
                paddingUp != pref.paddingUp ||
                paddingRight != pref.paddingRight ||
                paddingDown != pref.paddingDown;
    }
}
