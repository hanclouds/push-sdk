package com.hanclouds;

import java.util.List;

/**
 * 连接推送平台的客户端的操作接口
 *
 * @author szl
 * @date 2018/8/6
 */
public interface PushClient {

    /**
     * 连接推送服务器，并订阅某些topic
     * 其内部步骤如下：
     * 先向HanClouds 公开的restful api 申请连接推送平台的接入凭证
     * 用接入凭证作为mqtt Connect消息中的鉴权参数接入推送平台
     * 在mqtt连接上用subscribe消息订阅感兴趣的数据。
     *
     * @param productKey  productKey
     * @param queryKey    queryKey
     * @param querySecret querySecret
     * @param filters     想订阅的filter
     * @param callback    推送回调
     * @param transType   传输类型 0表示TCP、1标识WSS
     * @param secure      是否启用加密模式。加密模式时，推送平台会把数据加密后再发送给订阅者
     * @return 订阅成功返回true，订阅失败返回false
     */
    boolean subscribe(String productKey, String queryKey, String querySecret, List<String> filters
            , PushCallback callback, int transType, boolean secure);

    /**
     * 断开本订阅者与推送平台的socket连接
     */
    void disconnect();

    /**
     * 判别本订阅者当前是否已经连上服务器
     *
     * @return
     */
    boolean isConnected();
}
