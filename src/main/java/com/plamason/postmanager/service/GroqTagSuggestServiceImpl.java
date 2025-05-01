package com.plamason.postmanager.service;

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
public class GroqTagSuggestServiceImpl implements TagSuggestService {

    private final WebClient webClient;
    private static final String BASE_URL = "https://api.groq.com/openai/v1/chat/completions";

    @Override
    public List<String> suggestTag(String apiKey, String llmModel, String prompt, String content) {

        Map<String, Object> requestBody = Map.of(
                "model", llmModel,
                "messages", List.of(
                        Map.of("role", "system", "content", "You are a helpful assistant that recommends hashtags."),
                        Map.of("role", "user", "content", prompt+content)
                ),
                "temperature", 0.7
        );

        log.info("Sending POST request to {}, body: {}", BASE_URL, requestBody);
        Map<String, Object> response = webClient.post()
                .uri(BASE_URL)
                .contentType(MediaType.APPLICATION_JSON)
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + apiKey)
                .bodyValue(requestBody)
                .retrieve()
                .bodyToMono(new ParameterizedTypeReference<Map<String, Object>>() {})
                .block();

        log.info("Response from groc: {}", response);

        return extractHashtags(response);
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
}
