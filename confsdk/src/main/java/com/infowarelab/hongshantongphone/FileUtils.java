package com.infowarelab.hongshantongphone;

import android.util.Log;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;

/**
 * Created by xiaor on 2019/12/19.
 */

public class FileUtils {
    /*
     * Java文件操作 获取文件扩展名
     * */
    public static String getExtensionName(String filename) {
        if ((filename != null) && (filename.length() > 0)) {
            int dot = filename.lastIndexOf('.');
            if ((dot > -1) && (dot < (filename.length() - 1))) {
                return filename.substring(dot + 1);
            }
        }
        return filename;
    }

    /*
     * Java文件操作 获取不带扩展名的文件名
     * */
    public static String getFileNameNoEx(String filename) {
        if ((filename != null) && (filename.length() > 0)) {
            int dot = filename.lastIndexOf('.');
            if ((dot > -1) && (dot < (filename.length()))) {
                return filename.substring(0, dot);
            }
        }
        return filename;
    }

    /**
     * 删除单个文件
     *
     * @param filePath 被删除文件的文件名
     * @return 文件删除成功返回true，否则返回false
     */
    public static boolean deleteFile(String filePath) {
        File file = new File(filePath);
        if (file.isFile() && file.exists()) {
            return file.delete();
        }
        return false;
    }

    /**
     * 删除文件夹以及目录下的文件
     *
     * @param filePath 被删除目录的文件路径
     * @return 目录删除成功返回true，否则返回false
     */
    public static boolean deleteDirectory(String filePath) {
        boolean flag = false;
        //如果filePath不以文件分隔符结尾，自动添加文件分隔符
        if (!filePath.endsWith(File.separator)) {
            filePath = filePath + File.separator;
        }
        File dirFile = new File(filePath);
        if (!dirFile.exists() || !dirFile.isDirectory()) {
            return false;
        }
        flag = true;
        File[] files = dirFile.listFiles();
        //遍历删除文件夹下的所有文件(包括子目录)
        for (int i = 0; i < files.length; i++) {
            if (files[i].isFile()) {
                //删除子文件
                flag = deleteFile(files[i].getAbsolutePath());
                if (!flag) break;
            } else {
                //删除子目录
                flag = deleteDirectory(files[i].getAbsolutePath());
                if (!flag) break;
            }
        }
        if (!flag) return false;
        //删除当前空目录
        return dirFile.delete();
    }

    /**
     * 根据路径删除指定的目录或文件，无论存在与否
     *
     * @param filePath 要删除的目录或文件
     * @return 删除成功返回 true，否则返回 false。
     */
    public static boolean DeleteFolder(String filePath) {
        File file = new File(filePath);
        if (!file.exists()) {
            return false;
        } else {
            if (file.isFile()) {
                // 为文件时调用删除文件方法
                return deleteFile(filePath);
            } else {
                // 为目录时调用删除目录方法
                return deleteDirectory(filePath);
            }
        }
    }

    //移除文件，获取文件时间与当前时间对比，我这时间定位0.2天，删除三天前的文件
    public static void removeFileByTime(String dirPath) {
        //获取目录下所有文件
        List<File> allFile = getDirAllFile(new File(dirPath));
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm");
        //获取当前时间
        Date end = new Date(System.currentTimeMillis());
        try {
            end = dateFormat.parse(dateFormat.format(new Date(System.currentTimeMillis())));
        } catch (Exception e) {
            Log.e("ttttt", "dataformat exeption e " + e.toString());
        }
        Log.e("ttttt", "getNeedRemoveFile  dirPath = " + dirPath);
        for (File file : allFile) {//ComDef
            try {
                //文件时间减去当前时间
                Date start = dateFormat.parse(dateFormat.format(new Date(file.lastModified())));
                long diff = end.getTime() - start.getTime();//这样得到的差值是微秒级别
                long days = diff / (1000 * 60 * 60 * 24);
                if (days >= 0.0) {
                    deleteFile(file);
                }
            } catch (Exception e) {
                Log.d("ttttt", "dataformat exeption e " + e.toString());
            }
        }
    }

    //删除文件夹及文件夹下所有文件
    public static void deleteFile(File file) {
        if (file.isDirectory()) {
            File[] files = file.listFiles();
            for (int i = 0; i < files.length; i++) {
                File f = files[i];
                deleteFile(f);
            }
            file.delete();
        } else if (file.exists()) {
            file.delete();
        }
    }

    //获取指定目录下一级文件
    public static List<File> getDirAllFile(File file) {
        List<File> fileList = new ArrayList<>();
        File[] fileArray = file.listFiles();
        if (fileArray == null) return fileList;
        for (File f : fileArray) {
            fileList.add(f);
        }
        fileSortByTime(fileList);
        return fileList;
    }

    //对文件进行时间排序
    public static void fileSortByTime(List<File> fileList) {
        Collections.sort(fileList, new Comparator<File>() {
            @Override
            public int compare(File p1, File p2) {
                boolean lInValid = (p1 == null || !p1.exists());
                boolean rInValid = (p2 == null || !p2.exists());
                boolean bothInValid = lInValid && rInValid;
                if (bothInValid) {
                    return 0;
                }
                if (lInValid) {
                    return -1;
                }
                if (rInValid) {
                    return 1;
                }
                if (p1.lastModified() < p2.lastModified()) {
                    return -1;
                }
                return 1;
            }
        });
    }
}
