package ru.developer.press.mytable.model;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;

import android.text.Layout;
import android.text.StaticLayout;
import android.text.TextPaint;

import com.google.gson.annotations.SerializedName;

import ru.developer.press.mytable.helpers.StaticMethods;

public abstract class CellAbstract extends ru.developer.press.mytable.helpers.Coordinate {

    private transient final TextPaint paintText;
    public transient final Paint paintBack;
    private transient StaticLayout staticLayout;

    public transient boolean isTouch;

    public Pref pref;
    @SerializedName("t")
    public String text;


    CellAbstract() {
        paintText = new TextPaint();
        paintText.setAntiAlias(true);
        paintText.setDither(true);

        pref = new Pref();

        paintBack = new Paint();
        paintBack.setColor(Color.WHITE);
        height = 70;
    }

    public void updateCell(float sizeRealFont) {

        paintText.setColor(pref.colorFont);
        paintText.setTextSize(sizeRealFont);
        paintText.setTypeface(StaticMethods.setTypeFace(this));

        paintBack.setColor(pref.colorBack);

        int width = (int) getWidthNoPadding();
        staticLayout = new StaticLayout(text, paintText, width, Layout.Alignment.ALIGN_CENTER, 1, -3, false);

    }

    private float getWidthNoPadding() {
        float widthWithout = width - pref.paddingLeft - pref.paddingRight;
        if (widthWithout < 0)
            widthWithout = 0;
        return widthWithout;
    }

    void copyPrefs(CellAbstract cellAbstract) {
        width = cellAbstract.width;
        height = cellAbstract.height;

        pref.copyPref(cellAbstract.pref);

    }

    public boolean isNotEquals(CellAbstract cellAbstract) {
        return
                height != cellAbstract.height ||
                width != cellAbstract.width ||
                pref.isNoEquals(cellAbstract.pref);
    }

    private void updateStaticLayout(){
        if (staticLayout == null || !staticLayout.getText().toString().equals(text)){

            float width = getWidthNoPadding();
            staticLayout = new StaticLayout(text, paintText, (int) width, Layout.Alignment.ALIGN_CENTER, 1, -3, false);
        }
    }

    public void drawText(Canvas canvas, float sX, float eX, float sY, float eY){
        canvas.save();
        int paddingLeft = pref.paddingLeft;
        int paddingUp = pref.paddingUp;
        int paddingDown = pref.paddingDown;
        canvas.clipRect(sX + paddingLeft,
                sY + paddingUp,
                eX - pref.paddingRight,
                eY - paddingDown);

        int xPos = (int) (sX + paddingLeft);
        int yPos = (int) (sY + paddingUp);

        updateStaticLayout();
        float height = staticLayout.getHeight();
                        // высота ячейки
        float cellHeight = eY - sY - paddingUp - paddingDown;
        if (height < cellHeight) { // если высота текста меньше чем высота ячейки
            yPos = (int) (yPos + (cellHeight / 2) - (height / 2)); // рассчет середины для позиции y
        }

        canvas.translate(xPos, yPos);
        staticLayout.draw(canvas);

        canvas.restore();
    }


    protected abstract int getHeight();

    public float getStaticHeight() {
        updateStaticLayout();
        return staticLayout.getHeight()  + pref.paddingUp + pref.paddingDown;
    }

    protected void select(){
        isTouch = true;
    }
    protected void unSelect(){
        isTouch = false;
    }
}
