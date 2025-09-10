package com.kiassist.core;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.runtime.preferences.IEclipsePreferences;
import org.eclipse.core.runtime.preferences.InstanceScope;
import org.json.JSONArray;
import org.json.JSONObject;

import com.kiassist.Activator;
import com.kiassist.preferences.PreferenceConstants;

/**
 * 모델 설정을 관리하는 클래스 (JSON 형태로 저장)
 * JDK 7 호환을 위해 간단한 JSON 파싱 사용
 */
public class ModelConfigManager {
    
    private static ModelConfigManager instance;
    private List<ModelConfig> models;
    
    private ModelConfigManager() {
        models = new ArrayList<ModelConfig>();
        loadModels();
    }
    
    public static synchronized ModelConfigManager getInstance() {
        if (instance == null) {
            instance = new ModelConfigManager();
        }
        return instance;
    }
    
    /**
     * 모델 목록 로드
     */
    private void loadModels() {
        IEclipsePreferences prefs = InstanceScope.INSTANCE.getNode(Activator.PLUGIN_ID);
        String configsJson = prefs.get(PreferenceConstants.MODEL_CONFIGS, "");
        
        System.out.println("저장된 모델 설정 로드: " + (configsJson.isEmpty() ? "없음" : configsJson.length() + " 문자"));
        
        if (configsJson.isEmpty()) {
            // 기본 모델들 초기화
            System.out.println("기본 모델들을 초기화합니다.");
            initializeDefaultModels();
        } else {
            // JSON에서 파싱 (간단한 파싱)
            System.out.println("저장된 설정에서 모델들을 로드합니다.");
            parseModelsFromJson(configsJson);
            //initializeDefaultModels();
        }
    }
    
    /**
     * 기본 모델들 초기화
     */
    private void initializeDefaultModels() {
        models.clear();
        
        // KI Assist 커스텀 모델
        ModelConfig kiAssist = new ModelConfig("ki-assist-custom", "ki-assist-custom", "KI Assist (커스텀)", 
            "http://114.207.145.84:8000/chat", "API_KEY");
        models.add(kiAssist);
        
        // GPT 모델들
        ModelConfig gpt35 = new ModelConfig("gpt-3.5-turbo", "gpt-3.5-turbo", "GPT-3.5-turbo", 
            "https://api.openai.com/v1/chat/completions", "");
        models.add(gpt35);
        
        ModelConfig gpt4 = new ModelConfig("gpt-4", "gpt-4", "GPT-4", 
            "https://api.openai.com/v1/chat/completions", "");
        models.add(gpt4);
        
        // Claude 모델들
        ModelConfig claudeHaiku = new ModelConfig("claude-3-haiku", "claude-3-haiku-20240307", "Claude-3-haiku", 
            "https://api.anthropic.com/v1/messages", "");
        models.add(claudeHaiku);
        
        ModelConfig claudeSonnet = new ModelConfig("claude-3-sonnet", "claude-3-sonnet-20240229", "Claude-3-sonnet", 
            "https://api.anthropic.com/v1/messages", "");
        models.add(claudeSonnet);
        
        // Gemini 모델
        ModelConfig gemini = new ModelConfig("gemini-pro", "gemini-pro", "Gemini-Pro", 
            "https://generativelanguage.googleapis.com/v1/models/gemini-pro:generateContent", "");
        models.add(gemini);
        
        saveModels();
    }
    
    /**
     * JSON에서 모델 설정 파싱 (org.json 사용)
     */
    private void parseModelsFromJson(String jsonString) {
        System.out.println("parseModelsFromJson : " + jsonString);
        models.clear();
        
        try {
            JSONArray jsonArray = new JSONArray(jsonString);
            
            for (int i = 0; i < jsonArray.length(); i++) {
                JSONObject jsonObject = jsonArray.getJSONObject(i);
                
                ModelConfig model = new ModelConfig();
                model.setId(jsonObject.optString("id", ""));
                model.setName(jsonObject.optString("name", ""));
                model.setDisplayName(jsonObject.optString("displayName", ""));
                model.setApiUrl(jsonObject.optString("apiUrl", ""));
                model.setApiKey(jsonObject.optString("apiKey", ""));
                model.setModelName(jsonObject.optString("modelName", ""));
                model.setMaxTokens(jsonObject.optInt("maxTokens", 1000));
                model.setTemperature(jsonObject.optDouble("temperature", 0.7));
                model.setEnabled(jsonObject.optBoolean("enabled", true));
                
                System.out.println("파싱된 모델: " + model.getDisplayName());
                System.out.println("  - API URL: " + model.getApiUrl());
                System.out.println("  - API Key: " + (model.getApiKey().isEmpty() ? "없음" : "[설정됨]"));
                
                models.add(model);
            }
            
            System.out.println("총 " + models.size() + "개 모델을 로드했습니다.");
            
        } catch (Exception e) {
            System.err.println("모델 설정 파싱 오류: " + e.getMessage());
            e.printStackTrace();
            initializeDefaultModels();
        }
    }
    
