package com.tom.chat.entity.cometd;

public class authMessage {

    /**
     * 平台
     */
    private String platform;

    private String platformAuthUrl;

    /**
     * 平台token
     */
    private String token;

    public String getPlatform() {
        return platform;
    }

    public void setPlatform(String platform) {
        this.platform = platform;
    }

    public String getPlatformAuthUrl() {
        return platformAuthUrl;
    }

    public void setPlatformAuthUrl(String platformAuthUrl) {
        this.platformAuthUrl = platformAuthUrl;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    @Override
    public String toString() {
        return "authMessage{" +
                "platform='" + platform + '\'' +
                ", platformAuthUrl='" + platformAuthUrl + '\'' +
                ", token='" + token + '\'' +
                '}';
    }
}
