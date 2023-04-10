package com.fjk.camera.app;

import android.app.Activity;
import android.app.Application;
import android.content.Context;
import android.content.res.Configuration;
import android.content.res.Resources;

import com.fjk.camera.utils.ToastWhite;

public class AppLauncher extends Application {

    private static Context context;

    @Override
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(base);
    }

    @Override
    public void onCreate() {
        super.onCreate();
        context = getApplicationContext();
        ToastWhite.init(context);
//        AutoSize.checkAndInit(this);
//        AutoSizeCompat.autoConvertDensityOfGlobal(Resources.getSystem());
//        AutoSizeConfig.getInstance().setOnAdaptListener(new onAdaptListener() {
//            @Override
//            public void onAdaptBefore(Object target, Activity activity) {
//                AutoSizeConfig.getInstance().setScreenWidth(ScreenUtils.getRawScreenSize(activity)[0]);
//                AutoSizeConfig.getInstance().setScreenHeight(ScreenUtils.getRawScreenSize(activity)[1]);
//                if (activity.getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE) {
//
//                    AutoSizeConfig.getInstance()
//                            .setDesignWidthInDp(1920)
//                            .setDesignHeightInDp(1080);
//                } else {
//                    //设置竖屏设计尺寸
//                    AutoSizeConfig.getInstance()
//                            .setDesignWidthInDp(1080)
//                            .setDesignHeightInDp(1920);
//                }
//            }
//
//            @Override
//            public void onAdaptAfter(Object target, Activity activity) {
//                AutoSizeCompat.autoConvertDensityOfGlobal(Resources.getSystem());
//            }
//        });

    }

    public static Context getContext() {
        return context;
    }
}
