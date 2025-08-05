package com.autosdk.common.utils;

import android.text.TextUtils;

import androidx.annotation.Nullable;

import com.autonavi.gbl.common.observer.ITBTResReader;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;

import static com.autosdk.common.utils.FileUtils.safetyClose;

public class TBTResReaderImpl implements ITBTResReader {

    public static byte[] readAllBytes(String filePath) {
        if (TextUtils.isEmpty(filePath)) {
            return new byte[0];
        }
        return readAllBytes(new File(filePath));
    }

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
     * @return 数据长度
     * @brief 读取资源文件流
     * @details 此接口用于引擎读取资源文件。HMI提供宿主平台的文件io能力。
     * @param[in] strFile          文件名：用户底层已经拼接好完整路径，HMI不需要再拼接修改。
     * @remark 外界提供读取res资源文件的能力。
     * 引擎首先会从初始化设置的rootPath目录读取，如果读取不到在通过该接口读取。
     * 如果没有设置该类，则只从rootPaht目录读取。
     */
    @Override
    public byte[] readFile(String s) {
        return FileUtils.readAllBytes(s);
    }

    @Override
    public void release(byte[] tbtResData) {
    }
}
