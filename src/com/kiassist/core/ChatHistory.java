
/**
 * 채팅 히스토리 관리 클래스
 * JDK 7 호환
 */
package com.kiassist.core;

import java.util.ArrayList;
import java.util.List;

public class ChatHistory {
    
    private List<ChatMessage> messages;
    private static final int MAX_MESSAGES = 100; // 메모리 관리
    
    public ChatHistory() {
        messages = new ArrayList<ChatMessage>();
    }
    
    /**
     * 메시지 추가
     */
    public void addMessage(ChatMessage message) {
        // 타이핑 인디케이터가 있다면 제거
        removeTypingIndicator();
        
        messages.add(message);
        
        // 최대 메시지 수 제한
        if (messages.size() > MAX_MESSAGES) {
            messages.remove(0);
        }
    }
    
    /**
     * 타이핑 인디케이터 제거
     */
    private void removeTypingIndicator() {
        for (int i = messages.size() - 1; i >= 0; i--) {
            if (messages.get(i).getType() == ChatMessage.Type.TYPING) {
                messages.remove(i);
                break;
            }
        }
    }
    
    /**
     * 모든 메시지 가져오기
     */
    public List<ChatMessage> getMessages() {
        return new ArrayList<ChatMessage>(messages); // 복사본 반환
    }
    
    /**
     * 대화 히스토리 초기화
     */
    public void clear() {
        messages.clear();
    }
    
    /**
     * API 호출용 메시지 리스트 (시스템/타이핑 메시지 제외)
     */
    public List<ChatMessage> getApiMessages() {
        List<ChatMessage> apiMessages = new ArrayList<ChatMessage>();
        
        for (ChatMessage msg : messages) {
            if (msg.getType() == ChatMessage.Type.USER || 
                msg.getType() == ChatMessage.Type.ASSISTANT) {
                apiMessages.add(msg);
            }
        }
        
        return apiMessages;
    }
    
    /**
     * 마지막 N개 메시지만 가져오기 (컨텍스트 길이 제한)
     */
    public List<ChatMessage> getRecentApiMessages(int maxMessages) {
        List<ChatMessage> apiMessages = getApiMessages();
        
        if (apiMessages.size() <= maxMessages) {
            return apiMessages;
        }
        
        return apiMessages.subList(apiMessages.size() - maxMessages, apiMessages.size());
    }
    
    /**
     * 메시지 개수
     */
    public int size() {
        return messages.size();
    }
    
    /**
     * 비어있는지 확인
     */
    public boolean isEmpty() {
        return messages.isEmpty();
    }
    
    /**
     * 마지막 메시지 가져오기
     */
    public ChatMessage getLastMessage() {
        if (messages.isEmpty()) {
            return null;
        }
        return messages.get(messages.size() - 1);
    }
}