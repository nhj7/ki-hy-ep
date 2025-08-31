package com.kiassist.core;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.List;

import org.eclipse.core.runtime.preferences.IEclipsePreferences;
import org.eclipse.core.runtime.preferences.InstanceScope;

import com.kiassist.Activator;
import com.kiassist.preferences.PreferenceConstants;

/**
 * LLM과의 HTTP 통신을 담당하는 클래스
 * JDK 7+ 호환
 */
public class LLMClient {

    private static final String DEFAULT_API_URL = "https://api.openai.com/v1/chat/completions";
    private static final int TIMEOUT = 3000000; // 3000초

    /**
     * 채팅 메시지 전송 (대화 컨텍스트 포함)
     */
    public String sendChatMessage(ChatHistory chatHistory, String modelKey) throws Exception {
        
        // 설정값 안전하게 가져오기
        IEclipsePreferences prefs = InstanceScope.INSTANCE.getNode(Activator.PLUGIN_ID);
        
        // 설정이 초기화되지 않은 경우 강제 초기화
        ensureSettingsInitialized(prefs);
        
        String apiKey = prefs.get(PreferenceConstants.API_KEY, "");
        String apiUrl = prefs.get(PreferenceConstants.API_URL, DEFAULT_API_URL);
        
        // 커스텀 모델 처리
        if ("ki-assist-custom".equals(modelKey)) {
            apiUrl = prefs.get(PreferenceConstants.CUSTOM_API_URL, DEFAULT_API_URL);
            String customKey = prefs.get(PreferenceConstants.CUSTOM_API_KEY, "");
            if (!customKey.isEmpty()) {
                apiKey = customKey; // 커스텀 API 키가 있으면 사용
            }
        }
        
        if (apiKey.isEmpty()) {
            throw new Exception("API 키가 설정되지 않았습니다.\n\n" +
                "KI Assist → Settings에서 API 키를 입력해주세요.\n" +
                "또는 Window → Preferences → KI Assist에서 설정하세요.");
        }

        // 나머지 HTTP 호출 코드는 동일
        URL url = new URL(apiUrl);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        
        try {
            // 요청 설정
            conn.setRequestMethod("POST");
            conn.setRequestProperty("Content-Type", "application/json");
            conn.setRequestProperty("Authorization", "Bearer " + apiKey);
            conn.setConnectTimeout(TIMEOUT);
            conn.setReadTimeout(TIMEOUT);
            conn.setDoOutput(true);

            // JSON 요청 본문 생성 (채팅 히스토리 포함)
            String jsonRequest = createChatJsonRequest(chatHistory, modelKey);
            
            // 요청 전송
            OutputStreamWriter writer = new OutputStreamWriter(conn.getOutputStream(), "UTF-8");
            writer.write(jsonRequest);
            writer.flush();
            writer.close();

            // 응답 읽기
            int responseCode = conn.getResponseCode();
            
            if (responseCode == HttpURLConnection.HTTP_OK) {
                return readResponse(conn, modelKey);
            } else {
                throw new Exception("HTTP Error " + responseCode + ": " + conn.getResponseMessage());
            }
            
        } finally {
            conn.disconnect();
        }
    }

    /**
     * 설정이 초기화되지 않은 경우 강제 초기화
     */
    private void ensureSettingsInitialized(IEclipsePreferences prefs) {
        try {
            // 설정 초기화 여부 확인
            String version = prefs.get("kiassist.settings.version", "");
            if (version.isEmpty()) {
                System.out.println("LLMClient: 설정이 초기화되지 않음 - 기본값 설정");
                
                // 기본값 강제 설정
                prefs.put(PreferenceConstants.API_URL, DEFAULT_API_URL);
                prefs.put(PreferenceConstants.MAX_TOKENS, "1000");
                prefs.put(PreferenceConstants.TEMPERATURE, "0.7");
                prefs.put(PreferenceConstants.DEFAULT_MODEL, "ki-assist-custom");
                prefs.put(PreferenceConstants.CUSTOM_API_URL, DEFAULT_API_URL);
                prefs.put(PreferenceConstants.CUSTOM_MODEL_NAME, "gpt-3.5-turbo");
                prefs.put("kiassist.settings.version", "1.0.0");
                
                prefs.flush();
                System.out.println("LLMClient: 기본값 설정 완료");
            }
        } catch (Exception e) {
            System.err.println("LLMClient 설정 초기화 오류: " + e.getMessage());
        }
    }

