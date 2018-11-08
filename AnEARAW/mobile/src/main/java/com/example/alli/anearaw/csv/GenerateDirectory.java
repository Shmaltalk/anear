package com.example.alli.anearaw.csv;

import android.app.Application;
import android.content.Context;
import android.os.Build;
import android.os.Environment;
import android.util.Log;

import com.example.alli.anearaw.activities.MainActivity;

import java.io.File;

public class GenerateDirectory  extends Application {
    public static File getRootFile(Context context) {
        File root;
        root = new File("/storage/sdcard1");
        if (!root.exists() || !root.canWrite()) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                root = new File(Environment.getExternalStorageDirectory(), Environment.DIRECTORY_DOCUMENTS);
            } else {
                root = new File(Environment.getExternalStorageDirectory(), "Documents");
            }
        }
        File directory;
        String id = MainActivity.getString(context, MainActivity.KEY_IDENTIFIER, null);
        if (id == null || id.equals("")) {
            directory = new File(root, ".anear_aw");
        } else {
            directory = new File(root, ".anear_aw/" + id);
        }
        if (!directory.exists()) {
            if (directory.mkdirs()) {
                Log.d("MAIN", "Made parent directories");
            }
        }
        return directory;
    }
}