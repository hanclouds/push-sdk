package com.hanclouds;

import com.hanclouds.impl.PushClientImpl;

/**
 * PushClient 工厂类
 * 主要为了方便构造PushClient
 * @author szl
 * @date 2018/8/7
 */
public class PushClientFactory {
    public static PushClient createPushClient(){
        return new PushClientImpl();
    }
}
