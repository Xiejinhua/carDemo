package com.autosdk.common.utils;


import android.content.Context;
import android.content.res.AssetManager;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.zip.CRC32;

import timber.log.Timber;

public class CRCUtil {
    public static String getCRC32(File file) {
        CRC32 crc32 = new CRC32();
        FileInputStream fileinputstream = null;
        try {
            fileinputstream = new FileInputStream(file);
            byte[] buffer = new byte[8192];
            int length;
            while (-1 != (length = fileinputstream.read(buffer))) {
                crc32.update(buffer, 0, length);
            }
            return Long.toHexString(crc32.getValue());
        } catch (IOException e) {
            Timber.e(e, "copyAssetFile getCRC32 %s IOException", file);
        } finally {
            if (fileinputstream != null) {
                try {
                    fileinputstream.close();
                } catch (IOException e) {
                }
            }
        }
        return Long.toHexString(crc32.getValue());
    }

    public static String getAssetFileSCRC32(Context contex, String resName) {
        CRC32 crc32 = new CRC32();
        AssetManager assetManager = contex.getAssets();
        InputStream is = null;
        java.io.ByteArrayOutputStream bout = null;
        try {
            is = assetManager.open(resName);
            bout = new java.io.ByteArrayOutputStream();
            byte[] buffer = new byte[8192];
            byte[] bufferByte = new byte[1024];
            int l = 0;
            while ((l = is.read(buffer)) > -1) {
                crc32.update(buffer, 0, l);
            }
        } catch (IOException e) {
            Timber.e(e, "decodeAssetResData %s not found", resName);
            return null;
        } catch (OutOfMemoryError e) {
            Timber.e(e, "decodeAssetResData %s OutOfMemoryError", resName);
            return null;
        } catch (Exception e) {
            Timber.e(e, "decodeAssetResData %s exception", resName);
            return null;
        } finally {
            try {
                if (bout != null) {
                    bout.close();
                }
                if (is != null) {
                    is.close();
                }
            } catch (IOException e) {
            }
        }
        return Long.toHexString(crc32.getValue());
    }
}
