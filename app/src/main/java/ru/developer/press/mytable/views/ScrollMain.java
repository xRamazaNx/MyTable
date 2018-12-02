package ru.developer.press.mytable.views;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.ScrollView;

import uk.co.markormesher.android_fab.FloatingActionButton;

public class ScrollMain extends ScrollView {
    uk.co.markormesher.android_fab.FloatingActionButton addButton;
    public ScrollMain(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public void setAddButton(FloatingActionButton addButton) {
        this.addButton = addButton;
    }

    @Override
    public void scrollTo(int x, int y) {
        super.scrollTo(x, y);

    }

    @Override
    protected void onScrollChanged(int l, int t, int oldl, int oldt) {
        super.onScrollChanged(l, t, oldl, oldt);
        if (t-oldt > 0 && getScrollY() > 10 && addButton.isShown()){
            addButton.hide(false);
        } else if (t-oldt < 0 && !addButton.isShown() ){
            addButton.show();
        }
    }
}
