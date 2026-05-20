package com.classnote.entity;

import javax.persistence.*;

@Entity
@Table(name = "llm_config")
public class LLMConfigEntity {

    @Id
    @Column(length = 36)
    private String id = "default";

    @Column(nullable = false, length = 50)
    private String provider = "OPENAI";

    @Column(length = 500)
    private String apiKey;

    @Column(length = 500)
    private String baseUrl;

    @Column(length = 100)
    private String model = "gpt-4o";

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getProvider() { return provider; }
    public void setProvider(String provider) { this.provider = provider; }
    public String getApiKey() { return apiKey; }
    public void setApiKey(String apiKey) { this.apiKey = apiKey; }
    public String getBaseUrl() { return baseUrl; }
    public void setBaseUrl(String baseUrl) { this.baseUrl = baseUrl; }
    public String getModel() { return model; }
    public void setModel(String model) { this.model = model; }
}
