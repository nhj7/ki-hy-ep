package com.kiassist.views;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.swt.SWT;
import org.eclipse.swt.browser.Browser;
import org.eclipse.swt.events.KeyAdapter;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.part.ViewPart;
import org.eclipse.jface.dialogs.MessageDialog;

import com.kiassist.core.LLMClient;
import com.kiassist.core.ChatMessage;
import com.kiassist.core.ChatHistory;
import com.kiassist.core.MarkdownToHtmlConverter;
import com.kiassist.core.ModelConfigManager;
import com.kiassist.core.ModelConfig;

/**
 * KI Assist Chat View - JDK 7 호환
 */
public class KIAssistView extends ViewPart {

    public static final String ID = "com.kiassist.views.KIAssistView";

    // UI 컴포넌트
    private Combo modelCombo;
    private Browser chatBrowser;
    private Text inputText;
    private Button sendButton;
    private Button clearButton;
    
    // 채팅 관련
    private ChatHistory chatHistory;
    private LLMClient llmClient;
    private MarkdownToHtmlConverter htmlConverter;
    private boolean isFirstLoad = true;

    @Override
    public void createPartControl(Composite parent) {
    	System.out.println("createPartControl");
        // 메인 레이아웃
        parent.setLayout(new GridLayout(1, false));
        
        // 초기화
        chatHistory = new ChatHistory();
        llmClient = new LLMClient();
        htmlConverter = new MarkdownToHtmlConverter();

        // 상단: 모델 선택 영역
        createModelSelectionArea(parent);
        
        // 중간: 채팅 브라우저 영역
        createChatBrowserArea(parent);
        
        // 하단: 입력 영역
        createInputArea(parent);
        
        // 초기 HTML 로드
        loadInitialChatHTML();
    }

