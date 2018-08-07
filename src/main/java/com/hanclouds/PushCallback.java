package com.hanclouds;

/**
 * 客户端接受推送平台推送数据时的回调接口
 *
 * @author szl
 * @date 2018/8/7
 */
public interface PushCallback {

    /**
     * 当PushClient和服务端断开连接时，会回调此方法
     */
    void onDisconnected();

    /**
     * 当收到推送平台推送的整形的设备数据时，回调此接口
     *
     * @param topic topic
     * @param data  整数值
     */
    void onRecvDataInt(String topic, int data);

    /**
     * 当收到推送平台推送的字符串形式的设备数据时，回调此接口
     *
     * @param topic topic
     * @param data  字符串值
     */
    void onRecvDataString(String topic, String data);

    /**
     * 当收到推送平台推送的json形式的设备数据时，回调此接口
     *
     * @param topic topic
     * @param json  json形式的值
     */
    void onRecvDataJson(String topic, String json);

    /**
     * 当收到推送平台推送的浮点数形式的设备数据时，回调此接口
     *
     * @param topic topic
     * @param data  浮点数形式值
     */
    void onRecvDataDouble(String topic, Double data);

    /**
     * 当收到推送平台推送的二进制形式的设备数据时，回调此接口
     *
     * @param topic topic
     * @param data  二进制形式的设备数据字节数组
     */
    void onRecvDataBin(String topic, byte[] data);

    /**
     * 当收到推送平台推送的设备的上线通知时，回调此接口
     *
     * @param deviceKey deviceKey
     */
    void onDeviceConnect(String deviceKey);

    /**
     * 当收到推送平台推送的设备下线通知时，回调此接口
     *
     * @param deviceKey deviceKey
     */
    void onDeviceDisconnect(String deviceKey);

    /**
     * 当收到推送平台推送的设备继续在线通知时，回调此接口
     * 设备如果一直在线，HanClouds平台会周期性的推送设备继续在线的事件。默认为50分钟推送一次，后续可能会有调整
     *
     * @param deviceKey deviceKey
     */
    void onDeviceOnline(String deviceKey);

    /**
     * 当收到推送平台推送的其他类别的数据时，回调此接口
     *
     * @param topic topic
     * @param data  数据的字节数组
     */
    void onRecv(String topic, byte[] data);
}
