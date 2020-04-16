package com.example.cameraxdemo;

import android.content.Context;

import java.io.File;

class Utils {
    static File getOutputFile(Context context) {
        Context appContext = context.getApplicationContext();
        File[] mediaDirs = context.getExternalMediaDirs();
        if (mediaDirs != null && mediaDirs.length > 0 && mediaDirs[0] != null) {
            File appMediaDir = new File(mediaDirs[0], appContext.getResources().getString(R.string.app_name));
            appMediaDir.mkdirs();
            return appMediaDir;
        }
        return appContext.getFilesDir();
    }

    static File createPhotoFile(File outputFile) {
        return new File(outputFile, String.format("%s%s", System.currentTimeMillis(), ".png"));
    }
}
