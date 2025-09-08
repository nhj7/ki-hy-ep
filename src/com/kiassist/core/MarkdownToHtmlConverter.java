package com.kiassist.core;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.BufferedReader;
import java.util.List;

/**
 * 마크다운을 HTML로 변환하는 클래스
 * marked.js 사용 - JDK 7 호환
 */
public class MarkdownToHtmlConverter {

    private static final String CHAT_CSS = 
        "body { font-family: 'Segoe UI', Tahoma, Geneva, Verdana, sans-serif; margin: 0; padding: 10px; background-color: #f5f5f5; font-size: 80%; }" +
        ".chat-container { max-width: 100%; }" +
        ".message { margin-bottom: 15px; padding: 10px; border-radius: 8px; max-width: 80%; word-wrap: break-word; }" +
        ".message-user { background-color: #007acc; color: white; margin-left: auto; margin-right: 0; text-align: right; }" +
        ".message-assistant { background-color: white; color: #333; margin-left: 0; margin-right: auto; border: 1px solid #ddd; }" +
        ".message-system { background-color: #fff3cd; color: #856404; margin: 0 auto; text-align: center; font-style: italic; }" +
        ".message-typing { background-color: #e9ecef; color: #6c757d; margin-left: 0; margin-right: auto; font-style: italic; }" +
        ".timestamp { font-size: 0.8em; opacity: 0.7; margin-top: 5px; }" +
        ".model-tag { font-size: 0.7em; background-color: #6c757d; color: white; padding: 2px 6px; border-radius: 3px; margin-bottom: 5px; display: inline-block; }" +
        
        // 마크다운 스타일 (marked.js 출력용)
        "h1, h2, h3 { color: #333; margin-top: 20px; margin-bottom: 10px; }" +
        "h1 { font-size: 1.5em; border-bottom: 2px solid #007acc; }" +
        "h2 { font-size: 1.3em; border-bottom: 1px solid #ccc; }" +
        "h3 { font-size: 1.1em; }" +
        "p { line-height: 1.6; margin: 10px 0; }" +
        "strong { font-weight: bold; color: #d73502; }" +
        "em { font-style: italic; color: #666; }" +
        "code { background-color: #f8f9fa; color: #e83e8c; padding: 2px 4px; border-radius: 3px; font-family: 'Consolas', 'Monaco', monospace; }" +
        
        // 코드 블록 스타일
        "pre { background-color: #f8f9fa; border: 1px solid #e9ecef; border-radius: 5px; padding: 10px; overflow-x: auto; margin: 10px 0; }" +
        "pre code { background-color: transparent; color: white; padding: 0; font-family: 'Consolas', 'Monaco', 'Courier New', monospace; font-size: 0.9em; line-height: 1.4; }" +
        
        // 코드 블록 컨테이너 및 헤더 스타일 (다크 테마)
        ".code-block-container { margin: 10px 0; border-radius: 5px; overflow: hidden; border: 1px solid #555; background-color: #2d2d2d; }" +
        ".code-header { background-color: #404040; border-bottom: 1px solid #555; padding: 8px 12px; display: flex; justify-content: space-between; align-items: center; font-size: 0.8em; }" +
        ".language-tag { background-color: #007acc; color: #ccc; padding: 2px 6px; border-radius: 3px; font-weight: bold; }" +
        ".copy-btn { display: flex; align-items: center; gap: 4px; background: none; border: none; color: #ccc; cursor: pointer; font-size: 0.8rem; padding: 4px 8px; border-radius: 4px; transition: all 0.2s; position: relative; }" +
        ".copy-btn:hover { background-color: rgba(255, 255, 255, 0.1); color: #fff; }" +
        ".copy-btn::before { content: ''; display: inline-block; width: 12px; height: 12px; margin-right: 4px; border: 1px solid currentColor; border-radius: 2px; position: relative; }" +
        ".copy-btn::after { content: ''; position: absolute; left: 6px; top: 6px; width: 8px; height: 8px; border: 1px solid currentColor; border-radius: 1px; background-color: transparent; }" +
        ".check-icon { display: inline-block; width: 12px; height: 12px; margin-right: 4px; position: relative; }" +
        ".check-icon::before { content: ''; position: absolute; left: 2px; top: 6px; width: 4px; height: 2px; border-left: 2px solid currentColor; border-bottom: 2px solid currentColor; transform: rotate(-45deg); }" +
        ".code-block-container pre { margin: 0; border: none; border-radius: 0; background-color: #2d2d2d; }" +
        
