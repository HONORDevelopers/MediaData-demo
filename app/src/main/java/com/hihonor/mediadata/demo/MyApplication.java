/*
 * Copyright (c) Honor Terminal Co., Ltd. 2023-2024. All rights reserved.
 */

package com.hihonor.mediadata.demo;

import android.app.Application;
import android.util.Log;

public class MyApplication extends Application {
    private static final String TAG = "MyApplication";

    @Override
    public void onCreate() {
        super.onCreate();
        Log.i(TAG, "onCreate: application");
    }
}
