package com.autosdk.bussiness.widget.ui.util;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.List;

/**
 * List工具类
 */
public class ListUtil {

    /**
     * 判断List是否为空
     */
    public static boolean isEmpty(ArrayList list) {
        if (list == null) {
            return true;
        }
        return list.size() == 0;
    }


    /**
     * 判断List是否为空
     */
    public static boolean isEmpty(List list) {
        if (list == null) {
            return true;
        }
        return list.size() == 0;
    }


    /**
     * List深度拷贝
     * @param src
     */
    public static <E> List<E> deepCopy(List<E> src) {
        try {
            ByteArrayOutputStream byteOut = new ByteArrayOutputStream();
            ObjectOutputStream out = new ObjectOutputStream(byteOut);
            out.writeObject(src);

            ByteArrayInputStream byteIn = new ByteArrayInputStream(byteOut.toByteArray());
            ObjectInputStream in = new ObjectInputStream(byteIn);
            @SuppressWarnings("unchecked")
            List<E> dest = (List<E>) in.readObject();
            return dest;
        } catch (Exception e) {
            e.printStackTrace();
            return new ArrayList<E>();
        }
    }

}