        // 리스트 스타일
        "ul, ol { padding-left: 20px; }" +
        "li { margin: 5px 0; }" +
        
        // 인용구
        "blockquote { border-left: 4px solid #ddd; margin: 10px 0; padding-left: 10px; color: #666; font-style: italic; }";

    /**
     * 초기 채팅 페이지 HTML 생성 (marked.js 포함)
     */
    public String createChatPageHTML() {
        StringBuilder html = new StringBuilder();
        html.append("<!DOCTYPE html>");
        html.append("<html><head>");
        html.append("<meta charset='UTF-8'>");
        html.append("<style>").append(CHAT_CSS).append("</style>");
        
        // highlight.js CSS 테마 로드
        String highlightCss = loadHighlightCSS();
        if (!highlightCss.isEmpty()) {
            html.append("<style>").append(highlightCss).append("</style>");
        }
        
        html.append("<script>");
        html.append("function debugLog(message) {");
        html.append("  var debugDiv = document.getElementById('debug-info');");
        html.append("  if (!debugDiv) {");
        html.append("    debugDiv = document.createElement('div');");
        html.append("    debugDiv.id = 'debug-info';");
        html.append("    debugDiv.style.position = 'fixed';");
        html.append("    debugDiv.style.top = '0px';");
        html.append("    debugDiv.style.right = '0px';");
        html.append("    debugDiv.style.background = 'rgba(0,0,0,0.8)';");
        html.append("    debugDiv.style.color = '#0f0';");
        html.append("    debugDiv.style.padding = '5px';");
        html.append("    debugDiv.style.fontSize = '10px';");
        html.append("    debugDiv.style.maxWidth = '200px';");
        html.append("    debugDiv.style.maxHeight = '100px';");
        html.append("    debugDiv.style.overflow = 'auto';");
        html.append("    debugDiv.style.zIndex = '9999';");
        html.append("    document.body.appendChild(debugDiv);");
        html.append("  }");
        html.append("  var now = new Date().toLocaleTimeString();");
        html.append("  debugDiv.innerHTML = '[' + now + '] ' + message + '<br>' + debugDiv.innerHTML;");
        html.append("}");
        html.append("</script>");
        
        // marked.js 로드
        String markedJs = loadMarkedJS();
        if (!markedJs.isEmpty()) {
            html.append("<script>").append(markedJs).append("</script>");
        }
        
        // highlight.js 로드
        String highlightJs = loadHighlightJS();
        if (!highlightJs.isEmpty()) {
            html.append("<script>").append(highlightJs).append("</script>");
        }
        
        html.append("</head><body>");
        html.append("<div class='chat-container' id='chatContainer'>");
        html.append("<div class='message message-system'>");
        html.append("KI Assist 채팅이 시작되었습니다. 무엇이든 물어보세요!<br>");
        html.append("</div>");
        
        html.append("<pre><code class='javascript'>console.log('highlight.js 테스트 중...');\nfunction test() {\n  return 'CSS 적용 확인';\n}</code></pre>");
        
        html.append("</div>");
        
        // JavaScript 함수들 (가장 먼저 로드)
        html.append("<script>");
        
        
        
        
        // marked 커스텀 렌더러 설정 함수
        html.append("function setupMarkedRenderer() {");
        html.append("  if (typeof marked !== 'undefined') {");
        html.append("    debugLog('marked 커스텀 렌더러 설정 시작');");
        html.append("    try {");
        html.append("      var renderer = new marked.Renderer();");
        html.append("      renderer.link = function (href, title, text) {");
        html.append("        return '<a href=\"' + href + '\" title=\"' + (title || text) + '\" target=\"_blank\">' + text + '</a>';");
        html.append("      };");
        html.append("      renderer.code = function(code, language) {");
        html.append("        debugLog('코드 렌더러 호출: ' + typeof code + ', ' + language);");
        html.append("        var codeText = '';");
        html.append("        var langText = '';");
        html.append("        if (typeof code === 'string') {");
        html.append("          codeText = code;");
        html.append("          langText = language || 'code';");
        html.append("        } else if (code && typeof code === 'object') {");
        html.append("          codeText = code.text || code.code || code.raw || '';");
        html.append("          langText = code.lang || code.language || language || 'code';");
        html.append("        }");
        html.append("        var langClass = langText ? 'language-' + langText : '';");
        html.append("        var langDisplay = langText || 'code';");
        html.append("        return '<div class=\"code-block-container\"><div class=\"code-header\"><span class=\"language-tag\">' + langDisplay + '</span><button class=\"copy-btn\" type=\"button\" onclick=\"handleCopyCode(event)\"><span class=\"copy-text\">복사</span></button></div><pre><code class=\"' + langClass + '\">' + codeText + '</code></pre></div>';");
        html.append("      };");
        html.append("      marked.setOptions({ renderer: renderer });");
        html.append("      debugLog('marked 커스텀 렌더러 설정 완료');");
        html.append("      return true;");
        html.append("    } catch (e) {");
        html.append("      debugLog('marked 렌더러 설정 에러: ' + e.message);");
        html.append("      return false;");
        html.append("    }");
        html.append("  } else {");
        html.append("    debugLog('marked 객체가 정의되지 않음');");
        html.append("    return false;");
        html.append("  }");
        html.append("}");
        
        // marked.js 로드 확인 함수
        html.append("function checkMarkedJS() {");
        html.append("  if (typeof marked !== 'undefined') {");
        html.append("    debugLog('marked.js 로드 성공!');");
        html.append("    setupMarkedRenderer();");
        html.append("    return true;");
        html.append("  } else {");
        html.append("    debugLog('marked.js 로드 실패!');");
        html.append("    return false;");
        html.append("  }");
        html.append("}");
        html.append(";");
    
        
        // highlight.js 로드 확인 및 초기화 함수
        html.append("function checkHighlightJS() {");
        html.append("  if (typeof hljs !== 'undefined') {");
        html.append("    debugLog('highlight.js 로드 성공!');");
        html.append("    hljs.initHighlighting();");
        html.append("    return true;");
        html.append("  } else {");
        html.append("    debugLog('highlight.js 로드 실패!');");
        html.append("    return false;");
        html.append("  }");
        html.append("}");
        
        html.append("function addMessage(content, type, model, timestamp, isMarkdown) {");
        html.append("  var container = document.getElementById('chatContainer');");
        html.append("  var messageDiv = document.createElement('div');");
        html.append("  messageDiv.className = 'message message-' + type;");
        html.append("  ");
        html.append("  var html = '';");
        html.append("  if (model && type === 'assistant') {");
        html.append("    html += '<div class=\"model-tag\">' + model + '</div>';");
        html.append("  }");
        html.append("  ");
        html.append("  if (type === 'typing') {");
        html.append("    html += '응답을 생성 중입니다...';");
        html.append("  } else if (isMarkdown && window.marked) {");
        html.append("    try {");
        html.append("      html += marked.parse(content);");
        html.append("      debugLog('marked.parse 성공');");
        html.append("      ");
        html.append("      setTimeout(function() {");
        html.append("        debugLog('addMessage setTimeout 시작: highlight.js 적용 시도');");
        html.append("        if (typeof hljs !== 'undefined') {");
        html.append("          debugLog('addMessage hljs 객체 존재 확인됨');");
        html.append("          try {");
        html.append("            var codeBlocks = document.querySelectorAll('pre code');");
        html.append("            debugLog('addMessage 코드 블록 갯수: ' + codeBlocks.length);");
        html.append("            debugLog('for문 시작 전');");
        html.append("            for (var i = 0; i < codeBlocks.length; i++) {");
        html.append("              debugLog('for문 반복 ' + i + ' 시작');");
        html.append("              try {");
        html.append("                var block = codeBlocks[i];");
        html.append("                debugLog('블록 ' + i + ' 클래스: ' + block.className);");
        html.append("                debugLog('블록 ' + i + ' 태그명: ' + block.tagName);");
        html.append("                debugLog('블록 ' + i + ' 부모 태그: ' + block.parentNode.tagName);");
        html.append("                if (block.className && block.className.indexOf('language-') === 0) {");
        html.append("                  debugLog('언어 클래스 발견, highlightBlock 적용: ' + block.className);");
        html.append("                  hljs.highlightBlock(block);");
        html.append("                  debugLog('블록 ' + i + ' 하이라이트 적용 완료');");
        html.append("                } else {");
        html.append("                  debugLog('언어 클래스 없음: ' + block.className);");
        html.append("                }");;
        html.append("              } catch (innerE) {");
        html.append("                debugLog('for문 내부 에러 ' + i + ': ' + innerE.message);");
        html.append("              }");
        html.append("            }");
        html.append("            debugLog('for문 완료');");
        html.append("            debugLog('개별 블록 처리 완료, initHighlighting 생략');");
        html.append("          } catch (e) {");
        html.append("            debugLog('addMessage hljs.initHighlighting() 에러: ' + e.message);");
        html.append("          }");
        html.append("        } else {");
        html.append("          debugLog('addMessage hljs 객체가 정의되지 않음');");
        html.append("        }");
        html.append("      }, 10);");
        html.append("    } catch (e) {");
        html.append("      debugLog('marked.parse 오류: ' + e.message);");
        html.append("      html += content.replace(/\\n/g, '<br>');"); // 폴백
        html.append("    }");
        html.append("  } else {");
        html.append("    html += content.replace(/\\n/g, '<br>');");
        html.append("  }");
        html.append("  ");
        html.append("  html += '<div class=\"timestamp\">' + timestamp + '</div>';");
        html.append("  messageDiv.innerHTML = html;");
        html.append("  container.appendChild(messageDiv);");
        html.append("  window.scrollTo(0, document.body.scrollHeight);");
        html.append("}");
        
        // 코드 복사 기능 함수 (IE 11 전용 간단 버전)
        html.append("function handleCopyCode(event) {");
        html.append("  var e = event || window.event;");
        html.append("  var btn = e.target || e.srcElement;");
        html.append("  ");
        html.append("  var parent = btn.parentNode;");
        html.append("  for (var i = 0; i < 5 && parent; i++) {");
        html.append("    if (parent.className && parent.className.indexOf('code-block-container') >= 0) {");
        html.append("      break;");
        html.append("    }");
        html.append("    parent = parent.parentNode;");
        html.append("  }");
        html.append("  ");
        html.append("  if (!parent) return;");
        html.append("  ");
        html.append("  var codes = parent.getElementsByTagName('code');");
        html.append("  if (!codes || codes.length === 0) return;");
        html.append("  ");
        html.append("  var text = codes[0].innerText || codes[0].textContent || '';");
        html.append("  if (!text) return;");
        html.append("  ");
        html.append("  var success = false;");
        html.append("  ");
        html.append("  if (window.clipboardData) {");
        html.append("    try {");
        html.append("      window.clipboardData.setData('Text', text);");
        html.append("      success = true;");
        html.append("    } catch (e) {}");
        html.append("  }");
        html.append("  ");
        html.append("  if (!success) {");
        html.append("    var temp = document.createElement('textarea');");
        html.append("    temp.value = text;");
        html.append("    temp.style.position = 'fixed';");
        html.append("    temp.style.left = '-9999px';");
        html.append("    document.body.appendChild(temp);");
        html.append("    temp.select();");
        html.append("    try {");
        html.append("      success = document.execCommand('copy');");
        html.append("    } catch (e) {}");
        html.append("    document.body.removeChild(temp);");
        html.append("  }");
        html.append("  ");
        html.append("  if (success) {");
        html.append("    var orig = btn.innerHTML;");
        html.append("    btn.innerHTML = '복사완료';");
        html.append("    setTimeout(function() { btn.innerHTML = orig; }, 1000);");
        html.append("  }");
        html.append("}");
        html.append("");
                
        // 페이지 로드 완료 시 marked.js 또는 highlight.js 확인
        html.append("window.onload = function() {");
        html.append("  checkMarkedJS();");
        html.append("  checkHighlightJS();");
        html.append("};");
        
        html.append("</script>");
        
        html.append("</body></html>");
        
        return html.toString();
    }

