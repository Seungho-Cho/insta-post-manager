package com.plamason.postmanager.service;

import com.plamason.postmanager.enums.AppSettingName;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Slf4j
@Service
@RequiredArgsConstructor
public class GroqAiAssistServiceImpl implements AiAssistService {

    private final WebClient webClient;
    private final AppSettingService appSettingService;

    private static final String BASE_URL = "https://api.groq.com/openai/v1/chat/completions";
    private static final String TAG_SUGGEST_ROLE = "You are a helpful assistant that recommends hashtags.";
    private static final String TRANSLATE_ROLE = "You are a helpful assistant that translate content to English.";

    private String API_KEY;
    private String LLM_MODEL;
    private String BASE_PROMPT;
    private String TRANSLATE_PROMPT;

    @PostConstruct
    public void init() {
        Map<String, String> appSettings = appSettingService.getAllSettings();
        API_KEY = appSettings.get(AppSettingName.GROQ_API_KEY.toString());
        LLM_MODEL = appSettings.get(AppSettingName.GROQ_LLM_MODEL.toString());
        BASE_PROMPT = appSettings.get(AppSettingName.GROQ_BASE_PROMPT.toString());
        TRANSLATE_PROMPT = appSettings.get(AppSettingName.GROQ_TRANSLATE_PROMPT.toString());

        if (API_KEY == null || API_KEY.isBlank()) {
            throw new IllegalStateException("GROQ API key is not configured");
        }
        if (LLM_MODEL == null || LLM_MODEL.isBlank()) {
            throw new IllegalStateException("GROQ LLM model is not configured");
        }
        if (BASE_PROMPT == null || BASE_PROMPT.isBlank()) {
            throw new IllegalStateException("GROQ prompt is not configured");
        }
        if (TRANSLATE_PROMPT == null || TRANSLATE_PROMPT.isBlank()) {
            throw new IllegalStateException("GROQ translate prompt is not configured");
        }
    }

    @Override
    public List<String> getBaseTag() {
        String value = appSettingService.getSetting(AppSettingName.APP_BASE_TAG.toString()).getSettingValue();
        if (value == null || value.isBlank()) return List.of();
        return List.of(value.split(","));
    }

    @Override
    public List<String> suggestTag(String content) {
        return extractHashtags(sendPostRequest(TAG_SUGGEST_ROLE, BASE_PROMPT + content));
    }

    @SuppressWarnings("unchecked")
    private List<String> extractHashtags(Map<String, Object> response) {
        if(response == null) return List.of();

        List<Map<String, Object>> choices = (List<Map<String, Object>>) response.get("choices");
        if (choices == null || choices.isEmpty()) return List.of();

        Map<String, Object> message = (Map<String, Object>) choices.get(0).get("message");
        if (message == null) return List.of();

        String content = (String) message.get("content");
        if (content == null) return List.of();

        // 정규표현식으로 #태그 추출
        Pattern hashtagPattern = Pattern.compile("#([\\w\\-/]+)");
        Matcher matcher = hashtagPattern.matcher(content);

        List<String> tags = new ArrayList<>();
        while (matcher.find()) {
            tags.add(matcher.group(1)); // '#' 제외하고 태그만 추출
        }
        return tags;
    }
    @Override
    public String translateContent(String content) {
        Map<String, Object> response = sendPostRequest(TRANSLATE_ROLE, TRANSLATE_PROMPT + content);
        return extractContent(response);
    }

    @SuppressWarnings("unchecked")
    private String extractContent(Map<String, Object> response) {
        if (response == null) return "";
        
        List<Map<String, Object>> choices = (List<Map<String, Object>>) response.get("choices");
        if (choices == null || choices.isEmpty()) return "";
        
        Map<String, Object> message = (Map<String, Object>) choices.get(0).get("message");
        if (message == null) return "";
        
        String content = (String) message.get("content");
        return content != null ? content : "";
    }

    private Map<String, Object> sendPostRequest(String role, String prompt) {
        Map<String, Object> requestBody = Map.of(
                "model", LLM_MODEL,
                "messages", List.of(
                        Map.of("role", "system", "content", role),
                        Map.of("role", "user", "content", prompt)
                ),
                "temperature", 0.7
        );

        log.info("Sending POST request to {}, body: {}", BASE_URL, requestBody);
        Map<String, Object> response = webClient.post()
                .uri(BASE_URL)
                .contentType(MediaType.APPLICATION_JSON)
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + API_KEY)
                .bodyValue(requestBody)
                .retrieve()
                .bodyToMono(new ParameterizedTypeReference<Map<String, Object>>() {})
                .block();
        log.info("Response from groq: {}", response);

        return response;
    }
    
    
}