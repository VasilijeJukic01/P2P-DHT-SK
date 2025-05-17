package com.kids.file;

import java.io.*;

import static com.kids.app.ChordState.CHORD_SIZE;

public class FileOperations {

    public static boolean isFile(String root, String path) {
        File file = new File(root + "\\" + path);
        return file.isFile();
    }

    public static Integer hashFilePath(String path) {
        return path.hashCode() % CHORD_SIZE;
    }

}
