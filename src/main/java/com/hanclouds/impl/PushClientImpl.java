package com.hanclouds.impl;

import com.hanclouds.HanCloudsClient;
import com.hanclouds.PushCallback;
import com.hanclouds.PushClient;
import com.hanclouds.model.AppAuth;
import com.hanclouds.model.DataTopicWrapper;
import com.hanclouds.model.DeviceConnEventTopicWrapper;
import com.hanclouds.req.DataPushRequest;
import com.hanclouds.resp.DataPushResponse;
import org.eclipse.paho.client.mqttv3.*;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.List;

/**
 * 推送平台客户端的实现类
 *
 * @author szl
 * @date 2018/8/7
 */
public class PushClientImpl implements PushClient {

    private final static int AES_KEY_SIZE = 16;
    private final static String AES_CBC = "AES/CBC/PKCS5Padding";
    private final static String CHARSET_UTF8 = "utf-8";
    /**
     * the api url used to request the subscribe token
     */
    private final static String HANCLOUDS_API_URL = "https://api.hanclouds.com/api/v1";
    /**
     * the push server address with web socket ssl
     */
    private final static String PUSH_SERVER_ADDRESS_WSS = "wss://push.hanclouds.com:4883";
    /**
     * the push server address with tcp
     */
    private final static String PUSH_SERVER_ADDRESS_TCP = "tcp://push.hanclouds.com:3883";
    private final static int PUSH_TRANSPORT_TCP = 0;
    private final static int PUSH_TRANSPORT_WSS = 1;
    private static Logger logger = LoggerFactory.getLogger(PushClientImpl.class);
    private MqttClient mqttClient = null;
    private String secret = null;

    @Override
    public boolean subscribe(String productKey, String queryKey, String querySecret, List<String> filters, PushCallback callback, int transType, boolean secure) {
        if (mqttClient != null) {
            logger.warn("the subscribe client start disconnect");
            disconnect();
        }

        String address;
        switch (transType) {
            case PUSH_TRANSPORT_TCP: {
                address = PUSH_SERVER_ADDRESS_TCP;
            }
            break;
            case PUSH_TRANSPORT_WSS: {
                address = PUSH_SERVER_ADDRESS_WSS;
            }
            break;
            default: {
                address = PUSH_SERVER_ADDRESS_WSS;
            }
        }

        AppAuth appAuth = getSubscribeToken(productKey, queryKey, querySecret, filters);
        if (appAuth == null) {
            logger.error("create subscribe token failed!");
            return false;
        }

        String clientId;
        if (!secure) {
            clientId = "p:" + productKey + ":" + appAuth.getUserName();
        } else {
            clientId = "ps:" + productKey + ":" + appAuth.getUserName();
            this.secret = appAuth.getSecret();
        }

        MemoryPersistence persistence = new MemoryPersistence();
        MqttConnectOptions connOpts = new MqttConnectOptions();
        connOpts.setCleanSession(true);
        connOpts.setUserName(appAuth.getUserName());
        connOpts.setPassword(appAuth.getPassword().toCharArray());
        connOpts.setConnectionTimeout(30);
        connOpts.setKeepAliveInterval(50);
        connOpts.setCleanSession(true);
        connOpts.setAutomaticReconnect(false);

        try {
            mqttClient = new MqttClient(address, clientId, persistence);
            mqttClient.setCallback(new MqttCallback() {
                @Override
                public void connectionLost(Throwable cause) {
                    disconnect();
                    System.out.println("the subscribe client start disconnect" + "      cause:" + cause.getMessage());
                    callback.onDisconnected();
                }

                @Override
                public void messageArrived(String topic, MqttMessage message) {
                    if (callback == null) {
                        return;
                    }
                    try {
                        byte[] rcvData = message.getPayload();
                        if (secret != null && rcvData != null) {
                            try {
                                rcvData = decodeWithAesCbc(secret, rcvData);
                            } catch (Exception e) {
                                logger.error("decode with aes failed!");
                                return;
                            }
                        }
                        DataTopicWrapper dataTopicWrapper = new DataTopicWrapper();
                        DeviceConnEventTopicWrapper deviceConnEventTopicWrapper = new DeviceConnEventTopicWrapper();
                        if (dataTopicWrapper.init(topic)) {
                            switch (dataTopicWrapper.dataType()) {
                                case "int": {
                                    ByteBuffer byteBuffer = ByteBuffer.wrap(rcvData);
                                    Integer value = byteBuffer.asIntBuffer().get();
                                    callback.onRecvDataInt(topic, value);
                                }
                                break;
                                case "double": {
                                    ByteBuffer byteBuffer = ByteBuffer.wrap(rcvData);
                                    Double value = byteBuffer.asDoubleBuffer().get();
                                    callback.onRecvDataDouble(topic, value);
                                }
                                break;
                                case "string": {
                                    String value = new String(rcvData);
                                    callback.onRecvDataString(topic, value);
                                }
                                break;
                                case "json": {
                                    String value = new String(rcvData);
                                    callback.onRecvDataJson(topic, value);
                                }
                                break;
                                case "bin": {
                                    callback.onRecvDataBin(topic, rcvData);
                                }
                                break;
                                default: {
                                    logger.error("invalid data type. value = {}", dataTopicWrapper.dataType());
                                }
                            }
                        } else if (deviceConnEventTopicWrapper.init(topic)) {
                            switch (deviceConnEventTopicWrapper.event()) {
                                case "connect": {
                                    callback.onDeviceConnect(deviceConnEventTopicWrapper.deviceKey());
                                }
                                break;
                                case "disconnect": {
                                    callback.onDeviceDisconnect(deviceConnEventTopicWrapper.deviceKey());

                                }
                                break;
                                case "online": {
                                    callback.onDeviceOnline(deviceConnEventTopicWrapper.deviceKey());
                                }
                                break;
                                default: {
                                    logger.error("invalid device event. value = {}", deviceConnEventTopicWrapper.event());
                                }
                            }
                        } else {
                            callback.onRecv(topic, rcvData);
                        }
                    } catch (Exception e) {
                        logger.error("error occur in messageArrived(...). e = {}", e);
                    }
                }

                @Override
                public void deliveryComplete(IMqttDeliveryToken token) {
                }
            });
            mqttClient.connect(connOpts);
            String[] filter = filters.toArray(new String[0]);
            int[] qos = new int[filters.size()];
            for (int i = 0; i < qos.length; i++) {
                qos[i] = 0;
            }
            mqttClient.subscribe(filter, qos);
        } catch (Exception e) {
            try {
                mqttClient.close(true);
                mqttClient = null;
            } catch (MqttException e1) {
                logger.error("error occur. e = {}", e1);
            }
            return false;
        }
        return true;
    }

