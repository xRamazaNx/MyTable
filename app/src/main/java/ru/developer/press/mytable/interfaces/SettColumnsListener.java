package ru.developer.press.mytable.interfaces;

public abstract class SettColumnsListener {
    public int type;
    public int sizeTitle;
    public int sizeCell;
    public int styleTitle;
    public int styleCell;
    public int colorTitle;
    public int colorCell;

    public abstract void setType(int type);

    public abstract void setTextSizeTitle(int size);
    public abstract void setTextSizeCell(int size);

    public abstract void setStyleTitle(int style);
    public abstract void setStyleCell(int style);

    public abstract void setColorTitle(int color);
    public abstract void setColorCell(int color);

    public abstract void closeWindow();

}
