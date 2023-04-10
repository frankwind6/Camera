package com.fjk.camera.widget;

import android.content.Context;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.view.MotionEvent;

import androidx.appcompat.widget.AppCompatImageView;
import androidx.core.view.ViewCompat;


public class AppIconView extends AppCompatImageView {

    private static final float PRESSED_SCALE = 0.85f;

    public static final long CLICK_FEEDBACK_DURATION = 200;

    private Drawable mIcon;

    private int mIconSize;

    private ColorDrawable mDefaultDrawable;

    private float mDownX, mDownY;

    public AppIconView(Context context) {
        this(context, null, 0);
    }

    public AppIconView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public AppIconView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    private ColorDrawable getDefaultDrawable() {
        if (null == mDefaultDrawable) {
            mDefaultDrawable = new ColorDrawable();
        }
        return mDefaultDrawable;
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                mDownX = event.getX();
                mDownY = event.getY();
                setBtvAppReduce();
                break;
            case MotionEvent.ACTION_MOVE:
                float x = event.getX();
                float y = event.getY();
                if (Math.abs(y - mDownY) > 10 || Math.abs(x - mDownX) > 10) {
                    setBtvAppRecover();
                }
                break;
            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_CANCEL:
                setBtvAppRecover();
                break;
        }
        return super.onTouchEvent(event);
    }

    public void setBtvAppReduce() {
        ViewCompat.animate(this).cancel();
        ViewCompat.animate(this)
                .scaleX(PRESSED_SCALE)
                .scaleY(PRESSED_SCALE)
                .setDuration(CLICK_FEEDBACK_DURATION)
                .setInterpolator(Interpolators.ACCEL)
                .start();
    }

    public void setBtvAppRecover() {
        ViewCompat.animate(this).cancel();
        ViewCompat.animate(this)
                .scaleX(1)
                .scaleY(1)
                .setDuration(CLICK_FEEDBACK_DURATION)
                .setInterpolator(Interpolators.DEACCEL)
                .start();
    }
}
