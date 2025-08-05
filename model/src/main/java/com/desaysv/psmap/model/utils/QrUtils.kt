package com.desaysv.psmap.model.utils

import android.graphics.Bitmap
import android.graphics.Color
import com.google.zxing.BarcodeFormat
import com.google.zxing.EncodeHintType
import com.google.zxing.WriterException
import com.google.zxing.common.BitMatrix
import com.google.zxing.qrcode.QRCodeWriter
import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel
import timber.log.Timber
import java.io.ByteArrayOutputStream
import java.io.IOException
import java.util.EnumMap


/**
 * Author : wangmansheng
 * Date : 2024-1-17
 * Description : 二维码工具类
 */
object QrUtils {
    fun createQRImage(content: String?, widthPix: Int, heightPix: Int): Bitmap? {
        try {
            content?.let {
                //配置参数
                val hints: MutableMap<EncodeHintType, Any?> = EnumMap(EncodeHintType::class.java)
                hints[EncodeHintType.CHARACTER_SET] = "utf-8"
                //容错级别
                hints[EncodeHintType.ERROR_CORRECTION] = ErrorCorrectionLevel.H
                //设置空白边距的宽度
                hints[EncodeHintType.MARGIN] = 0 //default is 4

                // 图像数据转换，使用了矩阵转换
                val bitMatrix =
                    QRCodeWriter().encode(content, BarcodeFormat.QR_CODE, widthPix, heightPix, hints)
                val pixels = IntArray(widthPix * heightPix)
                // 下面这里按照二维码的算法，逐个生成二维码的图片，
                // 两个for循环是图片横列扫描的结果
                for (y in 0 until heightPix) {
                    for (x in 0 until widthPix) {
                        if (bitMatrix[x, y]) {
                            pixels[y * widthPix + x] = -0x1000000
                        } else {
                            pixels[y * widthPix + x] = -0x1
                        }
                    }
                }
                // 生成二维码图片的格式，使用ARGB_8888
                val bitmap = Bitmap.createBitmap(widthPix, heightPix, Bitmap.Config.ARGB_8888)
                bitmap!!.setPixels(pixels, 0, widthPix, 0, 0, widthPix, heightPix)
                val byteArrayOutputStream = ByteArrayOutputStream()
                //必须使用compress方法将bitmap保存到文件中再进行读取。直接返回的bitmap是没有任何压缩的，内存消耗巨大！
                bitmap.compress(Bitmap.CompressFormat.JPEG, 100, byteArrayOutputStream)
                byteArrayOutputStream.reset()
                return bitmap
            }
        } catch (e: WriterException) {
            Timber.d("WriterException ${e.message}")
        } catch (e: IOException) {
            Timber.d("IOException ${e.message}")
        }
        return null
    }

    /**
     * 将指定的内容生成成二维码
     * @param content 将要生成二维码的内容
     * @return 返回生成好的二维码事件
     */
    fun create2DCode(content: String?, width: Int, height: Int, margin: Int): Bitmap? {
        var width = width
        var height = height
        try {
            val hints = HashMap<EncodeHintType, Any>()
            hints[EncodeHintType.CHARACTER_SET] = "utf-8"
            hints[EncodeHintType.ERROR_CORRECTION] = ErrorCorrectionLevel.H
            hints[EncodeHintType.MARGIN] = 1
            var matrix = QRCodeWriter().encode(content, BarcodeFormat.QR_CODE, width, height)
            matrix = updateBit(matrix, margin) //删除白边
            width = matrix.width
            height = matrix.height
            val pixels = IntArray(width * height)
            for (y in 0 until height) {
                for (x in 0 until width) {
                    if (matrix[x, y]) {
                        pixels[y * width + x] = Color.BLACK
                    } else {
                        pixels[y * width + x] = Color.TRANSPARENT
                    }
                }
            }
            val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
            bitmap.setPixels(pixels, 0, width, 0, 0, width, height)
            return bitmap
        } catch (e: Exception) {
            Timber.d("exception e:${e.message}")
            return null
        }
    }

    /**
     * 将指定的内容生成成二维码
     * @param content 将要生成二维码的内容
     * @return 返回生成好的二维码事件
     */
    fun create2DCode(content: String?, width: Int, height: Int, margin: Int, borderWidth: Int, borderColor: Int): Bitmap? {
        var width = width
        var height = height
        try {
            val hints = HashMap<EncodeHintType, Any>()
            hints[EncodeHintType.CHARACTER_SET] = "utf-8"
            hints[EncodeHintType.ERROR_CORRECTION] = ErrorCorrectionLevel.H
            hints[EncodeHintType.MARGIN] = 1
            var matrix = QRCodeWriter().encode(content, BarcodeFormat.QR_CODE, width, height)
            matrix = updateBit(matrix, margin) //删除白边
            width = matrix.width
            height = matrix.height
            val pixels = IntArray(width * height)
            for (y in 0 until height) {
                for (x in 0 until width) {
                    if (matrix[x, y]) {
                        pixels[y * width + x] = Color.BLACK
                    } else if ((x < borderWidth || y < borderWidth || width - x <= borderWidth) || (height - y) <= borderWidth) {
                        pixels[y * width + x] = borderColor
                    } else {
                        pixels[y * width + x] = Color.TRANSPARENT
                    }
                }
            }
            val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
            bitmap.setPixels(pixels, 0, width, 0, 0, width, height)
            return bitmap
        } catch (e: Exception) {
            Timber.d("exception e:${e.message}")
            return null
        }
    }

    private fun updateBit(matrix: BitMatrix, margin: Int): BitMatrix {
        val tempM = margin * 2
        val rec = matrix.enclosingRectangle //获取二维码图案的属性
        val resWidth = rec[2] + tempM
        val resHeight = rec[3] + tempM
        val resMatrix = BitMatrix(resWidth, resHeight) // 按照自定义边框生成新的BitMatrix
        resMatrix.clear()
        for (i in margin until resWidth - margin) { //循环，将二维码图案绘制到新的bitMatrix中
            for (j in margin until resHeight - margin) {
                if (matrix[i - margin + rec[0], j - margin + rec[1]]) {
                    resMatrix[i] = j
                }
            }
        }
        return resMatrix
    }
}