    /**
     * 채팅용 JSON 요청 생성 (대화 히스토리 포함)
     */
    private String createChatJsonRequest(ChatHistory chatHistory, String modelKey) {
        // 설정에서 매개변수 가져오기
        IEclipsePreferences prefs = InstanceScope.INSTANCE.getNode(Activator.PLUGIN_ID);
        String maxTokens = prefs.get(PreferenceConstants.MAX_TOKENS, "1000");
        String temperature = prefs.get(PreferenceConstants.TEMPERATURE, "0.7");
        
        // 실제 모델명 변환
        String actualModel = convertModelKey(modelKey);
        
        // 커스텀 LLM은 단순 prompt 방식, 나머지는 messages 방식
        if ("ki-assist-custom".equals(modelKey)) {
            return createCustomJsonRequest(chatHistory, actualModel, maxTokens, temperature);
        } else {
            return createStandardJsonRequest(chatHistory, actualModel, maxTokens, temperature);
        }
    }

    /**
     * 커스텀 LLM용 단순 prompt 방식 JSON 생성 (최종 질문만)
     */
    private String createCustomJsonRequest(ChatHistory chatHistory, String actualModel, 
                                         String maxTokens, String temperature) {
        StringBuilder json = new StringBuilder();
        json.append("{");
        
        // 모델명 (선택사항)
        if (!actualModel.isEmpty()) {
            json.append("\"model\":\"").append(escapeJson(actualModel)).append("\",");
        }
        
        // 최종 사용자 질문만 가져오기
        String lastUserQuestion = getLastUserQuestion(chatHistory) + ". 한글로 답변해줘.";
        json.append("\"prompt\":\"").append(escapeJson(lastUserQuestion)).append("\"");
        
        // 매개변수 추가
        json.append(",\"max_tokens\":").append(maxTokens);
        json.append(",\"temperature\":").append(temperature);
        
        json.append("}");
        return json.toString();
    }

    /**
     * 마지막 사용자 질문 가져오기
     */
    private String getLastUserQuestion(ChatHistory chatHistory) {
        List<ChatMessage> messages = chatHistory.getMessages();
        
        // 뒤에서부터 찾아서 가장 최근 사용자 메시지 반환
        for (int i = messages.size() - 1; i >= 0; i--) {
            ChatMessage msg = messages.get(i);
            if (msg.getType() == ChatMessage.Type.USER) {
                return msg.getContent();
            }
        }
        
        // 사용자 메시지가 없는 경우 (일반적으로 발생하지 않음)
        return "안녕하세요";
    }

    /**
     * 표준 LLM용 messages 방식 JSON 생성 (OpenAI, Claude 등)
     */
    private String createStandardJsonRequest(ChatHistory chatHistory, String actualModel,
                                           String maxTokens, String temperature) {
        StringBuilder json = new StringBuilder();
        json.append("{");
        json.append("\"model\":\"").append(escapeJson(actualModel)).append("\",");
        json.append("\"messages\":[");
        
        // 대화 히스토리 추가 (최근 20개 메시지만)
        List<ChatMessage> apiMessages = chatHistory.getRecentApiMessages(20);
        for (int i = 0; i < apiMessages.size(); i++) {
            ChatMessage msg = apiMessages.get(i);
            
            if (i > 0) json.append(",");
            
            json.append("{");
            json.append("\"role\":\"").append(getRoleForType(msg.getType())).append("\",");
            json.append("\"content\":\"").append(escapeJson(msg.getContent())).append("\"");
            json.append("}");
        }
        
        json.append("],");
        json.append("\"max_tokens\":").append(maxTokens).append(",");
        json.append("\"temperature\":").append(temperature);
        json.append("}");
        
        return json.toString();
    }

