package com.desaysv.psmap.model.utils

import android.content.Context
import android.os.Environment
import com.autosdk.common.AutoConstant
import java.io.File
import java.text.DecimalFormat

/**
 * Created by AutoSdk on 2020/12/4.
 */
object DataCleanUtils {
    /**
     * 获取缓存大小
     *
     * @param context
     * @return
     */
    fun getTotalCacheSize(context: Context): String {
        var cacheSize = getFolderSize(File(AutoConstant.MAP_CACHE_DIR))
        if (Environment.getExternalStorageState() == Environment.MEDIA_MOUNTED) {
            cacheSize += getFolderSize(context.externalCacheDir)
        }
        return getFormatSize(cacheSize)
    }

    /**
     * 清除缓存
     *
     * @param context
     */
    fun clearAllCache(context: Context) {
        deleteDir(File(AutoConstant.MAP_CACHE_DIR))
        if (Environment.getExternalStorageState() == Environment.MEDIA_MOUNTED) {
            deleteDir(context.externalCacheDir)
        }
    }

    private fun deleteDir(dir: File?): Boolean {
        if (dir != null && dir.isDirectory) {
            val children = dir.list()
            var i = 0
            while (children != null && i < children.size) {
                val success = deleteDir(File(dir, children[i]))
                if (!success) {
                    return false
                }
                i++
            }
        }
        return dir?.delete() ?: false
    }

    // 获取文件大小
    //Context.getExternalFilesDir() --> SDCard/Android/data/你的应用的包名/files/ 目录，一般放一些长时间保存的数据
    //Context.getExternalCacheDir() --> SDCard/Android/data/你的应用包名/cache/目录，一般存放临时缓存数据
    fun getFolderSize(file: File?): Long {
        var size: Long = 0
        try {
            val fileList = file!!.listFiles()
            if (fileList != null) {
                for (i in fileList.indices) {
                    // 如果下面还有文件
                    size = if (fileList[i].isDirectory) {
                        size + getFolderSize(fileList[i])
                    } else {
                        size + fileList[i].length()
                    }
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return size
    }

    /**
     * 格式化单位
     *
     * @param size
     * @return
     */
    private fun getFormatSize(size: Long): String {
        if (size / (1024 * 1024 * 1024) > 0) {
            val tmpSize = size.toFloat() / (1024 * 1024 * 1024).toFloat()
            val df = DecimalFormat("#.##")
            return df.format(tmpSize.toDouble()) + "GB"
        } else if (size / (1024 * 1024) > 0) {
            val tmpSize = size.toFloat() / (1024 * 1024).toFloat()
            val df = DecimalFormat("#.##")
            return df.format(tmpSize.toDouble()) + "MB"
        } else if (size / 1024 > 0) {
            return (size / 1024).toString() + "KB"
        } else {
            return size.toString() + "B"
        }
    }
}
