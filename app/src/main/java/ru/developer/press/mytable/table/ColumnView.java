package ru.developer.press.mytable.table;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Canvas;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;

import ru.developer.press.mytable.interfaces.ColumnViewListener;
import ru.developer.press.mytable.model.CellAbstract;

public class ColumnView extends View {
    private ColumnViewListener columnViewListener;
    private GestureDetector gestureDetector;
    private float x = 0;
    private CellAbstract coordinateDraw;

    public ColumnView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        gestureDetector = new GestureDetector(context, gestureListener());
        coordinateDraw = new CellAbstract();
    }


    public void setColumnViewListener(ColumnViewListener columnViewListener) {
        this.columnViewListener = columnViewListener;
    }

    public CellAbstract getCoordinateDraw() {
        coordinateDraw.setBounds(getScrollX(),getWidth()+getScrollX(),0, 0);
        return coordinateDraw;
    }

    @SuppressLint("ClickableViewAccessibility")
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        x = event.getX() + getScrollX();

        gestureDetector.onTouchEvent(event);
        return true;
    }

    private GestureDetector.OnGestureListener gestureListener() {
        return new GestureDetector.SimpleOnGestureListener() {

            @Override
            public boolean onSingleTapUp(MotionEvent e) {
                columnViewListener.columnClick(x);
                return true;
            }
        };
    }

    @Override
    protected void onDraw(Canvas canvas) {
        if (columnViewListener != null)
            columnViewListener.drawColumns(canvas, getCoordinateDraw());
    }

}
