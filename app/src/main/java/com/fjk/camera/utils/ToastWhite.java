package com.fjk.camera.utils;
//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by FernFlower decompiler)
//

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.Resources;
import android.os.Handler;
import android.os.Looper;
import android.os.Build.VERSION;
import android.text.method.LinkMovementMethod;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.IdRes;

import com.fjk.camera.R;

import java.lang.ref.WeakReference;
import java.lang.reflect.Method;

/**
 * 功能，toast出现之后点击外部，toast消失，背景被白色
 * 内容：图标 + 文字
 */
public class ToastWhite {
    private static final long TOAST_DURATION = 3000L;
    private static final Handler sHandler = new Handler(Looper.getMainLooper());
    @SuppressLint({"StaticFieldLeak"})
    private static WindowManagerToast sWindowManagerToast;
    private static Context mApplicationContext;

    public ToastWhite() {
    }

    public static void init(Context context) {
        mApplicationContext = context;
    }

    private static View getToastView(Context context, CharSequence txt) {
        View mToastView = LayoutInflater.from(context.getApplicationContext()).inflate(R.layout.white_toast, (ViewGroup) null);
        TextView toastTv = (TextView) mToastView.findViewById(R.id.tv_content);
        toastTv.setText(txt);
        toastTv.setMovementMethod(LinkMovementMethod.getInstance());
        toastTv.setHighlightColor(context.getResources().getColor(R.color.universal_transparent, (Resources.Theme) null));
        return mToastView;
    }

    private static View getToastView(Context context, CharSequence txt, int imgId) {
        View mToastView = LayoutInflater.from(context.getApplicationContext()).inflate(R.layout.white_toast, (ViewGroup) null);
        TextView toastTv = (TextView) mToastView.findViewById(R.id.tv_content);
        toastTv.setText(txt);
        toastTv.setMovementMethod(LinkMovementMethod.getInstance());
        toastTv.setHighlightColor(context.getResources().getColor(R.color.universal_transparent, (Resources.Theme) null));
        return mToastView;
    }

    private static void showToast(String text) {
        dismissToast();
        if (sWindowManagerToast == null) {
            sWindowManagerToast = new WindowManagerToast(mApplicationContext);
        }

        sWindowManagerToast.show(getToastView(mApplicationContext, text), 3000L);
    }

    private static void showToast(String text, @IdRes int imgId) {
        dismissToast();
        if (sWindowManagerToast == null) {
            sWindowManagerToast = new WindowManagerToast(mApplicationContext);
        }

        sWindowManagerToast.show(getToastView(mApplicationContext, text, imgId), 3000L);
    }

    private static void showToast(CharSequence text) {
        dismissToast();
        if (sWindowManagerToast == null) {
            sWindowManagerToast = new WindowManagerToast(mApplicationContext);
        }

        sWindowManagerToast.show(getToastView(mApplicationContext, text), 3000L);
    }

    public static void dismissToast() {
        if (sWindowManagerToast != null) {
            sWindowManagerToast.dismiss();
        }

    }

    public static void toast(Context context, int msg, int imgId) {
        showToast(context.getString(msg), imgId);
    }

    public static void toast(Context context, int msg) {
        showToast(context.getString(msg));
    }

    public static void toast(String msg) {
        showToast(msg);
    }

    public static void toast(CharSequence msg) {
        showToast(msg);
    }

    static final class WindowManagerToast {
        private final Context mContext;
        private final WindowManager mWindowManager;
        private final WindowManager.LayoutParams mParams;
        private WeakReference<View> mViewRef;
        private final Runnable mDismissTask = this::dismiss;
        private int mToastAnim;

        WindowManagerToast(Context context) {
            this.mWindowManager = (WindowManager) context.getSystemService("window");
            this.mContext = context;

            try {
                Toast toast = new Toast(context);
                toast.setView(new View(context));
                Method method = toast.getClass().getDeclaredMethod("getWindowParams");
                method.setAccessible(true);
                WindowManager.LayoutParams params = (WindowManager.LayoutParams) method.invoke(toast);
                this.mToastAnim = params.windowAnimations;
            } catch (Exception var5) {
                var5.printStackTrace();
            }

            this.mParams = this.createLayoutParams();
        }

        private WindowManager.LayoutParams createLayoutParams() {
            WindowManager.LayoutParams params = new WindowManager.LayoutParams();
            params.height = -2;
            params.width = -2;
            params.format = -3;
            params.type = 2010;
            params.gravity = 48;
            params.windowAnimations = this.mToastAnim;
            int yOffset = this.mContext.getResources().getDimensionPixelOffset(R.dimen.s_dp_612);
            params.y = yOffset;
            if (VERSION.SDK_INT >= 30) {
                params.setFitInsetsIgnoringVisibility(true);
            }

            params.setTitle("H3CSettingToast");
            params.flags = 262280;
            return params;
        }

        public void show(View view, long duration) {
            View decor = this.createDecor(view);
            this.mViewRef = new WeakReference(decor);
            this.mWindowManager.addView(decor, this.mParams);
            sHandler.removeCallbacks(this.mDismissTask);
            sHandler.postDelayed(this.mDismissTask, duration);
        }

        private View createDecor(View child) {
            FrameLayout frameLayout = new FrameLayout(child.getContext()) {
                public boolean onTouchEvent(MotionEvent event) {
                    WindowManagerToast.this.dismiss();
                    return super.onTouchEvent(event);
                }
            };
            frameLayout.addView(child);
            return frameLayout;
        }

        public void dismiss() {
            View view = this.mViewRef == null ? null : (View) this.mViewRef.get();
            if (view != null) {
                this.mWindowManager.removeViewImmediate(view);
            }

            this.mViewRef = null;
        }
    }
}