    /**
     * 채팅 히스토리를 HTML로 변환 (기존 호환성 유지)
     */
    public String convertChatToHTML(ChatHistory chatHistory) {
        StringBuilder html = new StringBuilder();
        html.append("<!DOCTYPE html>");
        html.append("<html><head>");
        html.append("<meta charset='UTF-8'>");
        html.append("<style>").append(CHAT_CSS).append("</style>");
        
        // highlight.js CSS 테마 로드
        String highlightCss = loadHighlightCSS();
        if (!highlightCss.isEmpty()) {
            html.append("<style>").append(highlightCss).append("</style>");
        }
        
        // marked.js 로드
        String markedJs = loadMarkedJS();
        if (!markedJs.isEmpty()) {
            html.append("<script>").append(markedJs).append("</script>");
        }
        
        // highlight.js 로드
        String highlightJs = loadHighlightJS();
        if (!highlightJs.isEmpty()) {
            html.append("<script>").append(highlightJs).append("</script>");
        }
        
        html.append("</head><body>");
        html.append("<div class='chat-container' id='chatContainer'>");
        
        // 시작 메시지
        if (chatHistory.isEmpty()) {
            html.append("<div class='message message-system'>");
            html.append("KI Assist 채팅이 시작되었습니다. 무엇이든 물어보세요!");
            html.append("</div>");
        }
        
        // 각 메시지 변환
        List<ChatMessage> messages = chatHistory.getMessages();
        for (ChatMessage message : messages) {
            html.append(convertMessageToHTML(message));
        }
        
        html.append("</div>");
        
        // JavaScript 함수들 추가
        html.append("<script>");
        html.append("function debugLog(message) {");
        html.append("  var debugDiv = document.getElementById('debug-info');");
        html.append("  if (!debugDiv) {");
        html.append("    debugDiv = document.createElement('div');");
        html.append("    debugDiv.id = 'debug-info';");
        html.append("    debugDiv.style.position = 'fixed';");
        html.append("    debugDiv.style.top = '0px';");
        html.append("    debugDiv.style.right = '0px';");
        html.append("    debugDiv.style.background = 'rgba(0,0,0,0.8)';");
        html.append("    debugDiv.style.color = '#0f0';");
        html.append("    debugDiv.style.padding = '5px';");
        html.append("    debugDiv.style.fontSize = '10px';");
        html.append("    debugDiv.style.maxWidth = '200px';");
        html.append("    debugDiv.style.maxHeight = '100px';");
        html.append("    debugDiv.style.overflow = 'auto';");
        html.append("    debugDiv.style.zIndex = '9999';");
        html.append("    document.body.appendChild(debugDiv);");
        html.append("  }");
        html.append("  var now = new Date().toLocaleTimeString();");
        html.append("  debugDiv.innerHTML = '[' + now + '] ' + message + '<br>' + debugDiv.innerHTML;");
        html.append("}");
        
        html.append("if (typeof marked !== 'undefined') {");
        html.append("  debugLog('marked.js 로드 성공!');");
        html.append("} else {");
        html.append("  debugLog('marked.js 로드 실패!');");
        html.append("}");
        html.append("if (typeof hljs !== 'undefined') {");
        html.append("  debugLog('highlight.js 로드 성공!');");
        html.append("  hljs.initHighlighting();");
        html.append("} else {");
        html.append("  debugLog('highlight.js 로드 실패!');");
        html.append("}");
        html.append("</script>");
        
        html.append("</body></html>");
        
        return html.toString();
    }

