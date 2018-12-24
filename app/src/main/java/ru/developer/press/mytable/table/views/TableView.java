package ru.developer.press.mytable.table.views;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Canvas;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Scroller;

import ru.developer.press.mytable.helpers.Coordinate;
import ru.developer.press.mytable.interfaces.TableViewListener;

public class TableView extends View {
    private float x, y; // touch coordinate

    public Scroller scroller;
    private GestureDetector gestureDetector;
    // должен реализовать презентер
    private TableViewListener tableListener;
    Coordinate coordinateScreen;
    private boolean isSelectorWork;

    public TableView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        scroller = new Scroller(context);
        gestureDetector = new GestureDetector(getContext(), gestureListener());
        coordinateScreen = new Coordinate();
    }

    public void setTableListener(TableViewListener tableListener) {
        this.tableListener = tableListener;
    }

    private Coordinate getCoordinate() {
        coordinateScreen.setBounds(getScrollX(), getWidth() + getScrollX(), getScrollY(), getHeight() + getScrollY());
        return coordinateScreen;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        if (tableListener != null)
            tableListener.draw(canvas, getCoordinate());
    }

    @SuppressLint("ClickableViewAccessibility")
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        x = event.getX();
        y = event.getY();
        if (event.getAction() == MotionEvent.ACTION_UP
                || event.getAction() == MotionEvent.ACTION_CANCEL) {
            tableListener.eventUp();
        }
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
                } else {
                    isSelectorWork = tableListener.touchCoordinate(x + getScrollX(), y + getScrollY());
                }
                return true;
            }

            @SuppressLint("CheckResult")
            @Override
            public boolean onSingleTapUp(MotionEvent e) {

                tableListener.click(x + getScrollX(), y + getScrollY(), getCoordinate());
                return true;
            }

            public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
                // сскролим все
                if (isSelectorWork)
                    tableListener.moveSelector(TableView.this.x + getScrollX(), TableView.this.y + getScrollY(), getCoordinate());
                else
                    tableListener.scrollBy(distanceX, distanceY);
                return true;

            }

            @Override
            public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
                if (!isSelectorWork)
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
