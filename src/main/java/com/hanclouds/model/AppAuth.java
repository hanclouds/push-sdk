package com.hanclouds.model;

/**
 * 用于App推送的鉴权参数
 *
 * @author huangchen
 * @date 2018/4/23
 */
public class AppAuth {
    /**
     * mqtt用户名
     */
    private String userName;
    /**
     * mqtt密码
     */
    private String password;
    /**
     * 加密密钥
     */
    private String secret;

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getSecret() {
        return secret;
    }

    public void setSecret(String secret) {
        this.secret = secret;
    }

    @Override
    public String toString() {
        return "AppAuth{" +
                "userName='" + userName + '\'' +
                ", password='" + password + '\'' +
                ", secret='" + secret + '\'' +
                '}';
    }
}