    /**
     * 개별 메시지를 HTML로 변환
     */
    private String convertMessageToHTML(ChatMessage message) {
        StringBuilder html = new StringBuilder();
        
        String cssClass = getCssClassForType(message.getType());
        html.append("<div class='message ").append(cssClass).append("'>");
        
        // 모델 태그 (AI 메시지인 경우)
        if (message.getType() == ChatMessage.Type.ASSISTANT && message.getModel() != null) {
            html.append("<div class='model-tag'>").append(escapeHtml(message.getModel())).append("</div>");
        }
        
        // 메시지 내용
        if (message.getType() == ChatMessage.Type.TYPING) {
            html.append("응답을 생성 중입니다...");
        } else if (message.getType() == ChatMessage.Type.ASSISTANT) {
            // AI 응답은 마크다운으로 처리 (클라이언트사이드에서 파싱)
            String messageId = "msg-" + System.currentTimeMillis() + "-" + Math.random();
            html.append("<div id='").append(messageId).append("' data-markdown='true'>");
            html.append(escapeHtml(message.getContent()));
            html.append("</div>");
            html.append("<script>"); 
            html.append("(function() {");
            html.append("  var elem = document.getElementById('").append(messageId).append("');");
            html.append("  if (elem && typeof marked !== 'undefined') {");
            html.append("    try {");
            html.append("      elem.innerHTML = marked.parse(elem.textContent || elem.innerText);");
            html.append("      ");
            html.append("      setTimeout(function() {");
            html.append("        debugLog('convertMessageToHTML setTimeout 시작');");
            html.append("        if (typeof hljs !== 'undefined') {");
            html.append("          debugLog('convertMessageToHTML hljs 객체 존재');");
            html.append("          try {");
            html.append("            var codeBlocks = document.querySelectorAll('pre code');");
            html.append("            debugLog('convertMessageToHTML 코드 블록 갯수: ' + codeBlocks.length);");
            html.append("            hljs.initHighlighting();");
            html.append("            debugLog('convertMessageToHTML hljs.initHighlighting() 성공');");
            html.append("          } catch (e) {");
            html.append("            debugLog('convertMessageToHTML hljs.initHighlighting() 에러: ' + e.message);");
            html.append("          }");
            html.append("        } else {");
            html.append("          debugLog('convertMessageToHTML hljs 객체 없음');");
            html.append("        }");
            html.append("      }, 100);");
            html.append("    } catch (e) {");
            html.append("      console.log('Markdown parsing error:', e);");
            html.append("      elem.innerHTML = elem.innerHTML.replace(/\\n/g, '<br>');");
            html.append("    }");
            html.append("  }");
            html.append("})();");
            html.append("</script>");
        } else {
            // 사용자 메시지는 일반 텍스트
            String htmlContent = escapeHtml(message.getContent()).replaceAll("\n", "<br>");
            html.append(htmlContent);
        }
        
        // 타임스탬프
        html.append("<div class='timestamp'>").append(message.getFormattedTime()).append("</div>");
        
        html.append("</div>");
        
        return html.toString();
    }

