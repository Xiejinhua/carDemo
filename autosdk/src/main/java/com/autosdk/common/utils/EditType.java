package com.autosdk.common.utils;

/**
 * Created by AutoSdk on 2020/10/30.
 **/
public enum EditType {
    /**
     * 各编辑状态
     */
    EDIT_TOP("置顶"),
    EDIT_RENAME("重命名"),
    EDIT_CANCLEFAVORITE("取消收藏"),
    EDIT_CANCLETOP("取消置顶"),
    EDIT_CHANGEADDRESS("修改地址"),
    EDIT_DELETE("删除"),
    EDIT_CLOSE("关闭"),
    EDIT_DEL_LOG("删除记录"),
    EDIT_FAVORITE("收藏");

    String value;

    EditType(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }
}
