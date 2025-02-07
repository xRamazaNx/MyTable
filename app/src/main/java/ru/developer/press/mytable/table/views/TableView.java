package ru.developer.press.mytable.table.views;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Canvas;
import androidx.annotation.Nullable;
import android.util.AttributeSet;
import android.view.GestureDetector;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.PopupWindow;
import android.widget.Scroller;
import android.widget.TextView;

import ru.developer.press.myTable.R;
import ru.developer.press.mytable.helpers.Coordinate;
import ru.developer.press.mytable.helpers.StaticMethods;
import ru.developer.press.mytable.interfaces.table.callback.TableViewListener;

public class TableView extends View {
    public Scroller scroller;
    public float mScaleFactor = 1.f;
    Coordinate coordinateScreen;
    long startDelay = 0;
    private float x, y; // touch coordinate
    private GestureDetector gestureDetector;
    private ScaleGestureDetector mScaleDetector;
    // должен реализовать презентер
    private TableViewListener tableListener;
    private boolean isSelectorWork;
    private TextView scaleInfo;
    private PopupWindow popupWindow;

    public TableView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        scroller = new Scroller(context);
        gestureDetector = new GestureDetector(getContext(), gestureListener());
        mScaleDetector = new ScaleGestureDetector(context, new ScaleListener());
        coordinateScreen = new Coordinate();

        @SuppressLint("InflateParams")
        View view = LayoutInflater.from(getContext()).inflate(R.layout.scale_info, null);
        ImageButton scaleDefault = view.findViewById(R.id.scale_default);
        scaleInfo = view.findViewById(R.id.scale_info);
        scaleDefault.setOnClickListener(v -> {
            mScaleFactor = 1;
            tableListener.scrollToInTable();
            scaleInfo.setText(String.format("%s%%", (int) (100 * mScaleFactor)));
            invalidate();
        });
        popupWindow = StaticMethods.createPopupWindow(view, ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        popupWindow.setFocusable(false);
    }

    public void setTableListener(TableViewListener tableListener) {
        this.tableListener = tableListener;
    }

    public Coordinate getCoordinate() {
        float scrollX = getScrollXScaled();
        float scrollY = getScrollYScaled();
        coordinateScreen.setBounds(scrollX, getWidth() / mScaleFactor + scrollX, scrollY, getHeight() / mScaleFactor + scrollY);
        return coordinateScreen;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        if (tableListener != null) {
            canvas.save();
            canvas.scale(mScaleFactor, mScaleFactor);
            tableListener.draw(canvas, getCoordinate());
            canvas.restore();
        }
    }

    @SuppressLint("ClickableViewAccessibility")
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (tableListener == null)
            return true;

        // почему делим? потому что точка прикосновения должна увеличиваться если таблица уменьшается и наоборот
        x = event.getX() / mScaleFactor;
        y = event.getY() / mScaleFactor;
        if (event.getAction() == MotionEvent.ACTION_UP
                || event.getAction() == MotionEvent.ACTION_CANCEL) {
            tableListener.eventUp();

        }
//        if (!isScaleMode)

