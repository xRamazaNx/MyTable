package ru.developer.press.mytable.helpers;

public  class Coordinate {
    public float startX = 0;
    public float endX = 0;
    public float startY = 0;
    public float endY = 0;

    public float width = 0;
    public float height = 0;


    public void setBounds(float startX, float endX, float startY, float endY) {

        this.startX = startX;
        this.startY = startY;
        this.endX = endX;
        this.endY = endY;

        height = this.endY - this.startY;
        width = this.endX - this.startX;
    }

    public void setBounds(Coordinate coordinate) {
        this.startX = coordinate.startX;
        this.startY = coordinate.startY;
        this.endX =   coordinate.endX;
        this.endY =   coordinate.endY;

        height = this.endY - this.startY;
        width = this.endX - this.startX;
    }
}
