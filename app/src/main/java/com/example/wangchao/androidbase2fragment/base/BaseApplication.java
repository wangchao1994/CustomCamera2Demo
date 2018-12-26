package com.example.wangchao.androidbase2fragment.base;

import android.app.Application;

/**
 * global application
 */
public class BaseApplication extends Application {
    private static BaseApplication instance;
    @Override
    public void onCreate() {
        super.onCreate();
        instance=this;
    }
    public static BaseApplication getInstance() {
        return instance;
    }
}
