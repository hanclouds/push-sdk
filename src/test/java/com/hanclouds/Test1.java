package com.hanclouds;

import org.junit.Test;

import java.util.LinkedList;


public class Test1 {

    @Test
    public void test1() {
        //  构造所需要订阅的topic，注意订阅的topic第二部分为productKey，其他部分任意写。不过随便写可能会订阅不到数据。
        // 订阅某个产品下所有数据、事件和命令响应
        LinkedList<String> topic = new LinkedList<>();
        topic.add("+/fWm3jetY/#");

        // 构造推送平台的客户端
        PushClient pushClient = PushClientFactory.createPushClient();
        // 向推送平台请求订阅这些topic的数据
        pushClient.subscribe("fWm3jetY", "WqCsCkLb", "F4nTqMiB6m1p207f", topic, new MyPushCallback(), 0, true);

        try {
            Thread.sleep(1000);
        } catch (Exception e) {
        }
        //pushClient.disconnect();

        boolean isConnected = pushClient.isConnected();

        if (!isConnected) {
            //重新订阅
            pushClient.subscribe("fWm3jetY", "WqCsCkLb", "F4nTqMiB6m1p207f", topic, new MyPushCallback(), 1, false);
        }
        try {
            Thread.sleep(1000000);
        } catch (Exception e) {
        }
        //pushClient.disconnect();
    }

    @Test
    public void test2() {
        // 订阅某个设备的所有数据和事件
        LinkedList<String> topic = new LinkedList<>();
        topic.add("data/fWm3jetY/0a1c6dfe37ec4255965a5915d6dc583f/#");
        topic.add("event/fWm3jetY/0a1c6dfe37ec4255965a5915d6dc583f/#");

        // 构造推送平台的客户端
        PushClient pushClient = PushClientFactory.createPushClient();
        // 向推送平台请求订阅这些topic的数据
        pushClient.subscribe("fWm3jetY", "WqCsCkLb", "F4nTqMiB6m1p207f", topic, new MyPushCallback(), 0, true);
        try {
            Thread.sleep(100000);
        } catch (Exception e) {
        }
        //pushClient.disconnect();
    }
}
