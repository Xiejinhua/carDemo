package com.autosdk.bussiness.common.utils;

import android.app.Application;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Build;
import android.text.TextUtils;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.autosdk.bussiness.navi.constant.NaviConstant;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.Closeable;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;

import timber.log.Timber;

/**
 * Created by AutoSdk.
 */

public class FileUtils {
    /**
     * 存储二进制文件
     *
     * @param byteArr            byte数组
     * @param fileRelPathAndName 要写入的文件路径,如 aa/bb/c.bin, 也可以是绝对路径, 如 /aa/b.bin
     * @param append             true-追加 false-覆盖原文件内容
     */
    public static void writeToFile(@NonNull byte[] byteArr, @NonNull String fileRelPathAndName, boolean append) {
        if (TextUtils.isEmpty(fileRelPathAndName)) {
            return;
        }
        String filePath = fileRelPathAndName;
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

    public static byte[] file2Byte(String filePath) {
        if (TextUtils.isEmpty(filePath)) {
            return null;
        }
        return file2Byte(new File(filePath));
    }

    public static byte[] file2Byte(File file) {
        if (!file.exists()) {
            return null;
        }

        byte[] buffer = null;
        FileInputStream fis = null;
        ByteArrayOutputStream bos = null;
        try {
            fis = new FileInputStream(file);
            bos = new ByteArrayOutputStream();
            byte[] b = new byte[1024];
            int n;
            while ((n = fis.read(b)) != -1) {
                bos.write(b, 0, n);
            }
            buffer = bos.toByteArray();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            safetyClose(fis);
            safetyClose(bos);
        }
        Timber.d("File2byte: path = " + file);
        return buffer;
    }


    public static final String TAG = FileUtils.class.getSimpleName();

    /**
     * 检查 文件 / 文件夹 是否存在
     *
     * @param filepath 文件绝对路径
     */
    public static boolean checkFileExists(String filepath) {
        return new File(filepath).exists();
    }

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

        // 存在同名文件
        if (targetFile.exists()) {
            boolean isDir = targetFile.isDirectory();
            // 非目录,删除以便创建目录
            if (!isDir) {
                boolean result = targetFile.delete();
                Timber.d("dirPath:" + targetFile.getAbsolutePath() + " is a file, delete it, result=" + result);
            } else if (forceRecreate) {
                // 强制删除目录
                deleteDir(targetFile);
            } else {
                // 目录存在
                return true;
            }
        }

        return targetFile.mkdirs();
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
            e.printStackTrace();
        }
        Timber.i("createFile " + filepath + " , result = " + result);
        return result;
    }

    /**
     * 保存文件
     *
     * @param filePath 要保存的路径,如 /sdcard/amapauto20/jniScreenshot/xxx.jpg
     */
    public static void saveImage(Bitmap bitmap, String filePath) {
        if (bitmap == null || TextUtils.isEmpty(filePath)) {
            Timber.d("保存图片失败,请检查参数后再试");
            return;
        }
        FileOutputStream out = null;
        try {
            File file = new File(filePath);
            File folder = file.getParentFile();
            if (!folder.exists()) {
                folder.mkdirs();
            }
            if (file.exists()) {
                file.delete();
            }

            out = new FileOutputStream(file);

            Bitmap.CompressFormat format =
                    Bitmap.Config.ARGB_8888 == bitmap.getConfig() ? Bitmap.CompressFormat.PNG : Bitmap.CompressFormat.JPEG;
            bitmap.compress(format, 100, out);
            out.flush();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            safetyClose(out);
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
            Timber.i("deleteFile 不存在 file");
            return true;
        }

        if (file.isFile()) {
            return file.delete();
        } else {
            return deleteDir(file);
        }
    }

    /**
     * 删除目录
     */
    public static boolean deleteDir(String pPath) {
        if (TextUtils.isEmpty(pPath)) {
            return false;
        }
        return deleteDir(new File(pPath));
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
            if (file.isFile()) {
                // 删除所有文件
                file.delete();
            } else if (file.isDirectory()) {
                // 递归删除子目录
                deleteDir(file);
            }
        }
        // 删除空目录本身
        return dir.delete();
    }


    /**
     * 参考 {@link #containAllInfo(File, String...)}
     */
    public static boolean containAllInfo(@NonNull String filePath, @Nullable String... expectText) {
        return containAllInfo(new File(filePath));
    }

    /**
     * 指定文件的内容需要包含所有expectText才算匹配
     * 参考 {@link #containInfo(File, boolean, String...)}
     */
    public static boolean containAllInfo(@Nullable File file, @Nullable String... expectText) {
        return containInfo(file, true, expectText);
    }

    /**
     * 验证文件是否存在,并且包含特定文本
     *
     * @param file       文件
     * @param matchAll   true-所有expectText都需匹配才算ok, false-只需匹配 expectText 的任意一个即算ok
     * @param expectText 需要包含的文本, 若为空,则表示仅判断文件是否存在
     * @return true asset文件存在并包含给定的文本, false-asset文件不存在或不包含给定的文本
     */
    public static boolean containInfo(@Nullable File file, boolean matchAll, @Nullable String... expectText) {

        if (file == null || !file.exists()) {
            return false;
        }

        int expectArrLength = expectText == null ? 0 : expectText.length;
        boolean[] resultArr = new boolean[expectArrLength];
        boolean finalResult = false;
        InputStream inputStream = null;
        try {
            inputStream = new FileInputStream(file);

            if (expectArrLength == 0) {
                // 无异常则文件存在,直接返回
                return true;
            }

            byte[] buff = new byte[1024];
            int len = inputStream.read(buff);
            while (len >= 0) {
                String line = new String(buff);
                finalResult = true;

                for (int i = 0; i < expectArrLength; i++) {
                    if (!resultArr[i]) {
                        if (line.contains(expectText[i])) {
                            resultArr[i] = true;

                            // 只需匹配其中任意一个字符串,则退出循环, 匹配成功
                            if (!matchAll) {
                                finalResult = true;
                                break;
                            }
                        }
                    }
                    finalResult = finalResult && resultArr[i];
                }

                if (finalResult) {
                    break;
                }
                len = inputStream.read(buff);
            }

            return finalResult;
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            safetyClose(inputStream);
        }
        return false;
    }

    /**
     * 按行读取文件内容
     * 参考 {@link #readAllLine(File)}
     */
    @NonNull
    public static ArrayList<String> readAllLine(@Nullable String filePath) {
        if (TextUtils.isEmpty(filePath)) {
            return new ArrayList<>();
        }
        return readAllLine(new File(filePath));
    }


    /**
     * 按行读取指定文件的所有内容
     * 若文件不存在,则返回空list
     */
    @NonNull
    public static ArrayList<String> readAllLine(@Nullable File file) {
        ArrayList<String> contentList = new ArrayList<>();
        if (file == null || !file.exists()) {
            return contentList;
        }

        FileReader fr = null;
        BufferedReader bfr = null;
        try {
            fr = new FileReader(file);
            bfr = new BufferedReader(fr);

            String line = bfr.readLine();
            while (line != null) {
                contentList.add(line);
                line = bfr.readLine();
            }

            return contentList;
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            safetyClose(bfr);
            safetyClose(fr);
        }
        return contentList;
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
     * 移动文件到指定位置并重命名
     *
     * @param oriFilePath  源文件绝对路径
     * @param destFilePath 要移动到的目标位置绝对路径
     */
    public static boolean rename(@NonNull String oriFilePath, @NonNull String destFilePath) {
        File srcFile = new File(oriFilePath);
        if (!srcFile.exists()) {
            Timber.d("rename fail as " + oriFilePath + " not exist");
            return false;
        }

        File dest = new File(destFilePath);
        dest.getParentFile().mkdirs();
        return srcFile.renameTo(dest);
    }

    /**
     * 获取指定文件的字节大小, 若文件不存在则返回0
     */
    public static long getFileLen(String filePath) {
        if (TextUtils.isEmpty(filePath)) {
            return 0;
        }
        File file = new File(filePath);
        return file.length();
    }

    /**
     * 获取文件名, 包括扩展名, 如: a.9.png
     * 以分隔符"/",切分得到最后一部分
     *
     * @param filePath 文件路径
     */
    @NonNull
    public static String getFileName(String filePath) {
        if (TextUtils.isEmpty(filePath)) {
            return "";
        }
        // 兼容windows路径格式,避免误传
        filePath = filePath.replace("\\", "/");
        String[] arr = filePath.split("/");
        int len = arr.length;
        return arr[len - 1];
    }

    /**
     * @param fileName 文件名或者路径
     * @return 文件扩展名(不包括点.), 若是点9文件,则返回 "9.png"
     */
    @NonNull
    public static String getFileExt(String fileName) {
        String fileExt = "";
        if (TextUtils.isEmpty(fileName)) {
            return fileExt;
        }

        fileName = fileName.toLowerCase();
        String[] split = fileName.split("\\.");
        int len = split.length;
        if (len >= 2) {
            fileExt = split[len - 1];
        }

        if (len >= 3) {
            // 可能是点9文件, 如 .9.png, .9.avsg 等
            String subFileExt = split[len - 2];
            if ("9".equals(subFileExt)) {
                fileExt = "9." + fileExt;
            }
        }
        return fileExt;
    }

    /**
     * 列出指定路径目录的所有子文件列表(只包含一级子文件)
     * 若所给路径并未表示目录, 则返回空数据
     *
     * @param folderPath 目录路径
     */
    @Nullable
    public static File[] listSubFiles(String folderPath) {
        if (TextUtils.isEmpty(folderPath)) {
            return null;
        }

        File folder = new File(folderPath);
        boolean isDir = folder.exists() && folder.isDirectory();
        if (!isDir) {
            return null;
        }

        return folder.listFiles();
    }

    /**
     * 根据图片文件名,提取文件的原始字节数据(不进行bitmap解析)
     * 参考 {@link #getResDrawableRawBytes(Application, String, int)}
     */
    @Nullable
    public static byte[] getResDrawableRawBytes(Application application, String imageName) {
        return getResDrawableRawBytes(application, imageName, 0);
    }

    /**
     * 根据图片文件名或者文件id,提取文件的原始字节数据(不进行bitmap解析)
     * 若 resId 不等于0,则直接根据resId进行文件读取
     * 若 resId 等于0,则根据文件名称解析得到 resId, 再进行文件读取
     *
     * @param imageName 文件名, 如: a.png , 则表示 res/drawable-xxx/a.png
     * @param resID     文件资源id, 0 表示无效
     */
    @Nullable
    public static byte[] getResDrawableRawBytes(Application application, String imageName, int resID) {
        String fileExt = FileUtils.getFileExt(imageName);
        if (!TextUtils.isEmpty(fileExt)) {
            // 若带有扩展名,则需要先将扩展名去掉
            imageName = imageName.replace("." + fileExt, "");
        }

        Resources resources = application.getResources();
        if (resID == 0 || resID == NaviConstant.DEFAULT_ERR_CODE) {
            resID = resources.getIdentifier(imageName, "drawable", application.getPackageName());
        }

        byte[] buffer = null;
        InputStream fis = null;
        ByteArrayOutputStream bos = null;
        try {
            fis = resources.openRawResource(resID);
            bos = new ByteArrayOutputStream();
            byte[] b = new byte[1024];
            int readLen;
            while ((readLen = fis.read(b)) != -1) {
                bos.write(b, 0, readLen);
            }
            buffer = bos.toByteArray();
        } catch (Exception e) { // IOException不够, openRawResource() 可能抛出 NotFoundException
            e.printStackTrace();
            Timber.w(e, "getResDrawableRawBytes fail");
        } finally {
            safetyClose(fis);
            safetyClose(bos);
        }
        return buffer;
    }

    /**
     * 根据图片文件名,提取图片文件bitmap
     *
     * @param imageName 文件名, 如: a.png , 则表示 res/drawable-xxx/a.png
     */
    @Nullable
    public static Bitmap getResDrawableBitmap(Application application, String imageName) {
        String fileExt = FileUtils.getFileExt(imageName);
        if (!TextUtils.isEmpty(fileExt)) {
            // 若带有扩展名,则需要先将扩展名去掉
            imageName = imageName.replace("." + fileExt, "");
        }

        Resources resources = application.getResources();
        int resID = resources.getIdentifier(imageName, "drawable", application.getPackageName());
        return BitmapFactory.decodeResource(resources, resID);
    }

    /**
     * 根据图片文件名,提取图片文件的内容字节数组(解析成bitmap,在提取bitmap的字节内容)
     * 注意: 读取到的并非原始文件的二进制数据, 系统会根据当前设备参数进行缩放处理
     * 若有需要获取原始文件字节数据,请调用 {@link #getResDrawableRawBytes(Application, String)}
     *
     * @param imageName 文件名, 如: a.png , 则表示 res/drawable-xxx/a.png
     */
    @Nullable
    public static byte[] getResDrawableBitmapBytes(Application application, String imageName) {
        Bitmap bitmap = getResDrawableBitmap(application, imageName);
        if (bitmap == null) {
            return null;
        }
        ByteBuffer dataBuffer = ByteBuffer.allocate(bitmap.getByteCount());
        bitmap.copyPixelsToBuffer(dataBuffer);
        byte[] bytes = dataBuffer.array();
        bitmap.recycle();
        return bytes;
    }

    /**
     * 指定Assets文件名称转化为String
     */
    public static String getFileStringFromAssets(Application application, String fileName) {
        String fileString = null;

        if (null != application && null != fileName) {
            InputStream inputStream = null;
            try {
                inputStream = application.getResources().getAssets().open(fileName);
                byte[] buffer = new byte[inputStream.available()];
                inputStream.read(buffer);
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                    fileString = new String(buffer, StandardCharsets.UTF_8);
                }
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                if (inputStream != null) {
                    try {
                        inputStream.close();
                    } catch (IOException e) {

                    }
                }
            }

        }
        return fileString;
    }

    public static int copy(String fromFile, String toFile) {
        //要复制的文件目录
        File[] currentFiles;
        File root = new File(fromFile);
        //如同判断SD卡是否存在或者文件是否存在
        //如果不存在则 return出去
        if (!root.exists()) {
            Timber.i("copyFile 不存在 file");
            return -1;
        }
        //如果存在则获取当前目录下的全部文件 填充数组
        currentFiles = root.listFiles();
        //目标目录
        File targetDir = new File(toFile);
        //创建目录
        if (!targetDir.exists()) {
            targetDir.mkdirs();
        }
        //遍历要复制该目录下的全部文件
        for (int i = 0; i < currentFiles.length; i++) {
            if (currentFiles[i].isDirectory()) {//如果当前项为子目录 进行递归
                copy(currentFiles[i].getPath() + "/", toFile + currentFiles[i].getName() + "/");
            } else {//如果当前项为文件则进行文件拷贝
                copySdcardFile(currentFiles[i].getPath(), toFile + currentFiles[i].getName());
            }
        }
        return 0;
    }

    //文件拷贝
    //要复制的目录下的所有非子目录(文件夹)文件拷贝
    public static int copySdcardFile(String fromFile, String toFile) {
        InputStream fosFrom = null;
        OutputStream fosTo = null;
        try {
            fosFrom = new FileInputStream(fromFile);
            fosTo = new FileOutputStream(toFile);
            byte bt[] = new byte[1024];
            int c;
            while ((c = fosFrom.read(bt)) > 0) {
                fosTo.write(bt, 0, c);
            }
            fosFrom.close();
            fosTo.close();
            return 0;

        } catch (Exception ex) {
            try {
                if (fosFrom != null) {
                    fosFrom.close();
                }
                if (fosTo != null) {
                    fosTo.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
            return -1;
        }
    }

    /**
     * 复制文件。targetFile为目标文件，file为源文件
     *
     * @param targetFile
     * @param file
     */
    public static void copyFile(File file, File targetFile) {
        if (targetFile.exists()) {

        } else {
            File parentFile = new File(targetFile.getParent() + "/");
            parentFile.mkdirs();
            createFile(targetFile, true);
        }
        InputStream is = null;
        FileOutputStream fos = null;
        try {
            is = new FileInputStream(file);
            fos = new FileOutputStream(targetFile);
            byte[] buffer = new byte[1024];
            while (is.read(buffer) != -1) {
                fos.write(buffer);
            }
        } catch (IOException e) {
            e.printStackTrace();
            Timber.d("[copyFile] e = %s", Log.getStackTraceString(e));
        } finally {
            safetyClose(is);
            safetyClose(fos);
        }
    }

    public static void createFile(File file, boolean isFile) {
        if (!file.exists()) {
            if (file.getParentFile() != null && !file.getParentFile().exists()) {
                createFile(file.getParentFile(), false);
            } else {
                if (isFile) {
                    try {
                        file.createNewFile();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                } else {
                    file.mkdir();
                }
            }
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
}
