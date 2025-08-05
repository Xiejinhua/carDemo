package com.desaysv.psmap.base.utils

import android.text.TextUtils
import com.autosdk.bussiness.common.utils.FileUtils
import timber.log.Timber
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileInputStream
import java.io.FileNotFoundException
import java.io.FileOutputStream
import java.text.DecimalFormat


/**
 * 自定义文件保存settingData，获取settingData
 */
object CustomFileUtils {
    /**
     * 字符串保存到手机内存设备中
     * @param str
     */
    fun saveFile(str: String, fileName: String) { // 创建String对象保存文件名路径
        if (TextUtils.isEmpty(fileName)) {
            return
        }
        val file = File(BaseConstant.SETTING_DATA_PATH, fileName)
        try {
            if (file.exists()) {
                file.delete()
            }
            file.createNewFile()
            FileOutputStream(file).use { outStream ->
                outStream.write(str.toByteArray())
                outStream.flush()
            }
        } catch (e: Exception) {
            Timber.d("saveFile e:%s", e.message)
        }
    }

    /**
     * 读取文件里面的内容
     * @return
     */
    fun getFile(fileName: String): String? {
        if (TextUtils.isEmpty(fileName)) {
            return null
        }
        var result: String? = null
        val file = File(BaseConstant.SETTING_DATA_PATH, fileName)
        try {
            if (file.exists()) {
                FileInputStream(file).use { fis ->
                    ByteArrayOutputStream().use { baos ->
                        val buffer = ByteArray(1024)
                        var len: Int
                        while (fis.read(buffer).also { len = it } != -1) {
                            baos.write(buffer, 0, len)
                        }
                        result = String(baos.toByteArray())
                    }
                }
            }
        } catch (e: Exception) {
            Timber.d("getFile e:%s", e.message)
        }
        return result
    }


    /**
     * 获取文件指定文件的指定单位的大小
     * @param filePath 文件路径
     * @param sizeType 获取大小的类型1为B、2为KB、3为MB、4为GB
     * @return double值的大小
     */
    fun getFileOrFilesSize(filePath: String?, sizeType: Int): Double {
        val file = filePath?.let { File(it) }
        var blockSize: Long = 0
        if (file != null && !file.exists()) {
            return 0.0
        }
        try {
            if (file != null) {
                blockSize = if (file.isDirectory) {
                    getFileSizes(file)
                } else {
                    getFileSize(file)
                }
            }
        } catch (e: Exception) {
            Timber.e("获取文件大小失败!")
        }
        return formetFileSize(blockSize, sizeType)
    }

    /**
     * 调用此方法自动计算指定文件或指定文件夹的大小
     * @param filePath 文件路径
     * @return 计算好的带B、KB、MB、GB的字符串
     */
    fun getAutoFileOrFilesSize(filePath: String?): String {
        val file = filePath?.let { File(it) }
        var blockSize: Long = 0
        try {
            if (file != null) {
                blockSize = if (file.isDirectory) {
                    getFileSizes(file)
                } else {
                    getFileSize(file)
                }
            }
        } catch (e: Exception) {
            Timber.e("获取文件大小失败!")
        }
        return formetFileSize(blockSize)
    }

    /**
     * 获取指定文件大小
     * @param file
     * @return
     * @throws Exception
     */
    @Throws(java.lang.Exception::class)
    private fun getFileSize(file: File): Long {
        var size: Long = 0
        if (file.exists()) {
            var fis: FileInputStream? = null
            try {
                fis = FileInputStream(file)
                size = fis.available().toLong()
            } catch (e: java.lang.Exception) {
                e.printStackTrace()
            } finally {
                fis?.close()
            }
        } else {
            Timber.e("获取文件大小不存在!")
        }
        return size
    }

    /**
     * 获取指定文件夹
     * @param f
     * @return
     * @throws Exception
     */
    @Throws(Exception::class)
    private fun getFileSizes(f: File): Long {
        var size: Long = 0
        val flist = f.listFiles()
        for (i in flist.indices) {
            size = if (flist[i].isDirectory) {
                size + getFileSizes(flist[i])
            } else {
                size + getFileSize(flist[i])
            }
        }
        return size
    }

