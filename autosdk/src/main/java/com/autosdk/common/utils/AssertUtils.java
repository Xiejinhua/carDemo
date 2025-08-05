package com.autosdk.common.utils;

import android.content.Context;
import android.text.TextUtils;

import androidx.annotation.Nullable;

import com.autosdk.common.SdkApplicationUtils;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;

import timber.log.Timber;

/**
 * Created by AutoSdk.
 */

public class AssertUtils {
    public static final String TAG = AssertUtils.class.getSimpleName();

    /**
     * 从assets目录中复制整个文件夹内容
     *
     * @param context Context 使用CopyFiles类的Activity
     * @param oldPath String  原文件路径  如：/aa
     * @param newPath String  复制后路径  如：xx:/bb/cc
     */
    public static void copyFilesAssets(Context context, String oldPath, String newPath) {
        InputStream is = null;
        FileOutputStream fos = null;
        try {
            String[] fileNames = context.getAssets().list(oldPath);//获取assets目录下的所有文件及目录名
            if (fileNames != null && fileNames.length > 0) {//如果是目录
                Timber.d("[xzc] copyFilesAssets=" + fileNames.length);
                File file = new File(newPath);
                file.mkdirs();//如果文件夹不存在，则递归
                for (String fileName : fileNames) {
                    Timber.d("[xzc] copyFilesAssets11=" + fileName);
                    copyFilesAssets(context, oldPath + "/" + fileName, newPath + "/" + fileName);
                }
            } else {//如果是文件
                is = context.getAssets().open(oldPath);
                Timber.d("[xzc] copyFilesAssets=" + is);
                fos = new FileOutputStream(new File(newPath));
                byte[] buffer = new byte[1024];
                int byteCount = 0;
                while ((byteCount = is.read(buffer)) != -1) {//循环从输入流读取 buffer字节
                    fos.write(buffer, 0, byteCount);//将读取的输入流写入到输出流
                }
                fos.flush();//刷新缓冲区
            }
        } catch (Exception e) {
            Timber.e(e, "IOException occurred");

        } finally {
            FileUtils.safetyClose(is);
            FileUtils.safetyClose(fos);
        }
    }

