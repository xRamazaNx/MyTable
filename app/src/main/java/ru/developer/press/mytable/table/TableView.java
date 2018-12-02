package ru.developer.press.mytable.table;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Canvas;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Scroller;

import ru.developer.press.mytable.interfaces.TableViewListener;
import ru.developer.press.mytable.model.CellAbstract;

public class TableView extends View {
    private float x, y; // touch coordinate

    public Scroller scroller;
    private GestureDetector gestureDetector;
    // должен реализовать презентер
    private TableViewListener tableListener;
    CellAbstract coordinateScreen;

    public TableView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        scroller = new Scroller(context);
        gestureDetector = new GestureDetector(getContext(), gestureListener());
        coordinateScreen = new CellAbstract();
    }

    public void setTableListener(TableViewListener tableListener) {
        this.tableListener = tableListener;
    }

    private CellAbstract getCoordinate(){
        coordinateScreen.setBounds(getScrollX(),getWidth()+getScrollX(),getScrollY(), getHeight()+getScrollY());
        return coordinateScreen;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        if (tableListener != null)
            tableListener.drawCells(canvas, getCoordinate());
    }

    @SuppressLint("ClickableViewAccessibility")
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        x = event.getX();
        y = event.getY();

        if (gestureDetector.onTouchEvent(event))
            return true;
        return true;
    }

    private GestureDetector.OnGestureListener gestureListener() {

        return new GestureDetector.SimpleOnGestureListener() {
            @Override
            public boolean onDown(MotionEvent e) {
                if (!scroller.isFinished()) {
                    scroller.setFinalX(getScrollX());
                    scroller.setFinalY(getScrollY());
                    scroller.abortAnimation();
                }
                return true;
            }

            @SuppressLint("CheckResult")
            @Override
            public boolean onSingleTapUp(MotionEvent e) {
                tableListener.cellClick(x + getScrollX(), y + getScrollY());
                return true;
            }

            public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {

                int x = (int) distanceX;
                int y = (int) distanceY;

                int xOff = -getScrollX(), // точка отчета координат таблицы
                        yOff = -getScrollY();

                if (xOff <= 0) { // если по горизонтали таблица ушла влево
                    int mapWidth = tableListener.getTableWidth();
                    int ostatok = mapWidth - -xOff;
                    // это остаток таблицы с правой стороны за границой
                    int posleOstatka = ostatok - getWidth();

                    if (x > posleOstatka) { // если рывок по х будет больше чем остаток таблицы за гранями
                        x = posleOstatka;
                    }
                    if (x < xOff) x = xOff; // если рывок меньще чем координаты начала таблицы

                } else {
                    x = 0;
                }

                // тут аналогично
                if (yOff <= 0) {
                    int mapHeight = tableListener.getTableHeight();
                    int ostatok = mapHeight - -yOff;
                    int posleOstatka = ostatok - getHeight();

                    if (y > posleOstatka) {
                        y = posleOstatka;
                    }
                    if (y < yOff) y = yOff;
                } else y = 0;

                // сскролим все
                tableListener.scrollBy(x, y);
                return true;

            }

            @Override
            public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
                fling(velocityX, velocityY);
                return super.onFling(e1, e2, velocityX, velocityY);
            }
        };
    }


    public void fling(float velocityX, float velocityY) {
        int mapWidth = tableListener.getTableWidth();
        int mapHeight = tableListener.getTableHeight();

        scroller.fling(getScrollX(), getScrollY(),
                -(int) velocityX, -(int) velocityY,
                0, mapWidth - getWidth(),
                0, mapHeight - getHeight());
        awakenScrollBars(scroller.getDuration());
    }

    @Override
    public void computeScroll() {
        if (scroller.computeScrollOffset()) {
            int oldX = getScrollX();
            int oldY = getScrollY();
            int x = scroller.getCurrX();
            int y = scroller.getCurrY();

            tableListener.scrollTo(x, y); // в этом методе прокручиваются заголовки и нумерация
            if (oldX != getScrollX() || oldY != getScrollY()) {
                onScrollChanged(getScrollX(), getScrollY(), oldX, oldY);
//                titleView.onScrollChanged(getScrollX(), getScrollY(), oldX, oldY);
//                headerView.onScrollChanged(getScrollX(), getScrollY(), oldX, oldY);
            }

            postInvalidate();
        }
    }

    @Override
    protected int computeHorizontalScrollRange() {
        return tableListener.getTableWidth();
    }

    @Override
    protected int computeVerticalScrollRange() {
        return tableListener.getTableHeight();
    }
}