        if (event.getPointerCount() >= 2) { //  если нажали 2 или больше пальца то ScaleDetector используем
            mScaleDetector.onTouchEvent(event);
            if (!popupWindow.isShowing()) {
                popupWindow.showAtLocation(TableView.this, Gravity.START | Gravity.TOP, 0, getHeight() / 2);
                scaleInfo.setText(String.format("%s%%", (int) (100 * mScaleFactor)));
            }
        } else {
            if (popupWindow.isShowing())
                hideScaleInfo();
            gestureDetector.onTouchEvent(event);
        }
        return true;
    }

    private GestureDetector.OnGestureListener gestureListener() {

        return new GestureDetector.SimpleOnGestureListener() {
            //            @Override
//            public boolean onDoubleTap(MotionEvent e) {
//                mScaleFactor = 1.F;
//                invalidate();
//                return true;
//            }
            @Override
            public boolean onDown(MotionEvent e) {
                if (!scroller.isFinished()) {
                    scroller.setFinalX(getScrollX());
                    scroller.setFinalY(getScrollY());
                    scroller.abortAnimation();
                } else {
                    isSelectorWork = tableListener.touchCoordinate(x + getScrollXScaled(), y + getScrollYScaled());
                }
                return true;
            }

            @SuppressLint("CheckResult")
            @Override
            public boolean onSingleTapUp(MotionEvent e) {
                tableListener.click(x + getScrollXScaled(), y + getScrollYScaled(), getCoordinate());
                return true;
            }

            public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
                // сскролим все
                if (isSelectorWork)
                    tableListener.moveSelector(TableView.this.x + getScrollXScaled(), TableView.this.y + getScrollYScaled(), getCoordinate());
                else {
                    scroll((int) distanceX, (int) distanceY);
                }

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

    private void scroll(int distanceX, int distanceY) {
        float x = distanceX;
        float y = distanceY;

        float xOff = -getScrollX();// точка отчета координат таблицы
        float yOff = -getScrollY();


        if (xOff <= 0) { // если по горизонтали таблица ушла влево
            float tableWidth = tableListener.getTableWidth() * mScaleFactor;
            float ostatok = tableWidth - -xOff;
            // это остаток таблицы с правой стороны за границой
            float width = getWidth();
            float posleOstatka = ostatok - width;

            if (x > posleOstatka) { // если рывок по х будет больше чем остаток таблицы за гранями
                x = posleOstatka;
            }
            if (x < xOff) x = xOff; // если рывок меньще чем координаты начала таблицы

        } else {
            x = 0;
        }
        // тут аналогично
        if (yOff <= 0) {
            float tableHeight = tableListener.getTableHeight() * mScaleFactor;
            float ostatok = tableHeight - -yOff;
            float height = getHeight();
            float posleOstatka = ostatok - height;

            if (y > posleOstatka) {
                y = posleOstatka;
            }
            if (y < yOff) y = yOff;
        } else y = 0;

        scrollBy((int) x, (int) y);
    }

    public float getScrollXScaled() {
        return getScrollX() / mScaleFactor;
    }

    public float getScrollYScaled() {
        return getScrollY() / mScaleFactor;
    }


    public void fling(float velocityX, float velocityY) {
        int mapWidth = (int) (tableListener.getTableWidth() * mScaleFactor);
        int mapHeight = (int) (tableListener.getTableHeight() * mScaleFactor);

        scroller.fling(getScrollX(), getScrollY(),
                -(int) velocityX, -(int) velocityY,
                0, mapWidth - getWidth(),
                0, mapHeight - getHeight());
        awakenScrollBars(scroller.getDuration());
    }

    @Override
    public void computeScroll() {
        if (scroller.computeScrollOffset()) {
            float oldX = getScrollX();
            float oldY = getScrollY();

            int x = scroller.getCurrX();
            int y = scroller.getCurrY();

            scrollTo(x, y);
            float scrollX = getScrollX();
            float scrollY = getScrollY();

            if (oldX != scrollX || oldY != scrollY) {
                onScrollChanged((int) scrollX, (int) (scrollY), (int) oldX, (int) oldY);
            }

            postInvalidate();
        }
    }

    @Override
    protected int computeHorizontalScrollRange() {
        int horisontal = 0;
        if (tableListener != null)
            horisontal = (int) (tableListener.getTableWidth() * mScaleFactor);
        return horisontal;
    }

    @Override
    protected int computeVerticalScrollRange() {
        int vertical = 0;
        if (tableListener != null)
            vertical = (int) (tableListener.getTableHeight() * mScaleFactor);
        return vertical;
    }

    private void hideScaleInfo() {
        postDelayed(() -> {
            int delay = (int) (System.currentTimeMillis() - startDelay);
            if (delay > 1500)
                popupWindow.dismiss();
        }, 1500);
    }

    private class ScaleListener extends ScaleGestureDetector.SimpleOnScaleGestureListener {

        @Override
        public boolean onScaleBegin(ScaleGestureDetector detector) {
//            popupWindow.showAtLocation(TableView.this, Gravity.START | Gravity.TOP, 0, 0);

            startDelay = System.currentTimeMillis();
            return true;
        }

        @Override
        public void onScaleEnd(ScaleGestureDetector detector) {
            startDelay = System.currentTimeMillis();
            hideScaleInfo();
            super.onScaleEnd(detector);
        }


        @Override
        public boolean onScale(ScaleGestureDetector detector) {
            int focusX = (int) detector.getFocusX();
            int focusY = (int) detector.getFocusY();

            int oldFocusDistanceX = (int) ((getScrollX() + focusX) * mScaleFactor);
            int oldFocusDistanceY = (int) ((getScrollY() + focusY) * mScaleFactor);

            float temp = mScaleFactor * detector.getScaleFactor();
            // Don't let the object get too small or too large.
            temp = Math.max(0.3f, Math.min(temp, 2.3f));

//            if (getWidth() / temp >= tableListener.getWidthTable() && temp < 1)
//                return false;
            mScaleFactor = temp;
            invalidate();

            int newFocusX = (int) ((getScrollX() + focusX) * mScaleFactor);
            int newFocusY = (int) ((getScrollY() + focusY) * mScaleFactor);

            int distanceX = (int) ((newFocusX - oldFocusDistanceX) / mScaleFactor);
            int distanceY = (int) ((newFocusY - oldFocusDistanceY) / mScaleFactor);

            scroll(distanceX, distanceY);
            scaleInfo.setText(String.format("%s%%", (int) (100 * mScaleFactor)));
            popupWindow.update(0, focusY, ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);

            return true;
        }
    }
}