    /**
     * 메시지 타입에 따른 CSS 클래스
     */
    private String getCssClassForType(ChatMessage.Type type) {
        switch (type) {
            case USER: return "message-user";
            case ASSISTANT: return "message-assistant";
            case SYSTEM: return "message-system";
            case TYPING: return "message-typing";
            default: return "message-system";
        }
    }

    /**
     * 채팅 히스토리를 브라우저에 추가 (JavaScript 호출)
     */
    public String getAddMessageScript(ChatMessage message) {
        String content = escapeJavaScript(message.getContent());
        String type = getTypeString(message.getType());
        String model = message.getModel() != null ? escapeJavaScript(message.getModel()) : "";
        String timestamp = message.getFormattedTime();
        
        // AI 응답은 마크다운 파싱, 사용자 메시지는 일반 텍스트
        boolean isMarkdown = (message.getType() == ChatMessage.Type.ASSISTANT);
        
        return "addMessage('" + content + "', '" + type + "', '" + model + "', '" + 
               timestamp + "', " + isMarkdown + ");";
    }

    /**
     * 전체 채팅 히스토리를 브라우저에 로드
     */
    public String getAllMessagesScript(ChatHistory chatHistory) {
        StringBuilder script = new StringBuilder();
        
        // 기존 메시지들 삭제
        script.append("document.getElementById('chatContainer').innerHTML = '';");
        
        // 모든 메시지 추가
        List<ChatMessage> messages = chatHistory.getMessages();
        for (ChatMessage message : messages) {
            script.append(getAddMessageScript(message));
        }
        
        return script.toString();
    }

