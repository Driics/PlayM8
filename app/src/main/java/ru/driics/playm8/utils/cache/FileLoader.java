package ru.driics.playm8.utils.cache;

import android.util.SparseArray;

import java.io.File;

public class FileLoader {
    public static final int MEDIA_DIR_IMAGE = 0;
    public static final int MEDIA_DIR_CACHE = 4;
    private static SparseArray<File> mediaDirs = null;

    public static void setMediaDirs(SparseArray<File> dirs) {
        mediaDirs = dirs;
    }

    public static File checkDirectory(int type) {
        return mediaDirs.get(type);
    }

    public static File getDirectory(int type) {
        File dir = mediaDirs.get(type);
        if (dir == null && type != FileLoader.MEDIA_DIR_CACHE) {
            dir = mediaDirs.get(FileLoader.MEDIA_DIR_CACHE);
        }
        return dir;
    }
}
