package com.classnote.dto;

import javax.validation.constraints.NotBlank;

public class LLMConfigRequest {
    @NotBlank
    private String provider;
    private String apiKey;
    private String baseUrl;
    @NotBlank
    private String model;

    public String getProvider() { return provider; }
    public void setProvider(String provider) { this.provider = provider; }
    public String getApiKey() { return apiKey; }
    public void setApiKey(String apiKey) { this.apiKey = apiKey; }
    public String getBaseUrl() { return baseUrl; }
    public void setBaseUrl(String baseUrl) { this.baseUrl = baseUrl; }
    public String getModel() { return model; }
    public void setModel(String model) { this.model = model; }
}
