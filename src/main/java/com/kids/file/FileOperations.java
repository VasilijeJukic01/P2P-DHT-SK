package com.kids.file;

import com.kids.app.ChordState;

import java.io.*;

public class FileOperations {

    public static boolean isFile(String root, String path) {
        File file = new File(root + "\\" + path);
        return file.isFile();
    }

    public static Integer hashFilePath(String path) {
        return ChordState.chordHash(path);
    }

    public static boolean isImageFile(String path) {
        if (path == null || path.isEmpty()) return false;

        return path.toLowerCase().matches(".*\\.(jpg|jpeg|png|gif|bmp|tiff|webp|svg)$");
    }
}