    /**
     * 모델 설정을 JSON으로 저장 (org.json 사용)
     */
    public void saveModels() {
        try {
            JSONArray jsonArray = new JSONArray();
            
            for (int i = 0; i < models.size(); i++) {
                ModelConfig model = models.get(i);
                
                // 디버그: 각 모델의 현재 값들 출력
                System.out.println("저장할 모델 [" + i + "]: " + model.getDisplayName());
                System.out.println("  - ID: " + model.getId());
                System.out.println("  - API URL: " + model.getApiUrl());
                System.out.println("  - API Key: " + (model.getApiKey() != null && !model.getApiKey().isEmpty() ? "[설정됨]" : "없음"));
                System.out.println("  - Max Tokens: " + model.getMaxTokens());
                System.out.println("  - Temperature: " + model.getTemperature());
                
                JSONObject jsonObject = new JSONObject();
                jsonObject.put("id", model.getId());
                jsonObject.put("name", model.getName());
                jsonObject.put("displayName", model.getDisplayName());
                jsonObject.put("apiUrl", model.getApiUrl());
                jsonObject.put("apiKey", model.getApiKey());
                jsonObject.put("modelName", model.getModelName());
                jsonObject.put("maxTokens", model.getMaxTokens());
                jsonObject.put("temperature", model.getTemperature());
                jsonObject.put("enabled", model.isEnabled());
                
                jsonArray.put(jsonObject);
            }
            
            String jsonString = jsonArray.toString();
            
            IEclipsePreferences prefs = InstanceScope.INSTANCE.getNode(Activator.PLUGIN_ID);
            prefs.put(PreferenceConstants.MODEL_CONFIGS, jsonString);
            
            prefs.flush();
            System.out.println("모델 설정이 성공적으로 저장되었습니다: " + jsonString.length() + " 문자");
            System.out.println("저장된 JSON: " + jsonString);
            
        } catch (Exception e) {
            System.err.println("모델 설정 저장 오류: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    
    // 공개 메소드들
    public List<ModelConfig> getAllModels() {
        return new ArrayList<ModelConfig>(models);
    }
    
    public List<ModelConfig> getEnabledModels() {
        List<ModelConfig> enabled = new ArrayList<ModelConfig>();
        for (ModelConfig model : models) {
            if (model.isEnabled()) {
                enabled.add(model);
            }
        }
        return enabled;
    }
    
    public ModelConfig getModelById(String id) {
        for (ModelConfig model : models) {
            if (model.getId().equals(id)) {
                return model;
            }
        }
        return null;
    }
    
    public void addModel(ModelConfig model) {
        models.add(model);
        saveModels();
    }
    
    public void updateModel(ModelConfig updatedModel) {
        for (int i = 0; i < models.size(); i++) {
            if (models.get(i).getId().equals(updatedModel.getId())) {
                models.set(i, updatedModel);
                saveModels();
                return;
            }
        }
    }
    
    public void removeModel(String id) {
        for (int i = 0; i < models.size(); i++) {
            if (models.get(i).getId().equals(id)) {
                models.remove(i);
                saveModels();
                return;
            }
        }
    }
    
    public String[] getModelDisplayNames() {
        List<String> names = new ArrayList<String>();
        for (ModelConfig model : getEnabledModels()) {
            names.add(model.getDisplayName());
        }
        return names.toArray(new String[names.size()]);
    }
    
    public ModelConfig getModelByDisplayName(String displayName) {
        for (ModelConfig model : models) {
            if (model.getDisplayName().equals(displayName)) {
                return model;
            }
        }
        return null;
    }
}