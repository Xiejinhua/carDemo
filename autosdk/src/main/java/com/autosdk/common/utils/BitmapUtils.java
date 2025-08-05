package com.autosdk.common.utils;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.view.View;
import android.widget.ImageView;

import com.autosdk.R;


public class BitmapUtils {
    //节省每次创建时产生的开销，但要注意多线程操作synchronized
    private static final Canvas CANVAS = new Canvas();
    private static final float SCALE = 1;


    public static Bitmap createBitmapFromView(View view) {
        if (view instanceof ImageView) {
            Drawable drawable = ((ImageView) view).getDrawable();
            if (drawable != null && drawable instanceof BitmapDrawable) {
                return ((BitmapDrawable) drawable).getBitmap();
            }
        }
        view.measure(0, 0);
        //调用layout方法布局后，可以得到view的尺寸大小
        view.layout(0, 0, view.getMeasuredWidth(), view.getMeasuredHeight());
        Bitmap bitmap = createBitmapSafely((int) (view.getWidth() * SCALE),
                (int) (view.getHeight() * SCALE), Bitmap.Config.ARGB_8888, 1);
        if (bitmap != null) {
            synchronized (CANVAS) {
                Canvas canvas = CANVAS;
                canvas.setBitmap(bitmap);
                canvas.save();
                // 防止 View 上面有些区域空白导致最终 Bitmap 上有些区域变黑
                canvas.drawColor(ResUtil.getColor(R.color.auto_ui_000000_00));
                canvas.scale(SCALE, SCALE);
                view.draw(canvas);
                canvas.restore();
                canvas.setBitmap(null);
            }
        }
        return bitmap;
    }
    //安全的创建bitmap;如果新建 Bitmap 时产生了 OOM，可以主动进行一次 GC - System.gc()，然后再次尝试创建
    private static Bitmap createBitmapSafely(int width, int height, Bitmap.Config config, int retryCount) {
        try {
            return Bitmap.createBitmap(width, height, config);
        } catch (OutOfMemoryError e) {
            e.printStackTrace();
            if (retryCount > 0) {
                System.gc();
                return createBitmapSafely(width, height, config, retryCount - 1);
            }
            return null;
        }
    }
    /**
     * 转换图片成圆形
     *
     * @param bitmap 传入Bitmap对象
     * @return
     */
    public static Bitmap toRoundBitmap(Bitmap bitmap) {
        int width = bitmap.getWidth();
        int height = bitmap.getHeight();
        float roundPx;
        float left, top, right, bottom, dstLeft, dstTop, dstRight, dstBottom;
        if (width <= height) {
            roundPx = width / 2;
            left = 0;
            top = 0;
            right = width;
            bottom = width;
            height = width;
            dstLeft = 0;
            dstTop = 0;
            dstRight = width;
            dstBottom = width;
        } else {
            roundPx = height / 2;
            float clip = (width - height) / 2.0f;
            left = clip;
            right = width - clip;
            top = 0;
            bottom = height;
            width = height;
            dstLeft = 0;
            dstTop = 0;
            dstRight = height;
            dstBottom = height;
        }

        Bitmap output = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(output);

        final int color = 0xff424242;
        final Paint paint = new Paint();
        final Rect src = new Rect((int) left, (int) top, (int) right, (int) bottom);
        final Rect dst = new Rect((int) dstLeft, (int) dstTop, (int) dstRight, (int) dstBottom);
        final RectF rectF = new RectF(dst);

        paint.setAntiAlias(true);// 设置画笔无锯齿

        canvas.drawARGB(0, 0, 0, 0); // 填充整个Canvas
        paint.setColor(color);

        // 以下有两种方法画圆,drawRounRect和drawCircle
        // canvas.drawRoundRect(rectF, roundPx, roundPx, paint);// 画圆角矩形，第一个参数为图形显示区域，第二个参数和第三个参数分别是水平圆角半径和垂直圆角半径。
        canvas.drawCircle(roundPx, roundPx, roundPx, paint);

        paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC_IN));// 设置两张图片相交时的模式,参考http://trylovecatch.iteye.com/blog/1189452
        canvas.drawBitmap(bitmap, src, dst, paint); //以Mode.SRC_IN模式合并bitmap和已经draw了的Circle

        return output;
    }

    /**
     * 将RGB byte数组转成像素值数组
     *
     * @param bytes RGB数据流
     * @return
     */
    public static int[] convertBytesToColors(byte[] bytes) {
        if (bytes.length == 0) {
            return null;
        }
        //每三位RGB数据生成一个int类型的像素值
        int[] colors = new int[bytes.length / 3];
        int r, g, b;
        for (int i = 0; i < colors.length; i++) {
            r = convertByteToInt(bytes[i * 3]);
            g = convertByteToInt(bytes[i * 3 + 1]);
            b = convertByteToInt(bytes[i * 3 + 2]);

            // 获取RGB分量值通过按位或生成int的像素值
            colors[i] = (r << 16) | (g << 8) | b | 0xFF000000;
        }
        return colors;
    }

    /**
     * 将byte数当成无符号的变量去转化成int
     */
    public static int convertByteToInt(byte b) {
        int heightBit = (int) ((b >> 4) & 0x0F);
        int lowBit = (int) (0x0F & b);
        return heightBit * 16 + lowBit;
    }


}
