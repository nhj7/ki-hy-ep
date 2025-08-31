package com.kiassist.preferences;

import org.eclipse.core.runtime.preferences.InstanceScope;
import org.eclipse.jface.preference.FieldEditorPreferencePage;
import org.eclipse.jface.preference.StringFieldEditor;
import org.eclipse.jface.preference.ComboFieldEditor;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;
import org.eclipse.core.runtime.preferences.IEclipsePreferences;

import com.kiassist.Activator;

/**
 * KI Assist 설정 페이지
 * JDK 7+ 호환
 */
public class KIAssistPreferencePage extends FieldEditorPreferencePage 
        implements IWorkbenchPreferencePage {

    public KIAssistPreferencePage() {
        super(GRID);
        setPreferenceStore(Activator.getDefault().getPreferenceStore());
        setDescription("KI Assist 플러그인 설정");
    }

    @Override
    public void createFieldEditors() {
        // API URL 설정
        addField(new StringFieldEditor(
            PreferenceConstants.API_URL,
            "API URL:",
            getFieldEditorParent()));

        // API 키 설정 (비밀번호 필드)
        StringFieldEditor apiKeyEditor = new StringFieldEditor(
            PreferenceConstants.API_KEY,
            "API Key:",
            getFieldEditorParent()) {
            @Override
            protected void doFillIntoGrid(org.eclipse.swt.widgets.Composite parent, int numColumns) {
                super.doFillIntoGrid(parent, numColumns);
                // 비밀번호 필드로 만들기
                getTextControl().setEchoChar('*');
            }
        };
        addField(apiKeyEditor);

        // 기본 모델 설정
        addField(new ComboFieldEditor(
            PreferenceConstants.DEFAULT_MODEL,
            "기본 모델:",
            new String[][] {
                {"GPT-3.5-turbo", "gpt-3.5-turbo"},
                {"GPT-4", "gpt-4"},
                {"Claude-3-haiku", "claude-3-haiku"},
                {"Claude-3-sonnet", "claude-3-sonnet"},
                {"Gemini-Pro", "gemini-pro"}
            },
            getFieldEditorParent()));

        // 최대 토큰 수 설정
        addField(new StringFieldEditor(
            PreferenceConstants.MAX_TOKENS,
            "최대 토큰 수:",
            getFieldEditorParent()));

        // Temperature 설정
        addField(new StringFieldEditor(
            PreferenceConstants.TEMPERATURE,
            "Temperature (0.0-1.0):",
            getFieldEditorParent()));
    }

    @Override
    public void init(IWorkbench workbench) {
        // 초기화 작업 (필요시)
    }

    @Override
    public boolean performOk() {
        boolean result = super.performOk();
        
        // 설정 저장 후 추가 작업이 필요한 경우
        try {
            IEclipsePreferences prefs = InstanceScope.INSTANCE.getNode(Activator.PLUGIN_ID);
            prefs.flush();
        } catch (Exception e) {
            e.printStackTrace();
        }
        
        return result;
    }
}