    private void createModelSelectionArea(Composite parent) {
        Composite modelArea = new Composite(parent, SWT.NONE);
        modelArea.setLayout(new GridLayout(3, false));
        modelArea.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
        
        Label modelLabel = new Label(modelArea, SWT.NONE);
        modelLabel.setText("모델:");
        
        modelCombo = new Combo(modelArea, SWT.DROP_DOWN | SWT.READ_ONLY);
        
        // 동적 모델 목록 로드
        ModelConfigManager modelManager = ModelConfigManager.getInstance();
        String[] modelNames = modelManager.getModelDisplayNames();
        modelCombo.setItems(modelNames);
        
        if (modelNames.length > 0) {
            modelCombo.select(0); // 첫 번째 모델 기본 선택
        }
        modelCombo.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
        
        clearButton = new Button(modelArea, SWT.PUSH);
        clearButton.setText("대화 삭제");
        clearButton.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                clearChat();
            }
        });
    }

    private void createChatBrowserArea(Composite parent) {
        chatBrowser = new Browser(parent, SWT.BORDER);
        
        GridData browserData = new GridData(SWT.FILL, SWT.FILL, true, true);
        browserData.heightHint = 400;
        chatBrowser.setLayoutData(browserData);
        
        // 브라우저 엔진 정보를 디버그 로그에 출력
        String browserType = chatBrowser.getBrowserType();
        System.out.println("SWT Browser Engine: " + browserType);
        
        // 버전 정보도 함께 출력
        
        try {
            // JavaScript를 통해 IE 버전 정보 얻기
            Object userAgent = chatBrowser.evaluate("return navigator.userAgent;");
            if (userAgent != null) {
                System.out.println("IE User Agent: " + userAgent.toString());
            }
            
            Object appVersion = chatBrowser.evaluate("return navigator.appVersion;");
            if (appVersion != null) {
                System.out.println("IE App Version: " + appVersion.toString());
            }
            
            System.out.println("userAgent : "+userAgent + ", appVersion : " + appVersion);
        } catch (Exception e) {
            System.out.println("Failed to get IE version info: " + e.getMessage());
        }
        
    }

    private void createInputArea(Composite parent) {
        Composite inputArea = new Composite(parent, SWT.NONE);
        inputArea.setLayout(new GridLayout(2, false));
        inputArea.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
        
        // 멀티라인 입력창
        inputText = new Text(inputArea, SWT.BORDER | SWT.MULTI | SWT.WRAP | SWT.V_SCROLL);
        GridData inputData = new GridData(SWT.FILL, SWT.FILL, true, false);
        inputData.heightHint = 60;
        inputData.widthHint = 300;
        inputText.setLayoutData(inputData);
        //inputText.setText("안녕하세요! KI Assist에게 질문해보세요.");
        
        // Enter 키로 전송 (Ctrl+Enter)
        inputText.addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
            	System.out.println("e.keyCode : "+e.keyCode + ", SWT.CR : " + SWT.CR + ", e.stateMask : " + e.stateMask + ", SWT.CTRL : " +SWT.CTRL);
                // Ctrl+Enter 조합 확인
                if (( e.keyCode == 13) && (e.stateMask == 4194304)) {
                    e.doit = false; // 기본 동작 방지 (줄바꿈 방지)
                    sendMessage();
                }
                // Shift+Enter는 줄바꿈 허용 (기본 동작)
            }
        });
        
        // 전송 버튼
        sendButton = new Button(inputArea, SWT.PUSH);
        sendButton.setText("전송\n(Ctrl+Enter)");
        sendButton.setLayoutData(new GridData(SWT.FILL, SWT.FILL, false, false));
        sendButton.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                sendMessage();
            }
        });
    }

    private void loadInitialChatHTML() {
        String initialHTML = htmlConverter.createChatPageHTML();
        chatBrowser.setText(initialHTML);
    }

    private void sendMessage() {
        String question = inputText.getText().trim();
        final String selectedModel = getSelectedModelKey();
        
        if (question.isEmpty()) {
            MessageDialog.openWarning(
                getSite().getShell(),
                "경고",
                "메시지를 입력해주세요.");
            return;
        }

        // 사용자 메시지 추가
        ChatMessage userMessage = new ChatMessage(ChatMessage.Type.USER, question);
        chatHistory.addMessage(userMessage);
        updateChatDisplay();
        
        // 입력창 초기화 및 버튼 비활성화
        inputText.setText("");
        sendButton.setEnabled(false);
        sendButton.setText("전송 중...");
        
        // 타이핑 인디케이터 표시
        showTypingIndicator();

        // 백그라운드에서 LLM 호출
        Thread worker = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    final String response = llmClient.sendChatMessage(chatHistory, selectedModel);
                    
                    // UI 업데이트는 메인 스레드에서
                    getSite().getShell().getDisplay().asyncExec(new Runnable() {
                        @Override
                        public void run() {
                            // AI 응답 메시지 추가
                            ChatMessage aiMessage = new ChatMessage(ChatMessage.Type.ASSISTANT, response);
                            chatHistory.addMessage(aiMessage);
                            updateChatDisplay();
                            
                            // 버튼 재활성화
                            sendButton.setEnabled(true);
                            sendButton.setText("전송\n(Ctrl+Enter)");
                            inputText.setFocus();
                        }
                    });
                    
                } catch (final Exception ex) {
                    getSite().getShell().getDisplay().asyncExec(new Runnable() {
                        @Override
                        public void run() {
                            // 오류 메시지 추가
                            ChatMessage errorMessage = new ChatMessage(
                                ChatMessage.Type.SYSTEM, 
                                "오류 발생: " + ex.getMessage());
                            chatHistory.addMessage(errorMessage);
                            updateChatDisplay();
                            
                            // 버튼 재활성화
                            sendButton.setEnabled(true);
                            sendButton.setText("전송\n(Ctrl+Enter)");
                        }
                    });
                }
            }
        });
        worker.start();
    }

    private String getSelectedModelKey() {
        String selectedDisplayName = modelCombo.getText();
        ModelConfigManager modelManager = ModelConfigManager.getInstance();
        ModelConfig selectedModel = modelManager.getModelByDisplayName(selectedDisplayName);
        
        if (selectedModel != null) {
            return selectedModel.getId();
        }
        
        // 기본값 반환
        return "ki-assist-custom";
    }

    private void showTypingIndicator() {
        ChatMessage typingMessage = new ChatMessage(ChatMessage.Type.TYPING, "");
        chatHistory.addMessage(typingMessage);
        updateChatDisplay();
    }

    private void updateChatDisplay() {
        // 마지막 메시지만 추가
        ChatMessage lastMessage = chatHistory.getLastMessage();
        System.out.println("lastMessage : "+lastMessage.getContent());
        if (lastMessage != null) {
            String script = htmlConverter.getAddMessageScript(lastMessage);
            System.out.println("script : "+script);
            chatBrowser.execute(script);
        }
    }

    private void clearChat() {
        chatHistory.clear();
        // 브라우저 컨테이너만 초기화
        chatBrowser.execute("document.getElementById('chatContainer').innerHTML = '<div class=\"message message-system\">KI Assist 채팅이 시작되었습니다. 무엇이든 물어보세요!</div>';");
        inputText.setFocus();
    }

    @Override
    public void setFocus() {
        inputText.setFocus();
    }
}