    /**
     * 验证指定的assets文件存在,并且包含特定文本
     *
     * @param assertFilePath 文件相对路径
     * @param expectText     需要包含的文本(全部满足才算ok), 若为空,则表示仅判断文件是否存在
     * @return true asset文件存在并包含给定的文本, false-asset文件不存在或不包含给定的文本
     */
    public static boolean containInfo(Context context, String assertFilePath, @Nullable String... expectText) {
        if (context == null) {
            return false;
        }

        int expectArrLength = expectText == null ? 0 : expectText.length;
        if (expectArrLength == 1) {
            if (expectText[0] == null) {
                // 空指针传入
                expectArrLength = 0;
            }
        }

        InputStream inputStream = null;
        try {
            inputStream = context.getAssets().open(assertFilePath);

            if (expectArrLength == 0) { // 无异常则文件存在,直接返回
                return true;
            }

            boolean[] resultArr = new boolean[expectArrLength];
            boolean finalResult = false;
            byte[] buff = new byte[1024];
            int len = inputStream.read(buff);
            while (len >= 0) {
                String line = new String(buff);
                finalResult = true;
                for (int i = 0; i < expectArrLength; i++) {
                    if (!resultArr[i]) {
                        if (line.contains(expectText[i])) {
                            resultArr[i] = true;
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
            Timber.e(e, "IOException occurred");

        } finally {
            if (inputStream != null) {
                try {
                    inputStream.close();
                } catch (IOException e) {
                    Timber.e(e, "IOException occurred");

                }
            }

        }
        return false;
    }


    /**
     * 判断assert文件是否存在,不支持目录
     *
     * @param assertFilePath 文件相对路径
     */
    public static boolean isFileExist(Context context, String assertFilePath) {
        String text = null; // 避免编译警告, 使用了非varargs
        return containInfo(context, assertFilePath, text);
    }

    public static boolean copyAssets(Context context, File dest, String assetFile) {
        if (context == null) {
            return false;
        }
        InputStream inputStream = null;
        OutputStream outputStream = null;
        try {
            if (!dest.exists()) {
                dest.getParentFile().mkdirs();
            }
            inputStream = context.getAssets().open(assetFile);
            outputStream = new FileOutputStream(dest);
            byte[] buff = new byte[1024];
            int len = 0;
            while ((len = inputStream.read(buff)) >= 0) {
                outputStream.write(buff, 0, len);
            }
            outputStream.flush();
            return true;
        } catch (IOException e) {
            Timber.e(e, "IOException occurred");
        } finally {
            if (inputStream != null) {
                try {
                    inputStream.close();
                } catch (IOException e) {
                    Timber.e(e, "IOException occurred");

                }
            }
            if (outputStream != null) {
                try {
                    outputStream.close();
                } catch (IOException e) {
                    Timber.e(e, "IOException occurred");

                }
            }
        }
        return false;
    }

    /**
     * 拷贝文件时增加CRC的校验，解决部分配置文件变更，没有覆盖替换，导致启动失败问题
     *
     * @param context
     * @param assetFile
     * @param newPath
     * @return
     */
    public static void copyAssetsFileForCrc(Context context, String assetFile, String newPath) {
        InputStream is = null;
        FileOutputStream fos = null;
        try {
            File destFile = new File(newPath);
            if (destFile.exists() && destFile.isFile()) {//如果是文件
                boolean isSame = compareCrc(destFile, assetFile);
                if (isSame) {
                    return;
                } else {
                    destFile.delete();
                }
            }
            is = context.getAssets().open(assetFile);
            Timber.d("[xzc] copyFilesAssets=" + is);
            fos = new FileOutputStream(new File(newPath));
            byte[] buffer = new byte[1024];
            int byteCount = 0;
            while ((byteCount = is.read(buffer)) != -1) {//循环从输入流读取 buffer字节
                fos.write(buffer, 0, byteCount);//将读取的输入流写入到输出流
            }
            fos.flush();//刷新缓冲区
        } catch (Exception e) {
            Timber.e(e, "IOException occurred");

        } finally {
            if (is != null) {
                try {
                    is.close();
                } catch (IOException e) {
                    Timber.e(e, "IOException occurred");

                }
            }
            if (fos != null) {
                try {
                    fos.close();
                } catch (IOException e) {
                    Timber.e(e, "IOException occurred");

                }
            }
        }
    }

    /**
     * CRC比对
     *
     * @param destFile
     * @param assetFile
     * @return
     */
    private static boolean compareCrc(File destFile, String assetFile) {
        Long time = System.currentTimeMillis();
        String destCrc = CRCUtil.getCRC32(destFile);
        String assetCrc = CRCUtil.getAssetFileSCRC32(SdkApplicationUtils.getApplication(), assetFile);
        Timber.d("copyAssetFile crctime = ", Math.abs(System.currentTimeMillis() - time));
        Timber.d("copyAssetFile  destCrc:%s assetCrc:%s", destCrc, assetCrc);
        if (!TextUtils.isEmpty(destCrc) && !TextUtils.isEmpty(assetCrc) && destCrc.equalsIgnoreCase(assetCrc)) {
            Timber.d("chz.d", "copyAssetFile crc return");
            return true;
        }
        return false;
    }

    /**
     * 同步读取 文本文件
     * @param context 上下文
     * @param fileName 文件名
     * @return String
     */
    public static String readAssetsFile(Context context, String fileName) {
        try {
            InputStream is = context.getAssets().open(fileName);
            BufferedReader reader = new BufferedReader(new InputStreamReader(is));
            StringBuilder stringBuilder = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                stringBuilder.append(line);
            }
            reader.close();
            is.close();
            return stringBuilder.toString();
        } catch (IOException e) {
            Timber.e(TAG, "读取 JSON 文件失败: " + e.getMessage());
            return null;
        }
    }
}
