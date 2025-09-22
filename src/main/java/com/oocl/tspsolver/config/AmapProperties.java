package com.oocl.tspsolver.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix="amap")
public class AmapProperties {
    private String apiKey;
    private String jsApiKey;
    private String securityCode;
    private int qpsDelayMs;
    private int maxRetries;
    private String endpoint;

    public String getApiKey() {
        return apiKey;
    }

    public void setApiKey(String apiKey) {
        this.apiKey = apiKey;
    }

    public String getJsApiKey() {
        return jsApiKey;
    }

    public void setJsApiKey(String jsApiKey) {
        this.jsApiKey = jsApiKey;
    }

    public int getQpsDelayMs() {
        return qpsDelayMs;
    }

    public void setQpsDelayMs(int qpsDelayMs) {
        this.qpsDelayMs = qpsDelayMs;
    }

    public int getMaxRetries() {
        return maxRetries;
    }

    public void setMaxRetries(int maxRetries) {
        this.maxRetries = maxRetries;
    }

    public String getEndpoint() {
        return endpoint;
    }

    public void setEndpoint(String endpoint) {
        this.endpoint = endpoint;
    }

    public String getSecurityCode() {
        return securityCode;
    }

    public void setSecurityCode(String securityCode) {
        this.securityCode = securityCode;
    }
}