    @Override
    public void disconnect() {
        if (mqttClient != null) {
            try {
                mqttClient.disconnectForcibly();
                mqttClient.close(true);
                mqttClient = null;
            } catch (Exception e) {
            }
        }
        secret = null;
    }

    @Override
    public boolean isConnected() {
        if (mqttClient == null) {
            return false;
        }
        try {
            return mqttClient.isConnected();
        } catch (Exception e) {
            return false;
        }
    }

    private AppAuth getSubscribeToken(String productKey, String queryKey, String querySecret, List<String> filters) {
        HanCloudsClient hanCloudsClient = new HanCloudsClient(HANCLOUDS_API_URL);
        hanCloudsClient.putProductAuthParams(productKey, queryKey, querySecret);

        DataPushRequest request = new DataPushRequest();
        request.setProductKey(productKey);
        request.setTopics(filters);
        DataPushResponse response;

        try {
            response = hanCloudsClient.execute(request);
            if (response != null && response.isSucceed()) {
                AppAuth appAuth = new AppAuth();
                appAuth.setUserName(response.getResponse().getUserName());
                appAuth.setPassword(response.getResponse().getPassword());
                appAuth.setSecret(response.getResponse().getSecret());
                return appAuth;
            }
            return null;
        } catch (Exception e) {
            logger.error("create data push token failed. {}", e);
        }

        return null;

    }

    private byte[] decodeWithAesCbc(String secret, byte[] content) {
        if (secret.length() < AES_KEY_SIZE) {
            return null;
        }
        if (secret.length() > AES_KEY_SIZE) {
            secret = secret.substring(0, AES_KEY_SIZE);
        }
        try {
            SecretKeySpec key = new SecretKeySpec(secret.getBytes(StandardCharsets.UTF_8), "AES");
            Cipher cipher = Cipher.getInstance(AES_CBC);
            byte[] ivBytes = new byte[16];
            System.arraycopy(content, 0, ivBytes, 0, 16);
            IvParameterSpec iv = new IvParameterSpec(ivBytes);
            cipher.init(Cipher.DECRYPT_MODE, key, iv);
            byte[] encBytes = new byte[content.length - 16];
            System.arraycopy(content, 16, encBytes, 0, encBytes.length);
            return cipher.doFinal(encBytes);
        } catch (Exception e) {
            logger.error("decodeWithAesCbc(...) failed, {}", e.getMessage());
        }
        return null;
    }
}
