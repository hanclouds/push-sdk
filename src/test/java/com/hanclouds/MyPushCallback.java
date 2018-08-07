package com.hanclouds;

import org.apache.commons.codec.binary.Base64;

public class MyPushCallback implements PushCallback {

    @Override
    public void onDisconnected() {
        System.out.println("the subscribe is disconnected from the server");
    }

    @Override
    public void onRecvDataInt(String topic, int data) {
        System.out.printf("receive data from %s, data = %d", topic, data);
        System.out.println();
    }

    @Override
    public void onRecvDataString(String topic, String data) {
        System.out.printf("receive data from %s, data = %s", topic, data);
        System.out.println();
    }

    @Override
    public void onRecvDataJson(String topic, String json) {
        System.out.printf("receive data from %s, data = %s", topic, json);
        System.out.println();
    }

    @Override
    public void onRecvDataDouble(String topic, Double data) {
        System.out.printf("receive data from %s, data = %f", topic, data);
        System.out.println();
    }

    @Override
    public void onRecvDataBin(String topic, byte[] data) {
        System.out.printf("receive data from %s, data = %s", topic, new String(Base64.encodeBase64(data)));
        System.out.println();
    }

    @Override
    public void onDeviceConnect(String deviceKey) {
        System.out.printf("device %s is connected", deviceKey);
        System.out.println();
    }

    @Override
    public void onDeviceDisconnect(String deviceKey) {
        System.out.printf("device %s is disconnected", deviceKey);
        System.out.println();
    }

    @Override
    public void onDeviceOnline(String deviceKey) {
        System.out.printf("device %s is continue online", deviceKey);
        System.out.println();
    }

    @Override
    public void onRecv(String topic, byte[] data) {
        System.out.printf("recv data on topic %s, data=%s", topic, new String(data));
        System.out.println();
    }
}
