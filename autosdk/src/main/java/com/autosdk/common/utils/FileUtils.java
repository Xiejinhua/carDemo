package com.autosdk.common.utils;

import android.text.TextUtils;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.autosdk.common.AutoConstant;

import java.io.BufferedWriter;
import java.io.Closeable;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.util.Arrays;
import java.util.Comparator;

import timber.log.Timber;

/**
 * Created by AutoSdk.
 */
public class FileUtils {

    public static final String TAG = FileUtils.class.getSimpleName();
    public static String path = AutoConstant.PATH;

    /**
     * 创建文件夹,若已存在则不重新创建
     *
     * @param dirpath 路径
     */
    public static boolean createDir(String dirpath) {
        return createDir(dirpath, false);
    }

    /**
     * 创建文件夹
     * 若文件存在,但非目录,则删除重建
     * 参考 {@link #createDir(File, boolean)}
     */
    public static boolean createDir(String dirpath, boolean forceRecreate) {
        return createDir(new File(dirpath), forceRecreate);
    }

    /**
     * 创建文件夹
     * 若文件存在,但非目录,则删除重建
     *
     * @param targetFile    要创建的目标目录文件
     * @param forceRecreate 若目录已存在,是否要强制重新闯进(删除后,新建)
     * @return 是否创建成功
     */
    public static boolean createDir(File targetFile, boolean forceRecreate) {
        if (targetFile == null) {
            return false;
        }

        if (targetFile.exists()) { // 存在同名文件
            boolean isDir = targetFile.isDirectory();
            if (!isDir) { // 非目录,删除以便创建目录
                boolean result = targetFile.delete();
                Timber.d("dirPath:" + targetFile.getAbsolutePath() + " is a file, delete it, result=" + result);
            } else if (forceRecreate) { // 强制删除目录
                deleteDir(targetFile);
            } else { // 目录存在
                return true;
            }
        }

        return targetFile.mkdirs();
    }

    /**
     * 删除指定目录
     * 若存在同名非目录文件,则不处理
     */
    public static boolean deleteDir(File dir) {
        if (dir == null || !dir.exists() || !dir.isDirectory()) {
            return true;
        }

        for (File file : dir.listFiles()) {
            if (file.isFile()) {// 删除所有文件
                file.delete();
            } else if (file.isDirectory()) { // 递归删除子目录
                deleteDir(file);
            }
        }
        return dir.delete();// 删除空目录本身
    }

    /**
     * 写文件
     *
     * @param msg                写入的内容
     * @param fileRelPathAndName 绝对路径或相对地址,如:  aa/bb 表示 /sdcard/AutoSdkDemo/aa/bb 文件
     * @param append             是否是追加模式
     */
    public static void writeToFile(String msg, String fileRelPathAndName, boolean append) {
        if (TextUtils.isEmpty(fileRelPathAndName)) {
            return;
        }

        String targetFilePath = fileRelPathAndName;
        if (!fileRelPathAndName.startsWith("/")) { // 相对路径, 自动在 AutoSdkDemo/ 目录下创建文件
            targetFilePath = (path + fileRelPathAndName);
        }

        targetFilePath = targetFilePath
                .replace("\\", "/")
                .replace("//", "/");

        createFile(targetFilePath);

        FileWriter fileWriter = null;
        BufferedWriter bufferedWriter = null;
        try {
            File file = new File(targetFilePath);
            fileWriter = new FileWriter(file, append);
            bufferedWriter = new BufferedWriter(fileWriter);
            bufferedWriter.write(msg + "\r\n");
        } catch (IOException e) {
            Timber.e(e.getMessage());
        } finally {
            safetyClose(bufferedWriter);
            safetyClose(fileWriter);
        }
    }

    /**
     * 存储二进制文件
     *
     * @param byteArr            byte数组
     * @param fileRelPathAndName 要写入的文件路径, 可以 {@link #path} 下的相对路径,如 aa/bb/c.bin, 也可以是绝对路径, 如 /aa/b.bin
     * @param append             true-追加 false-覆盖原文件内容
     */
    public static void writeToFile(@NonNull byte[] byteArr, @NonNull String fileRelPathAndName, boolean append) {
        if (TextUtils.isEmpty(fileRelPathAndName)) {
            return;
        }

        String filePath = fileRelPathAndName;
        if (!filePath.startsWith("/")) { // 非分隔符开头,则表示传入的是相对路径
            filePath = (path + fileRelPathAndName);
        }
        filePath = filePath.replace("\\", "/")
                .replace("//", "/");

        createFile(filePath);
        DataOutputStream dataOutputStream = null;
        try {
            dataOutputStream = new DataOutputStream(new FileOutputStream(filePath, append));
            for (byte b : byteArr) {
                dataOutputStream.write(b);
            }
            dataOutputStream.flush();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            safetyClose(dataOutputStream);
        }
    }

