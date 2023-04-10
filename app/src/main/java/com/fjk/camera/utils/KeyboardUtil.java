package com.fjk.camera.utils;

import android.app.Activity;
import android.content.Context;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;

import androidx.annotation.NonNull;

public class KeyboardUtil {

    public static void showOrHideKeyboard(@NonNull Context context) {
        InputMethodManager imm = (InputMethodManager) context.getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.toggleSoftInput(2, InputMethodManager.HIDE_NOT_ALWAYS);
    }

    public static void hideKeyboard(@NonNull Context context) {
        InputMethodManager imm = (InputMethodManager) context.getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.toggleSoftInput(0, InputMethodManager.RESULT_UNCHANGED_HIDDEN);
    }

    public static void showKeyboard(@NonNull Context context, View target) {
        InputMethodManager imm = (InputMethodManager) context.getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.showSoftInputFromInputMethod(target.getWindowToken(), 0);
    }

    public static void showKeyboardForce(@NonNull Context context, View target) {
        InputMethodManager imm = (InputMethodManager) context.getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.showSoftInput(target, InputMethodManager.SHOW_FORCED); //强制显示键盘
    }

    public static void hideKeyboard(@NonNull Context context, View target) {
        InputMethodManager imm = (InputMethodManager) context.getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(target.getWindowToken(), 0); //强制隐藏键盘
    }

    public static void hideKeyboard(@NonNull Activity currentActivity) {
        if (currentActivity.getCurrentFocus() == null) {
            return;
        }
        ((InputMethodManager) currentActivity.getSystemService(Context.INPUT_METHOD_SERVICE))
                .hideSoftInputFromWindow(currentActivity.getCurrentFocus().getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
    }

    public static boolean isKeyboardOpen(@NonNull Context context) {
        InputMethodManager imm = (InputMethodManager) context.getSystemService(Context.INPUT_METHOD_SERVICE);
        return imm.isActive();//isOpen若返回true，则表示输入法打开
    }

    public static void openKeyboard(EditText editText, Context context) {
        InputMethodManager imm = (InputMethodManager) context.getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.showSoftInput(editText, InputMethodManager.RESULT_SHOWN);
        imm.toggleSoftInput(InputMethodManager.SHOW_FORCED, InputMethodManager.HIDE_IMPLICIT_ONLY);
    }
}
