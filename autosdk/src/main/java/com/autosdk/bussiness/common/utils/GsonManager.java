package com.autosdk.bussiness.common.utils;


import com.google.gson.Gson;

import java.lang.reflect.Type;

/**
 * @author AutoSDK
 */
public class GsonManager {

    public static final String TAG = GsonManager.class.getSimpleName();
    private static Gson gson;
    private volatile static GsonManager gsonManager;

    private GsonManager() {
        gson = new Gson();
    }

    public static GsonManager getInstance() {
        if (gsonManager == null) {
            synchronized (GsonManager.class) {
                if (gsonManager == null) {
                    gsonManager = new GsonManager();
                }
            }
        }
        return gsonManager;
    }

    public <T> T fromJson(String json, Type type) {
        return gson.fromJson(json, type);
    }

    public <T> T fromJson(String json, Class<T> clz) {
        return gson.fromJson(json, clz);
    }

    public String toJson(Object src) {
        if (src == null) {
            return "";
        }
        return gson.toJson(src);
    }
}