    /**
     * ChatMessage.Type을 문자열로 변환
     */
    private String getTypeString(ChatMessage.Type type) {
        switch (type) {
            case USER: return "user";
            case ASSISTANT: return "assistant";
            case SYSTEM: return "system";
            case TYPING: return "typing";
            default: return "system";
        }
    }

    /**
     * JavaScript 문자열 이스케이프
     */
    private String escapeJavaScript(String text) {
        if (text == null) return "";
        return text.replaceAll("\\\\", "\\\\\\\\")
                  .replaceAll("'", "\\\\'")
                  .replaceAll("\"", "\\\\\"")
                  .replaceAll("\n", "\\\\n")
                  .replaceAll("\r", "\\\\r")
                  .replaceAll("\t", "\\\\t");
    }

    /**
     * HTML 이스케이프 처리 (기본 안전장치)
     */
    private String escapeHtml(String text) {
        if (text == null) return "";
        return text.replaceAll("&", "&amp;")
                  .replaceAll("<", "&lt;")
                  .replaceAll(">", "&gt;")
                  .replaceAll("\"", "&quot;")
                  .replaceAll("'", "&#x27;");
    }

    /**
     * marked.js 파일 로드
     */
    private String loadMarkedJS() {
        try {
            InputStream is = getClass().getClassLoader().getResourceAsStream("web/marked.min.js");
            if (is == null) {
                System.err.println("marked.min.js 파일을 찾을 수 없습니다. resources/web/marked.min.js 경로를 확인하세요.");
                return "";
            }
            
            BufferedReader reader = new BufferedReader(new InputStreamReader(is, "UTF-8"));
            StringBuilder content = new StringBuilder();
            String line;
            
            while ((line = reader.readLine()) != null) {
                content.append(line).append("\n");
            }
            reader.close();
            
            System.out.println("marked.js 로드 완료 (" + content.length() + " 문자)");
            return content.toString();
            
        } catch (Exception e) {
            System.err.println("marked.js 로드 오류: " + e.getMessage());
            e.printStackTrace();
            return "";
        }
    }

