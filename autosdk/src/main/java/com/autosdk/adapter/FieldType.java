package com.autosdk.adapter;

import androidx.annotation.IntDef;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * @author AutoSDK
 */
@IntDef({
    FieldType.STRING,
    FieldType.DOUBLE,
    FieldType.BOOLEAN,
    FieldType.DATE,
    FieldType.SHORT,
    FieldType.LIST,
    FieldType.INT,
    FieldType.FLOAT,
    FieldType.LONG,
    FieldType.OBJECT,
    FieldType.UNKOWN,
})
@Retention(RetentionPolicy.SOURCE)
public @interface FieldType {
    int STRING = 100;
    int DOUBLE = 101;
    int BOOLEAN = 102;
    int DATE = 103;
    int SHORT = 104;
    int LIST = 105;
    int INT = 106;
    int FLOAT = 107;
    int LONG = 108;
    int OBJECT = 109;
    int UNKOWN = 110;
}
