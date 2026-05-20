package com.classnote.dto;

import com.classnote.entity.LLMConfigEntity;

public class LLMConfigResponse {
    private String provider;
    private String apiKey;    // masked
    private String baseUrl;
    private String model;

    public static LLMConfigResponse from(LLMConfigEntity entity) {
        LLMConfigResponse r = new LLMConfigResponse();
        r.provider = entity.getProvider();
        r.apiKey = maskKey(entity.getApiKey());
        r.baseUrl = entity.getBaseUrl();
        r.model = entity.getModel();
        return r;
    }

    private static String maskKey(String key) {
        if (key == null || key.length() <= 8) return "****";
        return key.substring(0, 4) + "****" + key.substring(key.length() - 4);
    }

    public String getProvider() { return provider; }
    public void setProvider(String provider) { this.provider = provider; }
    public String getApiKey() { return apiKey; }
    public void setApiKey(String apiKey) { this.apiKey = apiKey; }
    public String getBaseUrl() { return baseUrl; }
    public void setBaseUrl(String baseUrl) { this.baseUrl = baseUrl; }
    public String getModel() { return model; }
    public void setModel(String model) { this.model = model; }
}
