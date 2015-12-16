package com.amlogic.DTVPlayer.storage;

import android.os.Build;

import java.io.File;

/**
 * Created by daniel on 11/9/15.
 */
public class StorageUtils {
    public static String externalDirBase;

    static
    {
        //if(Build.VERSION.SDK_INT > Build.VERSION_CODES.KITKAT)
        //media_rw是4.4引入的,但是4.4的挂载路径改成了storage下,所以暂时只判断5.1
        externalDirBase = "/mnt/media_rw";
    }

    public static boolean onExtDir(File file)
    {
        if(Build.VERSION.SDK_INT == 22) {
            if (file.getPath().startsWith("/storage/external_storage/sd") && !(file.getPath().startsWith("/storage/external_storage/sdcard")))
                return true;
            else
                return false;
        }
        else
        {
            if(file.getPath().startsWith("/storage/external_storage/udisk"))
                return true;
            else
                return false;
        }
    }

    public static String getSdCardPath()
    {
        if(Build.VERSION.SDK_INT == 22) {
            return "/storage/udisk0";
        }
        else
            return "/storage/external_storage/udisk0";
    }
}