    /**
     * 转换文件大小
     * @param fileS
     * @return
     */
    fun formetFileSize(fileS: Long): String {
        val df = DecimalFormat("#.0")
        var fileSizeString = ""
        val wrongSize = "0B"
        if (fileS == 0L) {
            return wrongSize
        }
        fileSizeString = if (fileS < 1024) {
            df.format(fileS.toDouble()) + "B"
        } else if (fileS < 1048576) {
            df.format(fileS.toDouble() / 1024) + "KB"
        } else if (fileS < 1073741824) {
            df.format(fileS.toDouble() / 1048576) + "MB"
        } else {
            df.format(fileS.toDouble() / 1073741824) + "GB"
        }
        return fileSizeString
    }

    /**
     * 转换文件大小,指定转换的类型
     * @param fileS
     * @param sizeType
     * @return
     */
    private fun formetFileSize(fileS: Long, sizeType: Int): Double {
        val df = DecimalFormat("#.0")
        var fileSizeLong = 0.0
        when (sizeType) {
            BaseConstant.SIZE_TYPE_B -> fileSizeLong = java.lang.Double.valueOf(df.format(fileS.toDouble()))
            BaseConstant.SIZE_TYPE_KB -> fileSizeLong = java.lang.Double.valueOf(df.format(fileS.toDouble() / 1024))
            BaseConstant.SIZE_TYPE_MB -> fileSizeLong = java.lang.Double.valueOf(df.format(fileS.toDouble() / 1048576))
            BaseConstant.SIZE_TYPE_GB -> fileSizeLong = java.lang.Double.valueOf(df.format(fileS.toDouble() / 1073741824))
            else -> {}
        }
        return fileSizeLong
    }

    //判断文件是否存在
    fun isExistFile(fileStr: String): Boolean {
        if (TextUtils.isEmpty(fileStr)) {
            return false
        }
        val file = File(fileStr)
        try {
            return file.exists()
        } catch (e: Exception) {
            Timber.d("saveFile e:%s", e.message)
            return false
        }
    }

    /**
     * 读取文件里面的内容
     *
     * @return
     */
    fun getGpsPointsFile(id: String): String? {
        var result: String? = null
        var fis: FileInputStream? = null
        var baos: ByteArrayOutputStream? = null
        try {
            val file: File = File(BaseConstant.NAV_MAP_ADAS + id + "/", "pose.txt") // 创建文件
            if (file.exists()) { // 创建FileInputStream对象
                var data: ByteArray? = ByteArray(0)
                fis = FileInputStream(file)
                val b = ByteArray(1024) // 创建字节数组 每次缓冲1M
                var len = 0 // 一次读取1024字节大小，没有数据后返回-1.
                baos = ByteArrayOutputStream() // 创建ByteArrayOutputStream对象
                while ((fis.read(b).also { len = it }) != -1) { // 一次读取1024个字节，然后往字符输出流中写读取的字节数
                    baos.write(b, 0, len)
                }
                data = baos.toByteArray() // 将读取的字节总数生成字节数组

                result = String(data) //字符串对象
            }
        } catch (e: FileNotFoundException) {
            Timber.e("catch Exception:$e")
        } catch (e: NullPointerException) {
            Timber.e("catch Exception:$e")
        } catch (e: java.lang.Exception) {
            Timber.e("catch Exception:$e")
        } finally {
            FileUtils.safetyClose(baos)
            FileUtils.safetyClose(fis)
        }
        return result
    }

    fun getLatestFile(directoryPath: String): File? {
        val dir = File(directoryPath)
        if (!dir.exists() || !dir.isDirectory) return null

        val files = dir.listFiles() ?: return null
        if (files.isEmpty()) return null

        // 按最后修改时间排序，最新的排在最前面
        return files.maxByOrNull { it.lastModified() }
    }

}