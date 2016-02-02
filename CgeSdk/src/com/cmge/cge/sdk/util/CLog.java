package com.cmge.cge.sdk.util;

import android.annotation.SuppressLint;
import android.content.Context;
import android.util.Log;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class CLog {

    public static final boolean DEBUG = true;
    
    public static final String TAG_CORE = "CgeSdk_Core";

    public static final String TAG_SERVER = "CgeSdk_Server";
    
    public static final String TAG_HTTP = "CgeSdk_Http";

    public static final String TAG_SECURITY = "CgeSdk_Security";

    public static final String TAG_CHANNEL = "CgeSdk_Channel";
    
    public static final String TAG_TEST = "CgeSdk_Test";

    private static final String LOG_FILE_NAME = "cge.log";
    
    private static File mLogFile = null;
    
    public static void init(Context context) {
        if (!DEBUG) {
            return;
        }
        
        File externalFiles = context.getExternalFilesDir(null);
        if (externalFiles == null) {
            Log.e(TAG_CORE, "no external media mounted");
            return;
        }
        
        mLogFile = new File(externalFiles.getAbsolutePath() + File.separator + LOG_FILE_NAME);
        if (mLogFile.exists()) {
            mLogFile.delete();
        }
        
        try {
            mLogFile.createNewFile();
        } catch (IOException e) {
            e.printStackTrace();
        }
        
        Log.i(CLog.TAG_CORE, "writing cge logs to " + mLogFile.getAbsolutePath());
    }
    
    public static void e(String tag, String message) {
        Log.e(tag, message);
        writeLog("E", tag, message);
    }

    public static void w(String tag, Throwable e) {
        Log.w(tag, e);
        writeLog("W", tag, e.getMessage());
    }

    public static void w(String tag, String message) {
        Log.w(tag, message);
        writeLog("W", tag, message);
    }

    public static void d(String tag, String message) {
        if (!DEBUG) {
            return;
        }
        
        Log.d(tag, message);
        writeLog("D", tag, message);
    }
    
    public static void writeLog(String level, String tag, String message) {
        if (!DEBUG) {
            return;
        }
        
        if (mLogFile == null) {
            return;
        }
        
        String time = getTime();
        BufferedWriter writer = null;
        try {
            writer = new BufferedWriter(new FileWriter(mLogFile, true));
            writer.write(time + " - " + level + " - " + tag + " - " + message);
            writer.newLine();
            writer.flush();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (writer != null) {
                try {
                    writer.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }
    
    @SuppressLint("SimpleDateFormat")
    public static String getTime() {
        SimpleDateFormat formatter = new SimpleDateFormat("MM-dd HH:mm:ss");
        return formatter.format(new Date());
    }
}