    /**
     * 创建文件
     * 若存在同名文件/目录,则直接返回 true
     *
     * @param filepath 路径
     * @return 创建文件结果
     */
    public static boolean createFile(String filepath) {
        File file = new File(filepath);
        if (file.exists()) {
            return true;
        }
        boolean result = false;
        try {
            createDir(file.getParent());

            result = file.createNewFile();
        } catch (IOException e) {
            Timber.e(e.getMessage());
        }
        Timber.d("createFile " + filepath + " , result = " + result);
        return result;
    }

    public static void safetyClose(Closeable closeable) {
        if (closeable != null) {
            try {
                closeable.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * 删除文件或者目录
     *
     * @return true-目标文件不存在(包括原本就不存在以及删除成功两种情况)
     * false-目标文件仍存在
     */
    public static boolean deleteFile(@NonNull String path) {
        File file = new File(path);
        if (!file.exists()) {
            return true;
        }

        if (file.isFile()) {
            return file.delete();
        } else {
            return deleteDir(file);
        }
    }

    /**
     * 判断指定目录是否为空
     * 若路径实际非目录,则直接返回false
     *
     * @param folderPath 目录路径
     * @return
     */
    public static boolean isDirEmpty(String folderPath) {
        File[] files = listSubFiles(folderPath);
        int size = files == null ? 0 : files.length;
        return size == 0;
    }

    /**
     * 列出指定路径目录的所有子文件列表(只包含一级子文件)
     * 参考 {@link #listSubFiles(File, Comparator)}
     */
    @Nullable
    public static File[] listSubFiles(String folderPath) {
        return listSubFiles(folderPath, null);
    }

    /**
     * 列出指定路径目录的所有子文件列表(只包含一级子文件)
     * 参考 {@link #listSubFiles(File, Comparator)}
     */
    @Nullable
    public static File[] listSubFiles(String folderPath, @Nullable Comparator<File> comparator) {
        if (TextUtils.isEmpty(folderPath)) {
            return null;
        }

        File folder = new File(folderPath);
        return listSubFiles(folder, comparator);
    }

    /**
     * 列出指定路径目录的所有子文件列表(只包含一级子文件)
     * 若所给路径并未表示目录, 则返回空数据
     *
     * @param folder     目录文件
     * @param comparator 对目录下的子文件进行排序,若为null,则不做排序,直接返回
     */
    @Nullable
    public static File[] listSubFiles(@NonNull File folder, @Nullable Comparator<File> comparator) {
        boolean isDir = folder.exists() && folder.isDirectory();
        if (!isDir) {
            return null;
        }

        File[] files = folder.listFiles();
        if (comparator != null) {
            Arrays.sort(files, comparator);
        }
        return files;
    }

    /**
     * 读取文件原始字节数组
     */
    @NonNull
    public static byte[] readAllBytes(@NonNull String filePath) {
        return readAllBytes(new File(filePath));
    }

    /**
     * 读取文件原始字节数组
     */
    @NonNull
    public static byte[] readAllBytes(@Nullable File file) {
        byte[] result = new byte[0];
        if (file == null || !file.exists()) {
            return result;
        }

        FileChannel channel = null;
        FileInputStream fs = null;
        try {
            fs = new FileInputStream(file);
            channel = fs.getChannel();
            ByteBuffer byteBuffer = ByteBuffer.allocate((int) channel.size());
            while ((channel.read(byteBuffer)) > 0) {
                // do nothing
            }
            result = byteBuffer.array();
            return result;
        } catch (IOException e) {
            e.printStackTrace();
            return result;
        } finally {
            safetyClose(channel);
            safetyClose(fs);
        }
    }

    /**
     * 删除指定目录内的所有文件
     * 若存在同名非目录文件,则不处理,不删除空目录
     */
    public static boolean deleteAllFile(File dir) {
        if (dir == null || !dir.exists() || !dir.isDirectory()) {
            return true;
        }

        for (File file : dir.listFiles()) {
            if (file.isFile()) {// 删除所有文件
                file.delete();
            } else if (file.isDirectory()) { // 递归删除子目录
                deleteDir(file);
            }
        }
        return false;
    }
}
