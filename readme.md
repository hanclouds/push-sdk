# 推送平台使用帮助

## 概述

推送平台是一个mqtt服务器集群，是HanClouds实时向外推送设备数据、设备连接事件、命令响应事件等的公共平台，其主要使用场景是用于物联网saas应用中展示数据的实时刷新。web页面或者手机App上可以通过订阅其感兴趣的数据，当设备上传这些数据时，推送平台把这些数据实时推送给web页面或者手机App，因此规避了web/App侧频繁调用API的开销，也增加了实时性。

推送平台的主要特性如下：

- 支持websocket ssl接入，支持TCP接入
- 支持数据加密传输
- 推送平台只给订阅者推送实时数据，不推送历史数据
- 当订阅者网络拥塞时，接收速度跟不上推送平台上数据产生速度时，推送平台会主动丢弃数据

推送平台的工作模型如下：

- 对于设备上传的设备数据，在推送mqtt集群上以`data/{productKey}/{deviceKey}/{stream}/{dataType}`形式的topic publish, 供订阅者订阅。如果没有订阅者订阅，那这条消息publish后就被丢掉了。topic形式如下：

```
data/S0iUubfz/63444e131f99493b9dcc951e315ad175/temperature/int
data/S0iUubfz/63444e131f99493b9dcc951e315ad175/position/string
```

- 对于设备上线、下线事件，推送平台的处理方式和处理设备数据一样，会在mqtt集群上以`event/{productKey}/{deviceKey}/{connect|disconnect|online}`形式的topicpublish

```
event/S0iUubfz/63444e131f99493b9dcc951e315ad175/connect
event/S0iUubfz/63444e131f99493b9dcc951e315ad175/disconnect
event/S0iUubfz/63444e131f99493b9dcc951e315ad175/online
```

- 对于命令响应事件，会在mqtt集群上以`cmd/{productKey}/{deviceKey}/{commandId}`形式的topic publish

```
cmd/S0iUubfz/63444e131f99493b9dcc951e315ad175/0823b250f0f74a80911f7206b6d1ed14
```

- 推送的数据类型后期会增加流计算结果、大数据分析结果等等。

不建议采用推送平台作为物联网saas应用后端和HanClouds之间批量设备数据的传输方案，这主要是因为一方面推送平台的核心目的是推送实时数据，并不推送历史数据；另一方面，如果订阅者网络拥塞时，推送平台并不会缓存数据而是直接丢弃，因此可能会丢失数据。

对于系统间批量数据的传输方案HanClouds提供云接入的方式来解决。可以参考HanClouds云接入方案。

## 推送鉴权模型

在HanClouds上，推送平台鉴权模型主要解决的是某个订阅者是否有权限订阅某些topic filter的数据。如果不鉴权就可以订阅某个topic的数据，那么可能导致数据泄露。推送鉴权其实就是要约定订阅者身份和其所订阅的topic filter之间的权限关系。

为了便于鉴权，推送平台约定topic的形式为如下三部分：`{type}/{productKey}/{other}`

- type 表示数据类型，如`data`、`event`、`cmd`等等
- productKey 为产品的唯一标识
- other部分为各个具体类别的topic等等，为topic中前两部分之外的顺序部分

上述约定中，topic中第二部分为productKey，要订阅某个topic的数据，相当于要读取某个产品下的数据，因此考虑使用产品级的{queryKey:querySecret}进行鉴权。

由于推送平台支持websocket，订阅者可能在web页面或者手机上用js 代码订阅，如果把产品级的鉴权参数传送到web页面上的js代码中，有很大的泄露风险。

为此考虑到的推送鉴权模型如下：

1. 订阅者用productKeyA，queryKeyA，querySecretA等参数调用平台的restful API，申请订阅某些topic filter的数据。
2. 平台收到订阅请求后，抽取topic filter中第二部分，提取productKey，确认所有topic filter中productKey和productKeyA相等，如果不等则直接拒绝；相等则进入下一步。
3. 推送平台用校验productKeyA、queryKeyA、querySecretA是否匹配，如果匹配则进入下一步；如果不匹配则直接拒绝。
4. 推送平台生成一组订阅者用mqtt登陆推送平台时的userName、password、secret反馈给订阅者。
5. 订阅者用第四步中返回的userName、password用mqtt协议Connect推送平台，推送平台验证通过。
6. 订阅者向推送平台发送mqtt subscribe消息，申请订阅其第一步中申请的topic filter。

> 上述userName、password、secret都是一次性的，使用其用mqtt connect到推送平台后就立即失效，再次使用其作为参数连接到推送平台时，会被拒绝。如果订阅者断开和服务端的网络连接，必须再次重新按照上述步骤从第一步开始逐步进行。

订阅filter举例：

* 如果要订阅某个产品下所有设备的数据，则可以订阅topic：data/{productKey}/#
* 如果要订阅某个产品下所有设备的数据、事件、命令响应，则可以订阅 +/{productKey}/#
* 如果要订阅某个产品下某个设备的所有数据流的数据，则可以订阅topic： data/{productKey}/{deviceKey}/#

## 推送模块sdk

为了便于使用HanClouds的推送模块，把上述鉴权过程封装到一个push-sdk模块中。

HanClouds支持的推送方式和公网地址如下：

|             | 接入地址                         | 说明                                       |
| ----------- | -------------------------------- | ------------------------------------------ |
| websocket   | wss://push.hanclouds.com:4883    | 只支持websocket ssl，不支持普通的websocket |
| tcp         | tcp://push.hanclouds.com:3883    | 普通的tcp连接                              |
| restful api | https://api.hanclouds.com/api/v1 | 可以参考HanClouds门户中的api文档           |

代码说明

```java
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
    boolean subscribe(String productKey, String queryKey, String querySecret, List<String> filters, PushCallback callback, int transType, boolean secure);

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
```

subscribe方法中，transType标识用什么传输层连接，有wss和tcp两种方式。secure标识是否对mqtt推送数据的净荷加密。在使用时，如果用wss，建议secure为false，因为wss本身是ssl的。如果是tcp方式，在安全性要求高的场合下，可以考虑设置secure为true。

subscribe后，如果返回true，则标识订阅成功。

callback为外部传入PushCallback的回调接口，具体方法如下：

 ```java
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

 ```











