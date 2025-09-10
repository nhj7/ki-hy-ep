package com.kiassist.core;

/**
 * LLM 모델 설정 정보를 담는 클래스
 */
public class ModelConfig {
    private String id;
    private String name;
    private String displayName;
    private String apiUrl;
    private String apiKey;
    private String modelName;
    private int maxTokens;
    private double temperature;
    private boolean enabled;
    
    public ModelConfig() {
        this.enabled = true;
        this.maxTokens = 1000;
        this.temperature = 0.7;
    }
    
    public ModelConfig(String id, String name, String displayName, String apiUrl, String apiKey) {
        this();
        this.id = id;
        this.name = name;
        this.displayName = displayName;
        this.apiUrl = apiUrl;
        this.apiKey = apiKey;
        this.modelName = name;
    }
    
    // Getters and Setters
    public String getId() {
        return id;
    }
    
    public void setId(String id) {
        this.id = id;
    }
    
    public String getName() {
        return name;
    }
    
    public void setName(String name) {
        this.name = name;
    }
    
    public String getDisplayName() {
        return displayName;
    }
    
    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }
    
    public String getApiUrl() {
        return apiUrl;
    }
    
    public void setApiUrl(String apiUrl) {
        this.apiUrl = apiUrl;
    }
    
    public String getApiKey() {
        return apiKey;
    }
    
    public void setApiKey(String apiKey) {
        this.apiKey = apiKey;
    }
    
    public String getModelName() {
        return modelName;
    }
    
    public void setModelName(String modelName) {
        this.modelName = modelName;
    }
    
    public int getMaxTokens() {
        return maxTokens;
    }
    
    public void setMaxTokens(int maxTokens) {
        this.maxTokens = maxTokens;
    }
    
    public double getTemperature() {
        return temperature;
    }
    
    public void setTemperature(double temperature) {
        this.temperature = temperature;
    }
    
    public boolean isEnabled() {
        return enabled;
    }
    
    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }
}