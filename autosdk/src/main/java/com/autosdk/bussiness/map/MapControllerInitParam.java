package com.autosdk.bussiness.map;

import android.content.Context;
import java.util.ArrayList;

/**
 * 主图控制器初始化参数
 */
public class MapControllerInitParam {

    /** 程序运行环境上下文信息内容 */
    public Context mContext;

    /** 地图渲染底图样式文件存放路径 */
    public String mStrDataPath;

    /** 屏幕视图参数列表 */
    public ArrayList<SurfaceViewParam> mSurfaceViewParamArrayList;
}
