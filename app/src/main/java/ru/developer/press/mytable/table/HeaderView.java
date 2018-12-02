package ru.developer.press.mytable.table;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Canvas;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;

import ru.developer.press.mytable.interfaces.HeaderViewListener;
import ru.developer.press.mytable.model.CellAbstract;


public class HeaderView extends View {

    private HeaderViewListener headerListener;
    private GestureDetector gestureDetector;
    private float y = 0;
    private CellAbstract coordinateDraw;


    public HeaderView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        gestureDetector = new GestureDetector(context, gestureListener());
        coordinateDraw = new CellAbstract();
    }

    public void setHeaderListener(HeaderViewListener headerListener) {
        this.headerListener = headerListener;
    }

    public CellAbstract getCoordinateDraw() {
        coordinateDraw.setBounds(0, 0, getScrollY(), getHeight() + getScrollY());
        return coordinateDraw;
    }

    @SuppressLint("ClickableViewAccessibility")
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        y = event.getY() + getScrollY();

        gestureDetector.onTouchEvent(event);
        return true;
    }

    private GestureDetector.OnGestureListener gestureListener() {
        return new GestureDetector.SimpleOnGestureListener() {
            @Override
            public boolean onSingleTapUp(MotionEvent e) {
                headerListener.headerClick(y);
                return true;
            }
        };

    }

    @Override
    protected void onDraw(Canvas canvas) {
        if (headerListener != null)
            headerListener.drawHeaders(canvas, getCoordinateDraw());
    }

}
