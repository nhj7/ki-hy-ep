package com.kiassist.core;

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * 채팅 메시지 데이터 클래스
 * JDK 7 호환
 */
public class ChatMessage {
    
    public enum Type {
        USER,       // 사용자 메시지
        ASSISTANT,  // AI 응답
        SYSTEM,     // 시스템 메시지 (오류 등)
        TYPING      // 타이핑 인디케이터
    }
    
    private Type type;
    private String content;
    private long timestamp;
    private String model; // 사용된 모델명
    
    public ChatMessage(Type type, String content) {
        this.type = type;
        this.content = content;
        this.timestamp = System.currentTimeMillis();
    }
    
    public ChatMessage(Type type, String content, String model) {
        this(type, content);
        this.model = model;
    }
    
    // Getters
    public Type getType() { return type; }
    public String getContent() { return content; }
    public long getTimestamp() { return timestamp; }
    public String getModel() { return model; }
    
    // Setters
    public void setContent(String content) { this.content = content; }
    public void setModel(String model) { this.model = model; }
    
    /**
     * 시간 포맷팅
     */
    public String getFormattedTime() {
        SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss");
        return sdf.format(new Date(timestamp));
    }
}
