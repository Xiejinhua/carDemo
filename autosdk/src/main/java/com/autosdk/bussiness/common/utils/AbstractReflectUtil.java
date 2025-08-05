package com.autosdk.bussiness.common.utils;


/**
 * 单位转换工具
 */
public abstract class AbstractReflectUtil {

    public static Number toValue(Number value, Class<? extends Object> type) {
        if (type == Long.class) {
            return value.longValue();
        } else if (type == Integer.class) {
            return value.intValue();
        } else if (type == Short.class) {
            return value.shortValue();
        } else if (type == Byte.class) {
            return value.byteValue();
        } else if (type == Double.class) {
            return value.doubleValue();
        } else if (type == Float.class) {
            return value.floatValue();
        } else {
            Class<? extends Object> clazz = AbstractReflectUtil.toWrapper(type);
            if (clazz == type) {
                return null;
            } else {
                return toValue(value, clazz);
            }
        }
    }

    public final static Class<? extends Object> toWrapper(
            Class<? extends Object> type) {
        if (type.isPrimitive()) {
            if (Byte.TYPE == type) {
                return Byte.class;
            } else if (Short.TYPE == type) {
                return Short.class;
            } else if (Integer.TYPE == type) {
                return Integer.class;
            } else if (Long.TYPE == type) {
                return Long.class;
            } else if (Float.TYPE == type) {
                return Float.class;
            } else if (Double.TYPE == type) {
                return Double.class;
            } else if (Character.TYPE == type) {
                return Character.class;
            } else if (Boolean.TYPE == type) {
                return Boolean.class;
            }
        }
        return type;
    }
}
