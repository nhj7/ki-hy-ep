
/**
 * 설정 초기화 클래스
 */
package com.kiassist.preferences;

import org.eclipse.core.runtime.preferences.AbstractPreferenceInitializer;
import org.eclipse.core.runtime.preferences.IEclipsePreferences;
import org.eclipse.core.runtime.preferences.InstanceScope;

import com.kiassist.Activator;

public class PreferenceInitializer extends AbstractPreferenceInitializer {

    @Override
    public void initializeDefaultPreferences() {
        IEclipsePreferences prefs = InstanceScope.INSTANCE.getNode(Activator.PLUGIN_ID);
        
        prefs.put(PreferenceConstants.API_URL, PreferenceConstants.DEFAULT_API_URL);
        prefs.put(PreferenceConstants.DEFAULT_MODEL, PreferenceConstants.DEFAULT_MODEL_VALUE);
        prefs.put(PreferenceConstants.MAX_TOKENS, PreferenceConstants.DEFAULT_MAX_TOKENS);
        prefs.put(PreferenceConstants.TEMPERATURE, PreferenceConstants.DEFAULT_TEMPERATURE);
        
        prefs.put(PreferenceConstants.CUSTOM_API_URL, "http://114.207.145.84:8000/chat");
        prefs.put(PreferenceConstants.CUSTOM_API_KEY, "API_KEY");
        
        System.out.println("initializeDefaultPreferences : "+prefs);
        
        // API 키는 기본값 없음 (사용자가 직접 입력해야 함)
    }
}