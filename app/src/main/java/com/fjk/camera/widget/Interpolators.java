//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by FernFlower decompiler)
//

package com.fjk.camera.widget;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Path;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.Interpolator;
import android.view.animation.LinearInterpolator;
import android.view.animation.OvershootInterpolator;
import android.view.animation.PathInterpolator;

@SuppressLint({"NewApi"})
public class Interpolators {
    public static final Interpolator LINEAR = new LinearInterpolator();
    public static final Interpolator ACCEL = new AccelerateInterpolator();
    public static final Interpolator ACCEL_0_75 = new AccelerateInterpolator(0.75F);
    public static final Interpolator ACCEL_1_5 = new AccelerateInterpolator(1.5F);
    public static final Interpolator ACCEL_2 = new AccelerateInterpolator(2.0F);
    public static final Interpolator DEACCEL = new DecelerateInterpolator();
    public static final Interpolator DEACCEL_1_5 = new DecelerateInterpolator(1.5F);
    public static final Interpolator DEACCEL_1_7 = new DecelerateInterpolator(1.7F);
    public static final Interpolator DEACCEL_2 = new DecelerateInterpolator(2.0F);
    public static final Interpolator DEACCEL_2_5 = new DecelerateInterpolator(2.5F);
    public static final Interpolator DEACCEL_3 = new DecelerateInterpolator(3.0F);
    public static final Interpolator DEACCEL_5 = new DecelerateInterpolator(5.0F);
    public static final Interpolator ACCEL_DEACCEL = new AccelerateDecelerateInterpolator();
    public static final Interpolator FAST_OUT_SLOW_IN = new PathInterpolator(0.4F, 0.0F, 0.2F, 1.0F);
    public static final Interpolator AGGRESSIVE_EASE = new PathInterpolator(0.2F, 0.0F, 0.0F, 1.0F);
    public static final Interpolator AGGRESSIVE_EASE_IN_OUT = new PathInterpolator(0.6F, 0.0F, 0.4F, 1.0F);

}
