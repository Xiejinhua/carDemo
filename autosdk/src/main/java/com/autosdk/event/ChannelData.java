package com.autosdk.event;

import com.autonavi.gbl.consis.ChannelParcel;
import com.autosdk.bussiness.common.utils.GsonManager;
import com.google.gson.JsonSyntaxException;

import org.json.JSONException;
import org.json.JSONObject;

import java.lang.reflect.Type;

import timber.log.Timber;

/**
 * @author AutoSDK
 */
public class ChannelData {

    private final static String TAG = "ChannelData";

    private JSONObject jsonObject;
    private String readString;
    private int action;
    private String data;

    public int getAction() {
        jsonParser();
        return action;
    }

    public <T> T getData(Class<T> t) {
        jsonParser();
        try {
            return GsonManager.getInstance().fromJson(this.data, t);
        } catch (JsonSyntaxException e) {
            Timber.d("getData: JsonSyntaxException is %s", e.toString());
            return null;
        }
    }

    public <T> T getData(Type type) {
        jsonParser();
        return GsonManager.getInstance().fromJson(this.data, type);
    }

    private void jsonParser() {
        if (null != jsonObject) {
            return;
        }
        try {
            jsonObject = new JSONObject(readString);
            action = jsonObject.getInt("action");
            data = jsonObject.getString("data");
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    /**
     * 多屏消息转本地消息
     *
     * @param data
     * @return
     */
    public static ChannelData channelParcelToChannelData(ChannelParcel data) {
        ChannelData channelData = new ChannelData();
        channelData.readString = data.readString();
        return channelData;
    }
}
