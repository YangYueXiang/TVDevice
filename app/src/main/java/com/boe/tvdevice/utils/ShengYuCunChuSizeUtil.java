package com.boe.tvdevice.utils;

import android.os.Environment;
import android.os.StatFs;
import android.util.Log;

import java.io.File;

public class ShengYuCunChuSizeUtil {
    public static long readSystem() {
        File root = Environment.getRootDirectory();
        StatFs sf = new StatFs(root.getPath());
        long blockSize = sf.getBlockSize();
        long blockCount = sf.getBlockCount();
        long availCount = sf.getAvailableBlocks();
        Log.d("", "block大小:" + blockSize + ",block数目:" + blockCount + ",总大小:" + blockSize * blockCount / 1024 + "KB");
        Log.d("", "可用的block数目：:" + availCount + ",可用大小:" + availCount * blockSize / 1024 + "KB");
        return availCount * blockSize / 1024;
    }
}
