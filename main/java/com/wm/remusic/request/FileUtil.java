package com.wm.remusic.request;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

/**
 * 创建  文件File 和 目录Folder 
 */
public class FileUtil {

    public static void append2file(String filePath, String content) {
        if (filePath == null || filePath.length() <= 0|| content == null || content.length() <= 0) {
            return;
        }

        File fWtf = new File(filePath);
        File folder = fWtf.getParentFile();
        if (folder.exists() == false || folder.isDirectory() == false) {
            folder.mkdirs();
        }

        BufferedWriter bufWriter = null;

        try {
            bufWriter = new BufferedWriter(new FileWriter(fWtf, true), 8096);
            bufWriter.write(content);
            bufWriter.write('\r');
            bufWriter.write('\n');
            bufWriter.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }finally {
            if (bufWriter != null) {
                try {
                    bufWriter.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

    }

    // 先renameTo()然后再delete，避免后续可能出现的写异常
    public static void deleteFile(File f) {
        if (f == null) {
            return;
        }
        String pathDel = f.getAbsolutePath() + "." + System.currentTimeMillis() + ".del";
        File fDel = new File(pathDel);
        boolean bool = f.renameTo(fDel);
        if (bool) {
            fDel.delete();
        } else {
            f.delete();
        }
    }
}