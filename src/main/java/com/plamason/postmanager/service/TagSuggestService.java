package com.plamason.postmanager.service;

import org.springframework.stereotype.Service;

import java.util.List;

@Service
public interface TagSuggestService {
    public List<String> suggestTag(String apiKey, String llmModel, String prompt, String content);
}
