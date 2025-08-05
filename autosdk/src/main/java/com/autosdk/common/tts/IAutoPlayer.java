package com.autosdk.common.tts;

/**
 * 定义了TTS播报需要的相关接口，不同的项目可通过实现接口来做差异化处理
 */
public interface IAutoPlayer {

    /**
     * 初始化TTS player
     */
    void init();

    /**
     * 释放TTS player
     */
    public void release();

    /**
     * 播报普通文本，不能打断
     *
     * @param text 需要播报的文本
     */
    void playText(String text);

    /**
     * 播报文本,可以打断，导航/巡航等播报
     *
     * @param text 需要播报的文本，如引导信息
     */
    void playTextNavi(String text);

    /**
     * 播报文本，并设置音量
     *
     * @param text         需要播报的文本，如引导信息
     * @param volumPercent 音量,0~100
     */
    void playText(String text, int volumPercent);

    /**
     * 给文本语音自己播报文本
     */
    void playTextByVoiceControl(String text, int type);

    /**
     * 设置音量
     *
     * @param value
     */
    void setVolume(int value);

    /**
     * 获取声道
     *
     * @return
     */
    int getStreamType();

    /**
     * 设置声道
     *
     * @param streamType
     */
    void setStreamType(int streamType);

    /**
     * 停止播报
     */
    void stop(boolean isSimulationNavi);

    /**
     * 是否正在播报
     *
     * @return
     */
    boolean isPlaying();

    /**
     * 叮咚声播报
     *
     * @param type
     */
    void playNaviWarningSound(int type);

    void playArSound(int type);

    /**
     * 对讲录音音效，即开始录音的音效和结束录音的音效（结束后回立刻发送）。
     *
     * @param type
     */
    void playGroupSound(int type);

    /**
     * 注册播报状态的监听
     */
    void registerITTSListener(ITTSListener listener);

    void unregisterITTSListener(ITTSListener listener);

}