    /**
     * 모델 키를 실제 API 모델명으로 변환
     */
    private String convertModelKey(String modelKey) {
        if ("ki-assist-custom".equals(modelKey)) {
            IEclipsePreferences prefs = InstanceScope.INSTANCE.getNode(Activator.PLUGIN_ID);
            return prefs.get(PreferenceConstants.CUSTOM_MODEL_NAME, "gpt-3.5-turbo");
        }
        
        // 다른 모델들은 표준 변환
        if ("gpt35turbo".equals(modelKey)) return "gpt-3.5-turbo";
        if ("gpt4".equals(modelKey)) return "gpt-4";
        if ("claude3haiku".equals(modelKey)) return "claude-3-haiku";
        if ("claude3sonnet".equals(modelKey)) return "claude-3-sonnet";
        if ("geminipro".equals(modelKey)) return "gemini-pro";
        
        return modelKey; // 기본값
    }

    /**
     * ChatMessage.Type을 API role로 변환
     */
    private String getRoleForType(ChatMessage.Type type) {
        switch (type) {
            case USER: return "user";
            case ASSISTANT: return "assistant";
            case SYSTEM: return "system";
            default: return "user";
        }
    }

    private String escapeJson(String text) {
        if (text == null) return "";
        return text.replace("\\", "\\\\")
                  .replace("\"", "\\\"")
                  .replace("\n", "\\n")
                  .replace("\r", "\\r")
                  .replace("\t", "\\t");
    }

    private String readResponse(HttpURLConnection conn, String modelKey) throws IOException {
        BufferedReader reader = new BufferedReader(
            new InputStreamReader(conn.getInputStream(), "UTF-8"));
        
        StringBuilder response = new StringBuilder();
        String line;
        
        while ((line = reader.readLine()) != null) {
            response.append(line);
        }
        reader.close();
        
        // 간단한 JSON 파싱 (모델에 따라 다른 필드 파싱)
        return parseJsonResponse(response.toString(), modelKey);
    }

    private String parseJsonResponse(String jsonResponse, String modelKey) {
        // 디버깅용 - JSON 응답 전체 출력
        System.out.println("=== JSON Response Debug ===");
        System.out.println("Model Key: " + modelKey);
        System.out.println(jsonResponse);
        System.out.println("=== End JSON Response ===");
        
        try {
            // 커스텀 모델은 "response" 필드, 표준 모델은 "content" 필드
            String fieldName = "ki-assist-custom".equals(modelKey) ? "response" : "content";
            
            int fieldStart = jsonResponse.indexOf("\"" + fieldName + "\":");
            if (fieldStart == -1) {
                return "응답 파싱 오류: " + fieldName + "를 찾을 수 없습니다.";
            }
            
            fieldStart = jsonResponse.indexOf("\"", fieldStart + fieldName.length() + 3);
            if (fieldStart == -1) {
                return "응답 파싱 오류: " + fieldName + " 값을 찾을 수 없습니다.";
            }
            
            int fieldEnd = findJsonStringEnd(jsonResponse, fieldStart + 1);
            if (fieldEnd == -1) {
                return "응답 파싱 오류: " + fieldName + " 끝을 찾을 수 없습니다.";
            }
            
            String content = jsonResponse.substring(fieldStart + 1, fieldEnd);
            return unescapeJson(content);
            
        } catch (Exception e) {
            return "JSON 파싱 오류: " + e.getMessage() + "\n\n원본 응답:\n" + jsonResponse;
        }
    }

    private int findJsonStringEnd(String json, int start) {
        for (int i = start; i < json.length(); i++) {
            char c = json.charAt(i);
            if (c == '"' && (i == 0 || json.charAt(i - 1) != '\\')) {
                return i;
            }
        }
        return -1;
    }

    private String unescapeJson(String text) {
        return text.replace("\\\"", "\"")
                  .replace("\\\\", "\\")
                  .replace("\\n", "\n")
                  .replace("\\r", "\r")
                  .replace("\\t", "\t");
    }
}