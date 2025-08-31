package com.kiassist;

import org.eclipse.core.runtime.preferences.IEclipsePreferences;
import org.eclipse.core.runtime.preferences.InstanceScope;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.osgi.framework.BundleContext;
import org.osgi.service.prefs.BackingStoreException;

/**
 * KI Assist Plugin Activator
 * JDK 7+ 호환
 */
public class Activator extends AbstractUIPlugin {

    // Plugin ID
    public static final String PLUGIN_ID = "com.kiassist.plugin";

    // Shared instance
    private static Activator plugin;

    /**
     * Constructor
     */
    public Activator() {
    }

    /*
     * (non-Javadoc)
     * @see org.eclipse.ui.plugin.AbstractUIPlugin#start(org.osgi.framework.BundleContext)
     */
    public void start(BundleContext context) throws Exception {
        super.start(context);
        plugin = this;
        System.out.println("KI Assist Plugin started successfully!");
        
        // 무조건 기본 설정값 초기화 (PreferenceInitializer보다 확실함)
        initializeDefaultSettings();
    }

    /**
     * 기본 설정값 강제 초기화
     */
    private void initializeDefaultSettings() {
        try {
            IEclipsePreferences prefs = InstanceScope.INSTANCE.getNode(PLUGIN_ID);
            
            // 설정이 처음인지 확인 (버전 체크)
            String version = prefs.get("kiassist.settings.version", "");
            if (version.isEmpty()) {
                System.out.println("첫 실행 감지 - 기본 설정값을 초기화합니다.");
                
                // 기본 API 설정 (상수 사용)
                prefs.put("kiassist.api.url", "https://api.openai.com/v1/chat/completions");
                prefs.put("kiassist.default.model", "ki-assist-custom");
                prefs.put("kiassist.max.tokens", "1000");
                prefs.put("kiassist.temperature", "0.7");
                
                // 커스텀 모델 기본 설정  
                prefs.put("kiassist.custom.api.url", "http://114.207.145.84:8000/chat");
                prefs.put("kiassist.custom.api.key", "custom_api_key");
                prefs.put("kiassist.custom.model.name", "gpt-3.5-turbo");
                
                // 설정 완료 버전 표시
                prefs.put("kiassist.settings.version", "1.0.0");
                
                prefs.flush(); // 즉시 디스크에 저장
                System.out.println("기본 설정값 초기화 완료!");
                
            } else {
                System.out.println("기존 설정 발견 - 버전: " + version);
                
                // 기존 설정값 출력 (디버깅용)
                String apiUrl = prefs.get("kiassist.api.url", "없음");
                String maxTokens = prefs.get("kiassist.max.tokens", "없음");
                System.out.println("API URL: " + apiUrl);
                System.out.println("Max Tokens: " + maxTokens);
            }
            
        } catch (BackingStoreException e) {
            System.err.println("설정 초기화 오류: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /*
     * (non-Javadoc)
     * @see org.eclipse.ui.plugin.AbstractUIPlugin#stop(org.osgi.framework.BundleContext)
     */
    public void stop(BundleContext context) throws Exception {
        plugin = null;
        super.stop(context);
        System.out.println("KI Assist Plugin stopped.");
    }

    /**
     * Returns the shared instance
     *
     * @return the shared instance
     */
    public static Activator getDefault() {
        return plugin;
    }
}