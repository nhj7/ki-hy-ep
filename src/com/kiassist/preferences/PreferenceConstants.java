package com.kiassist.preferences;

/**
 * KI Assist 설정 상수
 * JDK 7+ 호환
 */
public class PreferenceConstants {

    // 기본 설정 (Activator의 하드코딩 문자열과 일치시킴)
    public static final String API_URL = "kiassist.api.url";
    public static final String API_KEY = "kiassist.api.key";
    public static final String DEFAULT_MODEL = "kiassist.default.model";
    public static final String MAX_TOKENS = "kiassist.max.tokens";
    public static final String TEMPERATURE = "kiassist.temperature";
    
    // 커스텀 모델 설정
    public static final String CUSTOM_API_URL = "kiassist.custom.api.url";
    public static final String CUSTOM_API_KEY = "kiassist.custom.api.key";
    public static final String CUSTOM_MODEL_NAME = "kiassist.custom.model.name";
    
    // 시스템 설정
    public static final String SETTINGS_VERSION = "kiassist.settings.version";
    
    // 동적 모델 관리
    public static final String MODEL_CONFIGS = "kiassist.model.configs"; // JSON 형태로 저장
    
    // 기본값들
    public static final String DEFAULT_API_URL = "http://114.207.145.84:8000/chat";
    public static final String DEFAULT_MODEL_VALUE = "ki-assist-custom";
    public static final String DEFAULT_MAX_TOKENS = "1000";
    public static final String DEFAULT_TEMPERATURE = "0.7";
    public static final String DEFAULT_CUSTOM_MODEL = "ki-assist-custom";
}