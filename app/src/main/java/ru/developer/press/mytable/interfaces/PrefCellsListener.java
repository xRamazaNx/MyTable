package ru.developer.press.mytable.interfaces;

public abstract class PrefCellsListener {
    public int type;
    public String function;
    public int sizeText;
    public int bold;
    public int italic;
    public int colorText;
    public int colorBack;
    public boolean isCellsEdit = true;
    public boolean isCellsEditOnly;
    public boolean isHeaderEdit;

    public abstract void setType(int type);

    public abstract void setFunction(String function);

    public abstract void setSizeText(int size);

    public abstract void setBold(int bold);

    public abstract void setItalic(int italic);

    public abstract void setColorText(int color);

    public abstract void setColorBack(int color);

    public abstract void closeWindow();

}
