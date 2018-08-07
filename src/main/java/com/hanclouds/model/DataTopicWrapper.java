package com.hanclouds.model;

import com.hanclouds.util.StringUtils;

import java.util.HashSet;

/**
 * data/{productKey}/{deviceKey}/{streamName}/{datatype}
 *
 * @author szl
 * @date 2018/3/28
 */
public class DataTopicWrapper {

    private final static String TOPIC_DATA_PREFIX = "data/";
    private final static HashSet<String> DATA_TYPE_SET = new HashSet<>();
    private final static int ITEM_COUNT_DATA_TOPIC = 5;

    static {
        DATA_TYPE_SET.add("bin");
        DATA_TYPE_SET.add("json");
        DATA_TYPE_SET.add("double");
        DATA_TYPE_SET.add("string");
        DATA_TYPE_SET.add("int");
    }

    private String topic;
    private String productKey;
    private String deviceKey;
    private String stream;
    private String dataType;

    public boolean init(String topic) {
        this.topic = topic;
        if (StringUtils.isEmpty(topic)) {
            return false;
        }
        if (topic.startsWith(TOPIC_DATA_PREFIX)) {
            String[] result = topic.split("/");
            if (result.length == ITEM_COUNT_DATA_TOPIC) {
                productKey = result[1];
                deviceKey = result[2];
                stream = result[3];
                dataType = result[4];
                if (!DATA_TYPE_SET.contains(dataType)) {
                    return false;
                }
                return true;
            }
        }
        return false;
    }

    public String productKey() {
        return productKey;
    }

    public String deviceKey() {
        return deviceKey;
    }

    public String stream() {
        return stream;
    }

    public String dataType() {
        return dataType;
    }
}
