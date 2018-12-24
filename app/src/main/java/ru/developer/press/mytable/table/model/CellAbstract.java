package ru.developer.press.mytable.table.model;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;

import android.text.Layout;
import android.text.StaticLayout;
import android.text.TextPaint;

import ru.developer.press.mytable.helpers.StaticMethods;

public abstract class CellAbstract extends ru.developer.press.mytable.helpers.Coordinate {

    protected final TextPaint paintText;
    public final Paint paintBack;
    public boolean isTouch;

    public String text;
    public float sizeFont;
    public int colorFont;
    public int colorBack;
    public int bold;
    public int italic;

    public int leftPadding;
    public int upPadding;
    public int rightPadding;
    public int downPadding;
    protected StaticLayout staticLayout;

    public CellAbstract() {
        paintText = new TextPaint();
        paintText.setAntiAlias(true);

        paintBack = new Paint();
        sizeFont = 14;
        height = 70;
        colorFont = Color.parseColor("#181818");
        colorBack = Color.parseColor("#f1f1f1");

        leftPadding = 8;
        upPadding = 8;
        rightPadding = 8;
        downPadding = 8;

    }

    public void updateCell(float sizeRealFont) {

        paintText.setColor(colorFont);
        paintText.setTextSize(sizeRealFont);
        paintText.setTypeface(StaticMethods.setTypeFace(this));

        paintBack.setColor(colorBack);

        int width = (int) getWidthNoPadding();
        staticLayout = new StaticLayout(text, paintText, width, Layout.Alignment.ALIGN_CENTER, 1, -3, false);

    }

    private float getWidthNoPadding() {
        return width - leftPadding - rightPadding;
    }

    public void copyPrefs(CellAbstract cellAbstract) {
        width = cellAbstract.width;
        height = cellAbstract.height;

        this.sizeFont = cellAbstract.sizeFont;
        this.colorFont = cellAbstract.colorFont;
        this.colorBack = cellAbstract.colorBack;
        this.bold = cellAbstract.bold;
        this.italic = cellAbstract.italic;

        leftPadding = cellAbstract.leftPadding;
        upPadding = cellAbstract.upPadding;
        rightPadding = cellAbstract.rightPadding;
        downPadding = cellAbstract.downPadding;

    }

    public boolean isNotEquals(CellAbstract cellAbstract) {
        return sizeFont != cellAbstract.sizeFont ||
                colorFont != cellAbstract.colorFont ||
                colorBack != cellAbstract.colorBack ||
                bold != cellAbstract.bold ||
                italic != cellAbstract.italic ||
                height != cellAbstract.height ||
                width != cellAbstract.width ||
                leftPadding != cellAbstract.leftPadding ||
                upPadding != cellAbstract.upPadding ||
                rightPadding != cellAbstract.rightPadding ||
                downPadding != cellAbstract.downPadding;
    }

    protected void updateStaticLayout(){
        if (staticLayout == null){

            float width = getWidthNoPadding();
            staticLayout = new StaticLayout(text, paintText, (int) width, Layout.Alignment.ALIGN_CENTER, 1, -3, false);
        }
    }

    public void drawText(Canvas canvas, int sX, int eX, int sY, int eY){
        canvas.save();
        canvas.clipRect(sX, sY, eX, eY);

        int xPos = sX + leftPadding;
        int yPos = sY + upPadding;

//        updateStaticLayout();

        float height = staticLayout.getHeight();

        float cellHeight = getHeight() - upPadding - downPadding;
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
        return staticLayout.getHeight()  + upPadding + downPadding;
    }

    protected void select(){
        isTouch = true;
    }
    protected void unSelect(){
        isTouch = false;
    }
}
