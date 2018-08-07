package com.hanclouds.model;

/**
 * 设备上下线事件通知的topic
 * @author szl
 * @date 2018/8/7
 *
 * event/{productKey}/{deviceKey}/connect
 * event/{productKey}/{deviceKey}/disconnect
 * event/{productKey}/{deviceKey}/online
 */
public class DeviceConnEventTopicWrapper {

    private final static String TOPIC_EVENT_PREFIX = "event";
    private final static int TOPIC_LENGTH = 4;

    private String productKey;
    private String deviceKey;
    private String event;

    public boolean init(String topic){
        if (topic == null || topic.isEmpty()){
            return false;
        }

        if (!topic.startsWith(TOPIC_EVENT_PREFIX)){
            return false;
        }

        String[] result = topic.split("/");
        if (result.length != TOPIC_LENGTH){
            return false;
        }
        productKey = result[1];
        deviceKey = result[2];
        event = result[3];
        return true;
    }

    public String productKey(){
        return productKey;
    }

    public String deviceKey(){
        return deviceKey;
    }

    public String event(){
        return event;
    }



}
