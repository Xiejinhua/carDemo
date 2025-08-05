package com.autosdk.bussiness.common.utils;

import android.content.Context;
import android.content.res.AssetManager;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;

import androidx.annotation.Nullable;

import timber.log.Timber;

/**
 *@author AutoSDk
 */
public class AssetUtils {
    public static final String TAG = AssetUtils.class.getSimpleName();

    /**
     * 从assets目录中复制整个文件夹内容
     * 参考 {@link #copyAssetsFolder(AssetManager, Context, String, String, boolean)}
     */
    public static void copyAssetsFolder(Context context, String assetsRelPath, String newPath, boolean recreateDestIfNeed) {
        //文件夹已存在就不拷贝
        if (!recreateDestIfNeed && FileUtils.checkFileExists(newPath)) {
            return;
        }
        copyAssetsFolder(null, context, assetsRelPath, newPath, recreateDestIfNeed);
    }

    /**
     * 从assets目录中复制整个文件夹内容
     *
     * @param assetManager           外部传入 AssetManager(如espresso),若为null,则使用参数 context 自动获取
     * @param context                Context 使用CopyFiles类的Activity
     * @param assetsRelPath          String  assets原目录/文件的相对路径  如：aa,表示 assets/aa/ 目录 或者 assets/aa 文件
     * @param destPath               String  复制后路径  如：xx:/bb/cc
     * @param recreateDestFileIfNeed true-若目标文件存在,则强制删除后新建  false-若目标文件存在,不再重新创建
     */
    public static void copyAssetsFolder(@Nullable AssetManager assetManager,
                                        @Nullable Context context,
                                        String assetsRelPath,
                                        String destPath,
                                        boolean recreateDestFileIfNeed) {
        int len = assetsRelPath == null ? 0 : assetsRelPath.length();
        if (len == 0) {
            Timber.d("copyAssetsFolder fail as srcFile path is empty,destPath:" + destPath);
            return;
        }

        if (assetManager == null) {
            if (context == null) {
                Timber.d("copyAssetsFolder fail as context is null,src:" + assetsRelPath);
                return;
            }
            assetManager = context.getAssets();
        }

        try {
            // 带分隔符会被识别为文件,而非目录,导致报错
            if (assetsRelPath.endsWith("/")) {
                assetsRelPath = assetsRelPath.substring(0, len - 1);
            }
            //获取assets目录下的所有文件及目录名
            String[] fileNames = assetManager.list(assetsRelPath);
            // 是否是目录
            boolean isDir = fileNames != null && fileNames.length > 0;
            if (isDir) {
                FileUtils.createDir(destPath, recreateDestFileIfNeed);
                for (String fileName : fileNames) {
                    copyAssetsFolder(assetManager, context, assetsRelPath + "/" + fileName, destPath + "/" + fileName, recreateDestFileIfNeed);
                }
            } else {
                if (!recreateDestFileIfNeed && FileUtils.checkFileExists(destPath)) {
                    return;
                }

                InputStream is = assetManager.open(assetsRelPath);
                FileOutputStream fos = new FileOutputStream(new File(destPath));
                byte[] buffer = new byte[1024];
                int byteCount = 0;
                while ((byteCount = is.read(buffer)) != -1) {
                    fos.write(buffer, 0, byteCount);
                }
                fos.flush();
                FileUtils.safetyClose(is);
                FileUtils.safetyClose(fos);
            }
        } catch (Exception e) {
            e.printStackTrace();
            Timber.d("复制asset文件出错: " + assetsRelPath + "\n" + e.getMessage());
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
        boolean[] resultArr = new boolean[expectArrLength];
        boolean finalResult = false;

        InputStream inputStream = null;
        InputStreamReader isr = null;
        BufferedReader br = null;
        try {
            // 无异常则文件存在,直接返回
            if (expectArrLength == 0) {
                return true;
            }

            inputStream = context.getAssets().open(assertFilePath);
            isr = new InputStreamReader(inputStream);
            br = new BufferedReader(isr);

            /*
             * 注意:不宜使用 inputStream.read(byte[]) ,因为同一行数据可能被截断,导致判断出错
             * */
            String line = br.readLine();
            while (line != null) {
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
                line = br.readLine();
            }
            return finalResult;
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            FileUtils.safetyClose(inputStream);
            FileUtils.safetyClose(isr);
            FileUtils.safetyClose(br);
        }
        return false;
    }


    /**
     * 判断assert文件是否存在,不支持目录
     *
     * @param assertFilePath 文件相对路径
     */
    public static boolean isFileExist(Context context, String assertFilePath) {
        String text = null;
        return containInfo(context, assertFilePath, text);
    }

    /**
     * 判断assets文件夹下的文件是否存在
     *
     * @return false 不存在    true 存在
     */
    public static boolean isExist(Context context, String assertFilePath) {
        AssetManager assetManager = context.getAssets();
        try {
            String[] names = assetManager.list("");
            for (int i = 0; i < names.length; i++) {
                if (names[i].equals(assertFilePath.trim())) {
                    return true;
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
        return false;
    }

    /**
     * 复制assets文件到指定目录,即使目标文件已存在,也要重新复制一次
     * 参考 {@link #copyAssets(Context, File, String, boolean)}
     */
    public static boolean copyAssets(Context context, File dest, String assetFile) {
        return copyAssets(context, dest, assetFile, true);
    }

    /**
     * 复制assets文件到指定目录
     *
     * @param dest               要复制的目标文件路径
     * @param assetFile          assert文件相对路径
     * @param forceCopyEvenExist true-强制复制 false-若目标文件存在,则不复制
     * @return
     */
    public static boolean copyAssets(Context context, File dest, String assetFile, boolean forceCopyEvenExist) {
        if (context == null) {
            return false;
        }

        if (!forceCopyEvenExist && FileUtils.checkFileExists(dest.getAbsolutePath())) {
            return true;
        }

        InputStream inputStream = null;
        OutputStream outputStream = null;
        try {
            FileUtils.createDir(dest.getParentFile().getAbsolutePath());
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
            Timber.d("copy " + assetFile + " to " + dest.getAbsolutePath() + " fail: " + e.getMessage());
        } finally {
            FileUtils.safetyClose(inputStream);
            FileUtils.safetyClose(outputStream);
        }
        return false;
    }

    /**
     * 读取assets文件内容字节数组
     *
     * @param assetFileRelPath assets文件相对路径, 如: f/a, 表示 assets/f/a 文件
     */
    public static byte[] getAssetFileContent(Context context, String assetFileRelPath) {
        if (context == null) {
            return null;
        }
        InputStream inputStream = null;
        byte[] result = null;

        try {
            inputStream = context.getAssets().open(assetFileRelPath);
            result = new byte[inputStream.available()];
            inputStream.read(result);
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            FileUtils.safetyClose(inputStream);
        }
        return result;
    }
}
