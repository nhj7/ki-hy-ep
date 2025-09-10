package com.kiassist.preferences;

import org.eclipse.core.runtime.preferences.InstanceScope;
import org.eclipse.jface.preference.FieldEditorPreferencePage;
import org.eclipse.jface.preference.StringFieldEditor;
import org.eclipse.jface.preference.ComboFieldEditor;
import org.eclipse.jface.preference.FieldEditor;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;
import org.eclipse.core.runtime.preferences.IEclipsePreferences;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.widgets.Combo;

import com.kiassist.Activator;
import com.kiassist.core.ModelConfigManager;
import com.kiassist.core.ModelConfig;

/**
 * KI Assist 설정 페이지
 * JDK 7+ 호환
 */
public class KIAssistPreferencePage extends FieldEditorPreferencePage 
        implements IWorkbenchPreferencePage {

    private ComboFieldEditor modelComboEditor;
    private StringFieldEditor apiUrlEditor;
    private StringFieldEditor apiKeyEditor;
    private StringFieldEditor maxTokensEditor;
    private StringFieldEditor temperatureEditor;
    private ModelConfigManager modelManager;

    public KIAssistPreferencePage() {
        super(GRID);
        setPreferenceStore(Activator.getDefault().getPreferenceStore());
        setDescription("KI Assist 플러그인 설정");
        modelManager = ModelConfigManager.getInstance();
    }

    @Override
    public void createFieldEditors() {
        // 기본 모델 선택 콤보박스 (동적 목록)
        String[][] modelChoices = getModelChoices();
        modelComboEditor = new ComboFieldEditor(
            PreferenceConstants.DEFAULT_MODEL,
            "기본 모델:",
            modelChoices,
            getFieldEditorParent()) {
            @Override
            protected void fireValueChanged(String property, Object oldValue, Object newValue) {
                super.fireValueChanged(property, oldValue, newValue);
                
                System.out.println("fireValueChanged - property: " + property + ", oldValue: " + oldValue + ", newValue: " + newValue);
                // 값이 변경될 때 호출
                if (newValue != null) {
                    onModelSelectionChanged(newValue.toString());
                }
            }
        };
        addField(modelComboEditor);

        // API URL 설정
        apiUrlEditor = new StringFieldEditor(
            PreferenceConstants.API_URL,
            "API URL:",
            getFieldEditorParent());
        addField(apiUrlEditor);

        // API 키 설정 (비밀번호 필드)
        apiKeyEditor = new StringFieldEditor(
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

        // 최대 토큰 수 설정
        maxTokensEditor = new StringFieldEditor(
            PreferenceConstants.MAX_TOKENS,
            "최대 토큰 수:",
            getFieldEditorParent());
        addField(maxTokensEditor);

        // Temperature 설정
        temperatureEditor = new StringFieldEditor(
            PreferenceConstants.TEMPERATURE,
            "Temperature (0.0-1.0):",
            getFieldEditorParent());
        addField(temperatureEditor);
        
        // 초기 모델 선택에 따른 값 설정
        initializeFieldsFromCurrentModel();
    }
    
    /**
     * 현재 선택된 모델에 따라 초기값 설정
     */
    private void initializeFieldsFromCurrentModel() {
        // 설정에서 현재 선택된 모델 ID 가져오기
        IEclipsePreferences prefs = InstanceScope.INSTANCE.getNode(Activator.PLUGIN_ID);
        String currentModelId = prefs.get(PreferenceConstants.DEFAULT_MODEL, "ki-assist-custom");
        onModelSelectionChanged(currentModelId);
        
//        ModelConfig currentModel = modelManager.getModelById(currentModelId);
//        if (currentModel != null) {
//            // 현재 모델의 값으로 필드들 초기화
//            apiUrlEditor.setStringValue(currentModel.getApiUrl() != null ? currentModel.getApiUrl() : "");
//            apiKeyEditor.setStringValue(currentModel.getApiKey() != null ? currentModel.getApiKey() : "");
//            maxTokensEditor.setStringValue(String.valueOf(currentModel.getMaxTokens()));
//            temperatureEditor.setStringValue(String.valueOf(currentModel.getTemperature()));
//        }
    }
    
    /**
     * 모델 선택이 변경되었을 때 호출
     * @param selectedModelId 새로 선택된 모델 ID
     */
    private void onModelSelectionChanged(String selectedModelId) {
        System.out.println("onModelSelectionChanged - selectedModelId: " + selectedModelId);
        ModelConfig selectedModel = modelManager.getModelById(selectedModelId);
        
        if (selectedModel != null) {
            // 선택된 모델의 기본값으로 필드들 업데이트
            apiUrlEditor.setStringValue(selectedModel.getApiUrl() != null ? selectedModel.getApiUrl() : "");
            apiKeyEditor.setStringValue(selectedModel.getApiKey() != null ? selectedModel.getApiKey() : "");
            maxTokensEditor.setStringValue(String.valueOf(selectedModel.getMaxTokens()));
            temperatureEditor.setStringValue(String.valueOf(selectedModel.getTemperature()));
            
            System.out.println("모델 선택 변경됨: " + selectedModel.getDisplayName());
            System.out.println("  - API URL: " + selectedModel.getApiUrl());
            System.out.println("  - Max Tokens: " + selectedModel.getMaxTokens());
            System.out.println("  - Temperature: " + selectedModel.getTemperature());
        } else {
            System.out.println("선택된 모델을 찾을 수 없습니다: " + selectedModelId);
        }
    }
    
    /**
     * 동적 모델 목록을 콤보박스 선택지 형태로 변환
     */
    private String[][] getModelChoices() {
        java.util.List<ModelConfig> models = modelManager.getEnabledModels();
        String[][] choices = new String[models.size()][2];
        
        for (int i = 0; i < models.size(); i++) {
            ModelConfig model = models.get(i);
            choices[i][0] = model.getDisplayName(); // 화면에 표시될 이름
            choices[i][1] = model.getId();         // 실제 저장될 값
        }
        
        return choices;
    }

    @Override
    public void init(IWorkbench workbench) {
        // 초기화 작업 (필요시)
    }

    @Override
    public boolean performOk() {
        boolean result = super.performOk();
        
        // 현재 선택된 모델의 설정도 업데이트
        updateSelectedModelConfig();
        
        // 설정 저장 후 추가 작업이 필요한 경우
        try {
            IEclipsePreferences prefs = InstanceScope.INSTANCE.getNode(Activator.PLUGIN_ID);
            prefs.flush();
        } catch (Exception e) {
            e.printStackTrace();
        }
        
        return result;
    }
    
    /**
     * 현재 선택된 모델의 설정을 업데이트
     */
    private void updateSelectedModelConfig() {
        // Preference Store에서 현재 설정된 값을 가져오기
        IEclipsePreferences prefs = InstanceScope.INSTANCE.getNode(Activator.PLUGIN_ID);
        String selectedModelId = prefs.get(PreferenceConstants.DEFAULT_MODEL, "ki-assist-custom");
        System.out.println("updateSelectedModelConfig - selectedModelId: " + selectedModelId);
        ModelConfig selectedModel = modelManager.getModelById(selectedModelId);
        
        if (selectedModel != null) {
            // 업데이트 전 현재 값들 출력
            System.out.println("업데이트 전 모델 상태:");
            System.out.println("  - API URL: " + selectedModel.getApiUrl());
            System.out.println("  - API Key: " + selectedModel.getApiKey());
            
            // 사용자가 입력한 값들 출력
            System.out.println("사용자 입력 값들:");
            System.out.println("  - API URL: " + apiUrlEditor.getStringValue());
            System.out.println("  - API Key: " + apiKeyEditor.getStringValue());
            System.out.println("  - Max Tokens: " + maxTokensEditor.getStringValue());
            System.out.println("  - Temperature: " + temperatureEditor.getStringValue());
            
            // 사용자가 입력한 값들로 모델 설정 업데이트
            selectedModel.setApiUrl(apiUrlEditor.getStringValue());
            selectedModel.setApiKey(apiKeyEditor.getStringValue());
            
            try {
                selectedModel.setMaxTokens(Integer.parseInt(maxTokensEditor.getStringValue()));
            } catch (NumberFormatException e) {
                selectedModel.setMaxTokens(1000); // 기본값
            }
            
            try {
                selectedModel.setTemperature(Double.parseDouble(temperatureEditor.getStringValue()));
            } catch (NumberFormatException e) {
                selectedModel.setTemperature(0.7); // 기본값
            }
            
            // 업데이트 후 값들 출력
            System.out.println("업데이트 후 모델 상태:");
            System.out.println("  - API URL: " + selectedModel.getApiUrl());
            System.out.println("  - API Key: " + selectedModel.getApiKey());
            System.out.println("  - Max Tokens: " + selectedModel.getMaxTokens());
            System.out.println("  - Temperature: " + selectedModel.getTemperature());
            
            // 모델 설정 저장
            modelManager.updateModel(selectedModel);
            
            System.out.println("모델 설정이 업데이트되었습니다: " + selectedModel.getDisplayName());
        }
    }
}