    /**
     * highlight.js 파일 로드
     */
    private String loadHighlightJS() {
        try {
            InputStream is = getClass().getClassLoader().getResourceAsStream("web/highlight.min.js");
            if (is == null) {
                System.err.println("highlight.min.js 파일을 찾을 수 없습니다. resources/web/highlight.min.js 경로를 확인하세요.");
                return "";
            }
            
            BufferedReader reader = new BufferedReader(new InputStreamReader(is, "UTF-8"));
            StringBuilder content = new StringBuilder();
            String line;
            
            while ((line = reader.readLine()) != null) {
                content.append(line).append("\n");
            }
            reader.close();
            
            System.out.println("highlight.js 로드 완료 (" + content.length() + " 문자)");
            return content.toString();
            
        } catch (Exception e) {
            System.err.println("highlight.js 로드 오류: " + e.getMessage());
            e.printStackTrace();
            return "";
        }
    }

    /**
     * highlight.js CSS 테마 로드
     */
    private String loadHighlightCSS() {
        try {
            InputStream is = getClass().getClassLoader().getResourceAsStream("web/highlight-dark.min.css");
            if (is == null) {
                System.err.println("highlight-dark.min.css 파일을 찾을 수 없습니다. resources/web/highlight-dark.min.css 경로를 확인하세요.");
                return "";
            }
            
            BufferedReader reader = new BufferedReader(new InputStreamReader(is, "UTF-8"));
            StringBuilder content = new StringBuilder();
            String line;
            
            while ((line = reader.readLine()) != null) {
                content.append(line).append("\n");
            }
            reader.close();
            
            System.out.println("highlight.js CSS 로드 완료 (" + content.length() + " 문자)");
            return content.toString();
            
        } catch (Exception e) {
            System.err.println("highlight.js CSS 로드 오류: " + e.getMessage());
            e.printStackTrace();
            return "";
        }
    }
}